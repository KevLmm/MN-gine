package entity;

public class TileMap {

    private int width, height;
    private Tile[][] tiles;
    private int tileWidth, tileHeight;
    
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
        this.tiles = new Tile[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                tiles[i][j] = new Tile(i * tileWidth, j * tileHeight, tileWidth, tileHeight);
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

}
