package com.emdadul.comedyking.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.emdadul.comedyking.adapter.PlayerSystemVerticalAdapter;
import com.emdadul.comedyking.datahelper.ChannelModel;
import com.emdadul.comedyking.databinding.FragmentHomeBinding;
import com.emdadul.comedyking.datahelper.ComedyKing;
import com.emdadul.comedyking.viewmodel.PlayerSystemVerticalViewModel;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;

public class HomeFragment extends Fragment {
	
	private FragmentHomeBinding binding;
	private ComedyKing comedyKing;
	private PlayerSystemVerticalAdapter adapter;
	
	// কিউ সিস্টেমের জন্য স্ট্যাটিক ভেরিয়েবল
	public static java.util.Queue<ChannelModel> pendingChannels = new java.util.LinkedList<>();
	public static boolean isCurrentlyLoading = false;
	
	private final ArrayList<ChannelModel> arrayList = new ArrayList<>();
	private int currentPage = 1;
	private final int pageSize = 15;
	private boolean isLoading = false;
	private boolean hasMore = true;
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentHomeBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		comedyKing = new ComedyKing(requireActivity());
		
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		binding.recyclerView.setHasFixedSize(true);
		
		adapter = new PlayerSystemVerticalAdapter(requireActivity(), arrayList);
		binding.recyclerView.setAdapter(adapter);
		
		loadData();
		
		binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
				if (dy > 0 && !isLoading && hasMore) {
					LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
					if (lm != null && lm.findLastVisibleItemPosition() >= arrayList.size() - 3) {
						currentPage++;
						loadData();
					}
				}
			}
		});
	}
	
	public void loadData() {
		if (isLoading) return;
		isLoading = true;
		if (currentPage == 1) binding.progressBar.setVisibility(View.VISIBLE);
		
		comedyKing.fetchPage(requireActivity(), currentPage, pageSize, new ComedyKing.PaginationResponseHandler() {
			@Override
			public void onSuccess(String json, int itemCount) {
				if (!isAdded() || binding == null) return;
				try {
					JSONObject jsonResponse = new JSONObject(json);
					hasMore = jsonResponse.optBoolean("has_more", false);
					JSONArray data = jsonResponse.getJSONArray("data");
					int startPosition = arrayList.size();
					
					for (int i = 0; i < data.length(); i++) {
						JSONObject obj = data.getJSONObject(i);
						ChannelModel model = new ChannelModel(obj.getString("channelName"), obj.getString("channelId"));
						arrayList.add(model);
					}
					
					binding.recyclerView.post(() -> {
						if (binding != null) {
							if (currentPage == 1) adapter.notifyDataSetChanged();
							else adapter.notifyItemRangeInserted(startPosition, data.length());
							isLoading = false;
							binding.progressBar.setVisibility(View.GONE);
						}
					});
					} catch (Exception e) {
					isLoading = false;
					hideProgressBar();
				}
			}
			}, new ComedyKing.ErrorHandler() {
			@Override
			public void onError(IOException exception) {
				isLoading = false;
				hideProgressBar();
			}
		});
	}
	
	private void hideProgressBar() {
		if (binding != null) binding.recyclerView.post(() -> binding.progressBar.setVisibility(View.GONE));
	}
	
	@Override
	public void onDestroyView() {
		pendingChannels.clear();
		isCurrentlyLoading = false;
		super.onDestroyView();
		binding = null;
	}
}