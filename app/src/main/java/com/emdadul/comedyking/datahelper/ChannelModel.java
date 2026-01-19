package com.emdadul.comedyking.datahelper;
import java.util.ArrayList;



public class ChannelModel {
	
	private String title;
	private String channelId;
	private ArrayList<String> videoIds;
	private ArrayList<String> videoTitles;
	
	public ChannelModel(String title, String channelId) {
		this.title = title;
		this.channelId = channelId;
		this.videoIds = new ArrayList<>();
		this.videoTitles = new ArrayList<>();
	}
	
	// Getters
	public String getTitle() { 
		return title;
	}
	
	public String getChannelId() {
		return channelId;
	}
	
	
	public ArrayList<String> getVideoIds() {
		
		return videoIds;
	}
	public ArrayList<String> getVideoTitles() {
		
		return videoTitles;
	}
	
	// ভিডিও ডাটা সেট করার জন্য
	public void setVideoData(ArrayList<String> ids, ArrayList<String> titles) {
		this.videoIds = ids;
		this.videoTitles = titles;
	}
}