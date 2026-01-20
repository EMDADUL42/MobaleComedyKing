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
import com.emdadul.comedyking.adapter.MyHorizontalAdapter;
import com.emdadul.comedyking.datahelper.ChannelModel;
import com.emdadul.comedyking.datahelper.ComedyKing;
import com.emdadul.comedyking.databinding.VerticalRecyclerItemBinding;
import com.eplayer.InfoRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashSet;

public class PlayerSystemVerticalViewModel extends RecyclerView.ViewHolder {
    
    public final VerticalRecyclerItemBinding binding;
    private final Context context;
    private final ComedyKing comedyKing;
    private final HashSet<String> loadingChannels;
    private MyHorizontalAdapter horizontalAdapter;
    private ChannelModel currentModel;

    public PlayerSystemVerticalViewModel(@NonNull VerticalRecyclerItemBinding binding, Context context, 
                        ComedyKing comedyKing, HashSet<String> loadingChannels, 
                        RecyclerView.RecycledViewPool sharedPool) {
        super(binding.getRoot());
        this.binding = binding;
        this.context = context;
        this.comedyKing = comedyKing;
        this.loadingChannels = loadingChannels;

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setItemViewCacheSize(10);
        binding.recyclerView.setRecycledViewPool(sharedPool);
    }

    public void bind(ChannelModel model) {
        this.currentModel = model;
        binding.contentTitle.setText(model.getTitle());
        
        horizontalAdapter = new MyHorizontalAdapter(context, model.getVideoIds(), model.getVideoTitles());
        binding.recyclerView.setAdapter(horizontalAdapter);

        String cacheKey = "channel_" + model.getChannelId();
        String cached = comedyKing.loadFromFileCache(context, cacheKey);

        if (cached != null && model.getVideoIds().isEmpty()) {
            loadFromCache(model, cached);
        } else if (model.getVideoIds().isEmpty() && !loadingChannels.contains(model.getChannelId())) {
            fetchFromServer(model);
        }
    }

    private void loadFromCache(ChannelModel model, String json) {
        try {
            JSONObject obj = new JSONObject(json);
            JSONArray jIds = obj.getJSONArray("videoIds");
            JSONArray jTitles = obj.getJSONArray("titles");
            
            model.getVideoIds().clear();
            model.getVideoTitles().clear();
            
            for (int i = 0; i < jIds.length(); i++) {
                model.getVideoIds().add(jIds.getString(i));
                model.getVideoTitles().add(i < jTitles.length() ? jTitles.getString(i) : "...");
            }
            horizontalAdapter.notifyDataSetChanged();
        } catch (Exception ignored) {}
    }

    private void fetchFromServer(ChannelModel model) {
        loadingChannels.add(model.getChannelId());
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (context instanceof Activity && !((Activity) context).isFinishing()) {
                executeEPlayerFetch(model);
            }
        }, 300);
    }

    private void executeEPlayerFetch(ChannelModel model) {
        FrameLayout container = new FrameLayout(context);
        ((Activity) context).addContentView(container, new FrameLayout.LayoutParams(2, 2));
        
        EPlayer player = new EPlayer(context);
        player.setEnabled(false);
        container.addView(player);

        player.setListener(new EPlayer.OnPlayerListener() {
            @Override public void onReady() { player.setPlaylistById(model.getChannelId()); }
            @Override public void onPlaylistFetched(ArrayList<String> fetchedIds) {
                model.getVideoIds().addAll(fetchedIds);
                for (int i = 0; i < fetchedIds.size(); i++) model.getVideoTitles().add("Data Is Loading...");
                
                horizontalAdapter.notifyDataSetChanged();
                for (int i = 0; i < fetchedIds.size(); i++) {
                    fetchTitle(fetchedIds.get(i), i, model);
                }
                releaseResources(container, player, model.getChannelId());
            }
            @Override public void onError(String error) { releaseResources(container, player, model.getChannelId()); }
            @Override public void onPlaylistEnded() {}
            @Override public void onVideoIdChanged(String id) {}
        });
    }

    private void fetchTitle(String videoId, int index, ChannelModel model) {
        new InfoRepository(context).getInfo(videoId, new InfoRepository.ResponseListener() {
            @Override
            public void onResponse(JSONObject json) {
                try {
                    String title = json.getString("title");
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (index < model.getVideoTitles().size()) {
                            model.getVideoTitles().set(index, title);
                            if (currentModel != null && currentModel.getChannelId().equals(model.getChannelId())) {
                                horizontalAdapter.notifyItemChanged(index, "TITLE_UPDATE");
                            }
                            saveCache(model);
                        }
                    });
                } catch (Exception ignored) {}
            }
            @Override public void onFailure(Exception e) {}
        });
    }

    private void saveCache(ChannelModel model) {
        try {
            JSONObject root = new JSONObject();
            root.put("videoIds", new JSONArray(model.getVideoIds()));
            root.put("titles", new JSONArray(model.getVideoTitles()));
            comedyKing.saveToFileCacheAsync(context, "channel_" + model.getChannelId(), root.toString());
        } catch (Exception ignored) {}
    }

    private void releaseResources(FrameLayout container, EPlayer player, String channelId) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (container != null && container.getParent() != null) {
                container.removeAllViews();
                ((ViewGroup) container.getParent()).removeView(container);
            }
            loadingChannels.remove(channelId);
        });
    }
}