package com.emdadul.comedyking.datahelper;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import com.emdadul.comedyking.databinding.DataLoadCustomDialogBinding;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {
	
	public static Dialog dialog;
	private final String versionUrl = "https://eh.jummania.com/ComedyKing/AppUpdate/appupdate.json";
	
	// ✅ Internet connection check
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
	
	// ✅ Hash generator (SHA-256)
	public static String getKey(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
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
	
	// ✅ Open URL in browser
	public static void browseInternet(Context context, String link) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(link));
		context.startActivity(intent);
	}
	
	// ✅ Smooth Zoom Animation
	public static void performSmoothZoomAnimation(View view) {
		view.animate()
		.scaleX(0.95f).scaleY(0.95f)
		.setDuration(50)
		.withEndAction(() ->
		view.animate().scaleX(1f).scaleY(1f).setDuration(50).start())
		.start();
	}
	
	
	
	// ✅ Force Update Checker
	public void checkForUpdate(Context context) {
		if (!isConnectedToInternet(context)) {
			return; // Internet না থাকলে কিছুই করবে না
		}
		
		new Thread(() -> {
			HttpURLConnection conn = null;
			BufferedReader reader = null;
			
			try {
				URL url = new URL(versionUrl);
				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setConnectTimeout(7000);
				conn.setReadTimeout(7000);
				
				reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuilder response = new StringBuilder();
				String line;
				
				while ((line = reader.readLine()) != null) {
					response.append(line);
				}
				
				JSONObject json = new JSONObject(response.toString());
				int latestVersion = json.getInt("latest_version");
				boolean forceUpdate = json.getBoolean("force_update");
				String message = json.optString("message", "A new update is available!");
				String updateUrl = json.getString("update_url");
				
				int currentVersion = context.getPackageManager()
				.getPackageInfo(context.getPackageName(), 0).versionCode;
				
				if (latestVersion > currentVersion) {
					new Handler(Looper.getMainLooper()).post(() ->
					showUpdateDialog(context, message, updateUrl, forceUpdate));
				}
				
				} catch (Exception e) {
				e.printStackTrace();
				} finally {
				try {
					if (reader != null) reader.close();
					if (conn != null) conn.disconnect();
				} catch (Exception ignored) {}
			}
		}).start();
	}
	
	// ✅ Update Dialog
	private void showUpdateDialog(Context context, String message, String updateUrl, boolean forceUpdate) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Update Available");
		builder.setMessage(message);
		builder.setCancelable(!forceUpdate);
		
		builder.setPositiveButton("Update", (dialog, which) -> {
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
				context.startActivity(intent);
				} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (forceUpdate && context instanceof Activity) {
				((Activity) context).finishAffinity(); // সব Activity close
			}
		});
		
		if (!forceUpdate) {
			builder.setNegativeButton("Later", (dialog, which) -> dialog.dismiss());
		}
		
		builder.show();
	}
	
	// ✅ Once-a-day Dialog (Data load info)
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
	
	// ✅ Animated Custom Dialog
	public static void setDataLoadDialog(Context context) {
		dialog = new Dialog(context);
		
		DataLoadCustomDialogBinding binding = DataLoadCustomDialogBinding.inflate(LayoutInflater.from(context));
		dialog.setContentView(binding.getRoot());
		dialog.setCancelable(false);
		
		if (dialog.getWindow() != null) {
			dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
			dialog.getWindow().setDimAmount(0.6f);
			dialog.getWindow().setGravity(Gravity.CENTER);
			dialog.getWindow().setLayout(
			WindowManager.LayoutParams.MATCH_PARENT,
			WindowManager.LayoutParams.WRAP_CONTENT
			);
		}
		
		// Initial off-screen
		binding.getRoot().setTranslationY(-2000f);
		binding.getRoot().setAlpha(0f);
		
		dialog.show();
		
		// Entry animation
		binding.getRoot().animate()
		.translationY(0)
		.alpha(1f)
		.setDuration(1000)
		.setInterpolator(new DecelerateInterpolator())
		.start();
		
		// Exit animation
		binding.dialogOk.setOnClickListener(v -> {
			binding.getRoot().animate()
			.translationY(-2000f)
			.alpha(0f)
			.setDuration(1000)
			.withEndAction(dialog::dismiss)
			.start();
		});
	}
	
	// ✅ Dialog Dismiss
	public static void dismissDialog() {
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
			dialog = null;
		}
	}
}