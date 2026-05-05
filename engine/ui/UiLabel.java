package ui;

import processing.core.PApplet;

public final class UiLabel {
    private float x, y;
    private String text;
    private boolean visible = true;
    private int textSize = 14;

    public UiLabel(float x, float y, String text) {
        this.x = x;
        this.y = y;
        this.text = text == null ? "" : text;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setTextSize(int textSize) {
        this.textSize = Math.max(6, textSize);
    }

    public void draw(PApplet p) {
        if (!visible) return;

        p.pushStyle();
        p.fill(235);
        p.noStroke();
        p.textAlign(PApplet.LEFT, PApplet.TOP);
        p.textSize(textSize);
        p.text(text, x, y);
        p.popStyle();
    }

}
