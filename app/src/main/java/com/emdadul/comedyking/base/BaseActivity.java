package com.emdadul.comedyking.base;

import android.net.ConnectivityManager;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewbinding.ViewBinding;
import com.emdadul.comedyking.datahelper.NoInternetChangeListener;



public abstract class BaseActivity<T extends ViewBinding> extends AppCompatActivity {

    private final ViewBindingFactory<T> getViewBinding;
    protected T binding;
	NoInternetChangeListener noInternetChangeListener;
		
	boolean isReceiverRegistered = false;
   
   
   

    public BaseActivity(ViewBindingFactory<T> getViewBinding) {
        this.getViewBinding = getViewBinding;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
		
		
		EdgeToEdge.enable(this);
		super.onCreate(savedInstanceState);
		
		binding = getViewBinding.create(getLayoutInflater());
		View root = binding.getRoot();
		setContentView(root);
		ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
			Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
			v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
			return insets;
			
			
			
		});
		
		noInternetChangeListener = new NoInternetChangeListener(this);
		
		
		
	}//onCreate
	
	
	@Override
	protected void onStart() {
		super.onStart();
		
		//
		
		if (!isReceiverRegistered) {
			IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
			registerReceiver(noInternetChangeListener, intentFilter);
			isReceiverRegistered = true;
		}
	}
	
	
	
	
	@Override
	protected void onStop() {
		super.onStop();
		
		//
		
		if (isReceiverRegistered) {
			try {
				unregisterReceiver(noInternetChangeListener);
				
			} catch (IllegalArgumentException e) {
				
				e.printStackTrace();
			}
			
			isReceiverRegistered = false;
		}
	}
        


    public interface ViewBindingFactory<T extends ViewBinding> {
        T create(LayoutInflater inflater);
    }


}
