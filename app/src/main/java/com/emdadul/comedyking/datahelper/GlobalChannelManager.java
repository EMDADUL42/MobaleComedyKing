package com.emdadul.comedyking.datahelper;



import android.util.Log;
import com.emdadul.comedyking.datahelper.ChannelModel;
import java.util.LinkedList;
import java.util.Queue;

public class GlobalChannelManager {
	private static final String TAG = "GlobalChannelMgr";
	private static GlobalChannelManager instance;
	
	// কিউ যেখানে অপেক্ষমাণ চ্যানেলগুলো থাকবে
	private final Queue<ChannelModel> pendingQueue = new LinkedList<>();
	
	// বর্তমানে কোনো চ্যানেল লোড হচ্ছে কিনা তার ফ্ল্যাগ
	private boolean isCurrentlyProcessing = false;
	
	private GlobalChannelManager() {}
	
	public static synchronized GlobalChannelManager getInstance() {
		if (instance == null) {
			instance = new GlobalChannelManager();
		}
		return instance;
	}
	
	// চ্যানেল কিউ-তে যোগ করার মেথড
	public synchronized void addToQueue(ChannelModel model, TaskCallback callback) {
		if (model == null) return;
		
		// ডুপ্লিকেট চেক
		if (isAlreadyQueued(model.getChannelId())) {
			Log.d(TAG, "Already in queue: " + model.getChannelId());
			return;
		}
		
		pendingQueue.add(model);
		Log.d(TAG, "Added to queue: " + model.getChannelId() + " | Size: " + pendingQueue.size());
		
		// যদি কেউ লোড না করছে থাকে, তবেই শুরু করো
		checkAndProcessNext(callback);
	}
	
	// পরবর্তী চ্যানেল প্রসেস করা
	private synchronized void checkAndProcessNext(TaskCallback callback) {
		if (isCurrentlyProcessing || pendingQueue.isEmpty()) {
			return;
		}
		
		isCurrentlyProcessing = true;
		ChannelModel nextChannel = pendingQueue.poll();
		
		if (nextChannel != null) {
			Log.d(TAG, ">>> STARTING FETCH: " + nextChannel.getChannelId());
			if (callback != null) {
				callback.onTaskReady(nextChannel);
			}
			} else {
			isCurrentlyProcessing = false;
		}
	}
	
	// যখন একটি চ্যানেলের লোডিং শেষ হবে, এই মেথড কল করতে হবে
	public synchronized void notifyTaskComplete() {
		Log.d(TAG, "<<< TASK COMPLETE");
		isCurrentlyProcessing = false;
		// নোট: যেহেতু আমরা এখানে callback রাখিনি, তাই পরের টাস্ক শুরু করতে হলে
		// ViewHolder থেকে নতুন করে trigger দিতে হবে না যদি লজিক সঠিক থাকে।
		// কিন্তু সেফ সাইডে, আমরা একটি ট্রিকার ব্যবহার করবো যা ViewHolder দেখবে।
	}
	
	// এটি কল করা হবে যখন একটি কাজ শেষ হয় এবং আমরা পরের জিনিস চাই
	public synchronized void triggerNext() {
		// আমরা এখানে callback পাচ্ছি না, তাই আমরা নিচের ভ্যারিয়েবল আপডেট করবো
		// এবং ViewHolder তা চেক করবে। (অথবা আমরা সরাসরি checkAndProcessNext কল করতে পারতাম যদি callback পাশ করতাম)
		
		// সহজ সমাধান: আমরা একটি স্ট্যাটিক ইন্টারফেস ব্যবহার করবো না, বরং সিস্টেমটি এমনভাবে সাজাবো
		// যাতে পরের রিকোয়েস্ট অটোমেটিক জেনারেট হয়।
		// কিন্তু নিরাপদ পথ হলো triggerNext কল করলে isCurrentlyProcessing ফালস হবে।
		// যে ক্লাস এটি কল করছে সেই ক্লাসের ভেতরে আবার addToQueue চেক করা লাগবে।
		
		isCurrentlyProcessing = false;
	}
	
	public boolean isBusy() {
		return isCurrentlyProcessing || !pendingQueue.isEmpty();
	}
	
	private boolean isAlreadyQueued(String channelId) {
		for (ChannelModel m : pendingQueue) {
			if (m.getChannelId().equals(channelId)) return true;
		}
		return false;
	}
	
	public interface TaskCallback {
		void onTaskReady(ChannelModel model);
	}
}