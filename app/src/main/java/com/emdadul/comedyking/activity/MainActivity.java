package com.emdadul.comedyking.activity;

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
import com.emdadul.comedyking.fragment.HomeFragment;




public class MainActivity extends BaseActivity<ActivityMainBinding> {
	
	ActivityMainBinding binding;
	private long backPressedTime = 0;
	
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
		
		
		
		loadFragment(new HomeFragment());
		
		
	}//onCreate End Here
	
	
	
	
	public void loadFragment (Fragment fragment){
		
		//Load Fragment Method
	
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.add(R.id.fragment_container,fragment);
		ft.commit();
		
	}

        

    

	
	
	
	public void backPressed(){
		
		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				
				if (backPressedTime + 2000 > System.currentTimeMillis()) {
					finish();  // Exit app
				} else {
					
					Toast.makeText(MainActivity.this, "Press back again to exit", Toast.LENGTH_SHORT).show();
				}
				backPressedTime = System.currentTimeMillis();
			}
		});
	}

  
}

