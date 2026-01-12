package com.yusdesign.audiovisualizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.view.View;

public class VisualizerView extends View {
    private byte[] mBytes;
    private float[] mPoints;
    private RectF mRect = new RectF();
    private Paint mForePaint = new Paint();
    private Visualizer mVisualizer;
    private MediaPlayer mMediaPlayer;
    private boolean mIsPlaying = false;
    
    public VisualizerView(Context context) {
        super(context);
        init();
    }
    
    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public VisualizerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    private void init() {
        mBytes = null;
        mForePaint.setStrokeWidth(3f);
        mForePaint.setAntiAlias(true);
        mForePaint.setColor(Color.argb(200, 56, 138, 252));
    }
    
    public void setColor(int color) {
        mForePaint.setColor(color);
    }
    
    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        mMediaPlayer = mediaPlayer;
        setupVisualizer();
    }
    
    private void setupVisualizer() {
        if (mVisualizer != null) {
            mVisualizer.release();
        }
        
        try {
            // Get audio session ID from media player
            int audioSessionId = mMediaPlayer.getAudioSessionId();
            
            // Create visualizer with max capture rate
            mVisualizer = new Visualizer(audioSessionId);
            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            
            // Set up listener for waveform data
            Visualizer.OnDataCaptureListener captureListener = new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                    updateVisualizer(bytes);
                }
                
                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                    // You can implement FFT visualization here
                }
            };
            
            // Set capture rate and start capturing
            mVisualizer.setDataCaptureListener(captureListener, 
                Visualizer.getMaxCaptureRate() / 2, true, false);
            mVisualizer.setEnabled(true);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void release() {
        if (mVisualizer != null) {
            mVisualizer.release();
            mVisualizer = null;
        }
    }
    
    public void updateVisualizer(byte[] bytes) {
        mBytes = bytes;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (mBytes == null) {
            return;
        }
        
        // Draw waveform
        drawWaveform(canvas);
    }
    
    private void drawWaveform(Canvas canvas) {
        if (mBytes == null || mBytes.length == 0) {
            return;
        }
        
        float width = getWidth();
        float height = getHeight();
        float centerY = height / 2;
        
        // Create points array if needed
        if (mPoints == null || mPoints.length < mBytes.length * 4) {
            mPoints = new float[mBytes.length * 4];
        }
        
        // Calculate points for the waveform
        for (int i = 0; i < mBytes.length - 1; i++) {
            float x = i * width / (mBytes.length - 1);
            float y = (mBytes[i] + 128) * height / 256;
            float nextX = (i + 1) * width / (mBytes.length - 1);
            float nextY = (mBytes[i + 1] + 128) * height / 256;
            
            mPoints[i * 4] = x;
            mPoints[i * 4 + 1] = centerY - y / 2;
            mPoints[i * 4 + 2] = nextX;
            mPoints[i * 4 + 3] = centerY - nextY / 2;
        }
        
        // Draw the waveform
        canvas.drawLines(mPoints, mForePaint);
        
        // Draw a circle visualization
        drawCircleVisualization(canvas, width, height);
    }
    
    private void drawCircleVisualization(Canvas canvas, float width, float height) {
        if (mBytes == null || mBytes.length < 2) {
            return;
        }
        
        float centerX = width / 2;
        float centerY = height / 2;
        float radius = Math.min(width, height) / 4;
        
        // Draw circle
        Paint circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(2);
        circlePaint.setColor(Color.argb(150, 255, 255, 255));
        canvas.drawCircle(centerX, centerY, radius, circlePaint);
        
        // Draw bars around circle
        int bars = 60;
        float angle = (float) (2 * Math.PI / bars);
        
        for (int i = 0; i < bars; i++) {
            int byteIndex = i * mBytes.length / bars;
            byteIndex = Math.min(byteIndex, mBytes.length - 1);
            
            float amplitude = Math.abs(mBytes[byteIndex]) / 128.0f;
            float barLength = radius * 0.5f + amplitude * radius;
            
            float x1 = centerX + (float) (radius * Math.cos(i * angle));
            float y1 = centerY + (float) (radius * Math.sin(i * angle));
            float x2 = centerX + (float) ((radius + barLength) * Math.cos(i * angle));
            float y2 = centerY + (float) ((radius + barLength) * Math.sin(i * angle));
            
            canvas.drawLine(x1, y1, x2, y2, mForePaint);
        }
    }
}
