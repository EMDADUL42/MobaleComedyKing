package com.emdadul.comedyking.viewmodel;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.comedyKing.eplayer.EPlayer;
import com.emdadul.comedyking.fragment.HomeFragment;
import com.emdadul.comedyking.adapter.PlayerSystemHorizontalAdapter;
import com.emdadul.comedyking.datahelper.ChannelModel;
import com.emdadul.comedyking.datahelper.ComedyKing;
import com.emdadul.comedyking.databinding.VerticalRecyclerItemBinding;
import com.eplayer.InfoRepository;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerSystemVerticalViewModel extends RecyclerView.ViewHolder {
	public final VerticalRecyclerItemBinding binding;
	private final Context context;
	private final ComedyKing comedyKing;
	private final HashSet<String> loadingChannels;
	private PlayerSystemHorizontalAdapter horizontalAdapter;
	private ChannelModel currentModel;
	
	private boolean isBatchLoading = false;
	private final int INITIAL_LOAD_SIZE = 20; // শুরুতে ২০টি লোড হবে
	private final int SCROLL_LOAD_SIZE = 10;  // স্ক্রল করলে আরও ১০টি করে আসবে
	
	private final ExecutorService executorService = Executors.newFixedThreadPool(2);
	private static final Handler mainHandler = new Handler(Looper.getMainLooper());
	private static PlayerSystemVerticalViewModel activeInstance = null;
	
	public PlayerSystemVerticalViewModel(@NonNull VerticalRecyclerItemBinding binding, Context context,
	ComedyKing comedyKing, HashSet<String> loadingChannels,
	RecyclerView.RecycledViewPool sharedPool) {
		super(binding.getRoot());
		this.binding = binding;
		this.context = context;
		this.comedyKing = comedyKing;
		this.loadingChannels = loadingChannels;
		activeInstance = this;
		
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
		binding.recyclerView.setRecycledViewPool(sharedPool);
		binding.recyclerView.setHasFixedSize(true);
		
		// হরাইজন্টাল স্ক্রল লিসেনার: ইউজার ডানে গেলে আরও ভিডিও লোড হবে
		binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
				if (dx > 0 && currentModel != null && !isBatchLoading) {
					LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
					if (lm != null) {
						int totalInDisplay = currentModel.getDisplayIds().size();
						int lastVisible = lm.findLastVisibleItemPosition();
						
						// যদি ইউজার ডিসপ্লে লিস্টের শেষ প্রান্তে পৌঁছায় এবং মাস্টার লিস্টে আরও ভিডিও থাকে
						if (lastVisible >= totalInDisplay - 3 && totalInDisplay < currentModel.getMasterIds().size()) {
							loadNextBatchFromMaster();
						}
					}
				}
			}
		});
	}
	
	public void bind(ChannelModel model) {
		if (model == null) return;
		this.currentModel = model;
		binding.contentTitle.setText(model.getTitle());
		
		horizontalAdapter = new PlayerSystemHorizontalAdapter(context, model.getDisplayIds(), model.getDisplayTitles());
		binding.recyclerView.setAdapter(horizontalAdapter);
		
		executorService.execute(() -> {
			String cached = comedyKing.loadFromFileCache(context, "channel_" + model.getChannelId());
			mainHandler.post(() -> {
				if (cached != null && model.getDisplayIds().isEmpty()) {
					loadFromCache(model, cached);
					} else if (model.getDisplayIds().isEmpty()) {
					addToQueue(model);
					} else {
					horizontalAdapter.notifyDataSetChanged();
				}
			});
		});
	}
	
	private synchronized void addToQueue(ChannelModel model) {
		if (!loadingChannels.contains(model.getChannelId())) {
			loadingChannels.add(model.getChannelId());
			HomeFragment.pendingChannels.add(model);
			checkAndStartNextLoading();
		}
	}
	
	public static void checkAndStartNextLoading() {
		mainHandler.post(() -> {
			if (HomeFragment.isCurrentlyLoading || HomeFragment.pendingChannels.isEmpty() || activeInstance == null) return;
			ChannelModel nextModel = HomeFragment.pendingChannels.poll();
			if (nextModel != null) {
				HomeFragment.isCurrentlyLoading = true;
				activeInstance.executeEPlayerFetch(nextModel);
			}
		});
	}
	
	private void executeEPlayerFetch(final ChannelModel model) {
		mainHandler.post(() -> {
			if (!isValidContext()) { finishAndNext(); return; }
			final FrameLayout container = new FrameLayout(context);
			((Activity) context).addContentView(container, new FrameLayout.LayoutParams(1, 1));
			final EPlayer player = new EPlayer(context);
			player.setEnabled(false);
			container.addView(player);
			
			player.setListener(new EPlayer.OnPlayerListener() {
				@Override public void onReady() { player.setPlaylistById(model.getChannelId()); }
				@Override public void onPlaylistFetched(ArrayList<String> fetchedIds) {
					if (fetchedIds != null && !fetchedIds.isEmpty()) {
						executorService.execute(() -> {
							model.getMasterIds().clear();
							model.getMasterIds().addAll(fetchedIds);
							for (int i = 0; i < fetchedIds.size(); i++) model.getMasterTitles().add("...");
							mainHandler.post(() -> {
								loadBatch(model, INITIAL_LOAD_SIZE); // শুরুতে ২০টি লোড
								cleanup(container, player);
								finishAndNext();
							});
						});
					} else { cleanup(container, player); finishAndNext(); }
				}
				@Override public void onError(String error) { cleanup(container, player); finishAndNext(); }
				@Override public void onPlaylistEnded() {}
				@Override public void onVideoIdChanged(String id) {}
			});
		});
	}
	
	// মাস্টার লিস্ট থেকে ডাটা নিয়ে ডিসপ্লে লিস্টে যোগ করা
	private void loadBatch(ChannelModel model, int size) {
		if (isBatchLoading) return;
		isBatchLoading = true;
		
		int currentSize = model.getDisplayIds().size();
		int masterSize = model.getMasterIds().size();
		int end = Math.min(currentSize + size, masterSize);
		
		for (int i = currentSize; i < end; i++) {
			model.getDisplayIds().add(model.getMasterIds().get(i));
			model.getDisplayTitles().add(model.getMasterTitles().get(i));
			fetchTitle(model.getDisplayIds().get(i), i, model);
		}
		
		if (horizontalAdapter != null && currentModel.getChannelId().equals(model.getChannelId())) {
			if (currentSize == 0) horizontalAdapter.notifyDataSetChanged();
			else horizontalAdapter.notifyItemRangeInserted(currentSize, end - currentSize);
		}
		isBatchLoading = false;
	}
	
	// স্ক্রল করলে কল হবে
	private void loadNextBatchFromMaster() {
		if (currentModel != null) loadBatch(currentModel, SCROLL_LOAD_SIZE);
	}
	
	private void fetchTitle(String videoId, int index, ChannelModel model) {
		new InfoRepository(context).getInfo(videoId, new InfoRepository.ResponseListener() {
			@Override
			public void onResponse(JSONObject json) {
				String title = json.optString("title", "...");
				mainHandler.post(() -> {
					if (isValidContext() && index < model.getDisplayTitles().size()) {
						model.getDisplayTitles().set(index, title);
						model.getMasterTitles().set(index, title);
						if (horizontalAdapter != null && currentModel.getChannelId().equals(model.getChannelId())) {
							horizontalAdapter.notifyItemChanged(index, "TITLE_UPDATE");
						}
						saveCache(model);
					}
				});
			}
			@Override public void onFailure(Exception e) {}
		});
	}
	
	private void loadFromCache(ChannelModel model, String json) {
		try {
			JSONObject obj = new JSONObject(json);
			JSONArray jIds = obj.getJSONArray("videoIds");
			JSONArray jTitles = obj.getJSONArray("titles");
			model.getMasterIds().clear(); model.getMasterTitles().clear();
			for (int i = 0; i < jIds.length(); i++) {
				model.getMasterIds().add(jIds.getString(i));
				model.getMasterTitles().add(jTitles.optString(i, "..."));
			}
			loadBatch(model, INITIAL_LOAD_SIZE);
		} catch (Exception ignored) {}
	}
	
	private void saveCache(ChannelModel model) {
		executorService.execute(() -> {
			try {
				JSONObject root = new JSONObject();
				root.put("videoIds", new JSONArray(model.getMasterIds()));
				root.put("titles", new JSONArray(model.getMasterTitles()));
				comedyKing.saveToFileCacheAsync(context, "channel_" + model.getChannelId(), root.toString());
			} catch (Exception ignored) {}
		});
	}
	
	private void cleanup(FrameLayout container, EPlayer player) {
		try {
			if (player != null) player.setListener(null);
			if (container != null && container.getParent() != null) ((ViewGroup) container.getParent()).removeView(container);
		} catch (Exception ignored) {}
	}
	
	private void finishAndNext() {
		HomeFragment.isCurrentlyLoading = false;
		checkAndStartNextLoading();
	}
	
	private boolean isValidContext() {
		return context instanceof Activity && !((Activity) context).isFinishing() && !((Activity) context).isDestroyed();
	}
}