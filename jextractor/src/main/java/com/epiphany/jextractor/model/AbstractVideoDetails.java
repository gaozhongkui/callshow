package com.epiphany.jextractor.model;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.epiphany.jextractor.YoutubeException;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractVideoDetails {

    private String videoId;
    private int lengthSeconds;
    private List<String> thumbnails;

    // Subclass specific extraction
    protected String title;
    protected String author;
    protected boolean isLive;

    protected abstract void checkDownload() throws YoutubeException.DownloadUnavailableException;

    public AbstractVideoDetails() {
    }

    public AbstractVideoDetails(JSONObject json) {
        videoId = json.getString("videoId");
        lengthSeconds = json.getIntValue("lengthSeconds");
        JSONArray jsonThumbnails = json.getJSONObject("thumbnail").getJSONArray("thumbnails");
        thumbnails = new ArrayList<>(jsonThumbnails.size());
        for (int i = 0; i < jsonThumbnails.size(); i++) {
            JSONObject jsonObject = jsonThumbnails.getJSONObject(i);
            if (jsonObject.containsKey("url"))
                thumbnails.add(jsonObject.getString("url"));
        }
    }

    public String videoId() {
        return videoId;
    }

    public String title() {
        return title;
    }

    public int lengthSeconds() {
        return lengthSeconds;
    }

    public List<String> thumbnails() {
        return thumbnails;
    }

    public String author() {
        return author;
    }

    public boolean isLive() {
        return isLive;
    }
}
