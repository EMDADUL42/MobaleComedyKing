package com.emdadul.comedyking.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.emdadul.comedyking.R;
import com.emdadul.comedyking.activity.PlayerActivity;
import com.emdadul.comedyking.databinding.GridRecyclerItemBinding;
import java.util.ArrayList;
import java.util.Collections;



public class GirdRecyclerAdapter extends RecyclerView.Adapter<GirdRecyclerAdapter.MyViewHolder> {
	
	private Context context;
	private ArrayList<String> videoIdArray;
	private ArrayList<String> videoTitleArray;
	
	public GirdRecyclerAdapter(Context context, ArrayList<String> videoIdArray, ArrayList<String> videoTitleArray) {
		this.context = context;
		
		// Pair list banai to shuffle ID-title together
		ArrayList<Pair> pairList = new ArrayList<>();
		for (int i = 0; i < videoIdArray.size(); i++) {
			pairList.add(new Pair(videoIdArray.get(i), videoTitleArray.get(i)));
		}
		
		// Shuffle the pair list
		Collections.shuffle(pairList);
		
		// Separate lists back
		this.videoIdArray = new ArrayList<>();
		this.videoTitleArray = new ArrayList<>();
		for (Pair p : pairList) {
			this.videoIdArray.add(p.id);
			this.videoTitleArray.add(p.title);
		}
	}
	
	private static class Pair {
		String id, title;
		Pair(String id, String title) {
			this.id = id;
			this.title = title;
		}
	}
	
	public static class MyViewHolder extends RecyclerView.ViewHolder {
		GridRecyclerItemBinding binding;
		
		public MyViewHolder(@NonNull GridRecyclerItemBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}
	
	@NonNull
	@Override
	public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		GridRecyclerItemBinding binding = GridRecyclerItemBinding.inflate(inflater, parent, false);
		return new MyViewHolder(binding);
	}
	
	@Override
	public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
		
		String getVideoId = videoIdArray.get(position);
		String getVideoTitle = videoTitleArray.get(position);
		int splitIndex = Math.min(10, getVideoTitle.length());
		String part1 = getVideoTitle.substring(0, splitIndex);
		String part2 = getVideoTitle.substring(splitIndex);
		String getTitle = part1 + part2;
		String customTitle = "Comedy_Video_" + getTitle;
		holder.binding.title.setText(customTitle);
		
		
		
		
		Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.slide_in_left);
		Animation animation1 = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.slide_in_right);
		
		if (position % 2 == 0) {
			holder.binding.title.setMaxLines(3);
			holder.itemView.startAnimation(animation);
			} else {
			holder.binding.title.setMaxLines(2);
			holder.itemView.startAnimation(animation1);
		}
		
		String thumbUrl = "https://img.youtube.com/vi/" + getVideoId + "/hqdefault.jpg";
		Glide.with(context).load(thumbUrl).override(480, 660).into(holder.binding.thumb);
		
		holder.binding.cardView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				v.animate()
				.scaleX(0.95f)
				.scaleY(0.95f)
				.setDuration(150)
				.withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).start())
				.start();
			}
		});
		
		
		
		holder.binding.getRoot().setOnClickListener(v->{
			
			Intent intent = new Intent(context, PlayerActivity.class);
			intent.putStringArrayListExtra("videoIdsArray",videoIdArray);
			intent.putStringArrayListExtra("videoTitleArray",videoTitleArray);
			intent.putExtra("videoId",getVideoId);
			context.startActivity(intent);
			
			
		});
		
		
	}
	
	@Override
	public int getItemCount() {
		return videoIdArray.size();
	}
}


