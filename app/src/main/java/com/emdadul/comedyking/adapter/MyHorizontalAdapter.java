package com.emdadul.comedyking.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.emdadul.comedyking.R;
import com.emdadul.comedyking.activity.PlayerActivity;
import com.emdadul.comedyking.databinding.CheckBinding;

import java.util.ArrayList;
import java.util.List;

public class MyHorizontalAdapter extends RecyclerView.Adapter<MyHorizontalAdapter.ViewHolder> {
	
	private final Context context;
	private final ArrayList<String> videoIdsArray;
	private final ArrayList<String> videoTitleArray;
	private int lastPosition = -1;
	
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
	
	// ১. Payload Handling: টাইটেল আপডেট হলে ইমেজ রিলোড আটকাবে
	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
		if (!payloads.isEmpty()) {
			for (Object payload : payloads) {
				if (payload.equals("TITLE_UPDATE")) {
					String title = (videoTitleArray.size() > position) ? videoTitleArray.get(position) : "...";
					holder.binding.contentTitle.setText(title);
				}
			}
			} else {
			// পেলোড না থাকলে স্বাভাবিক বাইন্ড কল হবে
			onBindViewHolder(holder, position);
		}
	}
	
	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		String videoId = videoIdsArray.get(position);
		String title = (videoTitleArray.size() > position) ? videoTitleArray.get(position) : "Loading...";
		
		holder.binding.contentTitle.setText(title);
		
		// ২. Glide Optimization (Memory Management)
		String thumbUrl = "https://img.youtube.com/vi/" + videoId + "/mqdefault.jpg";
		
		Glide.with(context)
		.load(thumbUrl)
		.centerCrop()
		.override(240, 135) // থাম্বনেইল ফিক্সড সাইজ
		.diskCacheStrategy(DiskCacheStrategy.ALL)
		.placeholder(R.drawable.a)
		.into(holder.binding.horizontalImageItem);
		
		// ৩. Click Event
		holder.binding.getRoot().setOnClickListener(v -> {
			Intent intent = new Intent(context, PlayerActivity.class);
			// বর্তমান লিস্টের ডাটা পাঠিয়ে দেওয়া হচ্ছে
			intent.putStringArrayListExtra("videoIdsArray", new ArrayList<>(videoIdsArray));
			intent.putStringArrayListExtra("videoTitleArray", new ArrayList<>(videoTitleArray));
			intent.putExtra("videoId", videoId);
			context.startActivity(intent);
		});
		
		// ৪. Animation
		setAnimation(holder.itemView, position);
	}
	
	private void setAnimation(android.view.View viewToAnimate, int position) {
		// নতুন আইটেম স্ক্রল হয়ে আসলে অ্যানিমেশন হবে
		if (position > lastPosition) {
			Animation animation = AnimationUtils.loadAnimation(context, R.anim.myanim);
			viewToAnimate.startAnimation(animation);
			lastPosition = position;
		}
	}
	
	@Override
	public int getItemCount() {
		return videoIdsArray != null ? videoIdsArray.size() : 0;
	}
	
	@Override
	public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
		holder.itemView.clearAnimation();
		super.onViewDetachedFromWindow(holder);
	}
	
	public static class ViewHolder extends RecyclerView.ViewHolder {
		final CheckBinding binding;
		public ViewHolder(@NonNull CheckBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}
}