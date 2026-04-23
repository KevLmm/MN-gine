package core;

import entity.TransformComponent;


 /** 
     * Top-Left of the view rectangle in world space.
     * Rendering subtracts(camX, camY) from world (x, y) to get screen (x, y).
     */
public final class Camera2D {

    private float camX;
    private float camY;

    public float getX() {
        return camX;
    }

    public float getY() {
        return camY;
    }

    public void setPosition(float x, float y) {
        this.camX = x;
        this.camY = y;
    }

    /**
     * Center the camera on the target, clamped so the view stays inside the world rectangle.
     * @param t
     * @param viewW
     * @param viewH
     * @param worldW
     * @param worldH
     */
    
    public void follow(TransformComponent t, float viewW, float viewH, float worldW, float worldH) {
        if (t == null) return;

        float cx = t.getX() + t.getWidth() * 0.5f;
        float cy = t.getY() + t.getHeight() * 0.5f;

        float x = cx - viewW * 0.5f;
        float y = cy - viewH * 0.5f;

        float maxX = Math.max(0f, worldW - viewW);
        float maxY = Math.max(0f, worldH - viewH);

        camX = clamp(x, 0f, maxX);
        camY = clamp(y, 0f, maxY);
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

}
