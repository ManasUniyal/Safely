package com.example.miniproject.Utilities;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.example.miniproject.R;

public class AlertUserAudio {

    private static AlertUserAudio instance = null;
    private static MediaPlayer mediaPlayer = null;
    private Context mContext;

    public static AlertUserAudio getInstance(Context context) {
        if(instance == null) {
            instance = new AlertUserAudio(context.getApplicationContext());
        }
        return instance;
    }

    private AlertUserAudio(Context context) {
        this.mContext = context;
    }

    public void startWarning() {
        //TODO: Select appropriate audio
        if (mediaPlayer == null || !mediaPlayer.isPlaying()) {
            mediaPlayer = MediaPlayer.create(mContext, R.raw.alertuser);
            mediaPlayer.start();
        }
    }

    //TODO: Handle bug, app crashes sometimes due to endWarning() method
    public void endWarning() {
        if (mediaPlayer != null) {
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        Log.e("Media Player", "Stopped");
    }

}
