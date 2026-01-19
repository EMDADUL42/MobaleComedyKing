package com.emdadul.comedyking.datahelper;



import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.emdadul.comedyking.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ComedyKing {
	
	private static final String CACHE_DIR = "json_cache";
	private static final String META_PREFS = "cache_meta";
	private static final String LAST_SYNC_KEY = "LAST_SYNC_TIME";
	
	private static final long DEFAULT_CACHE_DURATION = TimeUnit.DAYS.toMillis(1);
	
	
	private final OkHttpClient client;
	private final String baseUrl;
	private final String serverPassword;
	private final Handler mainHandler = new Handler(Looper.getMainLooper());
	private final LruCache<String, String> memoryCache;
	private final long cacheDuration;
	
	public ComedyKing(Context context) {
		this(context, DEFAULT_CACHE_DURATION);
	}
	
	public ComedyKing(Context context, long customCacheDuration) {
		client = new OkHttpClient.Builder()
		.connectTimeout(15, TimeUnit.SECONDS)
		.readTimeout(30, TimeUnit.SECONDS)
		.build();
		
		baseUrl = context.getString(R.string.baseUrl);
		serverPassword = "Md_Emdadul_huqe";
		cacheDuration = customCacheDuration;
		
		memoryCache = new LruCache<String, String>(20) {
			@Override
			protected int sizeOf(String key, String value) {
				return value.length();
			}
		};
		
		initCacheDir(context);
	}
	
	private void initCacheDir(Context context) {
		File cacheDir = new File(context.getFilesDir(), CACHE_DIR);
		if (!cacheDir.exists()) cacheDir.mkdirs();
	}
	
	private File getCacheFile(Context context, String cacheKey) {
		String filename = "cache_" + Math.abs(cacheKey.hashCode()) + ".json";
		return new File(context.getFilesDir() + File.separator + CACHE_DIR, filename);
	}
	
	public void saveToFileCacheAsync(final Context context, final String cacheKey, final String jsonData) {
		new Thread(() -> saveToFileCache(context, cacheKey, jsonData)).start();
	}
	
	public void saveToFileCache(Context context, String cacheKey, String jsonData) {
		File cacheFile = getCacheFile(context, cacheKey);
		try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
			fos.write(jsonData.getBytes(StandardCharsets.UTF_8));
		} catch (IOException ignored) {}
		memoryCache.put(cacheKey, jsonData);
		
		SharedPreferences prefs = context.getSharedPreferences(META_PREFS, Context.MODE_PRIVATE);
		prefs.edit().putLong(cacheKey, System.currentTimeMillis()).apply();
	}
	
	public String loadFromFileCache(Context context, String cacheKey) {
		String memCached = memoryCache.get(cacheKey);
		if (memCached != null) return memCached;
		
		File cacheFile = getCacheFile(context, cacheKey);
		if (!cacheFile.exists()) return null;
		
		SharedPreferences prefs = context.getSharedPreferences(META_PREFS, Context.MODE_PRIVATE);
		long lastSaved = prefs.getLong(cacheKey, 0);
		
		if (System.currentTimeMillis() - lastSaved > cacheDuration) {
			cacheFile.delete();
			memoryCache.remove(cacheKey);
			prefs.edit().remove(cacheKey).apply();
			return null;
		}
		
		try (FileInputStream fis = new FileInputStream(cacheFile)) {
			byte[] data = new byte[(int) cacheFile.length()];
			int read = fis.read(data);
			if (read > 0) {
				String json = new String(data, StandardCharsets.UTF_8);
				memoryCache.put(cacheKey, json);
				return json;
			}
		} catch (IOException ignored) {}
		
		return null;
	}
	
	// -------------------- Force Clear Cache --------------------
	public void clearAllCache(Context context) {
		// File cache
		File dir = new File(context.getFilesDir(), CACHE_DIR);
		if (dir.exists() && dir.isDirectory()) {
			for (File f : dir.listFiles()) f.delete();
		}
		
		// Memory cache
		memoryCache.evictAll();
		
		// SharedPreferences
		SharedPreferences prefs = context.getSharedPreferences(META_PREFS, Context.MODE_PRIVATE);
		prefs.edit().clear().apply();
	}
	
	public boolean isCacheExpired(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(META_PREFS, Context.MODE_PRIVATE);
		long lastSync = prefs.getLong(LAST_SYNC_KEY, 0);
		return System.currentTimeMillis() - lastSync > cacheDuration;
	}
	
	public void markCacheSyncTime(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(META_PREFS, Context.MODE_PRIVATE);
		prefs.edit().putLong(LAST_SYNC_KEY, System.currentTimeMillis()).apply();
	}
	
	// -------------------- Pagination Fetch --------------------
	public void fetchPage(@NonNull final Context context, final int page, final int limit, @NonNull final PaginationResponseHandler success,@Nullable final ErrorHandler failure) {
		final String cacheKey = baseUrl + "?page=" + page + "&limit=" + limit;
		
		String cached = loadFromFileCache(context, cacheKey);
		if (cached != null) {
			mainHandler.post(() -> success.onSuccess(cached, countItems(cached)));
			return;
		}
		
		MediaType JSON = MediaType.get("application/json; charset=utf-8");
		String payload = "{\"password\":\"" + serverPassword + "\",\"page\":" + page + ",\"limit\":" + limit + "}";
		RequestBody body = RequestBody.create(payload, JSON);
		Request request = new Request.Builder().url(baseUrl).post(body).build();
		
		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(@NonNull Call call, @NonNull IOException e) {
				String cached = loadFromFileCache(context, cacheKey);
				if (cached != null) {
					mainHandler.post(() -> success.onSuccess(cached, countItems(cached)));
					return;
				}
				if (failure != null) mainHandler.post(() -> failure.onError(e));
			}
			
			@Override
			public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
				ResponseBody body = response.body();
				if (response.isSuccessful() && body != null) {
					final String json = body.string();
					saveToFileCacheAsync(context, cacheKey, json);
					mainHandler.post(() -> success.onSuccess(json, countItems(json)));
					} else {
					String cached = loadFromFileCache(context, cacheKey);
					if (cached != null) {
						mainHandler.post(() -> success.onSuccess(cached, countItems(cached)));
						return;
					}
					if (failure != null) mainHandler.post(() -> failure.onError(new IOException("Server error: " + response.code())));
				}
			}
		});
	}
	
	private int countItems(String json) {
		try {
			org.json.JSONObject obj = new org.json.JSONObject(json);
			return obj.getJSONArray("data").length();
			} catch (Exception e) {
			return 0;
		}
	}
	
	// -------------------- Interfaces --------------------
	public interface PaginationResponseHandler {
		void onSuccess(String json, int itemCount);
	}
	
	public interface ErrorHandler {
		void onError(@Nullable IOException exception);
	}
}