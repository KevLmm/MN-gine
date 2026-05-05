package core;

/**
 * Drawables that advance a walk cycle from {@link entity.Player} movement input.
 */
public interface WalkAnimatedDrawable extends Drawable {

    void updateAnimation(boolean moving, float dt);
}
