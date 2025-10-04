package com.emdadul.comedyking.activity;

import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.emdadul.comedyking.R;
import com.emdadul.comedyking.adapter.GirdRecyclerAdapter;
import com.emdadul.comedyking.base.BaseActivity;
import com.emdadul.comedyking.databinding.FragmentGridBinding;
import java.util.ArrayList;

/*
This App Made By Md Emdadul Huqe
mdemdadulhuqe01@gmail.com
01928077542
*/



public class GridActivity extends BaseActivity<FragmentGridBinding> {
	
	public GridActivity() {
		super(FragmentGridBinding::inflate);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ArrayList<String> videoTitles = getIntent().getStringArrayListExtra("videoTitle");
		ArrayList<String> videoIds = getIntent().getStringArrayListExtra("videoIds");
		
		GirdRecyclerAdapter adapter = new GirdRecyclerAdapter(GridActivity.this, videoIds, videoTitles);
		StaggeredGridLayoutManager staggeredGridLayoutManager =
		new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
		
		binding.recyclerView.setLayoutManager(staggeredGridLayoutManager);
		binding.recyclerView.setAdapter(adapter);
		
		
		if (videoTitles != null && videoTitles.size() > 1) {
			Toast.makeText(this, "Your " + videoTitles.get(1), Toast.LENGTH_SHORT).show();
		}
	}
}