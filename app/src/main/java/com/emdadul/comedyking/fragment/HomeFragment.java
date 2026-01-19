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

import com.emdadul.comedyking.datahelper.ChannelModel;
import com.emdadul.comedyking.adapter.MyVerticalAdapter;
import com.emdadul.comedyking.databinding.FragmentHomeBinding;
import com.emdadul.comedyking.datahelper.ComedyKing;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class HomeFragment extends Fragment {
	
	private FragmentHomeBinding binding;
	private ComedyKing comedyKing;
	private MyVerticalAdapter adapter;
	
	// HashMap এর বদলে এখন ChannelModel এর লিস্ট
	private final ArrayList<ChannelModel> arrayList = new ArrayList<>();
	
	private int currentPage = 1;
	private final int pageSize = 15; // আপনার PHP লিমিটের সাথে মিল রেখে
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
		
		// RecyclerView Setup
		LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setHasFixedSize(true); // পারফরম্যান্সের জন্য
		
		// Adapter Setup
		adapter = new MyVerticalAdapter(arrayList,requireActivity());
		binding.recyclerView.setAdapter(adapter);
		
		// Initial Data Load
		loadData();
		
		// Pagination logic
		binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
				if (dy > 0 && !isLoading && hasMore) {
					// শেষ আইটেমের ৩টি আগে থাকতেই লোড শুরু হবে (স্মুথ এক্সপেরিয়েন্স)
					if (layoutManager.findLastVisibleItemPosition() >= arrayList.size() - 3) {
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
						
						// HashMap এর বদলে ChannelModel অবজেক্ট তৈরি
						ChannelModel model = new ChannelModel(
						obj.getString("channelName"),
						obj.getString("channelId")
						);
						arrayList.add(model);
					}
					
					// UI Update on Main Thread
					binding.recyclerView.post(() -> {
						if (binding != null) {
							if (currentPage == 1) {
								adapter.notifyDataSetChanged();
								} else {
								adapter.notifyItemRangeInserted(startPosition, data.length());
							}
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
		if (binding != null) {
			binding.recyclerView.post(() -> binding.progressBar.setVisibility(View.GONE));
		}
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null; // মেমোরি লিক প্রিভেনশন
	}
}