# audiovisualizer

# Audio Visualizer

Universal audio visualizer with LAB-inspired perceptual color mapping. Research-based approach to mapping audio frequencies to human perceptual color space.

## 🎨 Research Foundation

**LAB-Inspired Psychoacoustic Color Theory:**
- **Lightness (L)**: Overall volume + high frequency content
- **A-axis**: Warm lows (red) vs Cool highs (green)  
- **B-axis**: Complex spectra (blue) vs Pure tones (yellow)

## 🎯 Features

- 🎵 Universal audio source support (Internet radio, device audio, custom streams)
- 🎨 LAB-inspired perceptual color mapping
- 📱 Mobile-first development built entirely in Termux
- 🔬 Research-ready structure for FFT implementation
- 🆓 MIT Licensed

## 🛠️ Build

```bash
cargo build --target aarch64-linux-android --release
