package com.emdadul.comedyking.datahelper;


import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.comedyKing.eplayer.EPlayer;
import com.emdadul.comedyking.datahelper.ChannelModel;

import java.util.List;

public class ChannelSerialLoader {
	
	private final Context context;
	private final List<ChannelModel> channels;
	
	private int currentIndex = 0;
	private boolean isRunning = false;
	
	public ChannelSerialLoader(Context context, List<ChannelModel> channels) {
		this.context = context;
		this.channels = channels;
	}
	
	// 🔥 ONE VOID – EVERYTHING HERE
	public void start() {
		if (isRunning) return;
		isRunning = true;
		loadNext();
	}
	
	private void loadNext() {
		if (currentIndex >= channels.size()) {
			isRunning = false;
			return; // ✅ ALL DONE
		}
		
		ChannelModel model = channels.get(currentIndex);
		
		FrameLayout container = new FrameLayout(context);
		((Activity) context).addContentView(
		container,
		new FrameLayout.LayoutParams(1, 1)
		);
		
		EPlayer player = new EPlayer(context);
		player.setEnabled(false);
		container.addView(player);
		
		player.setListener(new EPlayer.OnPlayerListener() {
			
			@Override
			public void onReady() {
				player.setPlaylistById(model.getChannelId());
			}
			
			@Override
			public void onPlaylistFetched(java.util.ArrayList<String> ids) {
				
				if (ids != null) {
					model.getMasterIds().clear();
					model.getMasterTitles().clear();
					
					model.getMasterIds().addAll(ids);
					for (int i = 0; i < ids.size(); i++) {
						model.getMasterTitles().add("...");
					}
				}
				
				cleanup(container, player);
				
				currentIndex++;      // 🔥 MOVE TO NEXT
				loadNext();          // 🔥 AUTO CONTINUE
			}
			
			@Override
			public void onError(String error) {
				cleanup(container, player);
				currentIndex++;
				loadNext();
			}
			
			@Override public void onPlaylistEnded() {}
			@Override public void onVideoIdChanged(String id) {}
		});
	}
	
	private void cleanup(FrameLayout container, EPlayer player) {
		new Handler(Looper.getMainLooper()).post(() -> {
			try {
				if (player != null) player.setListener(null);
				if (container != null && container.getParent() != null) {
					((ViewGroup) container.getParent()).removeView(container);
				}
			} catch (Exception ignored) {}
		});
	}
}