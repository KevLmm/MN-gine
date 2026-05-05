package ui;

import processing.core.PApplet;


public final class UiButton {

    private float x, y, width, height;
    private String label;
    private boolean visible = true;
    private boolean enabled = true;

    private boolean hovered = false;
    private boolean pressed = false;


    public UiButton(float x, float y, float width, float height, String label) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.label = label == null ? "" : label;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isHovered() {
        return hovered;
    }

    public void update(float mx, float my) {
        if (!visible || !enabled) {
            hovered = false;
            return;
        }

        hovered = mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    public boolean mousePressed(int button, float mx, float my) {
        if (!visible || !enabled || button != PApplet.LEFT) return false;
        if (!(mx >= x && mx <= x + width && my >= y && my <= y + height)) return false;
        pressed = true;
        return true;
    }

    public boolean mouseReleased(int button, float mx, float my) {
        if (!visible || !enabled || button != PApplet.LEFT) return false;
        boolean was = pressed;
        pressed = false;
        return was && hovered;
    }

    public void draw(PApplet p) {
        if (!visible) return;

        p.pushStyle();

        int fillA = enabled ? (hovered ? 90 : 55) : 35;
        p.stroke(200);
        p.strokeWeight(2);
        p.fill(fillA, 220);
        p.rect(x, y, width, height, 8);

        p.fill(enabled ? 245 : 170);
        p.textAlign(PApplet.CENTER, PApplet.CENTER);
        p.textSize(14);
        p.text(label, x + width * 0.5f, y + height * 0.5f);

        p.popStyle();

    }

}
