package com.emdadul.comedyking.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.emdadul.comedyking.databinding.PlayerRecyclerItemBinding;
import java.util.ArrayList;



/*
This App Made By Md Emdadul Huqe
mdemdadulhuqe01@gmail.com
01928077542
*/


public class PlayerRecyclerAdapter extends RecyclerView.Adapter<PlayerRecyclerAdapter.MyViewHolder> {
	
	private final ArrayList<String> videoIdArray;
	private final ArrayList<String> titleArray;
	private final Context context;
	private final OnItemClickListener listener;
	
	// 🔥 Listener interface
	public interface OnItemClickListener {
		void onItemClick(int position);
	}
	
	// 🔥 Constructor with listener
	public PlayerRecyclerAdapter(Context context,
	ArrayList<String> videoIdArray,
	ArrayList<String> titleArray,
	OnItemClickListener listener) {
		this.context = context;
		this.titleArray = titleArray;
		this.videoIdArray = videoIdArray;
		this.listener = listener;
	}
	
	@NonNull
	@Override
	public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		PlayerRecyclerItemBinding binding = PlayerRecyclerItemBinding.inflate(
		LayoutInflater.from(parent.getContext()), parent, false);
		return new MyViewHolder(binding);
	}
	
	@Override
	public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
		String videoId = videoIdArray.get(position);
		String videoTitle = titleArray.get(position);
		
		
		String thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/0.jpg";
		Glide.with(context)
		.load(thumbnailUrl)
		.into(holder.binding.playerImageItem);
		
		holder.binding.contentTitle.setText(videoTitle);
		
		
		holder.itemView.setOnClickListener(v -> {
			if (listener != null) listener.onItemClick(position);
		});
	}
	
	@Override
	public int getItemCount() {
		return videoIdArray.size();
	}
	
	public static class MyViewHolder extends RecyclerView.ViewHolder {
		final PlayerRecyclerItemBinding binding;
		
		public MyViewHolder(@NonNull PlayerRecyclerItemBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}
}


