package com.emdadul.comedyking.adapter;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.emdadul.comedyking.R;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.emdadul.VideoInfoRepository;
import com.emdadul.comedyking.activity.GridActivity;
import com.emdadul.comedyking.databinding.VerticalRecyclerItemBinding;
import com.emdadul.comedyking.datahelper.ComedyKing;
import com.emdadul.comedyking.datahelper.Utils;
import com.emdadul.listener.OnEventListener;
import com.emdadul.listener.ResponseListener;
import com.emdadul.EPlayer;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;





public class MyVerticalAdapter extends RecyclerView.Adapter<MyVerticalAdapter.MyViewHolder> {
	
	private final ArrayList<HashMap<String, String>> arrayList;
	private final Context context;

	
	public MyVerticalAdapter(ArrayList<HashMap<String, String>> arrayList, Context context) {
		this.arrayList = arrayList;
		this.context = context;
		
	}
	
	@NonNull
	@Override
	public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		VerticalRecyclerItemBinding binding = VerticalRecyclerItemBinding.inflate(
		LayoutInflater.from(parent.getContext()), parent, false
		);
		return new MyViewHolder(binding);
	}
	
	@Override
	public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
		HashMap<String, String> hashMap = arrayList.get(position);
		String title = hashMap.get("title");
		String channelId = hashMap.get("channelId");
		
		holder.binding.title.setText(title);
		
		// Horizontal adapter initialize
		ArrayList<String> videoIdsList = new ArrayList<>();
		ArrayList<String> titlesList = new ArrayList<>();
		
		MyHorizontalAdapter horizontalAdapter = new MyHorizontalAdapter(context, videoIdsList, titlesList);
		holder.binding.recyclerView.setAdapter(horizontalAdapter);
		
		
		// 🔹 Check cache first
		ArrayList<String> cachedIds = ComedyKing.getVideoIds(context, channelId);
		if (!cachedIds.isEmpty() && !ComedyKing.shouldFetchFromServer(context, channelId)) {
			// Cached data use হবে
			for (String videoId : cachedIds) {
				fetchVideoTitle(videoId, horizontalAdapter);
			}
			} else {
			// প্রথমবার অথবা নতুন দিনে server hit হবে
			getVideoInfo(channelId, horizontalAdapter);
		}
		
		
		
		
		
		
		
		//for arrow icon to see more data
		
	
		holder.binding.arrow.setOnClickListener(new View.OnClickListener(){
			
			
			@Override
			public void onClick(View view) {
				if (videoIdsList.isEmpty() || titlesList.isEmpty()) {
					Toast.makeText(context, "Data is still loading, please wait...", Toast.LENGTH_SHORT).show();
					return;
				}
				
				Intent intent = new Intent(context, GridActivity.class);
				intent.putStringArrayListExtra("videoIds", videoIdsList);
				intent.putStringArrayListExtra("videoTitle", titlesList);
				context.startActivity(intent);
			}
		});
		
		
		Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.my);
		holder.binding.cardView.startAnimation(animation);
		
		
		
		
		
	}
	
	private void fetchVideoTitle(String videoId, MyHorizontalAdapter adapter) {
		VideoInfoRepository repository = new VideoInfoRepository(context);
		repository.getInfo(videoId, new ResponseListener() {
			@Override
			public void onResponse(JSONObject jSONObject) {
				try {
					String title = jSONObject.getString("title");
					((Activity) context).runOnUiThread(() -> {
						adapter.addItem(videoId, title);
						adapter.notifyItemInserted(adapter.getItemCount() - 1);
					});
					} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
	public int getItemCount() {
		return arrayList.size();
	}
	
	public static class MyViewHolder extends RecyclerView.ViewHolder {
		final VerticalRecyclerItemBinding binding;
		
		public MyViewHolder(@NonNull VerticalRecyclerItemBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}
	
	public void getVideoInfo(String channelId, MyHorizontalAdapter adapter) {
		
		Utils.checkAndDataShowDialog(context);
		
		
		FrameLayout container = new FrameLayout(context);
		container.setVisibility(FrameLayout.GONE);
		((Activity) context).addContentView(container, new FrameLayout.LayoutParams(0, 0));
		
		EPlayer ePlayer = new EPlayer(context);
		container.addView(ePlayer);
		
		ePlayer.addOnEventListener(new OnEventListener() {
			@Override
			public void onPlayerReady() {
				ePlayer.cueVideo(channelId, 0);
			}
			
			@Override
			public void onPlaybackStateChange(int playerState) {
				if (playerState == EPlayer.CUED) {
					ePlayer.onReceiveVideoIds();
				}
			}
			
			@Override
			public void onReceiveVideoIds(String[] videoIds) {
				ArrayList<String> videoIdList = new ArrayList<>(Arrays.asList(videoIds));
				ComedyKing.saveVideoIds(context, channelId, videoIdList);
				
				// Server থেকে ডেটা আসার পর সাথে সাথে Adapter update হবে
				for (String videoId : videoIdList) {
					fetchVideoTitle(videoId, adapter);
				}
				
				
				
				Utils.dismissDialog();
				
				
			}
		});
	}
	
	
}