package com.sp.milestrack;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;

public class splashScreen extends Activity {
    private final int SPLASH_DISPLAY_LENGTH = 1500; // in ms
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splashscreen);
        final MediaPlayer mp = MediaPlayer.create(this, R.raw.splashscreen_sound);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0);
        mp.setVolume(0.5f, 0.5f); // 0.0f mute, 1.0f max
        mp.start();
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(splashScreen.this, MainActivity.class);
                splashScreen.this.startActivity(mainIntent);
                splashScreen.this.finish();
                mp.release();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
