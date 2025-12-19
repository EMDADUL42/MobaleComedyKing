package com.emdadul.comedyking.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.emdadul.comedyking.R;
import com.emdadul.comedyking.activity.PlayerActivity;
import com.emdadul.comedyking.databinding.CheckBinding;

import java.util.ArrayList;

public class MyHorizontalAdapter extends RecyclerView.Adapter<MyHorizontalAdapter.ViewHolder> {
	
	private final Context context;
	private final ArrayList<String> videoIdsArray;
	private final ArrayList<String> videoTitleArray;
	
	public MyHorizontalAdapter(Context context, ArrayList<String> videoIds, ArrayList<String> titles) {
		this.context = context;
		this.videoIdsArray = videoIds;
		this.videoTitleArray = titles;
	}
	
	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		CheckBinding binding = CheckBinding.inflate(LayoutInflater.from(context), parent, false);
		return new ViewHolder(binding);
	}
	
	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		String videoId = videoIdsArray.get(position);
		String title = videoTitleArray.size() > position ? videoTitleArray.get(position) : "";
		
		// Glide optimized for smooth scrolling
		String thumbUrl = "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
		Glide.with(context)
		.load(thumbUrl)
		.centerCrop()
		.thumbnail(0.1f) // small placeholder while loading
		.into(holder.binding.horizontalImageItem);
		
		holder.binding.contentTitle.setText("Comedy_Video_" + title);
		
		// Click to PlayerActivity
		holder.binding.getRoot().setOnClickListener(v -> {
			Intent intent = new Intent(context, PlayerActivity.class);
			intent.putStringArrayListExtra("videoIdsArray", videoIdsArray);
			intent.putStringArrayListExtra("videoTitleArray", videoTitleArray);
			intent.putExtra("videoId", videoId);
			context.startActivity(intent);
		});
	}
	
	@Override
	public int getItemCount() {
		return videoIdsArray.size();
	}
	
	// Efficiently add new item
	public void addItem(String videoId, String title) {
		videoIdsArray.add(videoId);
		videoTitleArray.add(title);
		notifyItemInserted(videoIdsArray.size() - 1);
	}
	
	public static class ViewHolder extends RecyclerView.ViewHolder {
		final CheckBinding binding;
		
		public ViewHolder(@NonNull CheckBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}
}