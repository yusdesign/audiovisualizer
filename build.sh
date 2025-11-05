# Build the project
cargo build --target aarch64-linux-android --release

# Verify build
find target -name "*.so" -type f
file target/aarch64-linux-android/release/libaudiovisualizer.so
