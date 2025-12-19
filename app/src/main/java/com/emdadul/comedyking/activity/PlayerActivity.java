package com.emdadul.comedyking.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.comedyKing.eplayer.EPlayer;
import com.emdadul.comedyking.adapter.PlayerRecyclerAdapter;
import com.emdadul.comedyking.base.BaseActivity;
import com.emdadul.comedyking.databinding.PlayerActivityBinding;
import java.util.ArrayList;
import java.util.List;




/*
This App Made By Md Emdadul Huqe
mdemdadulhuqe01@gmail.com
01928077542
*/



public class PlayerActivity extends BaseActivity<PlayerActivityBinding> {
	
	ArrayList<String> videoTitles = new ArrayList<>();
	ArrayList<String> videoIds = new ArrayList<>();
	EPlayer ePlayer;
	String myId;
	private int currentVideoIndex = 0;
	private static final long ONE_DAY = 24 * 60 * 60 * 1000L;
	private static final String PLAYLIST_ID = "UUkKDpnzIG29MA--Sh6oOtWg";

	
	public PlayerActivity() {
		super(PlayerActivityBinding::inflate);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		EdgeToEdge.enable(this);
		
		// Safe padding for system bars
		ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});
		
		ePlayer = binding.ePlayer;
		
		// Receive video IDs & titles
		videoTitles = getIntent().getStringArrayListExtra("videoTitleArray");
		videoIds = getIntent().getStringArrayListExtra("videoIdsArray");
		myId = getIntent().getStringExtra("videoId");
		
		
		
		
		
		if (videoIds == null) videoIds = new ArrayList<>();
		
		// Setup RecyclerView with click listener
		PlayerRecyclerAdapter adapter = new PlayerRecyclerAdapter(
		this,
		videoIds,
		videoTitles,
		position -> {
			currentVideoIndex = position;
			ePlayer.loadByVideoId(videoIds.get(position));
			binding.recyclerView.smoothScrollToPosition(position);
		}
		);
		
		StaggeredGridLayoutManager layoutManager =
		new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);
		
		// Playlist handling
		if (!videoIds.isEmpty()) {
			setupRecyclerAndPlayer(videoIds);
			} else {
			fetchPlaylistFromServer();
		}
		
		//getInfo();
	}
	
	private void setupRecyclerAndPlayer(List<String> ids) {
		if (ids == null || ids.isEmpty()) return;
		
		// Play first video by default
		ePlayer.setPlaylistById(ids.get(0));
		currentVideoIndex = 0;
		
		
		ePlayer.setListener(new EPlayer.OnPlayerListener(){
			
			
			@Override
			public void onReady() {
				
				ePlayer.loadByVideoId(myId);
			}

			@Override
			public void onError(String arg0) {
			}

			@Override
			public void onPlaylistEnded() {
				
				
			}

			@Override
			public void onVideoIdChanged(String arg0) {
			}

			@Override
			public void onPlaylistFetched(ArrayList<String> arg0) {
			}
			
		});
		
		
		
		
	}
	
	private void playNextVideo(List<String> ids) {
		if (ids != null && !ids.isEmpty()) {
			currentVideoIndex = (currentVideoIndex + 1) % ids.size();
			ePlayer.loadByVideoId(ids.get(currentVideoIndex));
			binding.recyclerView.smoothScrollToPosition(currentVideoIndex);
		}
	}
	
	private void fetchPlaylistFromServer() {
		
		
		ePlayer.setListener(new EPlayer.OnPlayerListener(){
			
			@Override
			public void onReady() {
				//
				
				ePlayer.loadByVideoId(videoIds.get(currentVideoIndex));
			}

			@Override
			public void onError(String arg0) {
			}

			@Override
			public void onPlaylistEnded() {
			}

			@Override
			public void onVideoIdChanged(String arg0) {
			}

			@Override
			public void onPlaylistFetched(ArrayList<String> arg0) {
			}
			
		});
		
		
		
	}
	

}