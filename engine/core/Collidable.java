package core;

import entity.CollisionRect;
import entity.Entity;
import entity.TransformComponent;

/**
 * Collision volumes used by {@link systems.CollisionSystem}. The default hull matches the
 * {@link TransformComponent}; overrides can shrink or shift the box relative to the sprite.
 */
public interface Collidable {

    default CollisionRect getCollisionBounds(Entity self) {
        TransformComponent t = self.getComponent(TransformComponent.class);
        if (t == null) {
            return null;
        }
        return CollisionRect.fromTransform(t);
    }

    /**
     * Hit area for {@link systems.InteractionSystem} only. Defaults to {@link #getCollisionBounds(Entity)}
     * so behavior matches physics unless a type needs a larger or separate interact region.
     */
    default CollisionRect getInteractionBounds(Entity self) {
        return getCollisionBounds(self);
    }
}
