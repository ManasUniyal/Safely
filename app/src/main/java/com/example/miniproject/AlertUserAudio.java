package com.example.miniproject;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class AlertUserAudio {

    private static MediaPlayer mediaPlayer;

    public static void startWarning(Context context) {
        //TODO: Select appropriate audio
        mediaPlayer = MediaPlayer.create(context.getApplicationContext(), R.raw.alertuser);
        mediaPlayer.start();
    }

    public static void endWarning() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        Log.e("Media Player", "Stopped");
    }
}
