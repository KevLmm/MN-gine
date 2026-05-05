package rendering;

import processing.core.PApplet;
import processing.core.PImage;

/**
 * Draws a magnified view of a sprite sheet with per-cell rectangles and sequential indices.
 * Scan order is row-major: left→right, then next row (good for counting frames in a uniform grid).
 */
public final class SpriteSheetDebugGrid {

    private SpriteSheetDebugGrid() {
    }

    /**
     * @param drawX   screen x where the magnified sheet top-left is drawn
     * @param drawY   screen y where the magnified sheet top-left is drawn
     * @param zoom    scale factor (2–4 is readable)
     */
    /**
     * Scales the whole sheet to fit inside the sketch (with margin), then draws the same grid.
     * Use this when a fixed {@code zoom} would make a large sheet larger than the window.
     */
    public static void drawFit(
            PApplet p,
            PImage sheet,
            float margin,
            int originX,
            int originY,
            int frameW,
            int frameH,
            int strideX,
            int strideY
    ) {
        if (sheet == null) {
            return;
        }
        float innerW = p.width - 2f * margin;
        float innerH = p.height - 2f * margin;
        if (innerW <= 0f || innerH <= 0f) {
            return;
        }
        float z = Math.min(innerW / (float) sheet.width, innerH / (float) sheet.height);
        if (z <= 0f) {
            return;
        }
        float drawW = sheet.width * z;
        float drawH = sheet.height * z;
        float drawX = (p.width - drawW) * 0.5f;
        float drawY = (p.height - drawH) * 0.5f;
        draw(p, sheet, drawX, drawY, z, originX, originY, frameW, frameH, strideX, strideY);
    }

    public static void draw(
            PApplet p,
            PImage sheet,
            float drawX,
            float drawY,
            float zoom,
            int originX,
            int originY,
            int frameW,
            int frameH,
            int strideX,
            int strideY
    ) {
        if (sheet == null || zoom <= 0f) {
            return;
        }

        p.pushStyle();
        p.imageMode(PApplet.CORNER);

        p.image(sheet, drawX, drawY, sheet.width * zoom, sheet.height * zoom);

        p.noFill();
        p.stroke(255, 60, 60, 220);
        p.strokeWeight(1f);

        int index = 0;
        for (int row = 0; ; row++) {
            int srcY = originY + row * strideY;
            if (srcY + frameH > sheet.height) {
                break;
            }
            for (int col = 0; ; col++) {
                int srcX = originX + col * strideX;
                if (srcX + frameW > sheet.width) {
                    break;
                }

                float px = drawX + srcX * zoom;
                float py = drawY + srcY * zoom;
                float pw = frameW * zoom;
                float ph = frameH * zoom;

                p.rect(px, py, pw, ph);

                p.fill(255, 230);
                p.noStroke();
                p.textAlign(PApplet.LEFT, PApplet.TOP);
                p.textSize(Math.max(8f, 9f * zoom * 0.25f));
                p.text(Integer.toString(index), px + 2f, py + 2f);
                p.noFill();
                p.stroke(255, 60, 60, 220);

                index++;
            }
        }

        p.popStyle();
    }
}
