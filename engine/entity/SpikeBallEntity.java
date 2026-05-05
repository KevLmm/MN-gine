package entity;

import rendering.LoopSpriteFramesDrawable;
import rendering.SpriteSheetDrawable;

/**
 * Animated spike hazard that bounces inside a world rectangle (used on the survival map).
 * Sprite sheet: two horizontal frames (see {@code data/spikeball.png} layout constants below).
 */
public final class SpikeBallEntity extends Entity {

    /** Matches {@code spikeball.png}: 200×200 sheet, two 100×200 frames side by side. */
    private static final int FRAME_W = 100;
    private static final int FRAME_H = 200;
    private static final int COLUMNS = 2;
    private static final int STRIDE_X = 100;
    private static final int STRIDE_Y = 200;

    private static final float MOVE_SPEED = 155f;
    private static final float ANIM_FPS = 8f;

    /** Survival-mode damage size (centered in the sprite transform). Sprite bounds stay larger for art. */
    private static final float DAMAGE_RECT_W = 72f;
    private static final float DAMAGE_RECT_H = 96f;

    private final LoopSpriteFramesDrawable loopDraw;
    private float vx;
    private float vy;
    /** Bounce inside walkable area, inset from map edges (avoids overlap with border solids). */
    private final float boundMinX;
    private final float boundMinY;
    private final float boundMaxX;
    private final float boundMaxY;

    /**
     * @param insetX inset from left/right map edge in pixels (e.g. one tile width)
     * @param insetY inset from top/bottom map edge in pixels
     */
    public SpikeBallEntity(float x, float y, float width, float height, float worldW, float worldH,
            float insetX, float insetY) {
        super(x, y);
        boundMinX = Math.max(0f, insetX);
        boundMinY = Math.max(0f, insetY);
        boundMaxX = Math.max(boundMinX, worldW - width - insetX);
        boundMaxY = Math.max(boundMinY, worldH - height - insetY);
        SpriteSheetDrawable sheet = new SpriteSheetDrawable(
                "spikeball", FRAME_W, FRAME_H, COLUMNS, STRIDE_X, STRIDE_Y, 0, 0);
        this.loopDraw = new LoopSpriteFramesDrawable(sheet, COLUMNS, ANIM_FPS);
        addComponent(new TransformComponent(x, y, width, height));
        addComponent(new AppearanceComponent(loopDraw));
        double ang = Math.random() * Math.PI * 2d;
        vx = (float) (Math.cos(ang) * MOVE_SPEED);
        vy = (float) (Math.sin(ang) * MOVE_SPEED);
    }

    @Override
    public void update(float dt) {
        loopDraw.advance(dt);
        TransformComponent t = getComponent(TransformComponent.class);
        if (t == null) {
            return;
        }
        float nx = t.getX() + vx * dt;
        float ny = t.getY() + vy * dt;
        if (nx < boundMinX) {
            nx = boundMinX;
            vx = Math.abs(vx);
        } else if (nx > boundMaxX) {
            nx = boundMaxX;
            vx = -Math.abs(vx);
        }
        if (ny < boundMinY) {
            ny = boundMinY;
            vy = Math.abs(vy);
        } else if (ny > boundMaxY) {
            ny = boundMaxY;
            vy = -Math.abs(vy);
        }
        t.setX(nx);
        t.setY(ny);
    }

    /** Tighter rect than the full transform; used by survival mode spike damage only. */
    public CollisionRect damageCollisionRect() {
        TransformComponent t = getComponent(TransformComponent.class);
        if (t == null) {
            return null;
        }
        float tw = t.getWidth();
        float th = t.getHeight();
        float dw = Math.min(DAMAGE_RECT_W, tw);
        float dh = Math.min(DAMAGE_RECT_H, th);
        float hx = t.getX() + (tw - dw) * 0.5f;
        float hy = t.getY() + (th - dh) * 0.5f;
        return new CollisionRect(hx, hy, dw, dh);
    }
}
