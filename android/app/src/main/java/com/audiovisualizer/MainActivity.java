package com.audiovisualizer;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    
    static {
        System.loadLibrary("audiovisualizer");
    }
    
    // JNI methods from Rust
    public native String analyzeAudio(String frequencyData);
    public native String getStreamPresets();
    public native String testConnection();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        TextView resultView = findViewById(R.id.result_text);
        
        // Test the Rust library
        String connectionResult = testConnection();
        String presets = getStreamPresets();
        
        String displayText = connectionResult + "\n\nAvailable Presets:\n" + presets;
        resultView.setText(displayText);
    }
}
