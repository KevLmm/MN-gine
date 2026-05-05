package systems;

import core.Renderable;
import entity.Entity;
import entity.TransformComponent;
import entity.AppearanceComponent;
import rendering.TileMapRenderer;

import java.util.List;
import java.util.Comparator;
import java.util.ArrayList;
/**
 * Draw order: clear -> tile map -> entities. 
 */


public class RenderSystem {

    public void render(Renderable renderable, TileMapRenderer tileMapRenderer, List<Entity> entities) {
        if (renderable == null) {
            return;
        }

        renderable.clear();

        if (tileMapRenderer != null) {
            tileMapRenderer.render(tileMapRenderer.getTileMap());
        }

        List<Entity> sortedEntities = new ArrayList<>(entities);
        sortedEntities.sort(Comparator.comparingDouble(e -> {
            TransformComponent t = e.getComponent(TransformComponent.class);
            return t == null ? 0 : t.getY() + t.getHeight();
        }));

        for (Entity e : sortedEntities) {
            if (e.shouldDrawAfterOthers()) {
                continue;
            }
            drawEntity(renderable, e);
        }
        for (Entity e : sortedEntities) {
            if (e.shouldDrawAfterOthers()) {
                drawEntity(renderable, e);
            }
        }
    }

    private static void drawEntity(Renderable renderable, Entity e) {
        TransformComponent t = e.getComponent(TransformComponent.class);
        AppearanceComponent a = e.getComponent(AppearanceComponent.class);
        if (t != null && a != null && a.getDrawable() != null) {
            a.getDrawable().draw(renderable, t.getX(), t.getY(), t.getWidth(), t.getHeight());
        }
    }

}
