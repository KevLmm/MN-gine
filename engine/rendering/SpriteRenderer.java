package rendering;

import core.Camera2D;
import core.Renderable;
import processing.core.PApplet;
import assets.AssetsManager;
import processing.core.PImage;


public class SpriteRenderer implements Renderable {

    private PApplet applet;
    private AssetsManager assetsManager;
    private Camera2D cam;

    public SpriteRenderer(PApplet app, AssetsManager assetsManager) {
        this.applet = app;
        this.assetsManager = assetsManager;
    }

    public void clear() {
        applet.background(0);
        
        applet.imageMode(PApplet.CORNER);
    }

    public void setCamera(Camera2D cam) {
        this.cam = cam;
    }

    private float sx(float worldX) {
        return worldX - (cam == null ? 0f : cam.getX());
    }

    private float sy(float worldY) {
        return worldY - (cam == null ? 0f : cam.getY());
    }

    @Override
    public void drawRect(float x, float y, float w, float h) {
        applet.fill(0, 0, 255);
        applet.noStroke();
        applet.rect(sx(x), sy(y), w, h);
    }
    @Override
    public void drawCircle(float x, float y, float w, float h, int r, int g, int b) {
        applet.fill(r, g, b);
        applet.noStroke();
        applet.ellipse(sx(x) + w/2, sy(y) + h/2, w, h);
    }

    @Override
    public void drawSprite(String assetId, float x, float y, float w, float h, int frameIndex) {
        if (assetsManager == null) return;
        PImage img = assetsManager.getSprite(assetId);
        if (img != null) {
            applet.image(img, sx(x), sy(y), w, h);
        }
    }

    @Override
    public void drawText(String text, float x, float y) {
        applet.fill(255);
        applet.text(text, sx(x), sy(y));
    }
    public void setAssetsManager(AssetsManager assetsManager) {
        this.assetsManager = assetsManager;
    }
    @Override
    public void drawImage(PImage image, float x, float y, float width, float height) {
        applet.image(image, sx(x), sy(y), width, height);
    }
}
