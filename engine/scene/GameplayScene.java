package scene;

import processing.core.PApplet;
import processing.core.PImage;
import core.Engine;
import entity.Player;
import entity.Entity;
import input.*;
import music.SoundFX;
import entity.TileMap;
import entity.TransformComponent;
import assets.AssetsManager;
import rendering.TileMapRenderer;


public class GameplayScene extends Scene {

    private enum SpawnEdge { LEFT, RIGHT, TOP, BOTTOM }

    private static final int INTERACT_KEY = 69; 

    private static final int TEST_TO_BRIDGE_ROW = 8;
    private static final int BRIDGE_TO_TEST_ROW = 8;

    private static final float EDGE_EPSILON = 2f;

    private final Engine engine;
    private final Player player;
    private final InputManager input;
    private final SoundFX walkingSound;
    private final AssetsManager assets;
    private final TileMapRenderer tileMapRenderer;
    private final SceneTransition transition;

    private PApplet p;
    private boolean walkingSoundActive = false;
    private String currMapId = "test";

    public GameplayScene(Engine engine, Player player, InputManager input, 
        SoundFX walkingSound, AssetsManager assets, TileMapRenderer tileMapRenderer, 
        SceneTransition transition) {

        this.engine = engine;
        this.player = player;
        this.input = input;
        this.walkingSound = walkingSound;
        this.assets = assets;
        this.tileMapRenderer = tileMapRenderer;
        this.transition = transition;
    }

    @Override
    public void enter(PApplet p) {
        this.p = p;
        applyMapNow(currMapId); //ensure renderer and map id are in sync

    }

    @Override
    public void exit() {
        if (walkingSoundActive) {
            walkingSound.stop();
            walkingSoundActive = false;
        }
    }

    @Override 
    public void update(float dt) {
        if (p == null) return;
        engine.update(dt, p.width, p.height);
        updateWalkingSound();
        checkEdgeMapTransition();
    }

    @Override
    public void render() {
        engine.render();
    }

    @Override
    public void keyPressed(char key, int keyCode) {
        if (keyCode == INTERACT_KEY) {
            engine.tryInteract(player);
        }
        for (Entity e : engine.getEntities()) {
            if (e instanceof InputListener) {
                ((InputListener) e).onKeyPressed(key, keyCode);
            }
        }
    }

    @Override 
    public void keyReleased(char key, int keyCode) {
        for (Entity e : engine.getEntities()) {
            if (e instanceof InputListener) {
                ((InputListener) e).onKeyReleased(key, keyCode);
            }
        }
    }

    private void checkEdgeMapTransition() {
        if (transition.isBlocking()) return;

        TileMap map = tileMapRenderer.getTileMap();
        TransformComponent t = player.getTransform();
        if (map == null || t == null) return;

        int tileW = map.getTileWidth();
        int tileH = map.getTileHeight();

        int centerTileX = (int) ((t.getX() + t.getWidth() * 0.5f) / tileW);
        int centerTileY = (int) ((t.getY() + t.getHeight() * 0.5f) / tileH);

        float left = t.getX();
        float right = t.getX() + t.getWidth();

        if ("test".equals(currMapId)) {
            boolean onRightEdge = right >= map.getPixelWidth() - EDGE_EPSILON;
            boolean onGateTile = centerTileY == TEST_TO_BRIDGE_ROW;
            if (onRightEdge && onGateTile) {
                beginMapSwap("bridge", SpawnEdge.LEFT, centerTileY);
            }
        } else if ("bridge".equals(currMapId)) {
            boolean onLeftEdge = left <= EDGE_EPSILON;
            boolean onGateTile = centerTileY == BRIDGE_TO_TEST_ROW;
            if (onLeftEdge && onGateTile) {
                beginMapSwap("test", SpawnEdge.RIGHT, centerTileY);
            }
        }
    }



    private void beginMapSwap(String targetMapId, SpawnEdge spawnEdge, int preservedTileIndex) {
        transition.begin(() -> applyMapSwap(targetMapId, spawnEdge, preservedTileIndex));
    }

    private void applyMapSwap(String targetMapId, SpawnEdge spawnEdge, int preservedTileIndex) {
        TileMap targetMap = assets.getTilemap(targetMapId);
        PImage targetImage = assets.getSprite(targetMapId);
        if (targetMap == null || targetImage == null) return;

        tileMapRenderer.setTileMap(targetMap);
        tileMapRenderer.setTileMapImage(targetImage);
        currMapId = targetMapId;

        TransformComponent t = player.getTransform();
        if (t == null) return;

        float x = t.getX();
        float y = t.getY();

        int mapPixelW = targetMap.getPixelWidth();
        int mapPixelH = targetMap.getPixelHeight();
        int tileW = targetMap.getTileWidth();
        int tileH = targetMap.getTileHeight();

        switch (spawnEdge) {
            case LEFT:
                x = 1f;
                y = preservedTileIndex * tileH;
                break;
            case RIGHT:
                x = mapPixelW - t.getWidth() - 1f;
                y = preservedTileIndex * tileH;
                break;
            case TOP:
                y = 1f;
                x = preservedTileIndex * tileW;
                break;
            case BOTTOM:
                y = mapPixelH - t.getHeight() - 1f;
                x = preservedTileIndex * tileW;
                break;
        }

        t.setX(x);
        t.setY(y);
    }

    private void applyMapNow(String mapId) {
        TileMap map = assets.getTilemap(mapId);
        PImage img = assets.getSprite(mapId);
        if (map == null || img == null) return;
        tileMapRenderer.setTileMap(map);
        tileMapRenderer.setTileMapImage(img);
        currMapId = mapId;
    }

    private float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    private void updateWalkingSound() {
        boolean moving = input.isActionActive("MOVE_LEFT") 
                || input.isActionActive("MOVE_RIGHT")
                || input.isActionActive("MOVE_UP")
                || input.isActionActive("MOVE_DOWN");
        
        if (moving && !walkingSoundActive) {
            walkingSound.playLoop();
            walkingSoundActive = true;
        } else if (!moving && walkingSoundActive) {
            walkingSound.stop();
            walkingSoundActive = false;
        }
    }
    
}
