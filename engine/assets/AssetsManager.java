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

    /**
     * Loads a raster tile map: grid size is {@code floor(imageW / tileWidth)} ×
     * {@code floor(imageH / tileHeight)} from the loaded image.
     */
    public void loadTileMapFromRasterGrid(String assetId, String path, int tileWidth, int tileHeight) {
        loadSprite(assetId, path);
        PImage img = getSprite(assetId);
        if (!isUsableImage(img)) {
            tilemapPaths.put(assetId, path);
            initTileMap(assetId, 1, 1, tileWidth, tileHeight);
            return;
        }
        int pw = img.pixelWidth > 0 ? img.pixelWidth : img.width;
        int ph = img.pixelHeight > 0 ? img.pixelHeight : img.height;
        int tilesWide = Math.max(1, pw / tileWidth);
        int tilesHigh = Math.max(1, ph / tileHeight);
        tilemapPaths.put(assetId, path);
        initTileMap(assetId, tilesWide, tilesHigh, tileWidth, tileHeight);
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
        if (!isUsableImage(sprite)) {
            Path expected = Paths.get(System.getProperty("user.dir", ".")).resolve("data").resolve(baseFileName(path)).normalize();
            System.err.println("[AssetsManager] FAILED to load \"" + assetId + "\" from \"" + path + "\".");
            System.err.println("  expects file at: " + expected.toAbsolutePath() + " exists=" + Files.isRegularFile(expected));
            System.err.println("  cwd=" + System.getProperty("user.dir"));
            if (applet != null) {
                try {
                    System.err.println("  sketchPath=" + applet.sketchPath(""));
                } catch (Throwable t) {
                    System.err.println("  sketchPath=(unavailable)");
                }
            }
            System.err.println("  Rebuild/run from MN-gine root, or -Dmn.gine.data=<full path to data folder>");
        }
        sprites.put(assetId, sprite);
    }

    /** Register an in-memory image (e.g. pre-cut animation frames). */
    public void putSprite(String assetId, PImage sprite) {
        if (assetId != null) {
            sprites.put(assetId, sprite);
        }
    }

    /**
     * Copies a pixel rectangle into an ARGB image created with {@link PApplet#createImage(int, int, int)}.
     * Plain {@code new PImage} slices often fail to show in {@code image()} on some Processing setups.
     */
    public PImage copyImageRegion(PImage src, int sx, int sy, int sw, int sh) {
        if (src == null || sw <= 0 || sh <= 0 || !isUsableImage(src) || applet == null) {
            return null;
        }
        src.loadPixels();
        int pw = src.pixelWidth;
        int ph = src.pixelHeight;
        if (sx < 0 || sy < 0 || sx + sw > pw || sy + sh > ph) {
            return null;
        }
        PImage out = applet.createImage(sw, sh, PConstants.ARGB);
        out.loadPixels();
        for (int j = 0; j < sh; j++) {
            System.arraycopy(src.pixels, (sy + j) * pw + sx, out.pixels, j * sw, sw);
        }
        out.updatePixels();
        return out;
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
        // Optional override: -Dmn.gine.data=C:\full\path\to\MN-gine\data
        String overrideData = System.getProperty("mn.gine.data");
        if (overrideData != null && !overrideData.isEmpty()) {
            PImage imgO = tryLoadPImageFromFile(Paths.get(overrideData).resolve(base));
            if (isUsableImage(imgO)) {
                return imgO;
            }
        }
        // Relative path under data/ (keep subdirs if path was like "sub/tile.png")
        String underData = path.replace('\\', '/');
        if (!underData.startsWith("data/") && !underData.startsWith("/")) {
            underData = "data/" + base;
        }

        PImage img = null;
        if (applet != null) {
            // Processing sketch root — often correct when launch working directory = project (MN-gine)
            img = tryLoadPImageFromPathString(applet.sketchPath(underData));
            if (isUsableImage(img)) {
                return img;
            }
            img = tryLoadPImageFromPathString(applet.sketchPath(path.replace('\\', '/')));
            if (isUsableImage(img)) {
                return img;
            }
            // dataPath("foo.png") → <sketch>/data/foo.png
            try {
                img = tryLoadPImageFromPathString(applet.dataPath(base));
                if (isUsableImage(img)) {
                    return img;
                }
            } catch (Throwable ignored) {
                // fall through
            }
        }

        // Explicit project data/ next to JVM working directory (Eclipse default: MN-gine)
        img = tryLoadPImageFromPathString(
                Paths.get(System.getProperty("user.dir", ".")).resolve("data").resolve(base).toString());
        if (isUsableImage(img)) {
            return img;
        }

        for (Path file : candidateDataFiles(base)) {
            img = tryLoadPImageFromFile(file);
            if (isUsableImage(img)) {
                return img;
            }
        }
        try {
            if (applet != null) {
                Path dataPathFile = Paths.get(applet.dataPath(base));
                img = tryLoadPImageFromFile(dataPathFile);
                if (isUsableImage(img)) {
                    return img;
                }
            }
        } catch (Exception ignored) {
            // fall through
        }
        if (applet != null) {
            img = tryLoadPImageFromPathString(applet.dataPath(base));
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
        } else {
            img = tryLoadPImageFromPathString(path);
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
            Path p = Paths.get(pathStr).normalize();
            if (Files.isRegularFile(p)) {
                return tryLoadPImageFromFile(p);
            }
        } catch (Exception ignored) {
            // fall through
        }
        if (applet != null) {
            try {
                PImage img = applet.loadImage(pathStr);
                touchPixels(img);
                if (isUsableImage(img)) {
                    return img;
                }
            } catch (RuntimeException ignored) {
                // fall through
            }
        }
        return null;
    }

    private PImage tryLoadPImageFromFile(Path file) {
        if (!Files.isRegularFile(file)) {
            return null;
        }
        // Prefer ImageIO: some PNGs decode here when Processing's native loader returns width/height 0.
        try (InputStream in = Files.newInputStream(file)) {
            BufferedImage bi = ImageIO.read(in);
            if (bi != null) {
                PImage out = bufferedImageToPImage(bi);
                touchPixels(out);
                if (isUsableImage(out)) {
                    return out;
                }
            }
        } catch (IOException ignored) {
            // fall through
        }
        String abs = file.toAbsolutePath().toString();
        if (applet != null) {
            try {
                PImage img = applet.loadImage(abs);
                touchPixels(img);
                if (isUsableImage(img)) {
                    return img;
                }
            } catch (RuntimeException ignored) {
                // empty
            }
        }
        return null;
    }

    /** Ensures pixel buffer exists so {@link PImage#width}/{@link PImage#pixelWidth} are meaningful. */
    private static void touchPixels(PImage img) {
        if (img == null) {
            return;
        }
        try {
            img.loadPixels();
        } catch (Throwable ignored) {
            // ignore
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
        // ImageIO path: some runtimes leave width/height at 0 until explicitly aligned with pixel buffer.
        out.width = w;
        out.height = h;
        out.pixelWidth = w;
        out.pixelHeight = h;
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
        if (img == null) {
            return false;
        }
        int w = img.pixelWidth > 0 ? img.pixelWidth : img.width;
        int h = img.pixelHeight > 0 ? img.pixelHeight : img.height;
        return w > 0 && h > 0;
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
