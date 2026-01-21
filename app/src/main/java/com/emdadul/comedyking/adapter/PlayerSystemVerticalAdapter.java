package com.emdadul.comedyking.adapter;

import android.view.LayoutInflater;
import android.webkit.WebView;
import android.app.Application;
import android.os.Build;
import android.content.Context;
import com.emdadul.comedyking.databinding.VerticalRecyclerItemBinding;
import java.util.HashSet;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.emdadul.comedyking.datahelper.ChannelModel;
import com.emdadul.comedyking.datahelper.ComedyKing;
import com.emdadul.comedyking.viewmodel.PlayerSystemVerticalViewModel;
import java.util.ArrayList;

public class PlayerSystemVerticalAdapter extends RecyclerView.Adapter<PlayerSystemVerticalViewModel>{
	
	
	private final ArrayList<ChannelModel> arrayList;
	private final Context context;
	private final ComedyKing comedyKing;
	private final HashSet<String> loadingChannels = new HashSet<>();
	private final RecyclerView.RecycledViewPool sharedPool = new RecyclerView.RecycledViewPool();
	
	
	
	public PlayerSystemVerticalAdapter(Context context, ArrayList<ChannelModel> arrayList){
		
		this.arrayList=arrayList;
		this.context= context;
		this.comedyKing = new ComedyKing(context);
		initWebViewSuffix();
		
		
	}
	
	
	private void initWebViewSuffix() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			try {
				String processName = Application.getProcessName();
				if (!context.getPackageName().equals(processName)) {
					WebView.setDataDirectorySuffix(processName);
				}
			} catch (Exception ignored) {}
		}
	}
	
	
	
	@Override
	public PlayerSystemVerticalViewModel onCreateViewHolder(ViewGroup patent, int viewType) {
		
		VerticalRecyclerItemBinding binding = VerticalRecyclerItemBinding.inflate(LayoutInflater.from(context),patent,false);
		return new PlayerSystemVerticalViewModel(binding,context,comedyKing,loadingChannels,sharedPool);
		
	}
	
	@Override
	public void onBindViewHolder(PlayerSystemVerticalViewModel holder, int position) {
		
		ChannelModel channelModel = arrayList.get(position);
		holder.bind(channelModel);
		
		
	}
	
	@Override
	public int getItemCount() {
		
		return arrayList != null ? arrayList.size() : 0;
	}
	
}