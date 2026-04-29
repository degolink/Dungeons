const fs = require('fs');
const path = require('path');

const dirPath = String.raw`c:\Cosas\Rol\rol\src\test\java\com\dungeons_and_dragons\rol\integration`;

try {
    fs.mkdirSync(dirPath, { recursive: true });
    if (fs.existsSync(dirPath)) {
        console.log('✓ Directory created successfully!');
        console.log(`Path: ${dirPath}`);
        console.log(`Exists: ${fs.existsSync(dirPath)}`);
    } else {
        console.log('✗ Failed to create directory');
    }
} catch (error) {
    console.log(`✗ Error: ${error.message}`);
}
