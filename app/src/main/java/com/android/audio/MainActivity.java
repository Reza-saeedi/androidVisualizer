package com.android.audio;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.visualizer.R;
import com.android.visualizer.util.PermissionUtil;
import com.android.audio.calculators.AudioCalculator;
import com.android.audio.core.Callback;
import com.android.audio.core.Recorder;

public class MainActivity extends AppCompatActivity {

    private Recorder recorder;
    private AudioCalculator audioCalculator;
    private Handler handler;

    private TextView textAlarm;
    private TextView textAmplitude;
    private TextView textDecibel;
    private TextView textFrequency;
    private Button mPermissionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        recorder = new Recorder(callback);
        audioCalculator = new AudioCalculator();
        handler = new Handler(Looper.getMainLooper());

        textAlarm = (TextView) findViewById(R.id.textAlarm);
        textAmplitude = (TextView) findViewById(R.id.textAmplitude);
        textDecibel = (TextView) findViewById(R.id.textDecibel);
        textFrequency = (TextView) findViewById(R.id.textFrequency);


        mPermissionButton = (Button) findViewById(R.id.button_permission);
        if (PermissionUtil.hasPermission(this, Manifest.permission.RECORD_AUDIO)) {
            mPermissionButton.setVisibility(View.GONE);
            recorder.start();
        } else {
            mPermissionButton.setVisibility(View.VISIBLE);
            mPermissionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PermissionUtil.requestPermission(
                            MainActivity.this, Manifest.permission.RECORD_AUDIO,
                            PermissionUtil.PERMISSION_RESULT_RECORD_AUDIO);
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionUtil.PERMISSION_RESULT_RECORD_AUDIO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPermissionButton.setVisibility(View.GONE);
                    recorder.start();
                }
                break;
        }
    }

    private Callback callback = new Callback() {

        @Override
        public void onBufferAvailable(byte[] buffer) {
            audioCalculator.setBytes(buffer);
            final int amplitude = audioCalculator.getAmplitude();
            double decibel = audioCalculator.getDecibel();
            double frequency = audioCalculator.getFrequency();

            final String amp = String.valueOf(amplitude + " Amp");
            final String db = String.valueOf(decibel + " db");
            final String hz = String.valueOf(frequency + " Hz");

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (amplitude > 3000)
                        textAlarm.setVisibility(View.VISIBLE);
                    else
                        textAlarm.setVisibility(View.GONE);

                    textAmplitude.setText(amp);
                    textDecibel.setText(db);
                    textFrequency.setText(hz);
                }
            });
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        recorder.stop();
    }
}
