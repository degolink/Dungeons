import os

directory_path = r"c:\Cosas\Rol\rol\src\test\java\com\dungeons_and_dragons\rol\integration"

try:
    os.makedirs(directory_path, exist_ok=True)
    if os.path.isdir(directory_path):
        print("✓ Directory created successfully!")
        print(f"Path: {directory_path}")
        print(f"Exists: {os.path.exists(directory_path)}")
    else:
        print("✗ Failed to create directory")
except Exception as e:
    print(f"✗ Error: {e}")
