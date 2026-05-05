package core;

import entity.TransformComponent;


/**
 * World-space top-left of the view rectangle, plus zoom and optional letterbox padding.
 * Screen position: {@code (world - cam) * zoom + pad}.
 */
public final class Camera2D {

    private float camX;
    private float camY;
    /** World units per screen pixel (zoom &gt; 1 upscales a small world to the window). */
    private float zoom = 1f;
    /** Centers the scaled map when the whole world fits on-screen (horizontal). */
    private float padX;
    /** Centers the scaled map when the whole world fits on-screen (vertical). */
    private float padY;

    public float getX() {
        return camX;
    }

    public float getY() {
        return camY;
    }

    public float getZoom() {
        return zoom;
    }

    /**
     * Sets the world→screen scale. Call each frame (e.g. {@code min(viewW/worldW, viewH/worldH)}) so a
     * small map fills the sketch.
     */
    public void setZoom(float z) {
        if (z > 0f && !Float.isNaN(z) && !Float.isInfinite(z)) {
            this.zoom = z;
        }
    }

    public float getPadX() {
        return padX;
    }

    public float getPadY() {
        return padY;
    }

    public void setPosition(float x, float y) {
        this.camX = x;
        this.camY = y;
    }

    /**
     * Centers the camera on the target in world space, clamped so the view stays inside the world.
     * Uses {@link #zoom}: visible world size is {@code viewW/zoom} × {@code viewH/zoom}.
     * When the scaled world is smaller than the sketch, sets letterbox {@link #padX}/{@link #padY}.
     */
    public void follow(TransformComponent t, float viewW, float viewH, float worldW, float worldH) {
        if (t == null) return;

        float z = zoom > 0f ? zoom : 1f;
        float viewWorldW = viewW / z;
        float viewWorldH = viewH / z;

        float cx = t.getX() + t.getWidth() * 0.5f;
        float cy = t.getY() + t.getHeight() * 0.5f;

        float x = cx - viewWorldW * 0.5f;
        float y = cy - viewWorldH * 0.5f;

        float maxX = Math.max(0f, worldW - viewWorldW);
        float maxY = Math.max(0f, worldH - viewWorldH);

        camX = clamp(x, 0f, maxX);
        camY = clamp(y, 0f, maxY);

        float scaledW = worldW * z;
        float scaledH = worldH * z;
        padX = maxX <= 0f ? (viewW - scaledW) * 0.5f : 0f;
        padY = maxY <= 0f ? (viewH - scaledH) * 0.5f : 0f;
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

}
