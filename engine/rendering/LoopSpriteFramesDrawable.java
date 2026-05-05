package rendering;

import core.Drawable;
import core.Renderable;

/** Cycles a {@link SpriteSheetDrawable} through frame indices {@code 0 … frameCount-1} at a fixed rate. */
public final class LoopSpriteFramesDrawable implements Drawable {

    private final SpriteSheetDrawable sheet;
    private final int frameCount;
    private final float framesPerSecond;
    private float phase;

    public LoopSpriteFramesDrawable(SpriteSheetDrawable sheet, int frameCount, float framesPerSecond) {
        this.sheet = sheet;
        this.frameCount = Math.max(1, frameCount);
        this.framesPerSecond = Math.max(0f, framesPerSecond);
    }

    public void advance(float dt) {
        phase += dt * framesPerSecond;
        sheet.setFrameIndex(((int) phase) % frameCount);
    }

    @Override
    public void draw(Renderable r, float x, float y, float width, float height) {
        sheet.draw(r, x, y, width, height);
    }
}
