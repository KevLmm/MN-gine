package systems;

import entity.Entity;
import entity.TransformComponent;

import java.util.List;

/**
 * Resolves collisions. Call after entity updates.
 * - Screen bounds: clamps entities so they stay within the window
 * - Collidable entities: can be added later for walls, obstacles, etc.
 */
public class CollisionSystem {

    /** Clamp all entities with TransformComponent to stay within the given bounds. */
    public void resolveScreenBounds(List<Entity> entities, float left, float top, float right, float bottom) {
        for (Entity e : entities) {
            TransformComponent t = e.getComponent(TransformComponent.class);
            if (t == null) continue;

            float x = t.getX();
            float y = t.getY();
            float w = t.getWidth();
            float h = t.getHeight();

            if (x < left) x = left;
            if (y < top) y = top;
            if (x + w > right) x = right - w;
            if (y + h > bottom) y = bottom - h;

            t.setX(x);
            t.setY(y);
        }
    }
}
