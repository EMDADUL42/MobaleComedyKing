package com.emdadul.comedyking.datahelper;

import java.util.ArrayList;

public class ChannelModel {
  private String title;
  private String channelId;

  private ArrayList<String> displayIds = new ArrayList<>();
  private ArrayList<String> displayTitles = new ArrayList<>();
  private ArrayList<String> masterIds = new ArrayList<>();
  private ArrayList<String> masterTitles = new ArrayList<>();

  public ChannelModel(String title, String channelId) {
    this.title = title;
    this.channelId = channelId;
  }

  public String getTitle() {
    return title;
  }

  public String getChannelId() {
    return channelId;
  }

  // নতুন মেথডগুলো যা আপনার ViewModel ব্যবহার করছে
  public ArrayList<String> getDisplayIds() {
    return displayIds;
  }

  public ArrayList<String> getDisplayTitles() {
    return displayTitles;
  }

  public ArrayList<String> getMasterIds() {
    return masterIds;
  }

  public ArrayList<String> getMasterTitles() {
    return masterTitles;
  }
}
