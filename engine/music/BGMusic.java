package music;

import processing.core.PApplet;
import processing.sound.SoundFile;

/** Background music using the Processing Sound library. */
public class BGMusic {

    private final PApplet applet;
    private final String musicPath;
    private SoundFile soundFile;
    /** Linear amplitude 0 (silent) … 1 (full). Same as {@link SoundFile#amp(float)}. */
    private float volume = .5f;

    public BGMusic(PApplet applet, String musicPath) {
        this.applet = applet;
        this.musicPath = musicPath;
    }

    private void ensureLoaded() {
        if (soundFile == null) {
            soundFile = new SoundFile(applet, musicPath);
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

    public void playLoop() {
        ensureLoaded();
        soundFile.amp(volume);
        soundFile.loop();
    }

    public void playOnce() {
        ensureLoaded();
        soundFile.amp(volume);
        soundFile.play();
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
