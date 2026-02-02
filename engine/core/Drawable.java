package core;

/**
 * Defines how an entity is drawn. Implement this to provide custom visuals
 * (e.g. sprites, images, shapes) so others can "upload their own stuff".
 */
public interface Drawable {
    void draw(Renderable r, float x, float y, float width, float height);
}
