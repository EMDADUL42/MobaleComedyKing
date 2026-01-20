package com.emdadul.comedyking.viewmodel;


import android.content.Context;
import android.content.Intent;
import android.view.View;
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

public class PlayerSystemHorizontalViewHolder extends RecyclerView.ViewHolder {
	public final CheckBinding binding;
	private final Context context;
	
	public PlayerSystemHorizontalViewHolder(@NonNull CheckBinding binding, Context context) {
		super(binding.getRoot());
		this.binding = binding;
		this.context = context;
	}
	
	public void bind(String videoId, String title, ArrayList<String> videoIdsArray, ArrayList<String> videoTitleArray, int position, int lastPosition) {
		binding.contentTitle.setText(title);
		
		// Glide Optimization
		String thumbUrl = "https://img.youtube.com/vi/" + videoId + "/mqdefault.jpg";
		Glide.with(context)
		.load(thumbUrl)
		.centerCrop()
		.override(240, 135)
		.diskCacheStrategy(DiskCacheStrategy.ALL)
		.placeholder(R.drawable.a)
		.into(binding.horizontalImageItem);
		
		// Click Event
		binding.getRoot().setOnClickListener(v -> {
			Intent intent = new Intent(context, PlayerActivity.class);
			intent.putStringArrayListExtra("videoIdsArray", new ArrayList<>(videoIdsArray));
			intent.putStringArrayListExtra("videoTitleArray", new ArrayList<>(videoTitleArray));
			intent.putExtra("videoId", videoId);
			context.startActivity(intent);
		});
		
		// Animation
		if (position > lastPosition) {
			Animation animation = AnimationUtils.loadAnimation(context, R.anim.myanim);
			itemView.startAnimation(animation);
		}
	}
	
	public void updateTitle(String title) {
		binding.contentTitle.setText(title);
	}
	
	public void clearAnimation() {
		itemView.clearAnimation();
	}
}