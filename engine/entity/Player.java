package entity;

import core.Collidable;
import core.Drawable;
import core.WalkAnimatedDrawable;
import rendering.DefaultDrawable;
import input.InputListener;
import input.InputManager;

/**
 * The player entity. Uses Transform + Appearance so it can be drawn and moved.
 * To use custom visuals, get AppearanceComponent and setDrawable(yourDrawable).
 * Optional {@link #setPhysicsHull(float, float)} keeps a large transform for drawing while using
 * a smaller box for {@link Collidable} physics (spawn, tiles, entity overlap).
 */
public class Player extends Entity implements InputListener, Collidable {
    private InputManager input;
    public static final float DEFAULT_WIDTH = 100;
    public static final float DEFAULT_HEIGHT = 120;
    private float x, y;
    /** When both positive, {@link #getCollisionBounds} uses this rect centered in the transform. */
    private float physicsHullW = -1f;
    private float physicsHullH = -1f;

    public Player(float x, float y) {
        this(x, y, new DefaultDrawable());
    }

    /** Create a player with custom drawable so others can upload their own visuals and behaviors. */
    public Player(float x, float y, Drawable drawable) {
        super(x, y);
        addComponent(new TransformComponent(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT));
        addComponent(new AppearanceComponent(drawable));
        getTransform().setSpeed(400, 400);
        this.x = x;
        this.y = y;
        
    }

   

    public TransformComponent getTransform() {
        return getComponent(TransformComponent.class);
    }

    /** Clears {@link #setPhysicsHull(float, float)} so collision matches the full transform. */
    public void clearPhysicsHull() {
        physicsHullW = -1f;
        physicsHullH = -1f;
    }

    /**
     * World-space hitbox size centered in the sprite transform. Use for large sprites in tight tile
     * maps so corridors stay walkable.
     */
    public void setPhysicsHull(float width, float height) {
        if (width <= 0f || height <= 0f || Float.isNaN(width) || Float.isNaN(height)) {
            clearPhysicsHull();
            return;
        }
        physicsHullW = width;
        physicsHullH = height;
    }

    @Override
    public CollisionRect getCollisionBounds(Entity self) {
        TransformComponent t = self.getComponent(TransformComponent.class);
        if (t == null) {
            return null;
        }
        if (physicsHullW <= 0f || physicsHullH <= 0f) {
            return CollisionRect.fromTransform(t);
        }
        float cw = physicsHullW;
        float ch = physicsHullH;
        float hx = t.getX() + (t.getWidth() - cw) * 0.5f;
        float hy = t.getY() + (t.getHeight() - ch) * 0.5f;
        return new CollisionRect(hx, hy, cw, ch);
    }

    public AppearanceComponent getAppearance() {
        return getComponent(AppearanceComponent.class);
    }

    @Override
    public boolean shouldDrawAfterOthers() {
        return true;
    }

    public void update(float dt) {
        float dx = 0, dy = 0;
        if (input != null) {
            if (input.isActionActive("MOVE_LEFT")) dx -= 1;
            if (input.isActionActive("MOVE_RIGHT")) dx += 1;
            if (input.isActionActive("MOVE_UP")) dy -= 1;
            if (input.isActionActive("MOVE_DOWN")) dy += 1;
        }

        if (dx != 0 || dy != 0) {
            float inv = (float)(1.0 / Math.sqrt(2));
            dx *= inv; dy *= inv;
        }
        TransformComponent t = getTransform();

        x = t.getX() + dx * t.getSpeedX() * dt;
        y = t.getY() + dy * t.getSpeedY() * dt;
        t.setX(x);
        t.setY(y);

        Drawable vis = getAppearance() != null ? getAppearance().getDrawable() : null;
        if (vis instanceof WalkAnimatedDrawable) {
            boolean moving = input != null
                    && (input.isActionActive("MOVE_LEFT") || input.isActionActive("MOVE_RIGHT")
                    || input.isActionActive("MOVE_UP") || input.isActionActive("MOVE_DOWN"));
            ((WalkAnimatedDrawable) vis).updateAnimation(moving, dt);
        }
    }

    /** Keep internal x,y aligned when teleporting via {@link TransformComponent} only. */
    public void syncBodyFromTransform() {
        TransformComponent t = getTransform();
        if (t != null) {
            x = t.getX();
            y = t.getY();
        }
    }

    public void setInput(InputManager input) {
        this.input = input;
        
    }

    @Override
    public void onKeyPressed(char key, int keyCode) {
        input.onKeyPressed(key, keyCode);
        
    }
    @Override
    public void onKeyReleased(char key, int keyCode) {
        input.onKeyReleased(key, keyCode);
    }
    @Override
    public void onMousePressed(int button, int x, int y) {
        input.onMousePressed(button, x, y);
    }
    @Override
    public void onMouseReleased(int button, int x, int y) {
        input.onMouseReleased(button, x, y);
    }
    @Override
    public void onMouseMoved(int x, int y) {
        input.onMouseMoved(x, y);
    }
    @Override
    public void onMouseDragged(int x, int y) {
        input.onMouseDragged(x, y);
    }
    @Override
    public void onMouseWheel(int delta) {
        input.onMouseWheel(delta);
    }
    @Override
    public void onTouchStarted(int x, int y) {
        input.onTouchStarted(x, y);
    }
    @Override
    public void onTouchMoved(int x, int y) {
        input.onTouchMoved(x, y);
    }
    @Override
    public void onTouchEnded(int x, int y) {
        input.onTouchEnded(x, y);
    }
}
