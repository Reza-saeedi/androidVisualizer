package com.android.visualizer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.visualizer.util.PermissionUtil;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class VisualizerActivity extends AppCompatActivity {

    private VisualizerView mVisualizerView;
    private Button mPermissionButton;

    private AudioRecord mAudioRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizer);
        mVisualizerView = (VisualizerView) findViewById(R.id.view_visualizer);
        mPermissionButton = (Button) findViewById(R.id.button_permission);
        if (PermissionUtil.hasPermission(this, Manifest.permission.RECORD_AUDIO)) {
            mPermissionButton.setVisibility(View.GONE);
            initAudio();
        } else {
            mPermissionButton.setVisibility(View.VISIBLE);
            mPermissionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PermissionUtil.requestPermission(
                            VisualizerActivity.this, Manifest.permission.RECORD_AUDIO,
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
                    initAudio();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void initAudio() {
        startRecording();
    }


    private static final String TAG = "SoundRecorder";
    private static final int RECORDING_RATE = 8000; // can go up to 44K, if needed
    private static final int CHANNEL_IN = AudioFormat.CHANNEL_IN_MONO;
    private static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static int BUFFER_SIZE = AudioRecord
            .getMinBufferSize(RECORDING_RATE, CHANNEL_IN, FORMAT);


    private State mState = State.IDLE;

    private AsyncTask<Void, Void, Void> mRecordingAsyncTask;

    enum State {
        IDLE, RECORDING
    }


    @SuppressLint("StaticFieldLeak")
    public void startRecording() {


        mRecordingAsyncTask = new AsyncTask<Void, Void, Void>() {



            @Override
            protected void onPreExecute() {
                mState = State.RECORDING;
            }

            @Override
            protected Void doInBackground(Void... params) {
                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        RECORDING_RATE, CHANNEL_IN, FORMAT, BUFFER_SIZE*4 );
                BufferedOutputStream bufferedOutputStream = null;
                try {
                   final byte[] buffer = new byte[BUFFER_SIZE*4];
                    mAudioRecord.startRecording();
                    while (!isCancelled()) {
                        int read = mAudioRecord.read(buffer, 0, buffer.length);
                        //bufferedOutputStream.write(buffer, 0, read);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                mVisualizerView.updateVisualizer(buffer);
                            }
                        });
                    }
                } catch ( NullPointerException | IndexOutOfBoundsException e) {
                    Log.e(TAG, "Failed to record data: " + e);
                } finally {
                    if (bufferedOutputStream != null) {
                        try {
                            bufferedOutputStream.close();
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                    mAudioRecord.release();
                    mAudioRecord = null;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mState = State.IDLE;
                mRecordingAsyncTask = null;
            }

            @Override
            protected void onCancelled() {
                if (mState == State.RECORDING) {
                    Log.d(TAG, "Stopping the recording ...");
                    mState = State.IDLE;
                } else {
                    Log.w(TAG, "Requesting to stop recording while state was not RECORDING");
                }
                mRecordingAsyncTask = null;
            }
        };

        mRecordingAsyncTask.execute();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mRecordingAsyncTask!=null)
        mRecordingAsyncTask.cancel(true);
        if(mAudioRecord!=null)
        mAudioRecord.release();
    }
}
