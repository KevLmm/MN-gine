package systems;

import core.Collidable;
import entity.CollisionRect;
import entity.Entity;
import entity.SpikeBallEntity;
import entity.TileMap;
import entity.TransformComponent;

import java.util.List;

/**
 * Post-movement collision passes: entity-vs-entity, tile grid, then clamping to world bounds.
 * Call order is flexible; {@link core.Engine} runs entity collisions, tiles, then screen bounds.
 */
public class CollisionSystem {

    /**
     * For each entity, finds overlapping solid tiles and resolves penetration by the shortest axis.
     * Which tiles count as solid comes from {@link entity.TileMap} flags in the inner loop.
     * <p>
     * Typical sequence in {@link core.Engine}: {@code resolveEntityCollisions} → this → {@code resolveScreenBounds}.
     */
    public void resolveTileMapCollisions(List<Entity> entities, TileMap tileMap) {
        if (tileMap == null) {
            return;
        }

        int tw = tileMap.getTileWidth();
        int th = tileMap.getTileHeight();

        for (Entity e : entities) {
            if (e instanceof SpikeBallEntity) {
                continue;
            }
            TransformComponent mover = e.getComponent(TransformComponent.class);
            if (mover == null) {
                continue;
            }

            CollisionRect moverHull = hullOf(e, mover);
            float mx = moverHull.x;
            float my = moverHull.y;
            float mw = moverHull.width;
            float mh = moverHull.height;

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
                    moverHull = hullOf(e, mover);
                    resolveOverlapHulls(mover, moverHull, ox, oy, (float) tw, (float) th);
                }
            }
        }
    }

    /** Keeps each entity's box fully inside the rectangle (left, top) to (right, bottom). */
    public void resolveScreenBounds(List<Entity> entities, float left, float top, float right, float bottom) {
        for (Entity e : entities) {
            if (e instanceof SpikeBallEntity) {
                continue;
            }
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
            if (mover instanceof SpikeBallEntity) {
                continue;
            }
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

    public static CollisionRect hullOf(Entity e, TransformComponent t) {
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
