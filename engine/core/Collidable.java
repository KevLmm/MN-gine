package core;

import entity.CollisionRect;
import entity.Entity;
import entity.TransformComponent;

/**
 * Solid objects in {@link systems.CollisionSystem}. Default hitbox matches {@link TransformComponent};
 * override {@link #getCollisionBounds(Entity)} for a tighter or offset hull vs. the drawn size.
 */
public interface Collidable {

    default CollisionRect getCollisionBounds(Entity self) {
        TransformComponent t = self.getComponent(TransformComponent.class);
        if (t == null) {
            return null;
        }
        return CollisionRect.fromTransform(t);
    }
}
