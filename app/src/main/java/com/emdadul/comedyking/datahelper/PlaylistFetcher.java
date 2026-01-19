package com.emdadul.comedyking.datahelper;


import android.content.Context;
import com.comedyKing.eplayer.EPlayer;
import java.util.ArrayList;

public class PlaylistFetcher {
	private static PlaylistFetcher instance;
	private final EPlayer dataPlayer;
	
	private PlaylistFetcher(Context context) {
		// একটি মাত্র প্লেয়ার ইনস্ট্যান্স যা শুধু ডেটা ফেচ করবে
		dataPlayer = new EPlayer(context.getApplicationContext());
		dataPlayer.setEnabled(false);
	}
	
	public static synchronized PlaylistFetcher getInstance(Context context) {
		if (instance == null) {
			instance = new PlaylistFetcher(context);
		}
		return instance;
	}
	
	public interface FetchCallback {
		void onSuccess(ArrayList<String> ids);
		void onError(String error);
	}
	
	public void fetch(String channelId, FetchCallback callback) {
		dataPlayer.setListener(new EPlayer.OnPlayerListener() {
			@Override
			public void onReady() {
				dataPlayer.setPlaylistById(channelId);
			}
			
			@Override
			public void onPlaylistFetched(ArrayList<String> ids) {
				callback.onSuccess(ids);
			}
			
			@Override
			public void onError(String error) {
				callback.onError(error);
			}
			
			@Override public void onPlaylistEnded() {}
			@Override public void onVideoIdChanged(String id) {}
		});
	}
}