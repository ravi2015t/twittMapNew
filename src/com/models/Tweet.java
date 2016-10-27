package com.models;

import io.searchbox.annotations.JestId;

public class Tweet {
	@JestId
	private String tweetId;
	
	private String tweet;
	private double latitude;
	private double longitude;
	
	public Tweet() {}
	public Tweet(String tweetId, String tweetText, double lat, double longi) {
		this.tweetId = tweetId;
		this.tweet = tweetText;
		this.latitude = lat;
		this.longitude = longi;
	}
	
	public String getTweetId() {
		return this.tweetId;
	}
	
	public void setTweetId(String textId) {
		this.tweetId = textId;
	}
	
	public String getTweet() {
		return this.tweet;
	}
	
	public void setTweet(String text) {
		this.tweet = text;
	}
	
	public double getLatitude() {
		return this.latitude;
	}
	
	public void setLatitude(double lat) {
		this.latitude = lat;
	}
	
	public double getLongitude() {
		return this.longitude;
	}
	
	public void setLongitude(double longi) {
		this.longitude = longi;
	}
}
