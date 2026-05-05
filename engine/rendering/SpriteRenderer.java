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

    /** Matches {@link assets.AssetsManager} logic: some {@link PImage}s only populate {@link PImage#pixelWidth}. */
    private static boolean hasDrawablePixels(PImage img) {
        if (img == null) {
            return false;
        }
        int w = img.pixelWidth > 0 ? img.pixelWidth : img.width;
        int h = img.pixelHeight > 0 ? img.pixelHeight : img.height;
        return w > 0 && h > 0;
    }

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

    private float camZoom() {
        return cam == null ? 1f : cam.getZoom();
    }

    private float sx(float worldX) {
        float z = camZoom();
        float pad = cam == null ? 0f : cam.getPadX();
        return (worldX - (cam == null ? 0f : cam.getX())) * z + pad;
    }

    private float sy(float worldY) {
        float z = camZoom();
        float pad = cam == null ? 0f : cam.getPadY();
        return (worldY - (cam == null ? 0f : cam.getY())) * z + pad;
    }

    private float sw(float worldW) {
        return worldW * camZoom();
    }

    @Override
    public void drawRect(float x, float y, float w, float h) {
        applet.pushStyle();
        applet.fill(0, 0, 255);
        applet.noStroke();
        applet.rect(sx(x), sy(y), sw(w), sw(h));
        applet.popStyle();
    }
    @Override
    public void drawCircle(float x, float y, float w, float h, int r, int g, int b) {
        applet.pushStyle();
        applet.fill(r, g, b);
        applet.noStroke();
        float zw = sw(w);
        float zh = sw(h);
        applet.ellipse(sx(x) + zw * 0.5f, sy(y) + zh * 0.5f, zw, zh);
        applet.popStyle();
    }

    @Override
    public void drawSprite(String assetId, float x, float y, float w, float h, int frameIndex) {
        if (assetsManager == null) return;
        PImage img = assetsManager.getSprite(assetId);
        if (!hasDrawablePixels(img)) {
            return;
        }
        applet.pushStyle();
        applet.imageMode(PApplet.CORNER);
        applet.blendMode(PApplet.BLEND);
        applet.noTint();
        applet.image(img, sx(x), sy(y), sw(w), sw(h));
        applet.popStyle();
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
        if (!hasDrawablePixels(image)) {
            return;
        }
        applet.pushStyle();
        applet.imageMode(PApplet.CORNER);
        applet.blendMode(PApplet.BLEND);
        applet.noTint();
        applet.image(image, sx(x), sy(y), sw(width), sw(height));
        applet.popStyle();
    }

    @Override
    public void drawSpriteRegion(String assetId, float dstX, float dstY, float dstW, float dstH, float srcX,
                                 float srcY, float srcW, float srcH) {
        if (assetsManager == null) return;
        PImage img = assetsManager.getSprite(assetId);
        if (!hasDrawablePixels(img)) {
            return;
        }
        int ix = (int) srcX;
        int iy = (int) srcY;
        int iw = (int) srcW;
        int ih = (int) srcH;
        if (iw <= 0 || ih <= 0) {
            return;
        }
        // Prefer get(); ImageIO-backed sheets often need copyImageRegion for a drawable sub-image.
        PImage cell = img.get(ix, iy, iw, ih);
        if (!hasDrawablePixels(cell)) {
            cell = assetsManager.copyImageRegion(img, ix, iy, iw, ih);
        }
        if (!hasDrawablePixels(cell)) {
            return;
        }
        // Uniform scale so square pixels are not stretched into a tall hitbox (avoids "only top" look).
        int cw = cell.pixelWidth > 0 ? cell.pixelWidth : cell.width;
        int ch = cell.pixelHeight > 0 ? cell.pixelHeight : cell.height;
        if (cw <= 0 || ch <= 0) {
            return;
        }
        float k = Math.min(dstW / cw, dstH / ch);
        float worldW = cw * k;
        float worldH = ch * k;
        float offX = (dstW - worldW) * 0.5f;
        float offY = (dstH - worldH) * 0.5f;
        float dx = sx(dstX + offX);
        float dy = sy(dstY + offY);
        applet.pushStyle();
        applet.imageMode(PApplet.CORNER);
        applet.blendMode(PApplet.BLEND);
        applet.noTint();
        applet.image(cell, dx, dy, sw(worldW), sw(worldH));
        applet.popStyle();
    }
}
