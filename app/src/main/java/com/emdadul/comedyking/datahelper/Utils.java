package com.emdadul.comedyking.datahelper;


import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.animation.DecelerateInterpolator;
import android.view.WindowManager;
import android.net.Uri;
import android.view.View;
import com.emdadul.comedyking.databinding.DataLoadCustomDialogBinding;
import androidx.core.content.ContextCompat;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Utils {
	
	public static Dialog dialog;
	
	
	public static boolean isConnectedToInternet(Context context) {
		ConnectivityManager connectivityManager = ContextCompat.getSystemService(context, ConnectivityManager.class);
		if (connectivityManager != null) {
			Network network = connectivityManager.getActiveNetwork();
			if (network != null) {
				NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
				return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
			}
		}
		
		return false;
	}
	
	
	
	
	
	public static String getKey(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256"); // এখানে MD5-এর জায়গায় SHA-256
			byte[] hashBytes = md.digest(input.getBytes());
			StringBuilder hexString = new StringBuilder();
			for (byte b : hashBytes) {
				hexString.append(String.format("%02x", b));
			}
			return hexString.toString();
			} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 algorithm not found", e);
		}
	}
	
	
	
	
	
	
	
	public static void browseInternet(Context context, String link) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(link));
		context.startActivity(intent);
	}
	
	
	public static void performSmoothZoomAnimation(View view) {
		// Scale up the button with smooth animation
		
		view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(50).withEndAction(new Runnable() {
			@Override
			public void run() {
				
				view.animate().scaleX(1f).scaleY(1f).setDuration(50).start();
			}
		}).start();
		
		
		
	}
	
	
	
	
	
	
	public static void checkAndDataShowDialog(Context context) {
		
		String PREF_NAME = "MyPrefs";
		String LAST_SHOWN = "last_shown_time";
		long ONE_DAY = 24 * 60 * 60 * 1000;
		
		
		
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		long lastShown = prefs.getLong(LAST_SHOWN, 0);
		long currentTime = System.currentTimeMillis();
		
		if (currentTime - lastShown >= ONE_DAY) {
			setDataLoadDialog(context);
			prefs.edit().putLong(LAST_SHOWN, currentTime).apply();
		}
	}
	
	
	public static void setDataLoadDialog(Context context) {
		dialog = new Dialog(context);
		
		DataLoadCustomDialogBinding binding = DataLoadCustomDialogBinding.inflate(LayoutInflater.from(context));
		dialog.setContentView(binding.getRoot());
		dialog.setCancelable(false);
		if (dialog.getWindow() != null) {
			dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
			dialog.getWindow().setDimAmount(0.6f); // background dim
			dialog.getWindow().setGravity(Gravity.CENTER); // middle position
			dialog.getWindow().setLayout(
			WindowManager.LayoutParams.MATCH_PARENT,
			WindowManager.LayoutParams.WRAP_CONTENT
			);
		}
		
		// initial state: top off-screen
		binding.getRoot().setTranslationY(-2000f);
		binding.getRoot().setAlpha(0f);
		
		dialog.show();
		
		// enter animation: top -> middle
		binding.getRoot().animate()
		.translationY(0)
		.alpha(1f)
		.setDuration(1000)
		.setInterpolator(new DecelerateInterpolator())
		.start();
		
		// exit animation: middle -> top
		binding.dialogOk.setOnClickListener(v -> {
			binding.getRoot().animate()
			.translationY(-2000f)
			.alpha(0f)
			.setDuration(1000)
			.withEndAction(dialog::dismiss)
			.start();
		});
	}
	
	
	
	
	public static void dismissDialog() {
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
			dialog = null;
		}
	}
	
	
	
	
	
}