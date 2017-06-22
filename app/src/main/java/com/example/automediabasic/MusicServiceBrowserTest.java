package com.example.automediabasic;

import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.browse.MediaBrowser;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;

import android.os.SystemClock;
import android.service.media.MediaBrowserService;

import android.util.Log;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicServiceBrowserTest extends MediaBrowserService {
    private MediaSession mSession;
    private List<MediaMetadata> mMusic;
    private MediaPlayer mPlayer;
    private MediaMetadata mCurrentTrack;

    private final String TAG = MusicServiceBrowserTest.this.getClass().getSimpleName();
    private final String URL = "http://storage.googleapis.com/automotive-media/music.json";
    private VolleySingleton volleySingleton;
    private RequestQueue requestQueue;

    private Gson gson;
    private Music music;

    @Override
    public void onCreate() {
        super.onCreate();
        volleySingleton = VolleySingleton.getInstance(this.getApplicationContext());
        requestQueue = volleySingleton.getRequestQueue();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
        mMusic = new ArrayList<MediaMetadata>();
        mPlayer = new MediaPlayer();
        mSession = new MediaSession(this, "MiServicioMusical");
        getMusicalRepertoire();


        mSession = new MediaSession(this, "MiServicioMusical");
        mSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mSession.setActive(true);

    }

    private PlaybackState buildState(int state) {
        return new PlaybackState.Builder().setActions(PlaybackState.ACTION_PLAY |
                PlaybackState.ACTION_SKIP_TO_PREVIOUS |
                PlaybackState.ACTION_SKIP_TO_NEXT |
                PlaybackState.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackState.ACTION_PLAY_PAUSE)
                .setState(state, mPlayer.getCurrentPosition(), 1, SystemClock.elapsedRealtime())
                .build();
    }

    private void handlePlay() {
        mPlayer.seekTo(0);
        mSession.setPlaybackState(buildState(PlaybackState.STATE_PLAYING));
        mSession.setMetadata(mCurrentTrack);
        try {
            mPlayer.reset();
            mPlayer.seekTo(0);
            mPlayer.setDataSource(MusicServiceBrowserTest.this, Uri.parse(mCurrentTrack.getDescription().getMediaId()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.seekTo(0);
                mSession.setPlaybackState(buildState(PlaybackState.STATE_STOPPED));
            }
        });

        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mSession.setPlaybackState(buildState(PlaybackState.STATE_PLAYING));
                mediaPlayer.start();
            }
        });
        mPlayer.prepareAsync();
    }

    @Override
    public MediaBrowserService.BrowserRoot onGetRoot(String clientPackageName, int clientUid, Bundle rootHints) {
        return new MediaBrowserService.BrowserRoot("ROOT", null);
    }

    @Override
    public void onLoadChildren(String s, Result<List<MediaBrowser.MediaItem>> result) {
        List<MediaBrowser.MediaItem> list = new ArrayList<MediaBrowser.MediaItem>();
        for (MediaMetadata m : mMusic) {
            list.add(new MediaBrowser.MediaItem(m.getDescription(), MediaBrowser.MediaItem.FLAG_PLAYABLE));
        }
        result.sendResult(list);
    }

    @Override
    public void onDestroy() {
        mSession.release();
    }


    private void getMusicalRepertoire() {
        StringRequest request = new StringRequest(Request.Method.GET, URL, onPostsLoaded, onPostsError);
        requestQueue.add(request);
        Log.e(TAG, "request = " + requestQueue);
    }

    private final Response.Listener<String> onPostsLoaded = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            music = gson.fromJson(response, Music.class);
            Log.d(TAG, "Número de pistas de audio: " + music.getMusic().size());
            int slashPos = URL.lastIndexOf('/');
            String path = URL.substring(0, slashPos + 1);
            for (int i = 0; i < music.getMusic().size(); i++) {
                AudioTrack track = music.getMusic().get(i);
                Log.e(TAG, "" + track.getTitle());

                if (!track.getSource().startsWith("http"))
                    track.setSource(path + track.getSource());
                if (!track.getImage().startsWith("http")) track.setImage(path + track.getImage());

                music.getMusic().set(i, track);
                Log.e(TAG, "" + track.getTitle());
                Log.e(TAG, "" + track.getSource());
                Log.e(TAG, "" + track.getArtist());
                Log.e(TAG, "" + track.getDuration());
                mMusic.add(new MediaMetadata.Builder()
                        .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, track.getSource())
                        .putString(MediaMetadata.METADATA_KEY_TITLE, track.getTitle())
                        .putString(MediaMetadata.METADATA_KEY_ARTIST, track.getArtist())
                        .putLong(MediaMetadata.METADATA_KEY_DURATION, track.getDuration() * 1000).build());

            }

            Log.e(TAG, "onResponse:Ya tengo la musica " + mMusic.size());
            InitializeMediaSession();
        }
    };
    private final Response.ErrorListener onPostsError = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, error.toString());
        }
    };


    private void InitializeMediaSession() {
        mSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlayFromMediaId(String mediaId, Bundle extras) {
                for (MediaMetadata item : mMusic) {
                    if (item.getDescription().getMediaId().equals(mediaId)) {
                        mCurrentTrack = item;
                        break;
                    }
                }
                handlePlay();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                int currentIndex = mMusic.indexOf(mCurrentTrack);
                Log.e("onSkipToNext()", "click" + currentIndex);
                if (currentIndex < mMusic.size() - 1) {
                    currentIndex++;
                } else currentIndex = 0;
                mCurrentTrack = mMusic.get(currentIndex);
                mSession.setPlaybackState(buildState(PlaybackState.STATE_SKIPPING_TO_NEXT));
                handlePlay();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                Log.e("onSkipToPrevious()", "click");
                int currentIndex = mMusic.indexOf(mCurrentTrack);
                Log.e("onSkipToNext()", "click" + currentIndex);
                if (currentIndex == 0) {
                    currentIndex = mMusic.size() - 1;
                } else currentIndex--;
                mCurrentTrack = mMusic.get(currentIndex);
                mSession.setPlaybackState(buildState(PlaybackState.STATE_SKIPPING_TO_NEXT));
                handlePlay();

            }

            @Override
            public void onPlay() {
                if (mCurrentTrack == null) {
                    mCurrentTrack = mMusic.get(0);
                    handlePlay();
                } else {
                    mPlayer.start();
                    mSession.setPlaybackState(buildState(PlaybackState.STATE_PLAYING));
                }
            }

            @Override
            public void onPause() {
                mPlayer.pause();
                mSession.setPlaybackState(buildState(PlaybackState.STATE_PAUSED));
            }
        });

        setSessionToken(mSession.getSessionToken());
    }

}


