package rendering;
import core.Renderable;
import core.Drawable;
import entity.TileMap;

public class TileMapRenderer {

    private Renderable renderable;
    private Drawable drawable;
    private TileMap tileMap;

    public TileMapRenderer(Renderable renderable, Drawable drawable, TileMap tileMap) {
        this.renderable = renderable;
        this.drawable = drawable;
        this.tileMap = tileMap;
    }
    
    public void render(TileMap tileMap) {
        for (int i = 0; i < tileMap.getWidth(); i++) {
            for (int j = 0; j < tileMap.getHeight(); j++) {
                drawable.draw(renderable, i * tileMap.getTileWidth(), j * tileMap.getTileHeight(), tileMap.getTileWidth(), tileMap.getTileHeight());
            }
        }
    }
    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }
    public Drawable getDrawable() {
        return drawable;
    }
    public TileMap getTileMap() {
        return tileMap;
    }
}
