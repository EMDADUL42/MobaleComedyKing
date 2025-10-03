package com.emdadul.comedyking.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.emdadul.comedyking.R;
import com.emdadul.comedyking.activity.PlayerActivity;
import com.emdadul.comedyking.databinding.HorizontalRecyclerBinding;

import java.util.ArrayList;

public class MyHorizontalAdapter extends RecyclerView.Adapter<MyHorizontalAdapter.ViewHolder> {
	
	private final Context context;
	private final ArrayList<String> videoIds;
	private final ArrayList<String> titles;
	
	public MyHorizontalAdapter(Context context, ArrayList<String> videoIds, ArrayList<String> titles) {
		this.context = context;
		this.videoIds = videoIds;
		this.titles = titles;
	}
	
	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		HorizontalRecyclerBinding binding = HorizontalRecyclerBinding.inflate(
		LayoutInflater.from(context), parent, false);
		return new ViewHolder(binding);
	}
	
	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		String videoId = videoIds.get(position);
		String title = titles.size() > position ? titles.get(position) : "";
		
		String thumbUrl = "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
		Glide.with(context).load(thumbUrl).override(480, 660).into(holder.binding.horizontalImageItem);
		
		String customTitle = "Comedy_Video_" + title;
		holder.binding.contentTitle.setText(customTitle);
		
		holder.binding.cardView.setOnClickListener(v -> v.animate()
		.scaleX(0.95f)
		.scaleY(0.95f)
		.setDuration(150)
		.withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).start())
		.start());
		
		Animation animation = AnimationUtils.loadAnimation(context, R.anim.up_from_bottom);
		holder.binding.cardView.startAnimation(animation);
		
		holder.binding.getRoot().setOnClickListener(v -> {
			Intent intent = new Intent(context, PlayerActivity.class);
			intent.putExtra("VIDEO_ID", videoId);
			v.getContext().startActivity(intent);
		});
	}
	
	@Override
	public int getItemCount() {
		return videoIds.size();
	}
	
	// ✅ DiffUtil দিয়ে updateData
	public void updateData(ArrayList<String> newVideoIds, ArrayList<String> newTitles) {
		DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
			@Override
			public int getOldListSize() {
				return videoIds.size();
			}
			
			@Override
			public int getNewListSize() {
				return newVideoIds.size();
			}
			
			@Override
			public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
				return videoIds.get(oldItemPosition).equals(newVideoIds.get(newItemPosition));
			}
			
			@Override
			public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
				return titles.get(oldItemPosition).equals(newTitles.get(newItemPosition));
			}
		});
		
		videoIds.clear();
		videoIds.addAll(newVideoIds);
		
		titles.clear();
		titles.addAll(newTitles);
		
		diffResult.dispatchUpdatesTo(this);
	}
	
	// ✅ আগের addItem method safe রাখতে
	public void addItem(String videoId, String title) {
		ArrayList<String> newIds = new ArrayList<>(videoIds);
		ArrayList<String> newTitles = new ArrayList<>(titles);
		
		newIds.add(videoId);
		newTitles.add(title);
		
		updateData(newIds, newTitles);
	}
	
	public static class ViewHolder extends RecyclerView.ViewHolder {
		final HorizontalRecyclerBinding binding;
		
		public ViewHolder(@NonNull HorizontalRecyclerBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}
}


