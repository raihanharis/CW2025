# How to Fix Music Not Playing (IllegalAccessError)

## The Problem
You're getting an `IllegalAccessError` when trying to play music. This is a JavaFX module system issue.

## Solution 1: Add VM Arguments (Recommended)

### In IntelliJ IDEA:
1. Go to **Run** → **Edit Configurations...**
2. Select your **Main** configuration
3. In **VM options**, add:
   ```
   --add-opens javafx.media/javafx.scene.media=ALL-UNNAMED
   --add-opens javafx.base/com.sun.javafx=ALL-UNNAMED
   ```
4. Click **Apply** and **OK**
5. Run the game again

### Alternative: Convert to MP3
MP3 files often work better with JavaFX:
1. Convert your `tetris_theme.wav` to `tetris_theme.mp3`
2. Place it in `src/main/resources/sounds/tetris_theme.mp3`
3. The game will automatically use MP3 if available

## Solution 2: Use Maven to Run
Run from terminal:
```bash
mvn clean javafx:run
```

The pom.xml has been updated with the necessary VM arguments.

