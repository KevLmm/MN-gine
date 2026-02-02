package rendering;

import core.Drawable;
import core.Renderable;

/**
 * Default drawable: draws the entity as a rectangle. Replace with a custom
 * Drawable (e.g. sprite, image) to use your own visuals.
 */
public class DefaultDrawable implements Drawable {
    @Override
    public void draw(Renderable r, float x, float y, float width, float height) {
        r.drawRect(x, y, width, height);
    }
}
