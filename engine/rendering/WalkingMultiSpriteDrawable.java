package rendering;

import core.Renderable;
import core.WalkAnimatedDrawable;

/**
 * Walk cycle using separate pre-registered sprite ids (each frame is a full {@link assets.AssetsManager} entry).
 */
public final class WalkingMultiSpriteDrawable implements WalkAnimatedDrawable {

    private final String[] frameAssetIds;
    private final int frameCount;
    private final float framesPerSecond;
    private float phase;
    private int frameIndex;

    public WalkingMultiSpriteDrawable(String[] frameAssetIds, float framesPerSecond) {
        this.frameAssetIds = frameAssetIds != null ? frameAssetIds : new String[0];
        this.frameCount = this.frameAssetIds.length;
        this.framesPerSecond = Math.max(0f, framesPerSecond);
    }

    @Override
    public void updateAnimation(boolean moving, float dt) {
        if (frameCount <= 0) {
            return;
        }
        if (!moving) {
            frameIndex = 0;
            phase = 0f;
            return;
        }
        phase += dt * framesPerSecond;
        frameIndex = ((int) phase) % frameCount;
    }

    @Override
    public void draw(Renderable r, float x, float y, float width, float height) {
        if (frameCount <= 0) {
            return;
        }
        String id = frameAssetIds[frameIndex % frameCount];
        if (id == null) {
            return;
        }
        r.drawSprite(id, x, y, width, height, 0);
    }
}
