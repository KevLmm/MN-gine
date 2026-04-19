package systems;

import core.Interactable;
import entity.CollisionRect;
import entity.Entity;
import entity.TransformComponent;

import java.util.List;

public class InteractionSystem {

    /**
     * When the player presses interact near an entity, find the closest one in range
     * and call {@link Interactable#onInteract()} on it.
     * <p>
     * {@code range} is the max <strong>gap between world-space hitbox edges</strong> (0 when boxes touch
     * or overlap). Uses {@link Collidable#getCollisionBounds(Entity)} when the target is collidable.
     */
    public void tryInteract(Entity interactor, List<Entity> entities, float range) {
        TransformComponent interactorTransform = interactor.getComponent(TransformComponent.class);
        if (interactorTransform == null) {
            return;
        }

        CollisionRect interactorBox = CollisionSystem.hullOf(interactor, interactorTransform);

        Entity closest = null;
        float bestSeparation = Float.MAX_VALUE;

        for (Entity e : entities) {
            if (e == interactor) {
                continue;
            }
            if (!(e instanceof Interactable)) {
                continue;
            }
            TransformComponent t = e.getComponent(TransformComponent.class);
            if (t == null) {
                continue;
            }

            CollisionRect targetBox = CollisionSystem.hullOf(e, t);
            float sep = aabbEdgeSeparation(interactorBox, targetBox);
            if (sep <= range && sep < bestSeparation) {
                bestSeparation = sep;
                closest = e;
            }
        }

        if (closest != null) {
            closest.onInteract();
        }
    }

    static float aabbEdgeSeparation(CollisionRect a, CollisionRect b) {
        float ax = a.x;
        float ay = a.y;
        float aw = a.width;
        float ah = a.height;
        float bx = b.x;
        float by = b.y;
        float bw = b.width;
        float bh = b.height;

        float dx = 0f;
        if (ax + aw <= bx) {
            dx = bx - (ax + aw);
        } else if (bx + bw <= ax) {
            dx = ax - (bx + bw);
        }

        float dy = 0f;
        if (ay + ah <= by) {
            dy = by - (ay + ah);
        } else if (by + bh <= ay) {
            dy = ay - (by + bh);
        }

        if (dx > 0f && dy > 0f) {
            return (float) Math.hypot(dx, dy);
        }
        if (dx > 0f) {
            return dx;
        }
        if (dy > 0f) {
            return dy;
        }
        return 0f;
    }
}
