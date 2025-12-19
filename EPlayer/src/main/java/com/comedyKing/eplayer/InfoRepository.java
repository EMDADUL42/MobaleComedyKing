package com.eplayer;


import android.content.Context;
import androidx.annotation.NonNull;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException; // ✅ add this line
import java.nio.charset.StandardCharsets;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
/**
* Simple InfoRepository with file-based caching for EPlayer
* Uses YouTube oEmbed (no API key needed)
*/
public class InfoRepository {
	private final OkHttpClient client;
	private final Context context;
	
	public InfoRepository(@NonNull Context context) {
		this.context = context;
		this.client = new OkHttpClient();
	}
	
	public void getInfo(@NonNull String videoId, @NonNull ResponseListener responseListener) {
		JSONObject cached = loadFromFile(videoId);
		if (cached != null) {
			responseListener.onResponse(cached);
			return;
		}
		
		String url = "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v="
		+ videoId + "&format=json";
		
		Request request = new Request.Builder().url(url).build();
		
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e) {
				responseListener.onFailure(e);
			}
			
			@Override
			public void onResponse(@NonNull Call call, @NonNull Response response) {
				try (ResponseBody responseBody = response.body()) {
					if (responseBody == null) {
						responseListener.onFailure(new IOException("Empty response body"));
						return;
					}
					
					String jsonString = responseBody.string();
					JSONObject jsonObject = new JSONObject(jsonString);
					
					saveToFile(videoId, jsonString);
					responseListener.onResponse(jsonObject);
					} catch (Exception e) {
					responseListener.onFailure(e);
				}
			}
		});
	}
	
	private void saveToFile(String videoId, String jsonString) {
		try {
			File file = new File(context.getCacheDir(), videoId + ".json");
			try (FileOutputStream fos = new FileOutputStream(file)) {
				fos.write(jsonString.getBytes(StandardCharsets.UTF_8));
			}
			} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private JSONObject loadFromFile(String videoId) {
		try {
			File file = new File(context.getCacheDir(), videoId + ".json");
			if (!file.exists()) return null;
			
			StringBuilder builder = new StringBuilder();
			try (java.io.BufferedReader reader = new java.io.BufferedReader(
			new java.io.InputStreamReader(
			new java.io.FileInputStream(file), StandardCharsets.UTF_8))) {
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			}
			return new JSONObject(builder.toString());
			} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// Inner interface (or move to separate file)
	public static interface ResponseListener {
		void onResponse(JSONObject response);
		void onFailure(Exception e);
	}
}