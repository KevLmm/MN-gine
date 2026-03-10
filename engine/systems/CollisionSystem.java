package systems;

import core.Collidable;
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
    /**
     * Resolve overlaps: every entity with a Transform is pushed out of entities that implement Collidable.
     * Call after movement, before screen bounds.
     */
    public void resolveEntityCollisions(List<Entity> entities) {
        for (Entity mover : entities) {
            TransformComponent moverT = mover.getComponent(TransformComponent.class);
            if (moverT == null) continue;

            for (Entity other : entities) {
                if (mover == other) continue;
                if (!(other instanceof Collidable)) continue;

                TransformComponent otherT = other.getComponent(TransformComponent.class);
                if (otherT == null) continue;

                resolveOverlap(moverT, otherT);
            }
        }
    }

    /** Push mover out of obstacle using minimum penetration (AABB). */
    private void resolveOverlap(TransformComponent mover, TransformComponent obstacle) {
        float mx = mover.getX(), my = mover.getY(), mw = mover.getWidth(), mh = mover.getHeight();
        float ox = obstacle.getX(), oy = obstacle.getY(), ow = obstacle.getWidth(), oh = obstacle.getHeight();

        float overlapLeft = (mx + mw) - ox;
        float overlapRight = (ox + ow) - mx;
        float overlapTop = (my + mh) - oy;
        float overlapBottom = (oy + oh) - my;

        if (overlapLeft <= 0 || overlapRight <= 0 || overlapTop <= 0 || overlapBottom <= 0)
            return;

        float minX = Math.min(overlapLeft, overlapRight);
        float minY = Math.min(overlapTop, overlapBottom);

        if (minX < minY) {
            if (overlapLeft < overlapRight) mx = ox - mw;
            else mx = ox + ow;
        } else {
            if (overlapTop < overlapBottom) my = oy - mh;
            else my = oy + oh;
        }

        mover.setX(mx);
        mover.setY(my);
    }
}
