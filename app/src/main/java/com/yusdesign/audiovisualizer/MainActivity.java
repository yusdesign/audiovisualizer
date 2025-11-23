package com.yusdesign.audiovisualizer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "AudioVisualizer";
    private static final int AUDIO_PERMISSION_CODE = 1001;

    private MediaPlayer mediaPlayer;
    private Visualizer visualizer;
    private VisualizerView visualizerView;
    private Button startButton;
    private Button stopButton;

    private boolean isVisualizing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupClickListeners();

        if (hasAudioPermission()) {
            initializeAudioResources();
        } else {
            requestAudioPermission();
        }
    }

    private void initializeViews() {
        visualizerView = findViewById(R.id.visualizerView);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);

        updateButtonStates();
    }

    private void setupClickListeners() {
        startButton.setOnClickListener(v -> startVisualization());
        stopButton.setOnClickListener(v -> stopVisualization());
    }

    private boolean hasAudioPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                AUDIO_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Audio permission granted", Toast.LENGTH_SHORT).show();
                initializeAudioResources();
            } else {
                Toast.makeText(this, "Audio permission is required for visualization",
                             Toast.LENGTH_LONG).show();
                startButton.setEnabled(false);
            }
        }
    }

    private void initializeAudioResources() {
        // Resources will be initialized when starting visualization
        Log.d(TAG, "Audio resources ready for initialization");
    }

    private void startVisualization() {
        if (isVisualizing) {
            return;
        }

        try {
            setupMediaPlayer();
            setupVisualizer();
            mediaPlayer.start();
            isVisualizing = true;
            updateButtonStates();
            Toast.makeText(this, "Visualization started", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error starting visualization: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to start visualization", Toast.LENGTH_SHORT).show();
            cleanupResources();
        }
    }

    private void stopVisualization() {
        if (!isVisualizing) {
            return;
        }

        cleanupResources();
        isVisualizing = false;
        updateButtonStates();
        visualizerView.clearVisualization();
        Toast.makeText(this, "Visualization stopped", Toast.LENGTH_SHORT).show();
    }

    private void setupMediaPlayer() {
        try {
            // Try to use a sample audio file from raw resources
            int audioResource = getResources().getIdentifier("sample_audio", "raw", getPackageName());
            if (audioResource != 0) {
                mediaPlayer = MediaPlayer.create(this, audioResource);
            } else {
                // Fallback: create a silent media player for microphone input
                mediaPlayer = new MediaPlayer();
                // You would set up microphone input here
                Toast.makeText(this, "Using microphone input", Toast.LENGTH_SHORT).show();
            }

            if (mediaPlayer == null) {
                throw new IllegalStateException("MediaPlayer creation failed");
            }

            mediaPlayer.setLooping(true);

        } catch (Exception e) {
            Log.e(TAG, "MediaPlayer setup failed: " + e.getMessage(), e);
            throw new RuntimeException("MediaPlayer initialization failed", e);
        }
    }

    private void setupVisualizer() {
        if (mediaPlayer == null) {
            throw new IllegalStateException("MediaPlayer must be initialized first");
        }

        int audioSessionId = mediaPlayer.getAudioSessionId();
        if (audioSessionId == 0) {
            throw new IllegalStateException("Invalid audio session ID");
        }

        try {
            visualizer = new Visualizer(audioSessionId);
            int captureSize = Visualizer.getCaptureSizeRange()[1]; // Use max capture size

            if (!visualizer.setCaptureSize(captureSize)) {
                Log.w(TAG, "Failed to set desired capture size, using default");
            }

            visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                    visualizerView.updateVisualizer(waveform);
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                    // Optional: Implement FFT visualization
                    // visualizerView.updateFftData(fft);
                }
            }, Visualizer.getMaxCaptureRate() / 2, true, false);

            if (!visualizer.setEnabled(true)) {
                throw new IllegalStateException("Failed to enable visualizer");
            }

            Log.d(TAG, "Visualizer setup successful");

        } catch (Exception e) {
            Log.e(TAG, "Visualizer setup failed: " + e.getMessage(), e);
            cleanupResources();
            throw new RuntimeException("Visualizer initialization failed", e);
        }
    }

    private void cleanupResources() {
        if (visualizer != null) {
            try {
                visualizer.setEnabled(false);
                visualizer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing visualizer: " + e.getMessage(), e);
            }
            visualizer = null;
        }

        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing media player: " + e.getMessage(), e);
            }
            mediaPlayer = null;
        }
    }

    private void updateButtonStates() {
        startButton.setEnabled(!isVisualizing);
        stopButton.setEnabled(isVisualizing);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isVisualizing) {
            stopVisualization();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanupResources();
    }
}

