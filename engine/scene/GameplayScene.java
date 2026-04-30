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
import core.Camera2D;


public class GameplayScene extends Scene {

    

    private static final int INTERACT_KEY = 69; 



    private static final float EDGE_EPSILON = 2f;

    private final Engine engine;
    private final Player player;
    private final InputManager input;
    private final SoundFX walkingSound;
    private final AssetsManager assets;
    private final TileMapRenderer tileMapRenderer;
    private final SceneTransition transition;
    private final Camera2D cam;

    private PApplet p;
    private boolean walkingSoundActive = false;
    private String currMapId = "test";

    public GameplayScene(Engine engine, Player player, InputManager input, 
        SoundFX walkingSound, AssetsManager assets, TileMapRenderer tileMapRenderer, 
        SceneTransition transition, Camera2D cam) {

        this.engine = engine;
        this.player = player;
        this.input = input;
        this.walkingSound = walkingSound;
        this.assets = assets;
        this.tileMapRenderer = tileMapRenderer;
        this.transition = transition;
        this.cam = cam;
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
        TileMap map = tileMapRenderer.getTileMap();
        float worldW = map != null ? map.getPixelWidth() : p.width;
        float worldH = map != null ? map.getPixelHeight() : p.height;
        
        engine.update(dt, worldW, worldH);
        
        if (map != null && cam != null) {
            cam.follow(player.getTransform(), p.width, p.height, map.getPixelWidth(), map.getPixelHeight());
        }
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
        if (mapChangeCooldownFrames > 0) {
            mapChangeCooldownFrames--;
            return;
        }
        TileMap map = tileMapRenderer.getTileMap();
        TransformComponent t = player.getTransform();

        if (map == null || t == null) return;

        int tileW = map.getTileWidth();
        int tileH = map.getTileHeight();
        int centerTileX = (int) ((t.getX() + t.getWidth() * 0.5f) / tileW);
        int centerTileY = (int) ((t.getY() + t.getHeight() * 0.5f) / tileH);
        float left = t.getX();
        float right = t.getX() + t.getWidth();
        float top = t.getY();
        float bottom = t.getY() + t.getHeight();

        for (MapExit exit : MAP_EXITS) {
            if (!exit.fromMapId.equals(currMapId)) continue;
            boolean edgeOk = switch (exit.fromEdge) {
                case RIGHT -> right >= map.getPixelWidth() - EDGE_EPSILON;
                case LEFT -> left <= EDGE_EPSILON;
                case BOTTOM -> bottom >= map.getPixelHeight() - EDGE_EPSILON;
                case TOP -> top <= EDGE_EPSILON;
            };

            if (!edgeOk) continue;
            boolean gateOk = switch (exit.fromEdge) {
                case LEFT, RIGHT -> centerTileY >= exit.gateStartTile && centerTileY <= exit.gateEndTile;
                case TOP, BOTTOM -> centerTileX >= exit.gateStartTile && centerTileX <= exit.gateEndTile;
            };

            if (!gateOk) continue;
            beginMapSwap(exit.toMapId, exit.spawnEdge, exit.spawnTileIndex);
            mapChangeCooldownFrames = 15; // tune: ~0.25s at 60fps
            return;

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
                x = tileW + 2f;
                y = preservedTileIndex * tileH;
                float maxY = mapPixelH - t.getHeight();
                y = clamp(y, 0f, maxY);
                break;
            case RIGHT:
                x = mapPixelW - t.getWidth() - tileW - 2f;
                y = preservedTileIndex * tileH;
                maxY = mapPixelH - t.getHeight();
                y = clamp(y, 0f, maxY);
                break;
            case TOP:
                y = tileH + 1.5f;
                x = preservedTileIndex * tileW;
                float maxX = mapPixelW - t.getWidth();
                x = clamp(x, 0f, maxX);
                break;
            case BOTTOM:
                y = mapPixelH - t.getHeight() - 1.5f;
                x = preservedTileIndex * tileW;
                maxX = mapPixelW - t.getWidth();
                x = clamp(x, 0f, maxX);
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

    private static final MapExit[] MAP_EXITS = {

        new MapExit("test", SpawnEdge.RIGHT, 6, 11, "bridge", SpawnEdge.LEFT, 8),
        new MapExit("bridge", SpawnEdge.LEFT, 6, 11, "test", SpawnEdge.RIGHT, 8),
    };

    private int mapChangeCooldownFrames = 0;
    
}
