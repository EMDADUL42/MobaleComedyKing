package com.emdadul.comedyking.activity;

import androidx.core.view.GravityCompat;
import static androidx.core.view.GravityCompat.START;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;
import androidx.activity.OnBackPressedCallback;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import android.widget.Toast;
import android.view.View;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.Fragment;
import com.emdadul.comedyking.R;
import com.emdadul.comedyking.base.BaseActivity;
import com.emdadul.comedyking.databinding.ActivityMainBinding;
import com.emdadul.comedyking.databinding.CustomDrawerNavigationBinding;
import com.emdadul.comedyking.fragment.HomeFragment;
import com.google.android.material.navigation.NavigationView;




/*
This App Made By Md Emdadul Huqe
mdemdadulhuqe01@gmail.com
01928077542
*/



public class MainActivity extends BaseActivity<ActivityMainBinding> {
	
	//ActivityMainBinding binding;
	private long backPressedTime = 0;
	NavigationView navigationView;
	CustomDrawerNavigationBinding navigationBinding;
	
	public MainActivity() {
		super(ActivityMainBinding::inflate);
	}
	
	
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		EdgeToEdge.enable(this);
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
		});
		
		
		
		
		binding.menuIcon.setOnClickListener(v->{
			
			if (binding.main.isDrawerOpen(GravityCompat.END)){
				
				closeDrawer();
			}else {
				
				binding.main.openDrawer(GravityCompat.END);
			}
			
			
			
		});
		
		
		
		
		
		
		loadFragment(new HomeFragment());
		
		
		
		navigationClickListener();
		
		
		
	
	
	
	
		backPressed();
		
		
	}//onCreate End Here
	
	
	
	
	public void loadFragment (Fragment fragment){
		
		//Load Fragment Method
	
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.add(R.id.fragment_container,fragment);
		ft.commit();
		
	}
	
	
	
	
	public void navigationClickListener(){
		
		
		binding.customNavigationView.policy.setOnClickListener(v->{
			
			closeDrawer();
			
			Toast.makeText(MainActivity.this, "Policy", Toast.LENGTH_SHORT).show();
		});
		
		
		binding.customNavigationView.disclaimar.setOnClickListener(v->{
			
			closeDrawer();
			
			Toast.makeText(MainActivity.this, "Disclaimar", Toast.LENGTH_SHORT).show();
		});
		
		
		binding.customNavigationView.aboutUs.setOnClickListener(v->{
			
			closeDrawer();
			
			Toast.makeText(MainActivity.this, "AboutUs", Toast.LENGTH_SHORT).show();
		});
		
	
	}

        

    

	
	
	
	public void backPressed(){
		
		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				
				closeDrawer();
				
				if (backPressedTime + 2000 > System.currentTimeMillis()) {
					finish();  // Exit app
				} else {
					
					Toast.makeText(MainActivity.this, "Press back again to exit", Toast.LENGTH_SHORT).show();
				}
				backPressedTime = System.currentTimeMillis();
			}
		});
	}
	
	
	private void closeDrawer() {
        binding.main.closeDrawer(GravityCompat.END);
    }

  
  
  
  
  
  
  
}

