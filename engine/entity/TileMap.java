package entity;
import processing.core.PImage;

public class TileMap {

    private int width, height;
    private Tile[][] tiles;
    private boolean[][] solid;
    private int tileWidth, tileHeight;
    private PImage tileMapImage;



    public class Tile {
        public Tile(int x, int y, int tileWidth, int tileHeight) {
            this.x = x;
            this.y = y;
            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;
        }
        private int x, y;
        private int tileWidth, tileHeight;
        private int tileId;
        private int tileType;
        private int tileColor;
        private int tileTexture;
        private int tileTextureX;
        private int tileTextureY;
        private int tileTextureWidth;
        private int tileTextureHeight;
        private int tileTextureIndex;
    }  

    public TileMap(int width, int height, int tileWidth, int tileHeight) {
        this.width = width;
        this.height = height;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.tiles = new Tile[width][height];
        this.solid = new boolean[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                tiles[i][j] = new Tile(i * tileWidth, j * tileHeight, tileWidth, tileHeight);
                solid[i][j] = false;
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    /** Total map size in pixels (tiles × tile size). */
    public int getPixelWidth() {
        return width * tileWidth;
    }

    /** Total map size in pixels (tiles × tile size). */
    public int getPixelHeight() {
        return height * tileHeight;
    }

    public boolean isSolid(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return true;
        return solid[x][y];
    }

    public void setSolid(int x, int y, boolean isSolid) {
        if (x < 0 || x >= width || y < 0 || y >= height) return;
        solid[x][y] = isSolid;
    }

    public void setSolidRect(int x0, int y0, int x1, int y1, boolean isSolid) {
        int minX = Math.max(0, Math.min(x0, x1));
        int maxX = Math.min(width - 1, Math.max(x0, x1));
        int minY = Math.max(0, Math.min(y0, y1));
        int maxY = Math.min(height - 1, Math.max(y0, y1));
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                solid[x][y] = isSolid;
            }
        }
    }
}
