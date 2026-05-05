package scene;

import processing.core.PApplet;

/** Dim full-screen overlay with centered copy; closes on any key or mouse press. */
public final class ModalMessageBox {

    private String message = "";
    private boolean visible;

    public void open(String text) {
        message = text != null ? text : "";
        visible = true;
    }

    public boolean isVisible() {
        return visible;
    }

    public void dismiss() {
        visible = false;
    }

    public void draw(PApplet p) {
        if (!visible) {
            return;
        }
        float margin = 28f;
        float boxW = p.width - 2f * margin;
        float boxH = Math.min(320f, p.height * 0.58f);
        float bx = margin;
        float by = (p.height - boxH) * 0.5f;

        p.pushStyle();
        p.fill(0, 170);
        p.noStroke();
        p.rect(0, 0, p.width, p.height);

        p.fill(252, 250, 245);
        p.stroke(45, 45, 55);
        p.strokeWeight(5f);
        p.rect(bx, by, boxW, boxH, 14f);

        p.fill(22, 22, 30);
        p.noStroke();
        p.textAlign(PApplet.LEFT, PApplet.TOP);
        p.textSize(28f);
        float textPad = 32f;
        p.text(message, bx + textPad, by + textPad, boxW - 2f * textPad, boxH - 100f);

        p.fill(75, 75, 90);
        p.textSize(20f);
        p.text("Any key or click to close", bx + textPad, by + boxH - 52f);
        p.popStyle();
    }
}
