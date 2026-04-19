package entity;

/** World-space axis-aligned box used for physics and interaction queries. */
public final class CollisionRect {

    public final float x;
    public final float y;
    public final float width;
    public final float height;

    public CollisionRect(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public static CollisionRect fromTransform(TransformComponent t) {
        return new CollisionRect(t.getX(), t.getY(), t.getWidth(), t.getHeight());
    }
}
