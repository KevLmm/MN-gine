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
     * Draws the map texture once at world-space size ({@link TileMap#getPixelWidth()} ×
     * {@link TileMap#getPixelHeight()}). The {@link core.Renderable} / camera apply zoom so a small
     * world can fill the sketch (see {@link core.Camera2D#setZoom(float)}).
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

    public void setTileMap(TileMap tileMap) {
        this.tileMap = tileMap;
    }

    public void setTileMapImage(PImage tileMapImage) {
        this.tileMapImage = tileMapImage;
    }
}
