package music;

import processing.core.PApplet;
import processing.sound.SoundFile;

/** One-shot or reusable sound effect using the Processing Sound library. */
public class SoundFX {

    private final PApplet applet;
    private final String soundPath;
    private SoundFile soundFile;
    /** Linear amplitude 0 (silent) … 1 (full). Same as {@link SoundFile#amp(float)}. */
    private float volume = 1f;

    public SoundFX(PApplet applet, String soundPath) {
        this.applet = applet;
        this.soundPath = soundPath;
    }

    private void ensureLoaded() {
        if (soundFile == null) {
            soundFile = new SoundFile(applet, soundPath);
            soundFile.amp(volume);
        }
    }

    public void setVolume(float linearAmplitude) {
        volume = clamp01(linearAmplitude);
        if (soundFile != null) {
            soundFile.amp(volume);
        }
    }

    public float getVolume() {
        return volume;
    }

    public void play() {
        ensureLoaded();
        soundFile.amp(volume);
        soundFile.play();
    }

    /**
     * Loops until {@link #stop()}. Call only when starting movement (not every frame), or the sound
     * restarts.
     */
    public void playLoop() {
        ensureLoaded();
        soundFile.amp(volume);
        soundFile.loop();
    }

    public boolean isPlaying() {
        return soundFile != null && soundFile.isPlaying();
    }

    public void stop() {
        if (soundFile != null) {
            soundFile.stop();
        }
    }

    private static float clamp01(float v) {
        if (v < 0f) {
            return 0f;
        }
        if (v > 1f) {
            return 1f;
        }
        return v;
    }
}
