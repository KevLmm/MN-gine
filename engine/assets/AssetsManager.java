package assets;

import processing.core.PImage;
import processing.core.PConstants;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import processing.core.PApplet;
import entity.TileMap;

public class AssetsManager {

    private Map<String, PImage> sprites = new HashMap<>();
    private PApplet applet;
    private Map<String, TileMap> tilemaps = new HashMap<>();
    /** Source path per tile map id (for future TMX / file loading). */
    private Map<String, String> tilemapPaths = new HashMap<>();

    public AssetsManager(PApplet applet) {
        this.applet = applet;
    }

    /**
     * Creates and registers a tile map. {@code tilesWide} / {@code tilesHigh} are the grid size in tiles;
     * {@code tileWidth} / {@code tileHeight} are each tile’s size in pixels.
     */
    public void initTileMap(String assetId, int tilesWide, int tilesHigh, int tileWidth, int tileHeight) {
        tilemaps.put(assetId, new TileMap(tilesWide, tilesHigh, tileWidth, tileHeight));
    }

    /**
     * Same as {@link #initTileMap}, but also records {@code path} for when map data is loaded from disk.
     * If {@code path} is a raster image (e.g. {@code .png}), it is also loaded as a sprite under {@code assetId}
     * so {@link TileMapRenderer} can draw tiles.
     */
    public void loadTileMap(String assetId, String path, int tilesWide, int tilesHigh, int tileWidth, int tileHeight) {
        tilemapPaths.put(assetId, path);
        initTileMap(assetId, tilesWide, tilesHigh, tileWidth, tileHeight);
        if (path != null && isRasterImagePath(path)) {
            loadSprite(assetId, path);
        }
    }

    private static boolean isRasterImagePath(String path) {
        String p = path.toLowerCase();
        return p.endsWith(".png") || p.endsWith(".jpg") || p.endsWith(".jpeg")
                || p.endsWith(".gif") || p.endsWith(".webp") || p.endsWith(".tga");
    }

    public String getTileMapPath(String assetId) {
        return tilemapPaths.get(assetId);
    }

    public void loadSprite(String assetId, String path) {
        PImage sprite = loadImageAtPath(path);
        sprites.put(assetId, sprite);
    }

    /**
     * Loads a raster from the sketch {@code data} folder. Accepts either {@code "file.png"} or
     * legacy {@code "data/file.png"}; the latter breaks Processing’s usual rule (files live under
     * {@code data/} but {@link PApplet#loadImage(String)} expects the name relative to that folder).
     * <p>
     * After {@link PApplet#loadImage(String)}, falls back to {@link ImageIO} +
     * a manual {@link BufferedImage} → {@link PImage} copy, which works when the native loader fails.
     */
    private PImage loadImageAtPath(String path) {
        if (path == null) {
            return null;
        }
        String base = baseFileName(path);
        for (Path file : candidateDataFiles(base)) {
            PImage img = tryLoadPImageFromFile(file);
            if (isUsableImage(img)) {
                return img;
            }
        }
        try {
            Path dataPathFile = Paths.get(applet.dataPath(base));
            PImage img = tryLoadPImageFromFile(dataPathFile);
            if (isUsableImage(img)) {
                return img;
            }
        } catch (Exception ignored) {
            // fall through
        }
        PImage img = tryLoadPImageFromPathString(applet.dataPath(base));
        if (isUsableImage(img)) {
            return img;
        }
        img = tryLoadPImageFromPathString(path);
        if (isUsableImage(img)) {
            return img;
        }
        img = tryLoadPImageFromPathString(applet.sketchPath(path));
        if (isUsableImage(img)) {
            return img;
        }
        if (!path.equals(base)) {
            img = tryLoadPImageFromPathString(applet.sketchPath(base));
            if (isUsableImage(img)) {
                return img;
            }
        }
        img = tryLoadFromClasspath(base);
        if (isUsableImage(img)) {
            return img;
        }
        return null;
    }

    private PImage tryLoadPImageFromPathString(String pathStr) {
        if (pathStr == null) {
            return null;
        }
        try {
            Path p = Paths.get(pathStr);
            if (Files.isRegularFile(p)) {
                return tryLoadPImageFromFile(p);
            }
        } catch (Exception ignored) {
            // fall through
        }
        try {
            PImage img = applet.loadImage(pathStr);
            if (isUsableImage(img)) {
                return img;
            }
        } catch (RuntimeException ignored) {
            // fall through
        }
        return null;
    }

    private PImage tryLoadPImageFromFile(Path file) {
        if (!Files.isRegularFile(file)) {
            return null;
        }
        String abs = file.toAbsolutePath().toString();
        try {
            PImage img = applet.loadImage(abs);
            if (isUsableImage(img)) {
                return img;
            }
        } catch (RuntimeException ignored) {
            // try ImageIO
        }
        try (InputStream in = Files.newInputStream(file)) {
            BufferedImage bi = ImageIO.read(in);
            if (bi == null) {
                return null;
            }
            return bufferedImageToPImage(bi);
        } catch (IOException ignored) {
            return null;
        }
    }

    private PImage tryLoadFromClasspath(String baseFileName) {
        String[] locations = { "data/" + baseFileName, "/data/" + baseFileName };
        ClassLoader cl = AssetsManager.class.getClassLoader();
        for (String loc : locations) {
            try (InputStream in = cl.getResourceAsStream(loc)) {
                if (in == null) {
                    continue;
                }
                BufferedImage bi = ImageIO.read(in);
                if (bi != null) {
                    return bufferedImageToPImage(bi);
                }
            } catch (IOException ignored) {
                // try next
            }
        }
        return null;
    }

    private static PImage bufferedImageToPImage(BufferedImage bi) {
        int w = bi.getWidth();
        int h = bi.getHeight();
        BufferedImage argb = bi;
        int type = bi.getType();
        if (type != BufferedImage.TYPE_INT_ARGB && type != BufferedImage.TYPE_INT_ARGB_PRE
                && type != BufferedImage.TYPE_INT_RGB) {
            argb = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = argb.createGraphics();
            g.drawImage(bi, 0, 0, null);
            g.dispose();
        }
        PImage out = new PImage(w, h, PConstants.ARGB);
        out.loadPixels();
        argb.getRGB(0, 0, w, h, out.pixels, 0, w);
        out.updatePixels();
        return out;
    }

    /**
     * When the JVM cwd is {@code bin/} (common for IDE runs), Processing’s {@code dataPath} often points at
     * {@code bin/data}, while assets live in the project {@code data} folder. Walk parents of {@code user.dir}
     * and try each {@code &lt;ancestor&gt;/data/&lt;file&gt;}.
     */
    private static List<Path> candidateDataFiles(String baseFileName) {
        List<Path> out = new ArrayList<>();
        Path start = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        int depth = 0;
        for (Path p = start; p != null && depth < 10; p = p.getParent(), depth++) {
            out.add(p.resolve("data").resolve(baseFileName));
        }
        return out;
    }

    private static boolean isUsableImage(PImage img) {
        return img != null && img.width > 0 && img.height > 0;
    }

    private static String baseFileName(String path) {
        int i = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return i < 0 ? path : path.substring(i + 1);
    }

    public PImage getSprite(String assetId) {
        return sprites.get(assetId);
    }

    public TileMap getTilemap(String assetId) {
        return tilemaps.get(assetId);
    }
}
