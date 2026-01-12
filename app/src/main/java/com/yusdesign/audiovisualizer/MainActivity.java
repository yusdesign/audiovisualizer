package com.yusdesign.audiovisualizer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1;
    
    private VisualizerView mVisualizerView;
    private MediaPlayer mMediaPlayer;
    private Button mToggleButton;
    private Button mChangeColorButton;
    private boolean mIsPlaying = false;
    private int[] mColors = {
            0xFF3884FF, 0xFFFF5722, 0xFF4CAF50, 
            0xFFFFC107, 0xFF9C27B0, 0xFF00BCD4
    };
    private int mColorIndex = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize views
        mVisualizerView = findViewById(R.id.visualizerView);
        mToggleButton = findViewById(R.id.toggleButton);
        mChangeColorButton = findViewById(R.id.changeColorButton);
        
        // Check and request permissions
        checkPermissions();
        
        // Setup button click listeners
        mToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAudio();
            }
        });
        
        mChangeColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeColor();
            }
        });
        
        // Initialize media player
        initializeMediaPlayer();
    }
    
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, 
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }
    
    private void initializeMediaPlayer() {
        try {
            // Create a simple tone or load a sample audio
            // You can replace this with your own audio file in res/raw folder
            // For now, we'll use a generated tone
            mMediaPlayer = MediaPlayer.create(this, R.raw.sample_audio);
            
            if (mMediaPlayer == null) {
                // If no audio file exists, generate a tone
                generateTone();
            }
            
            mMediaPlayer.setLooping(true);
            mVisualizerView.setMediaPlayer(mMediaPlayer);
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing audio", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void generateTone() {
        // This is a fallback method to generate audio if no file exists
        try {
            mMediaPlayer = new MediaPlayer();
            // You would normally set a data source here
            // For now, we'll leave it as is - you can add your own audio file
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void toggleAudio() {
        if (mMediaPlayer == null) {
            return;
        }
        
        if (mIsPlaying) {
            mMediaPlayer.pause();
            mToggleButton.setText("Start Visualization");
            mIsPlaying = false;
        } else {
            mMediaPlayer.start();
            mToggleButton.setText("Stop Visualization");
            mIsPlaying = true;
        }
    }
    
    private void changeColor() {
        mColorIndex = (mColorIndex + 1) % mColors.length;
        mVisualizerView.setColor(mColors[mColorIndex]);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (mMediaPlayer != null && mIsPlaying) {
            mMediaPlayer.pause();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (mMediaPlayer != null && mIsPlaying) {
            mMediaPlayer.start();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            if (mIsPlaying) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mVisualizerView != null) {
            mVisualizerView.release();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, 
                                           String[] permissions, 
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && 
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
} 
