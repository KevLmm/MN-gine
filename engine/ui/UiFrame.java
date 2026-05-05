package ui;
import java.util.List;
import processing.core.PApplet;


public final class UiFrame {
    private float x, y, width, height;
    private String title;
    private boolean isVisible = true;
    private PApplet p;

    private float titleBarHeight = 28f;

    public UiFrame(float x, float y, float width, float height, String title) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.title = title == null ? "" : title;
    }

    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setTitleBarHeight(float titleBarHeight) {
        this.titleBarHeight = Math.max(0f, titleBarHeight);
    }

    public boolean contains(float mx, float my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }

    /** Inner area below title bar (for laying out labels/buttons). */

    public float contentX() { return x + 8f; }
    public float contentY() { return y + titleBarHeight + 8f; }
    public float contentWidth() { return width - 16f; }
    public float contentHeight() { return height - titleBarHeight - 16f; }

    public void draw(PApplet p) {
        if (!isVisible) return;

        p.pushStyle();

        p.stroke(90);
        p.strokeWeight(1f);
        p.fill(25, 200);
        p.rect(x, y, width, height, 8);

        if (titleBarHeight > 0f && !title.isEmpty()) {
            p.noStroke();
            p.fill(40, 220);
            p.rect(x, y, width, titleBarHeight, 8, 8, 0, 0);

            p.fill(230);
            p.textAlign(PApplet.CENTER, PApplet.CENTER);
            p.textSize(14);
            p.text(title, x + 10f, y + titleBarHeight * 0.5f);
        }

        p.popStyle();
        
    }

}
