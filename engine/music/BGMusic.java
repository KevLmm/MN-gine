package music;

import processing.core.PApplet;
import processing.sound.SoundFile;

/** Background music using the Processing Sound library. */
public class BGMusic {

    private final PApplet applet;
    private final String musicPath;
    private SoundFile soundFile;
    /**
     * User slider 0 (silent) … 1 (loudest). This is <em>not</em> passed straight to {@link SoundFile#amp(float)}
     * because linear {@code 0.5} still sounds very loud on hot-mastered tracks; see {@link #effectiveAmp()}.
     */
    private float volume = .25f;

    /** Max real amplitude when {@link #volume} is 1. Tune down if BGM still feels loud (try 0.12–0.25). */
    private static final float MAX_AMP = 0.11f;

    public BGMusic(PApplet applet, String musicPath) {
        this.applet = applet;
        this.musicPath = musicPath;
    }

    private void ensureLoaded() {
        if (soundFile == null) {
            soundFile = new SoundFile(applet, musicPath);
            soundFile.amp(effectiveAmp());
        }
    }

    /**
     * Maps user {@link #volume} to real {@link SoundFile#amp}: {@code volume² × MAX_AMP} so 0.5 is much
     * quieter than linear 0.5, and full slider still caps at {@link #MAX_AMP}.
     */
    private float effectiveAmp() {
        float v = clamp01(volume);
        return Math.min(1f, v * v * MAX_AMP);
    }

    public void setVolume(float linearAmplitude) {
        volume = clamp01(linearAmplitude);
        if (soundFile != null) {
            soundFile.amp(effectiveAmp());
        }
    }

    public float getVolume() {
        return volume;
    }

    public void playLoop() {
        ensureLoaded();
        soundFile.amp(effectiveAmp());
        soundFile.loop();
    }

    public void playOnce() {
        ensureLoaded();
        soundFile.amp(effectiveAmp());
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
