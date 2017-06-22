package com.example.automediabasic;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by usuwi on 20/06/2017.
 */

public class Music {
    @SerializedName("music")
    @Expose
    private List<AudioTrack> music = null;

    public List<AudioTrack> getMusic() {
        return music;
    }

    public void setMusic(List<AudioTrack> music) {
        this.music = music;
    }
}
