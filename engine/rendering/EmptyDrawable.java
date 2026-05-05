package rendering;

import core.Drawable;
import core.Renderable;

/** Hitbox / interactable with no visible art (dungeon uses tile graphics). */
public final class EmptyDrawable implements Drawable {
    @Override
    public void draw(Renderable renderer, float x, float y, float width, float height) {
    }
}
