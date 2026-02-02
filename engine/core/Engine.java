package core;

import entity.AppearanceComponent;
import entity.Entity;
import entity.TransformComponent;

import java.util.ArrayList;
import java.util.List;

public class Engine {

    private Renderable renderable;
    private final List<Entity> entities = new ArrayList<>();

    public void registerEntity(Entity entity) {
        entities.add(entity);
    }

    public void update(float dt) {
        // Systems (e.g. MovementSystem) can run here over entities
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
}
