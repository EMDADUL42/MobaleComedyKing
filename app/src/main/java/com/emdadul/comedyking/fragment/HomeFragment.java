package com.emdadul.comedyking.fragment;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.animation.DecelerateInterpolator;
import android.view.WindowManager;
import android.view.Gravity;
import android.os.Bundle;
import android.widget.EdgeEffect;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.emdadul.EPlayer;
import com.emdadul.comedyking.adapter.MyVerticalAdapter;
import com.emdadul.comedyking.R;
import com.emdadul.comedyking.databinding.DataLoadCustomDialogBinding;
import com.emdadul.comedyking.databinding.FragmentHomeBinding;
import com.emdadul.comedyking.datahelper.ComedyKing;
import com.emdadul.comedyking.datahelper.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


/*
This App Made By Md Emdadul Huqe
mdemdadulhuqe01@gmail.com
01928077542
*/




public class HomeFragment extends Fragment {
	
	EPlayer ePlayer;
	
	
	FragmentHomeBinding binding;
	ComedyKing comedyKing;
	ArrayList<HashMap<String, String>> arrayList;
	HashMap<String, String> hashMap;
	
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		
		binding = FragmentHomeBinding.inflate(inflater, container, false);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		
		
		
		
		
		
		comedyKing = new ComedyKing(requireActivity());
		getServerResponse();
		
		
		
		

		
		
		return binding.getRoot();
		
		
	}//onCreate End Here============================================
	
	
	
	
	//===============================================================
	//Server Response
	//===============================================================
	
	
	public void getServerResponse() {
		
		if (getActivity() != null) {
			
			
			comedyKing.getResponse(getActivity(), "getTitleAndIds.php", new ComedyKing.ResponseHandler() {
				@Override
				public void onSuccess(String response) {
					
					// নতুন ArrayList বানাও
					arrayList = new ArrayList<>();
					
					JSONArray jsonArray = null;
					try {
						
						jsonArray = new JSONArray(response);
						
						for (int i = 0; i < jsonArray.length(); i++) {
							
							
							JSONObject obj = jsonArray.getJSONObject(i);
							hashMap = new HashMap<>();
							hashMap.put("title", obj.getString("title"));
							hashMap.put("channelId", obj.getString("channelId"));
							arrayList.add(hashMap);
							
							
						}
						
						
						// RecyclerView অবশ্যই UI Thread এ update করতে হবে
						requireActivity().runOnUiThread(() -> {
							MyVerticalAdapter adapter = new MyVerticalAdapter(arrayList, getActivity());
							binding.recyclerView.setAdapter(adapter);
						});
						
						
						
						
						
						
						
						
					} catch (JSONException e) {
						
						throw new RuntimeException(e);
						
					}
					
				}
				
				
				}, new ComedyKing.ErrorHandler() {
				@Override
				public void onError(@Nullable IOException exception) {
					
					
					
					if (getActivity() == null) return;
					
					requireActivity().runOnUiThread(() -> {
						new AlertDialog.Builder(getActivity())
						.setTitle("Error")
						.setMessage("mes")
						.setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
						.show();
					});
					
					
					
				}
			});
		}
		
	}
	
	
	
	
	//===============================================================
	//Server Response
	//===============================================================
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}