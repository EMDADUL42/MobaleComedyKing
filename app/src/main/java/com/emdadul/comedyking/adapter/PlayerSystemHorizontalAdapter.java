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
	
	@NonNull
	@Override
	public PlayerSystemHorizontalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		CheckBinding binding = CheckBinding.inflate(LayoutInflater.from(context), parent, false);
		return new PlayerSystemHorizontalViewHolder(binding, context);
	}
	
	@Override
	public void onBindViewHolder(@NonNull PlayerSystemHorizontalViewHolder holder, int position, @NonNull List<Object> payloads) {
		if (!payloads.isEmpty()) {
			for (Object payload : payloads) {
				if (payload.equals("TITLE_UPDATE")) {
					String title = (videoTitleArray.size() > position) ? videoTitleArray.get(position) : "...";
					holder.updateTitle(title);
				}
			}
			} else {
			onBindViewHolder(holder, position);
		}
	}
	
	@Override
	public void onBindViewHolder(@NonNull PlayerSystemHorizontalViewHolder holder, int position) {
		String videoId = videoIdsArray.get(position);
		String title = (videoTitleArray.size() > position) ? videoTitleArray.get(position) : "Loading...";
		
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
		holder.clearAnimation();
		super.onViewDetachedFromWindow(holder);
	}
}