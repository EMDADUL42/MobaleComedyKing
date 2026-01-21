package com.emdadul.comedyking.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.emdadul.comedyking.databinding.CheckBinding;
import com.emdadul.comedyking.viewmodel.PlayerSystemHorizontalViewHolder;
import java.util.ArrayList;
import java.util.List;

public class PlayerSystemHorizontalAdapter extends RecyclerView.Adapter<PlayerSystemHorizontalViewHolder> {
	
	private final Context context;
	private final ArrayList<String> videoIdsArray;
	private final ArrayList<String> videoTitleArray;
	private int lastPosition = -1;
	
	public PlayerSystemHorizontalAdapter(Context context, ArrayList<String> videoIds, ArrayList<String> titles) {
		this.context = context;
		this.videoIdsArray = videoIds;
		this.videoTitleArray = titles;
	}
	
	// মাস্টার লিস্ট থেকে ডাটা লোড করার মেথড (Horizontal Lazy Loading)
	public void loadMore(ArrayList<String> masterIds, ArrayList<String> masterTitles) {
		int currentSize = videoIdsArray.size();
		int nextLimit = Math.min(currentSize + 10, masterIds.size());
		
		if (currentSize < nextLimit) {
			for (int i = currentSize; i < nextLimit; i++) {
				videoIdsArray.add(masterIds.get(i));
				videoTitleArray.add(masterTitles.get(i));
			}
			notifyItemRangeInserted(currentSize, nextLimit - currentSize);
		}
	}
	
	@NonNull
	@Override
	public PlayerSystemHorizontalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		CheckBinding binding = CheckBinding.inflate(LayoutInflater.from(context), parent, false);
		return new PlayerSystemHorizontalViewHolder(binding, context);
	}
	
	@Override
	public void onBindViewHolder(@NonNull PlayerSystemHorizontalViewHolder holder, int position, @NonNull List<Object> payloads) {
		if (!payloads.isEmpty() && payloads.get(0).equals("TITLE_UPDATE")) {
			// শুধুমাত্র টাইটেল আপডেট করবে, পুরো আইটেম রেন্ডার হবে না (Blinking বন্ধ হবে)
			String title = (videoTitleArray.size() > position) ? videoTitleArray.get(position) : "...";
			holder.updateTitle(title);
			} else {
			onBindViewHolder(holder, position);
		}
	}
	
	@Override
	public void onBindViewHolder(@NonNull PlayerSystemHorizontalViewHolder holder, int position) {
		String videoId = videoIdsArray.get(position);
		String title = (videoTitleArray.size() > position) ? videoTitleArray.get(position) : "...";
		
		holder.bind(videoId, title, videoIdsArray, videoTitleArray, position, lastPosition);
		
		if (position > lastPosition) {
			lastPosition = position;
		}
	}
	
	@Override
	public int getItemCount() {
		return videoIdsArray != null ? videoIdsArray.size() : 0;
	}
	
	@Override
	public void onViewDetachedFromWindow(@NonNull PlayerSystemHorizontalViewHolder holder) {
		// ভিউ স্ক্রিনের বাইরে চলে গেলে অ্যানিমেশন ক্লিয়ার করবে
		holder.clearAnimation();
		super.onViewDetachedFromWindow(holder);
	}
}