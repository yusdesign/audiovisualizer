package com.yusdesign.audiovisualizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class VisualizerView extends View {
    private byte[] audioData;
    private final Paint waveformPaint = new Paint();
    private final Paint backgroundPaint = new Paint();

    private VisualizationMode mode = VisualizationMode.WAVEFORM;
    private boolean isGradientEnabled = true;

    public enum VisualizationMode {
        WAVEFORM, BAR, CIRCLE
    }

    public VisualizerView(Context context) {
        super(context);
        init();
    }

    public VisualizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Setup waveform paint
        waveformPaint.setColor(Color.GREEN);
        waveformPaint.setStrokeWidth(3f);
        waveformPaint.setAntiAlias(true);
        waveformPaint.setStyle(Paint.Style.STROKE);

        // Setup background
        backgroundPaint.setColor(Color.BLACK);
        backgroundPaint.setStyle(Paint.Style.FILL);

        audioData = new byte[0];
    }

    public void updateVisualizer(byte[] data) {
        if (data != null && data.length > 0) {
            this.audioData = data.clone();
            postInvalidate();
        }
    }

    public void clearVisualization() {
        this.audioData = new byte[0];
        postInvalidate();
    }

    public void setVisualizationMode(VisualizationMode mode) {
        this.mode = mode;
        invalidate();
    }

    public void setGradientEnabled(boolean enabled) {
        this.isGradientEnabled = enabled;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw background
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        if (audioData.length == 0) {
            drawIdleState(canvas);
            return;
        }

        switch (mode) {
            case WAVEFORM:
                drawWaveform(canvas);
                break;
            case BAR:
                drawBarVisualizer(canvas);
                break;
            case CIRCLE:
                drawCircleVisualizer(canvas);
                break;
        }
    }

    private void drawIdleState(Canvas canvas) {
        Paint textPaint = new Paint();
        textPaint.setColor(Color.GRAY);
        textPaint.setTextSize(48f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        String message = "Tap Start to begin visualization";
        canvas.drawText(message, getWidth() / 2f, getHeight() / 2f, textPaint);
    }

    private void drawWaveform(Canvas canvas) {
        if (isGradientEnabled) {
            setupGradientPaint();
        }

        float width = getWidth();
        float height = getHeight();
        float centerY = height / 2;

        float[] points = new float[audioData.length * 4];
        int pointIndex = 0;

        for (int i = 0; i < audioData.length - 1; i++) {
            float x1 = (float) i / (audioData.length - 1) * width;
            float x2 = (float) (i + 1) / (audioData.length - 1) * width;

            // Convert byte to float and scale
            float y1 = centerY + (audioData[i] / 128.0f) * centerY * 0.8f;
            float y2 = centerY + (audioData[i + 1] / 128.0f) * centerY * 0.8f;

            points[pointIndex++] = x1;
            points[pointIndex++] = y1;
            points[pointIndex++] = x2;
            points[pointIndex++] = y2;
        }

        canvas.drawLines(points, waveformPaint);
    }

    private void drawBarVisualizer(Canvas canvas) {
        float width = getWidth();
        float height = getHeight();
        float barWidth = width / audioData.length;

        for (int i = 0; i < audioData.length; i += 4) { // Skip some bars for performance
            float left = i * barWidth;
            float barHeight = Math.abs(audioData[i] / 128.0f) * height * 0.9f;
            float top = (height - barHeight) / 2;
            float right = left + barWidth - 2; // Add some spacing

            canvas.drawRect(left, top, right, top + barHeight, waveformPaint);
        }
    }

    private void drawCircleVisualizer(Canvas canvas) {
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float maxRadius = Math.min(centerX, centerY) * 0.8f;

        for (int i = 0; i < audioData.length; i += 4) {
            float radius = Math.abs(audioData[i] / 128.0f) * maxRadius;
            canvas.drawCircle(centerX, centerY, radius, waveformPaint);
        }
    }

    private void setupGradientPaint() {
        LinearGradient gradient = new LinearGradient(
                0, 0, getWidth(), getHeight(),
                Color.GREEN, Color.CYAN, Shader.TileMode.MIRROR
        );
        waveformPaint.setShader(gradient);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Reset gradient when size changes
        if (isGradientEnabled) {
            setupGradientPaint();
        }
    }
}

