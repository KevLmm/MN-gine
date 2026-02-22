package rendering;

import core.Renderable;
import processing.core.PApplet;

public class ShapeRenderer implements Renderable {

    private PApplet applet;

    public ShapeRenderer(PApplet applet) {
        this.applet = applet;
    }

    @Override
    public void clear() {
        applet.background(0);
    }

    @Override
    public void drawRect(float x, float y, float width, float height) {
        applet.fill(255);
        applet.noStroke();
        applet.rect(x, y, width, height);
    }
}
