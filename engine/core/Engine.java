package core;

import entity.AppearanceComponent;
import entity.Entity;
import entity.TransformComponent;
import systems.CollisionSystem;
import systems.InteractionSystem;

import java.util.ArrayList;
import java.util.List;

public class Engine {

    private Renderable renderable;
    private final List<Entity> entities = new ArrayList<>();
    private final InteractionSystem interactionSystem = new InteractionSystem();
    private final CollisionSystem collisionSystem = new CollisionSystem();
    private static final float INTERACT_RANGE = 60f;

    public void registerEntity(Entity entity) {
        entities.add(entity);
    }

    public void update(float dt, float worldWidth, float worldHeight) {
        for (Entity e : entities) {
            e.update(dt);
        }
        collisionSystem.resolveScreenBounds(entities, 0, 0, worldWidth, worldHeight);
    }

    public void render() {
        if (renderable == null) return;
        renderable.clear();
        for (Entity e : entities) {
            TransformComponent t = e.getComponent(TransformComponent.class);
            AppearanceComponent a = e.getComponent(AppearanceComponent.class);
            if (t != null && a != null && a.getDrawable() != null) {
                a.getDrawable().draw(renderable, t.getX(), t.getY(), t.getWidth(), t.getHeight());
            }
        }
    }

    public void setRenderer(Renderable renderable) {
        this.renderable = renderable;
    }

    public List<Entity> getEntities() {
        return new ArrayList<>(entities);
    }

    /** Call when the player presses the interact key. Finds nearby interactables and triggers the closest one. */
    public void tryInteract(Entity interactor) {
        interactionSystem.tryInteract(interactor, entities, INTERACT_RANGE);
    }
}
