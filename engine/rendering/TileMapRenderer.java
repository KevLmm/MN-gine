package rendering;
import core.Renderable;
import core.Drawable;
import entity.TileMap;
import processing.core.PImage;

public class TileMapRenderer {

    private Renderable renderable;
    private Drawable drawable;
    private TileMap tileMap;
    private PImage tileMapImage;

    public TileMapRenderer(Renderable renderable, Drawable drawable, TileMap tileMap, PImage tileMapImage) {
        this.renderable = renderable;
        this.drawable = drawable;
        this.tileMap = tileMap;
        this.tileMapImage = tileMapImage;
    }

    /**
     * Draws the map texture once, scaled to the map’s pixel size ({@link TileMap#getPixelWidth()} ×
     * {@link TileMap#getPixelHeight()}). The image stretches to that rectangle (same as one full-screen
     * map sheet, not one copy per tile).
     */
    public void render(TileMap tileMap) {
        TileMap map = tileMap != null ? tileMap : this.tileMap;
        if (tileMapImage == null || map == null) {
            return;
        }
        renderable.drawImage(tileMapImage, 0, 0, map.getPixelWidth(), map.getPixelHeight());
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
