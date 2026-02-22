package systems;

import core.Interactable;
import entity.Entity;
import entity.TransformComponent;

import java.util.List;

public class InteractionSystem {

    /**
     * When the player presses interact near an entity, find the closest one in range
     * and call onInteract on it.
     */
    public void tryInteract(Entity interactor, List<Entity> entities, float range) {
        TransformComponent interactorTransform = interactor.getComponent(TransformComponent.class);
        if (interactorTransform == null) return;

        float px = interactorTransform.getX() + interactorTransform.getWidth() / 2f;
        float py = interactorTransform.getY() + interactorTransform.getHeight() / 2f;

        Entity closest = null;
        float closestDistSq = range * range;

        for (Entity e : entities) {
            if (e == interactor) continue;
            TransformComponent t = e.getComponent(TransformComponent.class);
            if (t == null) continue;

            float ex = t.getX() + t.getWidth() / 2f;
            float ey = t.getY() + t.getHeight() / 2f;
            float dx = ex - px;
            float dy = ey - py;
            float distSq = dx * dx + dy * dy;

            if (distSq <= closestDistSq && e instanceof Interactable) {
                closestDistSq = distSq;
                closest = e;
            }
        }

        if (closest != null) {
            closest.onInteract();
        }
    }
}
