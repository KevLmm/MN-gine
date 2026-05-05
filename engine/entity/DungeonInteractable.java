package entity;

import core.Collidable;
import core.Engine;
import rendering.EmptyDrawable;

/**
 * Invisible trigger over a 2×2 tile area for gates, chests, and NPC dialogue. Gates remove solids in
 * a configured rectangle once, then unregister; chests grant gold through {@link DungeonMessageSink}.
 */
public final class DungeonInteractable extends Entity implements Collidable {

    public enum Kind {
        GATE,
        CHEST,
        NPC
    }

    private static final float HULL_SCALE = 0.25f;

    private final Kind kind;
    private final TileMap map;
    private final DungeonMessageSink sink;
    private final Engine engine;
    private final int gateMinC;
    private final int gateMinR;
    private final int gateMaxC;
    private final int gateMaxR;
    private boolean gateOpened;

    public DungeonInteractable(
            Engine engine,
            float x,
            float y,
            float w,
            float h,
            Kind kind,
            TileMap map,
            DungeonMessageSink sink,
            int gateMinC,
            int gateMinR,
            int gateMaxC,
            int gateMaxR
    ) {
        super(x, y);
        this.engine = engine;
        this.kind = kind;
        this.map = map;
        this.sink = sink;
        this.gateMinC = gateMinC;
        this.gateMinR = gateMinR;
        this.gateMaxC = gateMaxC;
        this.gateMaxR = gateMaxR;
        addComponent(new TransformComponent(x, y, w, h));
        addComponent(new AppearanceComponent(new EmptyDrawable()));
    }

    @Override
    public CollisionRect getCollisionBounds(Entity self) {
        TransformComponent t = self.getComponent(TransformComponent.class);
        if (t == null) {
            return null;
        }
        float cw = t.getWidth() * HULL_SCALE;
        float ch = t.getHeight() * HULL_SCALE;
        float hx = t.getX() + (t.getWidth() - cw) * 0.5f;
        float hy = t.getY() + (t.getHeight() - ch) * 0.5f;
        return new CollisionRect(hx, hy, cw, ch);
    }

    /** Full transform footprint for interaction queries; physics remains the scaled {@link #HULL_SCALE} hull. */
    @Override
    public CollisionRect getInteractionBounds(Entity self) {
        TransformComponent t = self.getComponent(TransformComponent.class);
        if (t == null) {
            return null;
        }
        return CollisionRect.fromTransform(t);
    }

    @Override
    public void onInteract() {
        switch (kind) {
            case NPC -> sink.showModal("Hello, traveler. Welcome to the dungeon.");
            case CHEST -> {
                int gold = 1 + (int) (Math.random() * 100);
                sink.awardGold(gold);
                sink.showModal("Chest opened: " + gold + " gold.");
            }
            case GATE -> {
                sink.showModal("The gate opens.");
                if (!gateOpened && map != null) {
                    gateOpened = true;
                    for (int ty = gateMinR; ty <= gateMaxR; ty++) {
                        for (int tx = gateMinC; tx <= gateMaxC; tx++) {
                            map.setSolid(tx, ty, false);
                        }
                    }
                    engine.removeEntity(this);
                }
            }
        }
    }
}
