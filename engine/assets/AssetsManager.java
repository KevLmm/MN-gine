package assets;

import processing.core.PImage;
import java.util.HashMap;
import java.util.Map;
import processing.core.PApplet;

public class AssetsManager {

    private Map<String, PImage> sprites = new HashMap<>();
    private PApplet applet;

    public AssetsManager(PApplet applet) {
        this.applet = applet;
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
}
