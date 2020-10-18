package com.artembark.cooltimer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private TextView textView;
    private SeekBar seekBar;
    private Button startButton;
    private Button muteButton;
    private CountDownTimer countDownTimer;
    private boolean isTimerOn;
    private MediaPlayer mediaPlayer;
    private int defaultInterval;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        isTimerOn =false;
        textView = findViewById(R.id.textView);
        startButton = findViewById(R.id.button);
        muteButton = findViewById(R.id.muteButton);
        muteButton.setVisibility(View.GONE);
        seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(600);
        setIntervalFromSharedPreferences(sharedPreferences);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                 long progressInMillis= progress*1000;
                 updateTimer(progressInMillis);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        muteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer!=null){
                    mediaPlayer.stop();
                    muteButton.setVisibility(View.GONE);
                }
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isTimerOn){
                    startButton.setText("STOP");
                    seekBar.setEnabled(false);
                    countDownTimer = new CountDownTimer(seekBar.getProgress()*1000,1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            updateTimer(millisUntilFinished);
                        }

                        @Override
                        public void onFinish() {

                            if (sharedPreferences.getBoolean("enable_sound", true)){
                                muteButton.setVisibility(View.VISIBLE);
                                String melodyName = sharedPreferences.getString("timer_melody","bell");
                                if (melodyName.equals("bell")){
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.bell);
                                    mediaPlayer.start();
                                } else if (melodyName.equals("alarm_siren")){
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.alarm);
                                    mediaPlayer.start();
                                } else if (melodyName.equals("bip")){
                                    mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.bip);
                                    mediaPlayer.start();
                                }
                                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mp) {
                                        muteButton.setVisibility(View.GONE);
                                    }
                                });
                            }
                            resetTimer();
                        }
                    }.start();
                    isTimerOn=true;
                } else {
                    resetTimer();
                }
            }
        });

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

    }

    private  void resetTimer(){
        startButton.setText("START");
        seekBar.setEnabled(true);
        countDownTimer.cancel();
        isTimerOn=false;
        setIntervalFromSharedPreferences(sharedPreferences);
    }

private void updateTimer(long millisUntilFinished){
    int minutes = (int)millisUntilFinished/1000/60;
    int seconds = (int)millisUntilFinished/1000-(minutes*60);

    String minutesString="";
    String secondsString="";

    if (minutes<10){
        minutesString="0"+minutes;
    }else{
        minutesString=""+minutes;
    }

    if (seconds<10){
        secondsString="0"+seconds;
    }else{
        secondsString=""+seconds;
    }
    textView.setText(minutesString+":"+secondsString);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.timer_menu,menu);
        //замена верхним двум строчкам getMenuInflater().inflate(R.menu.timer_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id==R.id.action_settings){
            Intent openSettings = new Intent (this,SettingsActivity.class);
            startActivity(openSettings);
            return true;
        }else if (id==R.id.action_about){
            Intent openAbout = new Intent (this,AboutActivity.class);
            startActivity(openAbout);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setIntervalFromSharedPreferences(SharedPreferences sharedPreferences){
        defaultInterval = Integer.valueOf(sharedPreferences.getString("default_interval","30"));
        long defaultIntervalInMillis = defaultInterval*1000;
        updateTimer(defaultIntervalInMillis);
        seekBar.setProgress(defaultInterval);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("default_interval")){
            setIntervalFromSharedPreferences(sharedPreferences);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}
