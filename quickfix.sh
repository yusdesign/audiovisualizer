# cd ~/audiovisualizer

# Apply automatic fixes
cargo fix --lib -p audiovisualizer --allow-dirty

# Now let's create a cleaner version with the warnings fixed
cat > src/lib.rs << 'EOF'
use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jstring;

/// Perceptual color space mapping inspired by LAB color theory
struct AudioColorMapper;

impl AudioColorMapper {
    fn new() -> Self {
        Self
    }

    /// Map frequency spectrum to LAB-like perceptual colors
    fn spectrum_to_lab(&self, _frequencies: &[f32]) -> (f32, f32, f32) {
        // LAB-inspired mapping:
        // L (Lightness) = Overall volume + high frequency content
        // A (Red-Green) = Warm vs Cool balance  
        // B (Blue-Yellow) = Harmonic richness vs purity
        
        // Placeholder values - real implementation would use FFT
        let lightness = 0.7;  // Overall energy
        let a_value = 0.2;    // Warm-cool balance  
        let b_value = -0.1;   // Spectral richness
        
        (lightness, a_value, b_value)
    }

    /// Convert LAB-like values to RGB for display
    fn lab_to_rgb(&self, l: f32, a: f32, b: f32) -> (u8, u8, u8) {
        // Simplified LAB→RGB conversion
        let r = (l * 255.0).min(255.0).max(0.0) as u8;
        let g = ((l + a) * 255.0).min(255.0).max(0.0) as u8;
        let b_val = ((l + b) * 255.0).min(255.0).max(0.0) as u8;
        
        (r, g, b_val)
    }
}

// JNI Interface
#[no_mangle]
pub extern "system" fn Java_com_audiovisualizer_MainActivity_analyzeAudio(
    mut env: JNIEnv,
    _class: JClass,
    frequency_data_json: JString,
) -> jstring {
    let _data_json: String = env.get_string(&frequency_data_json)
        .expect("Couldn't get frequency data!")
        .into();

    let mapper = AudioColorMapper::new();
    let (l, a, b) = mapper.spectrum_to_lab(&[]); // Empty for demo
    let (r, g, b_val) = mapper.lab_to_rgb(l, a, b);
    
    let color_info = format!(
        "🎨 LAB Color Mapping\nL: {:.2} A: {:.2} B: {:.2}\nRGB: ({}, {}, {})", 
        l, a, b, r, g, b_val
    );
    
    env.new_string(color_info)
        .expect("Couldn't create java string!")
        .into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_audiovisualizer_MainActivity_getStreamPresets(
    mut env: JNIEnv,
    _class: JClass,
) -> jstring {
    let presets = r#"[
        {"name": "SomaFM: Indie Pop", "url": "https://ice1.somafm.com/indiepop-32-aac", "type": "internet"},
        {"name": "SomaFM: Drone Zone", "url": "https://ice1.somafm.com/dronezone-32-aac", "type": "internet"},
        {"name": "Device Microphone", "url": "device_mic", "type": "device"},
        {"name": "System Audio", "url": "device_system", "type": "device"}
    ]"#;
    
    env.new_string(presets)
        .expect("Couldn't create java string!")
        .into_raw()
}

#[no_mangle]
pub extern "system" fn Java_com_audiovisualizer_MainActivity_testConnection(
    mut env: JNIEnv,
    _class: JClass,
) -> jstring {
    env.new_string("✅ Audio Visualizer Ready!\n🎨 LAB Color Mapping Active\n🎵 Universal Stream Support")
        .expect("Couldn't create java string!")
        .into_raw()
}
EOF

# Test the clean build
cargo build --target aarch64-linux-android --release
