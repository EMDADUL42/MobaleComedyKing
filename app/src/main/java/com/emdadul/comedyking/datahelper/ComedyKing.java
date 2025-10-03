
package com.emdadul.comedyking.datahelper;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.emdadul.comedyking.R;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.io.IOException;
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
	
	// 24 hours in millis
	//private static final long EXPIRY_DURATION = 24 * 60 * 60 * 1000;
	private static final long EXPIRY_DURATION=30000;
	
	public ComedyKing(Context context) {
		client = new OkHttpClient.Builder()
		.connectTimeout(15, TimeUnit.SECONDS)
		.readTimeout(20, TimeUnit.SECONDS)
		.build();
		
		baseUrl = context.getString(R.string.baseUrl);
	}
	
	public void getResponse(@NonNull Context context,
	@NonNull String url,
	@NonNull ResponseHandler success,
	@Nullable ErrorHandler failure) {
		
		String fullUrl = baseUrl + url;
		String key = Utils.getKey(fullUrl);
		
		SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
		long lastSavedTime = prefs.getLong(key + "time", 0);
		long currentTime = System.currentTimeMillis();
		
		// Check cache validity (24 hours)
		if (prefs.contains(key) && (currentTime - lastSavedTime) < EXPIRY_DURATION) {
			String cachedResponse = prefs.getString(key, null);
			if (cachedResponse != null) {
				success.onSuccess(cachedResponse);
				return;
			}
		}
		
		// Build POST request body
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
				if (failure != null) {
					failure.onError(e);
				}
			}
			
			@Override
			public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
				if (response.isSuccessful()) {
					String body = response.body().string();
					
					prefs.edit()
					.putString(key, body)
					.putLong(key + "time", System.currentTimeMillis())
					.apply();
					
					success.onSuccess(body);
					} else {
					if (failure != null) {
						failure.onError(null);
					}
				}
			}
		});
	}
	
	// Callback interface for success
	public interface ResponseHandler {
		void onSuccess(String response);
	}
	
	// Callback interface for failure
	public interface ErrorHandler {
		void onError(@Nullable IOException exception);
	}
	
	public static void saveVideoIds(Context context, String channelId, ArrayList<String> videoIds) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		Gson gson = new Gson();
		String json = gson.toJson(videoIds);
		
		editor.putString(KEY_VIDEO_IDS + "_" + channelId, json);
		editor.putLong(KEY_LAST_FETCH + "_" + channelId, System.currentTimeMillis());
		editor.apply();
	}
	
	public static ArrayList<String> getVideoIds(Context context, String channelId) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		String json = prefs.getString(KEY_VIDEO_IDS + "_" + channelId, null);
		
		if (json == null) return new ArrayList<>();
		
		Gson gson = new Gson();
		Type type = new TypeToken<ArrayList<String>>() {}.getType();
		return gson.fromJson(json, type);
	}
	
	public static boolean shouldFetchFromServer(Context context, String channelId) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		long lastFetch = prefs.getLong(KEY_LAST_FETCH + "_" + channelId, 0);
		long now = System.currentTimeMillis();
		
		// Fetch again if more than 24 hours passed
		return (now - lastFetch) > EXPIRY_DURATION;
	}
}


/*
package com.emdadul.comedyking.datahelper;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.emdadul.comedyking.R;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.io.IOException;
import java.util.Calendar;
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
	
	
	
	public ComedyKing(Context context) {
		client = new OkHttpClient.Builder()
		.connectTimeout(15, TimeUnit.SECONDS)
		.readTimeout(20, TimeUnit.SECONDS)
		.build();
		
		baseUrl = context.getString(R.string.baseUrl);
	}
	
	public void getResponse(@NonNull Context context,
	@NonNull String url,
	@NonNull ResponseHandler success,
	@Nullable ErrorHandler failure) {
		
		String fullUrl = baseUrl + url;
		String key = Utils.getKey(fullUrl);
		
		SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
		long lastSavedTime = prefs.getLong(key + "time", 0);
		long currentTime = System.currentTimeMillis();
		
		// Check cache validity (24 hours)
		if (prefs.contains(key) && (currentTime - lastSavedTime) < 86400000) {
			String cachedResponse = prefs.getString(key, null);
			if (cachedResponse != null) {
				success.onSuccess(cachedResponse);
				return;
			}
		}
		
		// Build POST request body
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
				if (failure != null) {
					failure.onError(e);
				}
			}
			
			@Override
			public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
				if (response.isSuccessful()) {
					String body = response.body().string();
					
					prefs.edit()
					.putString(key, body)
					.putLong(key + "time", System.currentTimeMillis())
					.apply();
					
					success.onSuccess(body);
					} else {
					if (failure != null) {
						failure.onError(null);
					}
				}
			}
		});
	}
	
	// Callback interface for success
	public interface ResponseHandler {
		void onSuccess(String response);
	}
	
	// Callback interface for failure
	public interface ErrorHandler {
		void onError(@Nullable IOException exception);
	}
	
	
	
	
	
	
	public static void saveVideoIds(Context context, String channelId, ArrayList<String> videoIds) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		Gson gson = new Gson();
		String json = gson.toJson(videoIds);
		
		// save with channelId key
		editor.putString(KEY_VIDEO_IDS + "_" + channelId, json);
		editor.putLong(KEY_LAST_FETCH + "_" + channelId, System.currentTimeMillis());
		editor.apply();
	}
	
	public static ArrayList<String> getVideoIds(Context context, String channelId) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		String json = prefs.getString(KEY_VIDEO_IDS + "_" + channelId, null);
		
		if (json == null) return new ArrayList<>();
		
		Gson gson = new Gson();
		Type type = new TypeToken<ArrayList<String>>() {}.getType();
		return gson.fromJson(json, type);
	}
	
	public static boolean shouldFetchFromServer(Context context, String channelId) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		long lastFetch = prefs.getLong(KEY_LAST_FETCH + "_" + channelId, 0);
		
		Calendar lastCal = Calendar.getInstance();
		lastCal.setTimeInMillis(lastFetch);
		
		Calendar now = Calendar.getInstance();
		
		// check if it's a new day for this channel
		return now.get(Calendar.YEAR) != lastCal.get(Calendar.YEAR) ||
		now.get(Calendar.DAY_OF_YEAR) != lastCal.get(Calendar.DAY_OF_YEAR);
	}
	
	
}
*/
