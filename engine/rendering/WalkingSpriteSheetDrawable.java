package rendering;

import core.Renderable;
import core.WalkAnimatedDrawable;

/**
 * Advances a {@link SpriteSheetDrawable} while moving; shows an idle cell when still.
 * Walk and idle can use different strip indices (e.g. stand in column 0, walk cycles columns 1–3 only).
 */
public final class WalkingSpriteSheetDrawable implements WalkAnimatedDrawable {

    private final SpriteSheetDrawable sheet;
    private final int sheetFrameCount;
    private final float framesPerSecond;
    private final int idleFrameIndex;
    private final int walkFirstIndex;
    private final int walkLastIndex;
    private float phase;

    public WalkingSpriteSheetDrawable(SpriteSheetDrawable sheet, float framesPerSecond, int sheetFrameCount) {
        this(sheet, framesPerSecond, sheetFrameCount, 0);
    }

    /**
     * While moving, cycles through every sheet frame index {@code 0 … sheetFrameCount-1}.
     */
    public WalkingSpriteSheetDrawable(SpriteSheetDrawable sheet, float framesPerSecond, int sheetFrameCount,
            int idleFrameIndex) {
        this(sheet, framesPerSecond, sheetFrameCount, idleFrameIndex, 0, sheetFrameCount - 1);
    }

    /**
     * @param idleFrameIndex strip index while not moving
     * @param walkFirstIndex first strip index used while moving (inclusive)
     * @param walkLastIndex last strip index used while moving (inclusive)
     */
    public WalkingSpriteSheetDrawable(SpriteSheetDrawable sheet, float framesPerSecond, int sheetFrameCount,
            int idleFrameIndex, int walkFirstIndex, int walkLastIndex) {
        this.sheet = sheet;
        this.framesPerSecond = Math.max(0f, framesPerSecond);
        this.sheetFrameCount = Math.max(1, sheetFrameCount);
        int max = this.sheetFrameCount - 1;
        this.idleFrameIndex = Math.max(0, Math.min(idleFrameIndex, max));
        int wf = Math.max(0, Math.min(walkFirstIndex, max));
        int wl = Math.max(0, Math.min(walkLastIndex, max));
        if (wl < wf) {
            wf = 0;
            wl = max;
        }
        this.walkFirstIndex = wf;
        this.walkLastIndex = wl;
    }

    @Override
    public void updateAnimation(boolean moving, float dt) {
        if (!moving) {
            sheet.setFrameIndex(idleFrameIndex);
            phase = 0f;
            return;
        }
        phase += dt * framesPerSecond;
        int span = walkLastIndex - walkFirstIndex + 1;
        int f = walkFirstIndex + ((int) phase) % span;
        sheet.setFrameIndex(f);
    }

    @Override
    public void draw(Renderable r, float x, float y, float width, float height) {
        sheet.draw(r, x, y, width, height);
    }
}
