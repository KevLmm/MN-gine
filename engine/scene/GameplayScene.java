package scene;

import processing.core.PApplet;
import processing.core.PImage;
import core.Engine;
import entity.Player;
import entity.Entity;
import entity.DungeonMessageSink;
import entity.SpikeBallEntity;
import input.*;
import music.BGMusic;
import music.SoundFX;
import entity.CollisionRect;
import entity.TileMap;
import entity.TransformComponent;
import systems.CollisionSystem;
import assets.AssetsManager;
import rendering.TileMapRenderer;
import core.Camera2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Core gameplay: tile rendering, movement, map transitions, dungeon props, and survival hazards.
 * Pausing, modal messages, and mode-specific HUD overlay this scene.
 */
public class GameplayScene extends Scene {

    private static final String DUNGEON_MAP_ID = "dungeon";
    private static final String SURVIVAL_MAP_ID = "survival";

    private static final int INTERACT_KEY = 69; 
    private static final int MENU_KEY = 27; // Esc
    private static final int MENU_KEY_ALT = 77; // M



    private static final float EDGE_EPSILON = 2f;

    private final Engine engine;
    private final Player player;
    private final Player dungeonPlayer;
    private final InputManager input;
    private final SoundFX walkingSound;
    private final AssetsManager assets;
    private final TileMapRenderer tileMapRenderer;
    private final SceneTransition transition;
    private final Camera2D cam;
    private final SceneManager sceneManager;
    private final MainMenuScene mainMenu;
    private final BGMusic overworldBgm;
    private final BGMusic dungeonBgm;
    /** When true, {@link core.GameMain} draws the volume bar and handles [ ] / drag. */
    private final Consumer<Boolean> volumeHudVisible;
    /** Tells {@link core.GameMain} which {@link BGMusic} the slider adjusts for the current map. */
    private final Consumer<BGMusic> activeBgmBinder;

    private static final int PAUSE_KEY = 80; // P
    private static final int RESTART_KEY = 82; // R

    private boolean paused = false;

    /** Restart (R while paused) and menu entry restore this map mode. */
    private String homeMapId = "test";
    private static final float START_PLAYER_X = 100;
    private static final float START_PLAYER_Y = 100;
    private static final float START_PROP_X = 200;
    private static final float START_PROP_Y = 200;

    private PApplet p;
    private boolean walkingSoundActive = false;
    private String currMapId = "test";

    /** Interactables spawned by {@link DungeonLayout}; removed when leaving the dungeon map. */
    private final List<Entity> dungeonDemoEntities = new ArrayList<>();
    /** Spike balls etc. for survival only. */
    private final List<Entity> survivalProps = new ArrayList<>();

    private final ModalMessageBox modalMessage = new ModalMessageBox();
    /** Avoid restarting the same loop when moving only between test and bridge. */
    private boolean overworldBgmSession;
    /** Same for dungeon (single map; avoids clobbering volume on layout refresh). */
    private boolean dungeonBgmSession;
    private int dungeonGold;

    private static int survivalHighScore;
    private int survivalScore;
    private float survivalScoreAccumulator;
    private int survivalLives = 3;
    private float survivalInvincTimer;
    private boolean survivalGameOver;

    public GameplayScene(Engine engine, Player player, Player dungeonPlayer, InputManager input,
        SoundFX walkingSound, AssetsManager assets, TileMapRenderer tileMapRenderer,
        SceneTransition transition, Camera2D cam, SceneManager sceneManager, MainMenuScene mainMenu,
        BGMusic overworldBgm, BGMusic dungeonBgm, Consumer<Boolean> volumeHudVisible,
        Consumer<BGMusic> activeBgmBinder) {

        this.engine = engine;
        this.player = player;
        this.dungeonPlayer = dungeonPlayer;
        this.input = input;
        this.walkingSound = walkingSound;
        this.assets = assets;
        this.tileMapRenderer = tileMapRenderer;
        this.transition = transition;
        this.cam = cam;
        this.sceneManager = sceneManager;
        this.mainMenu = mainMenu;
        this.overworldBgm = overworldBgm;
        this.dungeonBgm = dungeonBgm;
        this.volumeHudVisible = volumeHudVisible;
        this.activeBgmBinder = activeBgmBinder;
    }

    /** Test, bridge, and survival use the overworld knight + overworld BGM . */
    private static boolean isOverworldStyleMapId(String mapId) {
        return "test".equals(mapId) || "bridge".equals(mapId) || SURVIVAL_MAP_ID.equals(mapId);
    }

    private void refreshGameplayAudioForCurrentMap() {
        boolean ow = isOverworldStyleMapId(currMapId);
        boolean dg = DUNGEON_MAP_ID.equals(currMapId);
        volumeHudVisible.accept(ow || dg);
        if (ow) {
            dungeonBgm.stop();
            dungeonBgmSession = false;
            activeBgmBinder.accept(overworldBgm);
            if (!overworldBgmSession) {
                overworldBgm.setVolume(0.18f);
                overworldBgm.playLoop();
                overworldBgmSession = true;
            }
        } else if (dg) {
            overworldBgm.stop();
            overworldBgmSession = false;
            activeBgmBinder.accept(dungeonBgm);
            if (!dungeonBgmSession) {
                dungeonBgm.setVolume(0.18f);
                dungeonBgm.playLoop();
                dungeonBgmSession = true;
            }
        } else {
            overworldBgm.stop();
            dungeonBgm.stop();
            overworldBgmSession = false;
            dungeonBgmSession = false;
        }
    }

    /**
     * Sets the starting map and restart target before {@link #enter(PApplet)} (typically from the menu).
     */
    public void configureStart(String mapId) {
        homeMapId = mapId;
        currMapId = mapId;
    }

    @Override
    public void enter(PApplet p) {
        this.p = p;
        input.clearActions();
        applyMapNow(currMapId);
    }

    @Override
    public void exit() {
        if (walkingSoundActive) {
            walkingSound.stop();
            walkingSoundActive = false;
        }
        overworldBgmSession = false;
        dungeonBgmSession = false;
        overworldBgm.stop();
        dungeonBgm.stop();
        volumeHudVisible.accept(false);
    }

    @Override
    public void update(float dt) {
        if (p == null) {
            return;
        }
        if (modalMessage.isVisible()) {
            return;
        }
        if (paused) {
            return;
        }
        TileMap map = tileMapRenderer.getTileMap();
        float worldW = map != null ? map.getPixelWidth() : p.width;
        float worldH = map != null ? map.getPixelHeight() : p.height;

        if (map != null && cam != null && worldW > 0f && worldH > 0f) {
            float vw = p.width;
            float vh = p.height;
            // Overworld (map larger than window): 1:1 world pixels + scrolling camera.
            // Small maps (dungeon): scale up so the whole map fills the sketch.
            boolean mapNeedsPan = worldW > vw || worldH > vh;
            if (mapNeedsPan) {
                cam.setZoom(1f);
            } else {
                cam.setZoom(Math.min(vw / worldW, vh / worldH));
            }
        } else if (cam != null) {
            cam.setZoom(1f);
        }

        boolean survivalFrozen = SURVIVAL_MAP_ID.equals(currMapId) && survivalGameOver;
        if (!survivalFrozen) {
            engine.update(dt, worldW, worldH);
            if (map != null && cam != null && worldW > 0f && worldH > 0f) {
                cam.follow(activePlayer().getTransform(), p.width, p.height, worldW, worldH);
            }
        }

        if (SURVIVAL_MAP_ID.equals(currMapId)) {
            updateSurvivalMode(dt);
        }

        if (!survivalFrozen) {
            updateWalkingSound();
            checkEdgeMapTransition();
        }
    }

    @Override
    public void render() {
        engine.render();

        if (paused && p != null) {
            p.pushStyle();
            p.fill(0, 140);
            p.noStroke();
            p.rect(0, 0, p.width, p.height);
            p.fill(255);
            p.textAlign(PApplet.CENTER, PApplet.CENTER);
            p.textSize(24);
            p.text("PAUSED\nP to resume R to restart", p.width / 2f, p.height / 2f);
            p.popStyle();
        }

        if (p != null && SURVIVAL_MAP_ID.equals(currMapId)) {
            if (!survivalGameOver) {
                drawSurvivalHud();
            }
            if (survivalGameOver) {
                drawSurvivalLossOverlay();
            }
        }

        if (p != null && DUNGEON_MAP_ID.equals(currMapId)) {
            drawDungeonHud();
        }

        if (modalMessage.isVisible() && p != null) {
            modalMessage.draw(p);
        }
    }

    @Override
    public void keyPressed(char key, int keyCode) {
        if (modalMessage.isVisible()) {
            modalMessage.dismiss();
            input.clearActions();
            return;
        }

        if (SURVIVAL_MAP_ID.equals(currMapId) && survivalGameOver) {
            if (keyCode == RESTART_KEY) {
                restartSurvivalRun();
            } else if (keyCode == MENU_KEY_ALT) {
                dismissSurvivalGameOverToMenu();
            }
            return;
        }

        if (sceneManager != null && mainMenu != null
                && (keyCode == MENU_KEY || keyCode == MENU_KEY_ALT)) {
            sceneManager.replaceWithTransition(mainMenu);
            return;
        }

        if (keyCode == PAUSE_KEY) {
            togglePause();
            return;
        }

        if (paused) {
            if (keyCode == RESTART_KEY) {
                restartRun();
            }
            return;
        }
        if (keyCode == INTERACT_KEY) {
            engine.tryInteract(activePlayer());
        }
        for (Entity e : engine.getEntities()) {
            if (e instanceof InputListener) {
                ((InputListener) e).onKeyPressed(key, keyCode);
            }
        }
    }

    @Override
    public void mousePressed(float mx, float my) {
        if (modalMessage.isVisible()) {
            modalMessage.dismiss();
            input.clearActions();
            return;
        }
    }

    @Override
    public void keyReleased(char key, int keyCode) {
        if (paused || modalMessage.isVisible() || (SURVIVAL_MAP_ID.equals(currMapId) && survivalGameOver)) {
            return;
        }
        for (Entity e : engine.getEntities()) {
            if (e instanceof InputListener) {
                ((InputListener) e).onKeyReleased(key, keyCode);
            }
        }
    }

    private void togglePause() {
        paused = !paused;
        if (paused) {
            if (walkingSoundActive) {
                walkingSound.stop();
                walkingSoundActive = false;
            }
            input.clearActions();
        }
    }

    /** Reloads {@link #homeMapId}, resets player poses, and clears non-survival props (pause menu restart). */
    private void restartRun() {
        if (transition.isBlocking()) {
            return;
        }

        paused = false;

        if (walkingSoundActive) {
            walkingSound.stop();
            walkingSoundActive = false;
        }

        input.clearActions();

        currMapId = homeMapId;
        applyMapNow(currMapId);

        TransformComponent pt = player.getTransform();
        if (pt != null) {
            pt.setX(START_PLAYER_X);
            pt.setY(START_PLAYER_Y);
        }
        TransformComponent dt = dungeonPlayer.getTransform();
        if (dt != null) {
            dt.setX(START_PLAYER_X);
            dt.setY(START_PLAYER_Y);
        }

        for (Entity e : engine.getEntities()) {
            if (e == player || e == dungeonPlayer) {
                continue;
            }
            if (survivalProps.contains(e)) {
                continue;
            }
            TransformComponent t = e.getComponent(TransformComponent.class);
            if (t != null) {
                t.setX(START_PROP_X);
                t.setY(START_PROP_Y);
            }
        }

        mapChangeCooldownFrames = 0;
    }

    private void checkEdgeMapTransition() {
        if (transition.isBlocking()) return;
        if (mapChangeCooldownFrames > 0) {
            mapChangeCooldownFrames--;
            return;
        }
        TileMap map = tileMapRenderer.getTileMap();
        TransformComponent t = activePlayer().getTransform();

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

        if (!DUNGEON_MAP_ID.equals(targetMapId)) {
            clearDungeonDemoEntities();
        }
        if (!SURVIVAL_MAP_ID.equals(targetMapId)) {
            clearSurvivalProps();
        }
        tileMapRenderer.setTileMap(targetMap);
        tileMapRenderer.setTileMapImage(targetImage);
        currMapId = targetMapId;

        Player avatar = avatarForMapId(targetMapId);
        TransformComponent t = avatar.getTransform();
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

        if (DUNGEON_MAP_ID.equals(targetMapId)) {
            syncDungeonLayout(false);
        }
        if (SURVIVAL_MAP_ID.equals(targetMapId)) {
            syncSurvivalLayout();
        }
        syncActivePlayerEntity();
        syncInputToActivePlayer();
        refreshGameplayAudioForCurrentMap();
    }

    private void applyMapNow(String mapId) {
        TileMap map = assets.getTilemap(mapId);
        PImage img = assets.getSprite(mapId);
        if (map == null || img == null) {
            syncActivePlayerEntity();
            syncInputToActivePlayer();
            return;
        }
        if (!DUNGEON_MAP_ID.equals(mapId)) {
            clearDungeonDemoEntities();
        }
        if (!SURVIVAL_MAP_ID.equals(mapId)) {
            clearSurvivalProps();
        }
        tileMapRenderer.setTileMap(map);
        tileMapRenderer.setTileMapImage(img);
        currMapId = mapId;
        if (DUNGEON_MAP_ID.equals(mapId)) {
            syncDungeonLayout(true);
        } else {
            resetOverworldPlayerPose();
        }
        if (SURVIVAL_MAP_ID.equals(mapId)) {
            syncSurvivalLayout();
        }
        syncActivePlayerEntity();
        syncInputToActivePlayer();
        refreshGameplayAudioForCurrentMap();
    }

    private void resetOverworldPlayerPose() {
        TransformComponent pt = player.getTransform();
        if (pt != null) {
            pt.setX(START_PLAYER_X);
            pt.setY(START_PLAYER_Y);
        }
    }

    private Player activePlayer() {
        return DUNGEON_MAP_ID.equals(currMapId) ? dungeonPlayer : player;
    }

    private Player avatarForMapId(String mapId) {
        return DUNGEON_MAP_ID.equals(mapId) ? dungeonPlayer : player;
    }

    /**
     * Only one player entity is registered at a time so two avatars never draw on the same map.
     */
    private void syncActivePlayerEntity() {
        engine.removeEntity(player);
        engine.removeEntity(dungeonPlayer);
        if (DUNGEON_MAP_ID.equals(currMapId)) {
            engine.registerEntity(dungeonPlayer);
        } else {
            engine.registerEntity(player);
        }
    }

    private void syncInputToActivePlayer() {
        if (DUNGEON_MAP_ID.equals(currMapId)) {
            player.setInput(null);
            dungeonPlayer.setInput(input);
        } else {
            dungeonPlayer.setInput(null);
            player.setInput(input);
        }
    }

    private void clearDungeonDemoEntities() {
        for (Entity e : dungeonDemoEntities) {
            engine.removeEntity(e);
        }
        dungeonDemoEntities.clear();
    }

    /** Removes spike entities and clears transient survival flags when leaving the map or rebuilding layout. */
    private void clearSurvivalProps() {
        for (Entity e : survivalProps) {
            engine.removeEntity(e);
        }
        survivalProps.clear();
        survivalGameOver = false;
        survivalInvincTimer = 0f;
    }

    /** Resets score, lives, invincibility, and game-over flag for a new survival run. */
    private void resetSurvivalRunState() {
        survivalScore = 0;
        survivalScoreAccumulator = 0f;
        survivalLives = 3;
        survivalInvincTimer = 0f;
        survivalGameOver = false;
    }

    /** Spawns survival spikes and applies {@link #resetSurvivalRunState()}. */
    private void syncSurvivalLayout() {
        clearSurvivalProps();
        resetSurvivalRunState();
        TileMap map = tileMapRenderer.getTileMap();
        if (map == null || !SURVIVAL_MAP_ID.equals(currMapId)) {
            return;
        }
        float ww = map.getPixelWidth();
        float wh = map.getPixelHeight();
        int tw = map.getTileWidth();
        int th = map.getTileHeight();
        float bw = 100f;
        float bh = 180f;
        // Shared spawn away from the fixed overworld start corner (START_PLAYER_X / START_PLAYER_Y).
        float minX = tw;
        float minY = th;
        float maxX = Math.max(minX, ww - bw - tw);
        float maxY = Math.max(minY, wh - bh - th);
        float margin = Math.max(tw, th) * 2f;
        float spawnX = clamp(ww - bw - tw - margin, minX, maxX);
        float spawnY = clamp(wh - bh - th - margin, minY, maxY);
        SpikeBallEntity a = new SpikeBallEntity(spawnX, spawnY, bw, bh, ww, wh, tw, th);
        SpikeBallEntity b = new SpikeBallEntity(spawnX, spawnY, bw, bh, ww, wh, tw, th);
        engine.registerEntity(a);
        engine.registerEntity(b);
        survivalProps.add(a);
        survivalProps.add(b);
    }

    /**
     * Rebuilds dungeon walls and interactables from {@link DungeonLayout}. Resets dungeon gold;
     * {@code placePlayerAtDefaultSpawn} moves the avatar to the demo spawn when entering from the menu.
     */
    private void syncDungeonLayout(boolean placePlayerAtDefaultSpawn) {
        clearDungeonDemoEntities();
        dungeonGold = 0;
        TileMap map = tileMapRenderer.getTileMap();
        if (map == null) {
            return;
        }
        DungeonMessageSink dungeonSink = new DungeonMessageSink() {
            @Override
            public void showModal(String message) {
                modalMessage.open(message);
            }

            @Override
            public void awardGold(int amount) {
                dungeonGold += amount;
            }
        };
        dungeonDemoEntities.addAll(
                DungeonLayout.applyDungeonDemo(engine, dungeonPlayer, map, placePlayerAtDefaultSpawn, dungeonSink));
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

    /**
     * Per-second score tick and spike damage for the survival map. Skips damage while invincibility
     * is active or after a game over.
     */
    private void updateSurvivalMode(float dt) {
        if (survivalGameOver) {
            return;
        }
        survivalScoreAccumulator += dt;
        while (survivalScoreAccumulator >= 1f) {
            survivalScoreAccumulator -= 1f;
            survivalScore++;
        }
        if (survivalInvincTimer > 0f) {
            survivalInvincTimer -= dt;
            if (survivalInvincTimer < 0f) {
                survivalInvincTimer = 0f;
            }
            return;
        }
        checkSurvivalSpikeCollision();
    }

    /**
     * Overlap test between the overworld player hull and each spike’s {@link entity.SpikeBallEntity#damageCollisionRect()}.
     */
    private void checkSurvivalSpikeCollision() {
        TransformComponent pt = player.getComponent(TransformComponent.class);
        if (pt == null) {
            return;
        }
        CollisionRect ph = CollisionSystem.hullOf(player, pt);
        for (Entity e : survivalProps) {
            if (!(e instanceof SpikeBallEntity)) {
                continue;
            }
            TransformComponent st = e.getComponent(TransformComponent.class);
            if (st == null) {
                continue;
            }
            CollisionRect sh = ((SpikeBallEntity) e).damageCollisionRect();
            if (sh == null || !rectsOverlap(ph, sh)) {
                continue;
            }
            survivalLives--;
            survivalInvincTimer = 2f;
            if (walkingSoundActive) {
                walkingSound.stop();
                walkingSoundActive = false;
            }
            input.clearActions();
            if (survivalLives <= 0) {
                survivalGameOver = true;
                if (survivalScore > survivalHighScore) {
                    survivalHighScore = survivalScore;
                }
            }
            break;
        }
    }

    private static boolean rectsOverlap(CollisionRect a, CollisionRect b) {
        return a.x < b.x + b.width && a.x + a.width > b.x && a.y < b.y + b.height && a.y + a.height > b.y;
    }

    /** Survival score and life squares (screen space, top-left). */
    private void drawSurvivalHud() {
        if (p == null) {
            return;
        }
        p.pushStyle();
        p.textAlign(PApplet.LEFT, PApplet.TOP);
        p.textSize(22f);
        p.fill(255);
        String line = "Score: " + survivalScore;
        p.text(line, 12f, 10f);
        float lifeY = 10f;
        float sq = 18f;
        float gap = 6f;
        float lifeX = 12f + p.textWidth(line) + 16f;
        for (int i = 0; i < 3; i++) {
            if (i < survivalLives) {
                p.fill(220, 45, 45);
            } else {
                p.fill(55, 55, 55);
            }
            p.stroke(0);
            p.strokeWeight(2f);
            p.rect(lifeX + i * (sq + gap), lifeY, sq, sq);
        }
        p.popStyle();
    }

    /** Dungeon gold total with a slight shadow for contrast on bright tiles. */
    private void drawDungeonHud() {
        if (p == null) {
            return;
        }
        p.pushStyle();
        p.textAlign(PApplet.LEFT, PApplet.TOP);
        p.textSize(22f);
        p.fill(24, 20, 8);
        p.text("Gold: " + dungeonGold, 13f, 11f);
        p.fill(255, 210, 72);
        p.text("Gold: " + dungeonGold, 12f, 10f);
        p.popStyle();
    }

    /** Full-screen dim overlay with score summary after all survival lives are lost. */
    private void drawSurvivalLossOverlay() {
        if (p == null) {
            return;
        }
        p.pushStyle();
        p.fill(0, 200);
        p.noStroke();
        p.rect(0, 0, p.width, p.height);
        p.fill(255);
        p.textAlign(PApplet.CENTER, PApplet.CENTER);
        p.textSize(32f);
        p.text("Game Over", p.width * 0.5f, p.height * 0.38f);
        p.textSize(22f);
        p.text("Score: " + survivalScore, p.width * 0.5f, p.height * 0.48f);
        p.text("Best: " + survivalHighScore, p.width * 0.5f, p.height * 0.55f);
        p.textSize(16f);
        p.fill(210);
        p.text("R — restart    M — menu", p.width * 0.5f, p.height * 0.66f);
        p.popStyle();
    }

    /** Leaves survival and returns to the main menu; clears input state. */
    private void dismissSurvivalGameOverToMenu() {
        survivalGameOver = false;
        input.clearActions();
        if (sceneManager != null && mainMenu != null) {
            sceneManager.replaceWithTransition(mainMenu);
        }
    }

    /** New survival attempt after game over (same map, fresh score/lives/spikes). */
    private void restartSurvivalRun() {
        if (transition.isBlocking() || !SURVIVAL_MAP_ID.equals(currMapId)) {
            return;
        }
        paused = false;
        if (walkingSoundActive) {
            walkingSound.stop();
            walkingSoundActive = false;
        }
        input.clearActions();
        syncSurvivalLayout();
        resetOverworldPlayerPose();
        player.syncBodyFromTransform();
        mapChangeCooldownFrames = 0;
    }

}
