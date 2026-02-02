package entity;

import core.Drawable;
import rendering.DefaultDrawable;

/**
 * The player entity. Uses Transform + Appearance so it can be drawn and moved.
 * To use custom visuals, get AppearanceComponent and setDrawable(yourDrawable).
 */
public class Player extends Entity {

    public static final float DEFAULT_WIDTH = 40;
    public static final float DEFAULT_HEIGHT = 60;

    public Player(float x, float y) {
        this(x, y, new DefaultDrawable());
    }

    /** Create a player with custom drawable so others can "upload their own stuff". */
    public Player(float x, float y, Drawable drawable) {
        addComponent(new TransformComponent(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT));
        addComponent(new AppearanceComponent(drawable));
    }

    public TransformComponent getTransform() {
        return getComponent(TransformComponent.class);
    }

    public AppearanceComponent getAppearance() {
        return getComponent(AppearanceComponent.class);
    }
}
