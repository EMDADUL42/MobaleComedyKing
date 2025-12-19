package com.comedyKing.eplayer;


import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import java.util.ArrayList;



public class EPlayer extends FrameLayout {
	
	private WebView webView;
	private String currentVideoId = "";
	private boolean isInitialized = false;
	private boolean hasUserInteracted = false;
	private boolean autoPlayOnReady = false;
	private OnPlayerListener listener;
	
	private ArrayList<String> playlist = new ArrayList<>();
	private String playlistId = "";
	private int currentIndex = 0;
	
	private View customView;
	private WebChromeClient.CustomViewCallback customViewCallback;
	private int originalOrientation;
	
	private String pendingVideoId = "";
	
	private static final String BASE_HTML =
	"<!DOCTYPE html><html><head>" +
	"<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">" +
	"<style>*{margin:0;padding:0}html,body,#d{height:100%;overflow:hidden;background:#000}iframe{position:absolute;width:100%;height:100%;border:0}</style>" +
	"<script src=\"https://www.youtube.com/iframe_api\"></script>" +
	"</head><body><div id=\"d\"><div id=\"player\"></div></div>" +
	"<script>" +
	"var player,currentVideoId='',currentList=[];" +
	"function onYouTubeIframeAPIReady(){" +
	"player=new YT.Player('player',{" +
	"playerVars:{autoplay:0,controls:1,modestbranding:1,rel:0,iv_load_policy:3,fs:1,playsinline:1}," +
	"events:{'onReady':onPlayerReady,'onStateChange':onPlayerStateChange}});" +
	"}" +
	"function onPlayerReady(e){Android.onReady();}" +
	"function onPlayerStateChange(e){" +
	"if(e.data==0)Android.onVideoEnded();" +
	"try{var vids=player.getPlaylist();if(vids && vids.length>0){Android.onPlaylistFetched(JSON.stringify(vids));}}catch(err){}" +
	"}" +
	"function loadPlaylistById(id,index){if(player)player.loadPlaylist({list:id,listType:'playlist',index:index||0});}" +
	"function loadPlaylistArray(ids,index){if(player)player.loadPlaylist(ids,index||0);}" +
	"</script></body></html>";
	
	public EPlayer(Context context) {
		super(context);
		init(context);
	}
	
	public EPlayer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context) {
		if (isInitialized) return;
		
		webView = new WebView(context.getApplicationContext());
		WebSettings s = webView.getSettings();
		s.setJavaScriptEnabled(true);
		s.setDomStorageEnabled(true);
		s.setMediaPlaybackRequiresUserGesture(false);
		s.setCacheMode(WebSettings.LOAD_NO_CACHE);
		s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		
		webView.addJavascriptInterface(new JSInterface(), "Android");
		
		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onShowCustomView(View view, CustomViewCallback callback) {
				if (customView != null) {
					callback.onCustomViewHidden();
					return;
				}
				customView = view;
				customViewCallback = callback;
				Activity a = (Activity) getContext();
				originalOrientation = a.getRequestedOrientation();
				FrameLayout decor = (FrameLayout) a.getWindow().getDecorView();
				decor.addView(customView, new FrameLayout.LayoutParams(-1, -1));
				hideSystemUI();
				a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				webView.setVisibility(View.GONE);
			}
			
			@Override
			public void onHideCustomView() {
				if (customView == null) return;
				FrameLayout decor = (FrameLayout) ((Activity) getContext()).getWindow().getDecorView();
				decor.removeView(customView);
				customView = null;
				if (customViewCallback != null) {
					customViewCallback.onCustomViewHidden();
					customViewCallback = null;
				}
				webView.setVisibility(View.VISIBLE);
				showSystemUI();
				((Activity) getContext()).setRequestedOrientation(originalOrientation);
			}
		});
		
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView v, String url) {
				isInitialized = true;
				if (listener != null) listener.onReady();
				
				if (!pendingVideoId.isEmpty()) {
					String js = "if(player){player.loadVideoById('" + pendingVideoId + "');}";
					runJS(js);
					pendingVideoId = "";
					return;
				}
				
				if (autoPlayOnReady) {
					if (!playlistId.isEmpty()) loadPlaylistById(playlistId, 0);
					else if (playlist.size() > 0) playVideoAtIndex(0);
				}
			}
		});
		
		webView.setBackgroundColor(Color.BLACK);
		addView(webView, new LayoutParams(-1, -1));
		
		webView.setOnTouchListener((v, e) -> {
			if (e.getAction() == MotionEvent.ACTION_DOWN) hasUserInteracted = true;
			return false;
		});
		
		webView.loadDataWithBaseURL("https://www.youtube-nocookie.com", BASE_HTML, "text/html", "utf-8", null);
	}
	
	public void enableAutoPlayOnReady() {
		this.autoPlayOnReady = true;
		this.hasUserInteracted = true;
	}
	
	public void setPlaylistById(String playlistId) {
		this.playlistId = playlistId;
		this.playlist.clear();
		this.currentIndex = 0;
		if (isInitialized && !playlistId.isEmpty()) loadPlaylistById(playlistId, 0);
	}
	
	
	
	private void playVideoAtIndex(int index) {
		if (index < 0 || index >= playlist.size()) return;
		currentIndex = index;
		currentVideoId = playlist.get(index);
		String js = "if(player){player.cueVideoById({'videoId':'" + currentVideoId + "'});setTimeout(()=>player.playVideo(),300);}";
		runJS(js);
	}
	
	
	private void loadPlaylistById(String id, int index) {
		String js = "if(player)loadPlaylistById('" + id + "'," + index + ");";
		runJS(js);
	}
	
	public void play() {
		runJS("if(player)player.playVideo();");
	}
	
	public void pause() {
		runJS("if(player)player.pauseVideo();");
	}
	
	private void runJS(String js) {
		if (isInitialized && webView != null) {
			webView.post(() -> webView.evaluateJavascript(js, null));
		}
	}
	
	public void setListener(OnPlayerListener l) {
		this.listener = l;
	}
	
	public class JSInterface {
		@JavascriptInterface
		public void onReady() {
			post(() -> {
				if (listener != null) listener.onReady();
			});
		}
		
		@JavascriptInterface
		public void onVideoEnded() {
			post(() -> {
				if (listener != null) listener.onPlaylistEnded();
			});
		}
		
		@JavascriptInterface
		public void onVideoIdChanged(String id) {
			currentVideoId = id;
			post(() -> {
				if (listener != null) listener.onVideoIdChanged(id);
			});
		}
		
		@JavascriptInterface
		public void onPlaylistFetched(String json) {
			try {
				ArrayList<String> ids = new ArrayList<>();
				json = json.replace("[", "").replace("]", "").replace("\"", "");
				for (String s : json.split(",")) {
					if (!s.trim().isEmpty()) ids.add(s.trim());
				}
				if (listener != null) listener.onPlaylistFetched(ids);
				} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	// ✅ Play single video by ID (even when playlist exists)
	public void loadByVideoId(String videoId) {
		this.currentVideoId = videoId;
		this.playlist.clear();
		this.playlistId = "";
		this.currentIndex = 0;
		
		if (!isInitialized) {
			pendingVideoId = videoId; // যদি player এখনো ready না হয়
			} else {
			String js = "if(player){player.loadVideoById('" + videoId + "');}";
			runJS(js);
		}
	}
	
	
	
	public interface OnPlayerListener {
		public void onReady();
		public void onError(String error);
		public void onPlaylistEnded();
		public void onVideoIdChanged(String videoId);
		public void onPlaylistFetched(ArrayList<String> ids);
	}
	
	private void hideSystemUI() {
		Activity a = (Activity) getContext();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			WindowInsetsControllerCompat c = WindowCompat.getInsetsController(a.getWindow(), a.getWindow().getDecorView());
			c.hide(androidx.core.view.WindowInsetsCompat.Type.statusBars() |
			androidx.core.view.WindowInsetsCompat.Type.navigationBars());
			c.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
			} else {
			a.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
			View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
	}
	
	private void showSystemUI() {
		Activity a = (Activity) getContext();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			WindowInsetsControllerCompat c = WindowCompat.getInsetsController(a.getWindow(), a.getWindow().getDecorView());
			c.show(androidx.core.view.WindowInsetsCompat.Type.statusBars() |
			androidx.core.view.WindowInsetsCompat.Type.navigationBars());
			} else {
			a.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
		}
	}
}