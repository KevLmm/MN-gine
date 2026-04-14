package assets;

import processing.core.PImage;
import java.util.HashMap;
import java.util.Map;
import processing.core.PApplet;
import entity.TileMap;

public class AssetsManager {

    private Map<String, PImage> sprites = new HashMap<>();
    private PApplet applet;
    private Map<String, TileMap> tilemaps = new HashMap<>();

    public AssetsManager(PApplet applet) {
        this.applet = applet;
    }

    public void loadTileMap(String assetId, String path) {
        TileMap tilemap = new TileMap(10, 10, 32, 32);
        tilemaps.put(assetId, tilemap);
    }

    public void loadSprite(String assetId, String path) {
        PImage sprite = applet.loadImage(path);
        if (sprite == null) {
            String alt = applet.sketchPath(path);
            sprite = applet.loadImage(alt);
        }
        sprites.put(assetId, sprite);
    }

    public PImage getSprite(String assetId) {
        return sprites.get(assetId);
    }

    public TileMap getTilemap(String assetId) {
        return tilemaps.get(assetId);
    }
}
