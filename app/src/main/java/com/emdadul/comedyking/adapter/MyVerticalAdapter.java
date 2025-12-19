package com.emdadul.comedyking.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.comedyKing.eplayer.EPlayer;
import com.emdadul.comedyking.activity.GridActivity;
import com.emdadul.comedyking.datahelper.ComedyKing;
import com.emdadul.comedyking.databinding.VerticalRecyclerItemBinding;
import com.eplayer.InfoRepository;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MyVerticalAdapter extends RecyclerView.Adapter<MyVerticalAdapter.MyViewHolder> {

    private final ArrayList<HashMap<String, String>> arrayList;
    private final Context context;
    private final LottieAnimationView progressBar;

    public static int TOTAL = 0;
    public static int DONE = 0;

    public MyVerticalAdapter(ArrayList<HashMap<String, String>> arrayList,
                             Context context,
                             LottieAnimationView progressBar) {

        this.arrayList = arrayList;
        this.context = context;
        this.progressBar = progressBar;

        TOTAL = arrayList.size();
        DONE = 0;

        // ✅ show loader once
       // progressBar.setVisibility(View.VISIBLE);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        VerticalRecyclerItemBinding binding =
                VerticalRecyclerItemBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );
        return new MyViewHolder(binding, context, progressBar);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        HashMap<String, String> hash = arrayList.get(position);
        String title = hash.get("title");
        String channelId = hash.get("channelId");

        holder.binding.contentTitle.setText(title);

        if (!holder.isLoaded) {
            holder.loadChannelData(channelId);
            holder.isLoaded = true;
        }

        holder.binding.arrow.setOnClickListener(v -> {
            if (holder.videoIdsList.isEmpty()) {
                Toast.makeText(context, "Loading…", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(context, GridActivity.class);
            intent.putStringArrayListExtra("videoIds",
                    new ArrayList<>(holder.videoIdsList));
            intent.putStringArrayListExtra("videoTitle",
                    new ArrayList<>(holder.titlesList));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    // =====================================================
    // VIEW HOLDER
    // =====================================================
    static class MyViewHolder extends RecyclerView.ViewHolder {

        final VerticalRecyclerItemBinding binding;
        final Context context;
        final LottieAnimationView progressBar;

        final ArrayList<String> videoIdsList = new ArrayList<>();
        final ArrayList<String> titlesList = new ArrayList<>();
        final MyHorizontalAdapter horizontalAdapter;

        boolean isLoaded = false;

        public MyViewHolder(@NonNull VerticalRecyclerItemBinding binding,
                            Context context,
                            LottieAnimationView progressBar) {

            super(binding.getRoot());
            this.binding = binding;
            this.context = context;
            this.progressBar = progressBar;

            horizontalAdapter =
                    new MyHorizontalAdapter(context, videoIdsList, titlesList);
            binding.recyclerView.setAdapter(horizontalAdapter);
        }

        void loadChannelData(String channelId) {

            ArrayList<String> cached =
                    ComedyKing.getVideoIds(context, channelId);

            if (!cached.isEmpty()
                    && !ComedyKing.shouldFetchFromServer(context, channelId)) {

                videoIdsList.addAll(cached);

                for (String id : cached) fetchVideoTitle(id);

                checkComplete();
                return;
            }

            fetchFromServer(channelId);
        }

        private void fetchFromServer(String channelId) {

            FrameLayout hidden = new FrameLayout(context);
            hidden.setVisibility(View.GONE);
            ((Activity) context).addContentView(hidden,
                    new FrameLayout.LayoutParams(1, 1));

            EPlayer player = new EPlayer(context);
			player.setEnabled(false);
            hidden.addView(player);

            player.setListener(new EPlayer.OnPlayerListener() {

                @Override
                public void onReady() {
                    player.setPlaylistById(channelId);
                }

                @Override
                public void onPlaylistFetched(ArrayList<String> ids) {

                    videoIdsList.addAll(ids);
                    ComedyKing.saveVideoIds(context, channelId,
                            new ArrayList<>(ids));

                    for (String id : ids) fetchVideoTitle(id);

                    checkComplete();
                }

                @Override public void onError(String error) {}
                @Override public void onPlaylistEnded() {}
                @Override public void onVideoIdChanged(String id) {}
            });
        }

        private void checkComplete() {
            if (videoIdsList.size() >= 200 && ++DONE == TOTAL) {
                ((Activity) context).runOnUiThread(() ->
                        progressBar.setVisibility(View.GONE)
                );
            }
        }

        private void fetchVideoTitle(String videoId) {
            InfoRepository repo = new InfoRepository(context);
            repo.getInfo(videoId, new InfoRepository.ResponseListener() {
                @Override
                public void onResponse(JSONObject json) {
                    try {
                        String title = json.getString("title");
                        ((Activity) context).runOnUiThread(() -> {
                            titlesList.add(title);
                            horizontalAdapter.notifyItemInserted(
                                    titlesList.size() - 1);
                        });
                    } catch (Exception ignored) {}
                }

                @Override public void onFailure(Exception e) {}
            });
        }
    }
}





/*

package com.emdadul.comedyking.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.comedyKing.eplayer.EPlayer;
import com.emdadul.comedyking.R;
import com.emdadul.comedyking.activity.GridActivity;
import com.emdadul.comedyking.datahelper.ComedyKing;
import com.emdadul.comedyking.datahelper.Utils;
import com.emdadul.comedyking.databinding.VerticalRecyclerItemBinding;
import com.eplayer.InfoRepository;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MyVerticalAdapter extends RecyclerView.Adapter<MyVerticalAdapter.MyViewHolder> {
	
	private final ArrayList<HashMap<String, String>> arrayList;
	private final Context context;
	public static int TOTAL = 0, DONE = 0;
	
	
	
	public MyVerticalAdapter(ArrayList<HashMap<String, String>> arrayList, Context context) {
		this.arrayList = arrayList;
		this.context = context;
		binding.progressBar.setVisibility(View.VISIBLE);
		TOTAL = arrayList.size();
		
        DONE = 0;
        
	}
	
	@NonNull
	@Override
	public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		VerticalRecyclerItemBinding binding =
		VerticalRecyclerItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
		
		return new MyViewHolder(binding, context);
	}
	
	@Override
	public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
		
		HashMap<String, String> hash = arrayList.get(position);
		String title = hash.get("title");
		String channelId = hash.get("channelId");
		
		holder.binding.contentTitle.setText(title);
		
		// Load channel data only once per row
		if (!holder.isLoaded) {
			holder.loadChannelData(channelId);
			holder.isLoaded = true;
		}
		
		// Arrow click to GridActivity
		holder.binding.arrow.setOnClickListener(v -> {
			if (holder.videoIdsList.isEmpty() || holder.titlesList.isEmpty()) {
				Toast.makeText(context, "Data is still loading, please wait...", Toast.LENGTH_SHORT).show();
				return;
			}
			
			Intent intent = new Intent(context, GridActivity.class);
			intent.putStringArrayListExtra("videoIds", new ArrayList<>(holder.videoIdsList));
			intent.putStringArrayListExtra("videoTitle", new ArrayList<>(holder.titlesList));
			context.startActivity(intent);
		});
	}
	
	@Override
	public int getItemCount() {
		return arrayList.size();
	}
	
	// ================================================================
	// VIEW HOLDER
	// ================================================================
	public static class MyViewHolder extends RecyclerView.ViewHolder {
		
		final VerticalRecyclerItemBinding binding;
		final Context context;
		
		final ArrayList<String> videoIdsList = new ArrayList<>();
		final ArrayList<String> titlesList = new ArrayList<>();
		final MyHorizontalAdapter horizontalAdapter;
		
		boolean isLoaded = false; // Prevent multiple loads per row
		
		public MyViewHolder(@NonNull VerticalRecyclerItemBinding binding, Context context) {
			super(binding.getRoot());
			this.binding = binding;
			this.context = context;
			
			// Horizontal adapter created once
			horizontalAdapter = new MyHorizontalAdapter(context, videoIdsList, titlesList);
			binding.recyclerView.setAdapter(horizontalAdapter);
		}
		
		// ================================================================
		// LOAD CHANNEL DATA (cached or server)
		// ================================================================
		public void loadChannelData(String channelId) {
			
			// 1️⃣ Try cached video IDs
			ArrayList<String> cached = ComedyKing.getVideoIds(context, channelId);
			
			if (!cached.isEmpty() && !ComedyKing.shouldFetchFromServer(context, channelId)) {
				for (String videoId : cached) {
					fetchVideoTitle(videoId);
					videoIdsList.add(videoId);
				}
				return;
			}
			
			// 2️⃣ Fetch from server
			fetchFromServer(channelId);
		}
		
		// ================================================================
		// FETCH FROM SERVER using hidden EPlayer
		// ================================================================
		private void fetchFromServer(String channelId) {
			
			Utils.checkAndDataShowDialog(context);
			
			// Hidden container for EPlayer
			FrameLayout hiddenContainer = new FrameLayout(context);
			hiddenContainer.setVisibility(FrameLayout.GONE);
			((Activity) context).addContentView(
			hiddenContainer,
			new FrameLayout.LayoutParams(1, 1)
			);
			
			EPlayer player = new EPlayer(context);
			player.enableAutoPlayOnReady();
			hiddenContainer.addView(player);
			
			player.setListener(new EPlayer.OnPlayerListener() {
				@Override
				public void onReady() {
					player.setPlaylistById(channelId);
				}
				
				@Override
				public void onPlaylistFetched(ArrayList<String> videoIds) {
					
					videoIdsList.addAll(videoIds);
					
					// Save to cache
					ComedyKing.saveVideoIds(context, channelId, new ArrayList<>(videoIdsList));
					
					// Fetch titles
					for (String id : videoIdsList) {
						fetchVideoTitle(id);
					}
					
					
					if (videoIdsList.size() >= 200 && ++DONE == TOTAL) {
						((Activity) context).runOnUiThread(() ->
						binding.progressBar.setVisibility(View.GONE)
						);
					}
					
					
					
					
					
					
					
				}
				
				@Override public void onError(String error) {}
				@Override public void onPlaylistEnded() {}
				@Override public void onVideoIdChanged(String id) {}
			});
		}
		
		// ================================================================
		// FETCH VIDEO TITLE
		// ================================================================
		private void fetchVideoTitle(String videoId) {
			
			InfoRepository repo = new InfoRepository(context);
			
			repo.getInfo(videoId, new InfoRepository.ResponseListener() {
				@Override
				public void onResponse(JSONObject json) {
					try {
						String title = json.getString("title");
						
						((Activity) context).runOnUiThread(() -> {
							titlesList.add(title);
							horizontalAdapter.notifyItemInserted(titlesList.size() - 1);
						});
						
					} catch (Exception ignored) {}
				}
				
				@Override
				public void onFailure(Exception e) {}
			});
		}
	}
}*/