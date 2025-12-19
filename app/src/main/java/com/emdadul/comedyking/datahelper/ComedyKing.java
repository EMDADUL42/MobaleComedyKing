package com.emdadul.comedyking.datahelper;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.emdadul.comedyking.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ComedyKing {
	
	private final OkHttpClient client;
	private final String baseUrl;
	
	private static final String PREF_NAME = "comedyking_pref";
	private static final String KEY_VIDEO_IDS = "video_ids";
	private static final String KEY_LAST_FETCH = "last_fetch";
	
	private static final long EXPIRY_DURATION = 24 * 60 * 60 * 1000; // 24 Hrs
	
	public ComedyKing(Context context) {
		client = new OkHttpClient.Builder()
		.connectTimeout(15, TimeUnit.SECONDS)
		.readTimeout(20, TimeUnit.SECONDS)
		.build();
		
		baseUrl = context.getString(R.string.baseUrl);
	}
	
	
	// -------------------------------------------------------------------------
	// FILE READ / WRITE HELPERS
	// -------------------------------------------------------------------------
	private void writeToFile(Context context, String fileName, String data) {
		try {
			FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
			fos.write(data.getBytes());
			fos.close();
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	private String readFromFile(Context context, String fileName) {
		try {
			FileInputStream fis = context.openFileInput(fileName);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			
			byte[] buffer = new byte[2048];
			int read;
			
			while ((read = fis.read(buffer)) != -1) {
				bos.write(buffer, 0, read);
			}
			
			fis.close();
			return bos.toString();
			
			} catch (Exception e) {
			return null;
		}
	}
	
	
	// -------------------------------------------------------------------------
	// GET RESPONSE (Cached + API)
	// -------------------------------------------------------------------------
	public void getResponse(@NonNull Context context,
	@NonNull String url,
	@NonNull ResponseHandler success,
	@Nullable ErrorHandler failure) {
		
		String fullUrl = baseUrl + url;
		String key = Utils.getKey(fullUrl);
		
		String cacheFile = key + ".json";     // file name
		String timeKey = key + "_time";       // timestamp key
		
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		
		long lastSaved = prefs.getLong(timeKey, 0);
		long now = System.currentTimeMillis();
		
		// ---------------- CHECK FILE CACHE -------------------
		if ((now - lastSaved) < EXPIRY_DURATION) {
			String cached = readFromFile(context, cacheFile);
			if (cached != null) {
				success.onSuccess(cached);
				return;
			}
		}
		
		// ---------------- CALL SERVER ------------------------
		RequestBody formBody = new FormBody.Builder()
		.add("password", "Md_Emdadul_huqe")
		.build();
		
		Request request = new Request.Builder()
		.url(fullUrl)
		.post(formBody)
		.build();
		
		client.newCall(request).enqueue(new Callback() {
			
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e) {
				if (failure != null) failure.onError(e);
			}
			
			@Override
			public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
				if (response.isSuccessful()) {
					
					String body = response.body().string();
					
					// Save JSON to FILE
					writeToFile(context, cacheFile, body);
					
					// Save timestamp
					prefs.edit()
					.putLong(timeKey, System.currentTimeMillis())
					.apply();
					
					success.onSuccess(body);
					
					} else {
					if (failure != null) failure.onError(null);
				}
			}
		});
	}
	
	
	// -------------------------------------------------------------------------
	// CALLBACK INTERFACES
	// -------------------------------------------------------------------------
	public interface ResponseHandler {
		void onSuccess(String response);
	}
	
	public interface ErrorHandler {
		void onError(@Nullable IOException exception);
	}
	
	
	
	// -------------------------------------------------------------------------
	// VIDEO ID SAVE / LOAD USING FILE (NOT SharedPreference)
	// -------------------------------------------------------------------------
	public static void saveVideoIds(Context context, String channelId, ArrayList<String> videoIds) {
		
		String fileName = "video_ids_" + channelId + ".json";
		
		Gson gson = new Gson();
		String json = gson.toJson(videoIds);
		
		// Save JSON to file
		try {
			FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
			fos.write(json.getBytes());
			fos.close();
		} catch (Exception ignored) {}
		
		// Save timestamp only in SP (very small)
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit()
		.putLong(KEY_LAST_FETCH + "_" + channelId, System.currentTimeMillis())
		.apply();
	}
	
	
	public static ArrayList<String> getVideoIds(Context context, String channelId) {
		
		String fileName = "video_ids_" + channelId + ".json";
		
		try {
			FileInputStream fis = context.openFileInput(fileName);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			
			byte[] buffer = new byte[2048];
			int read;
			
			while ((read = fis.read(buffer)) != -1) {
				bos.write(buffer, 0, read);
			}
			
			fis.close();
			String json = bos.toString();
			
			Gson gson = new Gson();
			Type type = new TypeToken<ArrayList<String>>() {}.getType();
			
			return gson.fromJson(json, type);
			
			} catch (Exception e) {
			return new ArrayList<>();
		}
	}
	
	
	public static boolean shouldFetchFromServer(Context context, String channelId) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		long lastFetch = prefs.getLong(KEY_LAST_FETCH + "_" + channelId, 0);
		long now = System.currentTimeMillis();
		
		return (now - lastFetch) > EXPIRY_DURATION;
	}
}