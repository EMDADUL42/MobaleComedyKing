package com.emdadul.comedyking.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.emdadul.comedyking.R;
import com.emdadul.comedyking.base.BaseActivity;
import com.emdadul.comedyking.databinding.ActivitySplashBinding;

public class ActivitySplash extends BaseActivity<ActivitySplashBinding>{
	
	public ActivitySplash(){
		
		super(ActivitySplashBinding::inflate);
	}
	
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		Animation imageAnimation = AnimationUtils.loadAnimation(ActivitySplash.this,R.anim.top_anim);
		Animation textAnimation = AnimationUtils.loadAnimation(ActivitySplash.this,R.anim.bottom_anim);
		
		binding.layoutSplash.startAnimation(imageAnimation);
		binding.splashAppName.startAnimation(textAnimation);
		
		
		
		goNext();
		
	}
	
	
	
	public void goNext(){
		
		new Handler().postDelayed(new Runnable(){
			
			@Override
			public void run() {
				
				startActivity(new Intent(ActivitySplash.this,MainActivity.class));
				finish();
			}
			
		},3000);
	}
	
}