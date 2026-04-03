package rendering;
import core.Drawable;
import core.Renderable;

public class SpriteDrawable implements Drawable {
    private String assetId;


    public SpriteDrawable(String assetId) {
        this.assetId = assetId;

    }

    @Override 
    public void draw(Renderable r, float x, float y, float width, float height) {
        r.drawSprite(assetId, x, y, width, height, 0);
    }
}
