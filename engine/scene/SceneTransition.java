package scene;

import processing.core.PApplet;

/**
 * Full-screen fade between scenes. Drive {@link #update(float)} from the main loop with the same
 * {@code dt} as simulation, then call {@link #render(PApplet)} after the current scene is drawn so
 * the fade draws on top.
 * <p>
 * Each frame: render the active scene, then {@link #render(PApplet)}. While {@link #isBlocking()},
 * gameplay updates can be skipped to keep the world frozen for the transition.
 */
public final class SceneTransition {

    public enum Phase {
        /** No transition running. */
        IDLE,
        /** Darkening to black. */
        FADE_OUT,
        /** Lightening from black. */
        FADE_IN
    }

    private Phase phase = Phase.IDLE;
    private float elapsedMs;
    private float fadeOutMs = 400f;
    private float fadeInMs = 400f;
    private Runnable onMidBlack;

    public void setDurationsMs(float fadeOut, float fadeIn) {
        this.fadeOutMs = Math.max(1f, fadeOut);
        this.fadeInMs = Math.max(1f, fadeIn);
    }

    public Phase getPhase() {
        return phase;
    }

    /** True while a fade is in progress (out or in). */
    public boolean isBlocking() {
        return phase == Phase.FADE_OUT || phase == Phase.FADE_IN;
    }

    /**
     * Starts a fade-out → optional swap at full black → fade-in.
     *
     * @param onMidBlack run once when the screen is fully black (load next scene / swap entities here)
     */
    public void begin(Runnable onMidBlack) {
        this.onMidBlack = onMidBlack;
        this.elapsedMs = 0f;
        this.phase = Phase.FADE_OUT;
    }

    /** @param deltaSeconds frame delta (e.g. {@code 1f/60f}) */
    public void update(float deltaSeconds) {
        if (phase == Phase.IDLE) {
            return;
        }
        float dtMs = deltaSeconds * 1000f;
        elapsedMs += dtMs;
        if (phase == Phase.FADE_OUT) {
            if (elapsedMs >= fadeOutMs) {
                if (onMidBlack != null) {
                    Runnable r = onMidBlack;
                    onMidBlack = null;
                    r.run();
                }
                phase = Phase.FADE_IN;
                elapsedMs = 0f;
            }
        } else if (phase == Phase.FADE_IN) {
            if (elapsedMs >= fadeInMs) {
                phase = Phase.IDLE;
                elapsedMs = 0f;
            }
        }
    }

    public void render(PApplet p) {
        if (phase == Phase.IDLE) {
            return;
        }
        float alphaNorm;
        if (phase == Phase.FADE_OUT) {
            alphaNorm = Math.min(1f, elapsedMs / fadeOutMs);
        } else {
            alphaNorm = 1f - Math.min(1f, elapsedMs / fadeInMs);
        }
        p.pushStyle();
        p.pushMatrix();
        p.noStroke();
        p.fill(0, alphaNorm * 255f);
        p.rectMode(PApplet.CORNER);
        p.rect(0, 0, p.width, p.height);
        p.popMatrix();
        p.popStyle();
    }
}
