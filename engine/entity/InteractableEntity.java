package entity;

import core.Collidable;
import rendering.EmptyDrawable;

/**
 * Solid interactable with a collision hull smaller than the {@link TransformComponent} size,
 * centered on the transform. Uses {@link rendering.EmptyDrawable} by default (invisible hitbox).
 */
public class InteractableEntity extends Entity implements Collidable {

    public static final float DEFAULT_DIAMETER = 50f;
    /** Hull width/height as a fraction of transform size (0–1], centered on the transform. */
    public static final float DEFAULT_COLLISION_HULL_SCALE = 0.52f;

    private final float collisionHullScale;

    public InteractableEntity(float x, float y) {
        this(x, y, DEFAULT_DIAMETER, DEFAULT_COLLISION_HULL_SCALE);
    }

    public InteractableEntity(float x, float y, float diameter) {
        this(x, y, diameter, DEFAULT_COLLISION_HULL_SCALE);
    }

    public InteractableEntity(float x, float y, float diameter, float collisionHullScale) {
        super(x, y);
        this.collisionHullScale = clampHullScale(collisionHullScale);
        addComponent(new TransformComponent(x, y, diameter, diameter));
        addComponent(new AppearanceComponent(new EmptyDrawable()));
    }

    @Override
    public CollisionRect getCollisionBounds(Entity self) {
        TransformComponent t = self.getComponent(TransformComponent.class);
        if (t == null) {
            return null;
        }
        float w = t.getWidth() * collisionHullScale;
        float h = t.getHeight() * collisionHullScale;
        float hx = t.getX() + (t.getWidth() - w) * 0.5f;
        float hy = t.getY() + (t.getHeight() - h) * 0.5f;
        return new CollisionRect(hx, hy, w, h);
    }

    private static float clampHullScale(float s) {
        if (s <= 0f || Float.isNaN(s)) {
            return DEFAULT_COLLISION_HULL_SCALE;
        }
        return Math.min(1f, s);
    }

    @Override
    public void onInteract() {
    }
}
