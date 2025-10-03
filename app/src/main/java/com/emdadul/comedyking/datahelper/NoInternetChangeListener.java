package com.emdadul.comedyking.datahelper;



import com.emdadul.comedyking.activity.MainActivity;
import android.content.BroadcastReceiver;
import android.view.animation.DecelerateInterpolator;
import android.view.WindowManager;
import android.view.Gravity;
import android.widget.Toast;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.LayoutInflater;
import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.content.Context;
import com.emdadul.comedyking.databinding.NoInternetDialogBoxBinding;


public class NoInternetChangeListener extends BroadcastReceiver {
	
	
	
	private AlertDialog dialog;
	Context context;
	public NoInternetChangeListener(Context context){
		this.context = context;
	}
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		//
		
		if (!Utils.isConnectedToInternet(context)) {
			
			// Show the no-internet dialog if not connected
			if (dialog == null || !dialog.isShowing()) {
				showNoInternetDialog(context);
			}
			
			} else {
			
			
			// Dismiss the dialog if connected
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
				Toast.makeText(context, "Internet is back", Toast.LENGTH_SHORT).show();
				reloadActivity(context);
			}
		}
		
	}
	
	
	
	public void showNoInternetDialog(Context context) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		NoInternetDialogBoxBinding binding = NoInternetDialogBoxBinding.inflate(LayoutInflater.from(context));
		builder.setView(binding.getRoot());
		builder.setCancelable(false);
		dialog = builder.create();
		
		
		
		if (dialog.getWindow() != null) {
			dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
			dialog.getWindow().setDimAmount(0.6f);
			dialog.getWindow().setGravity(Gravity.CENTER);
		}
		
		dialog.show();
		
		if (dialog.getWindow() != null) {
			dialog.getWindow().setLayout(
			WindowManager.LayoutParams.MATCH_PARENT,
			WindowManager.LayoutParams.WRAP_CONTENT
			);
		}
		
		// At first keep dialog root above screen (top)
		binding.getRoot().setTranslationY(-1000f); // start top side
		binding.getRoot().setAlpha(0f);
		
		// Animate smoothly to center
		binding.getRoot().animate()
		.translationY(0)   // move to center
		.alpha(1f)         // fade in
		.setDuration(600)  // speed
		.setInterpolator(new DecelerateInterpolator()) // smooth effect
		.start();
		
		
		
		binding.okButton.setOnClickListener(v -> {
			
			
			if (Utils.isConnectedToInternet(context)) {
				
				Toast.makeText(context, "Internet is back! Reloading...", Toast.LENGTH_SHORT).show();
				dialog.dismiss();
				reloadActivity(context);
				
				} else {
				Toast.makeText(context, "Still no internet found", Toast.LENGTH_SHORT).show();
			}
			
		});
		
		
		
	}
	
	
	
	
	public static void reloadActivity(Context context) {
		Intent intent = new Intent(context, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		context.startActivity(intent);
	}
	
	
}