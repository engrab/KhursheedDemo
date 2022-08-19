package com.data.cloner.phone;

import android.net.Uri;

public class VideoData {
    public String duration;
    public Uri videouri;
    public long videoId;
    public String videoName;
    public String videoPath;

    public VideoData(String videoName, Uri videouri, String videoPath, String duration) {
        this.videoName = videoName;
        this.videouri = videouri;
        this.videoPath = videoPath;
        this.duration = duration;
    }

}
