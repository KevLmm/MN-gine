package systems;

import core.Collidable;
import entity.CollisionRect;
import entity.Entity;
import entity.TileMap;
import entity.TransformComponent;

import java.util.List;

/**
 * Runs after entities move. Three separate steps you can call in any order you want:
 * entity-vs-entity, tile grid, then keeping everyone inside the screen.
 */
public class CollisionSystem {

    /**
     * For each entity, finds which map tiles its box overlaps. For each solid tile, nudges the entity
     * out the shortest way (same idea as entity collisions).
     * <p>
     * You still need to decide which tiles are solid (see the {@code solid} flag in the loop).
     * <p>
     * Common call order: {@code resolveEntityCollisions} → this → {@code resolveScreenBounds}.
     */
    public void resolveTileMapCollisions(List<Entity> entities, TileMap tileMap) {
        if (tileMap == null) {
            return;
        }

        int tw = tileMap.getTileWidth();
        int th = tileMap.getTileHeight();

        for (Entity e : entities) {
            TransformComponent mover = e.getComponent(TransformComponent.class);
            if (mover == null) {
                continue;
            }

            float mx = mover.getX(), my = mover.getY(), mw = mover.getWidth(), mh = mover.getHeight();

            int tx0 = (int) Math.floor(mx / tw);
            int ty0 = (int) Math.floor(my / th);
            int tx1 = (int) Math.floor((mx + mw - 1f) / tw);
            int ty1 = (int) Math.floor((my + mh - 1f) / th);

            for (int tx = tx0; tx <= tx1; tx++) {
                for (int ty = ty0; ty <= ty1; ty++) {
                    boolean solid = tileMap.isSolid(tx, ty);
                    if (!solid) {
                        continue;
                    }

                    float ox = tx * (float) tw;
                    float oy = ty * (float) th;
                    CollisionRect moverHull = hullOf(e, mover);
                    resolveOverlapHulls(mover, moverHull, ox, oy, (float) tw, (float) th);
                }
            }
        }
    }

    /** Keeps each entity's box fully inside the rectangle (left, top) to (right, bottom). */
    public void resolveScreenBounds(List<Entity> entities, float left, float top, float right, float bottom) {
        for (Entity e : entities) {
            TransformComponent t = e.getComponent(TransformComponent.class);
            if (t == null) {
                continue;
            }

            float x = t.getX();
            float y = t.getY();
            float w = t.getWidth();
            float h = t.getHeight();

            float maxX = right - w;
            float maxY = bottom - h;
            x = Math.max(left, Math.min(x, maxX));
            y = Math.max(top, Math.min(y, maxY));

            t.setX(x);
            t.setY(y);
        }
    }

    /**
     * If two boxes overlap, the moving entity is pushed out of anything that implements {@link Collidable}.
     * Uses each entity's {@link Collidable#getCollisionBounds(Entity)} when present (see default on
     * {@link Collidable}).
     */
    public void resolveEntityCollisions(List<Entity> entities) {
        for (Entity mover : entities) {
            TransformComponent moverT = mover.getComponent(TransformComponent.class);
            if (moverT == null) {
                continue;
            }

            for (Entity other : entities) {
                if (mover == other) {
                    continue;
                }
                if (!(other instanceof Collidable)) {
                    continue;
                }

                TransformComponent otherT = other.getComponent(TransformComponent.class);
                if (otherT == null) {
                    continue;
                }

                CollisionRect moverHull = hullOf(mover, moverT);
                CollisionRect otherHull = hullOf(other, otherT);
                resolveOverlapHulls(moverT, moverHull, otherHull.x, otherHull.y, otherHull.width, otherHull.height);
            }
        }
    }

    static CollisionRect hullOf(Entity e, TransformComponent t) {
        if (e instanceof Collidable c) {
            CollisionRect r = c.getCollisionBounds(e);
            if (r != null) {
                return r;
            }
        }
        return CollisionRect.fromTransform(t);
    }

    /**
     * Resolves overlap between {@code moverHull} and a fixed obstacle rect; applies the position delta
     * to {@code moverT}'s transform (so a smaller hull still moves the whole entity).
     */
    private void resolveOverlapHulls(TransformComponent moverT, CollisionRect moverHull, float ox, float oy, float ow, float oh) {
        float mx = moverHull.x;
        float my = moverHull.y;
        float mw = moverHull.width;
        float mh = moverHull.height;

        float overlapLeft = (mx + mw) - ox;
        float overlapRight = (ox + ow) - mx;
        float overlapTop = (my + mh) - oy;
        float overlapBottom = (oy + oh) - my;

        if (overlapLeft <= 0 || overlapRight <= 0 || overlapTop <= 0 || overlapBottom <= 0) {
            return;
        }

        float minX = Math.min(overlapLeft, overlapRight);
        float minY = Math.min(overlapTop, overlapBottom);

        float newMx = mx;
        float newMy = my;
        if (minX < minY) {
            if (overlapLeft < overlapRight) {
                newMx = ox - mw;
            } else {
                newMx = ox + ow;
            }
        } else {
            if (overlapTop < overlapBottom) {
                newMy = oy - mh;
            } else {
                newMy = oy + oh;
            }
        }

        moverT.setX(moverT.getX() + (newMx - mx));
        moverT.setY(moverT.getY() + (newMy - my));
    }
}
