package com.emdadul.comedyking.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.emdadul.comedyking.R;
import com.emdadul.comedyking.adapter.MyVerticalAdapter;
import com.emdadul.comedyking.datahelper.ComedyKing;
import com.emdadul.comedyking.databinding.DataLoadCustomDialogBinding;
import com.emdadul.comedyking.databinding.FragmentHomeBinding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

/*
* This App Made By Md Emdadul Huqe
* mdemdadulhuqe01@gmail.com
* 01928077542
* 
*/


public class HomeFragment extends Fragment {
	
	private FragmentHomeBinding binding;
	private ComedyKing comedyKing;
	private ArrayList<HashMap<String, String>> arrayList;
	private Dialog loadingDialog;
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentHomeBinding.inflate(inflater, container, false);
		comedyKing = new ComedyKing(requireActivity());
		
		// RecyclerView setup
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setItemViewCacheSize(20);
		
		
		
		
		
		
		// Show loading
		showLoadingDialog();
		
		// Fetch data
		getServerResponse();
		
		return binding.getRoot();
	}
	
	//===============================================================
	// Show/Hide Dialog
	//===============================================================
	private void showLoadingDialog() {
		if (loadingDialog != null && loadingDialog.isShowing()) return;
		
		DataLoadCustomDialogBinding dialogBinding = DataLoadCustomDialogBinding.inflate(getLayoutInflater());
		loadingDialog = new Dialog(requireContext());
		loadingDialog.setContentView(dialogBinding.getRoot());
		loadingDialog.setCancelable(false);
		
		if (loadingDialog.getWindow() != null) {
			loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		}
		loadingDialog.show();
	}
	
	private void hideLoadingDialog() {
		if (loadingDialog != null && loadingDialog.isShowing()) {
			loadingDialog.dismiss();
		}
	}
	
	//===============================================================
	// Server Response
	//===============================================================
	private void getServerResponse() {
		comedyKing.getResponse(requireActivity(), "getTitleAndIds.php",
		response -> {
			arrayList = new ArrayList<>();
			try {
				JSONArray jsonArray = new JSONArray(response);
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject obj = jsonArray.getJSONObject(i);
					HashMap<String, String> hashMap = new HashMap<>();
					hashMap.put("title", obj.optString("channelName", "Untitled"));
					hashMap.put("channelId", obj.optString("channelId", ""));
					arrayList.add(hashMap);
				}
				
				if (!isAdded()) return;
				
				requireActivity().runOnUiThread(() -> {
					MyVerticalAdapter adapter = new MyVerticalAdapter(arrayList, getActivity(),binding.progressBar);
					binding.recyclerView.setAdapter(adapter);
					hideLoadingDialog();
				});
				
				} catch (JSONException e) {
				e.printStackTrace();
				showError("Invalid data format received from server.");
			}
		},
		exception -> {
			showError("Network error. Please check your connection.");
		});
	}
	
	//===============================================================
	// Error Handler
	//===============================================================
	private void showError(String message) {
		if (!isAdded()) return;
		
		requireActivity().runOnUiThread(() -> {
			hideLoadingDialog();
			new AlertDialog.Builder(requireActivity())
			.setTitle("Error")
			.setMessage(message)
			.setCancelable(false)
			.setPositiveButton("Retry", (dialog, which) -> {
				dialog.dismiss();
				showLoadingDialog();
				getServerResponse();
			})
			.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
			.show();
		});
	}
}