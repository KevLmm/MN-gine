package rendering;

import core.Drawable;
import core.Renderable;
import core.Collidable;

public class CircleDrawable implements Drawable, Collidable {
    private int r, g, b;
    
    public CircleDrawable(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }
    
    @Override
    public void draw(Renderable renderer, float x, float y, float width, float height) {
        if (renderer instanceof ShapeRenderer) {
            ShapeRenderer sr = (ShapeRenderer) renderer;
            sr.drawCircle(x, y, width, height, r, g, b);
        }
    }
}
