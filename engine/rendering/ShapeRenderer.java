package rendering;

import core.Renderable;
import processing.core.PApplet;
import processing.core.PImage;

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
        applet.fill(0, 0, 255);
        applet.noStroke();
        applet.rect(x, y, width, height);
    }

    @Override
    public void drawCircle(float x, float y, float width, float height, int r, int g, int b) {
        applet.fill(r, g, b);
        applet.noStroke();
        applet.ellipse(x + width/2, y + height/2, width, height);
    }

    @Override
    public void drawSprite(String assetId, float x, float y, float w, float h, int frameIndex) {
        throw new UnsupportedOperationException("ShapeRenderer does not support drawing sprites");
    }

    @Override
    public void drawText(String text, float x, float y) {
        throw new UnsupportedOperationException("ShapeRenderer does not support drawing text");
    }
    @Override 
    public void drawImage(PImage image, float x, float y, float width, float height) {
        applet.image(image, x, y, width, height);
    }
}
