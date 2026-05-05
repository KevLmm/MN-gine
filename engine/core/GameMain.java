package core;
import entity.Player;
import processing.core.PApplet;
import rendering.SpriteRenderer;
import input.InputManager;
import assets.AssetsManager;
import rendering.SpriteDrawable;
import rendering.SpriteSheetDrawable;
import rendering.TileMapRenderer;
import rendering.WalkingSpriteSheetDrawable;
import processing.core.PImage;
import scene.SceneTransition;
import scene.SceneManager;
import music.BGMusic;
import music.SoundFX;
import scene.GameplayScene;
import scene.SplashScene;
import scene.MainMenuScene;
import entity.TileMap;
import entity.TransformComponent;

public class GameMain extends PApplet {

    /** Draw size on map (world units). Sprite is aspect-fitted inside this box. */
    private static final float DUNGEON_KNIGHT_DISPLAY_W = 128f;
    private static final float DUNGEON_KNIGHT_DISPLAY_H = 128f;
    /**
     * Physics hull for the dungeon avatar only — must fit dungeon corridors (16px tiles, 2-tile
     * gaps). The large display size stays for art; {@link entity.Player#setPhysicsHull} feeds
     * {@link core.Collidable} + tile collision.
     */
    private static final float DUNGEON_KNIGHT_PHYSICS_W = 24f;
    private static final float DUNGEON_KNIGHT_PHYSICS_H = 32f;

    /**
     * One cell in {@code dungeon_knight.png} in pixels (width and height of a single frame).
     * A sheet that matches a single cell yields one frame; use {@link SpriteDrawable} instead of a strip.
     * For a larger sheet: columns = floor(sheetWidth / cellW), rows = floor(sheetHeight / cellH), frames = columns × rows.
     */
    private static final int DUNGEON_KNIGHT_CELL_W = 32;
    private static final int DUNGEON_KNIGHT_CELL_H = 128;
    /** Pixels between the left/top edges of adjacent cells (often equals cell size; larger if gutters). */
    private static final int DUNGEON_KNIGHT_STRIDE_X = 32;
    private static final int DUNGEON_KNIGHT_STRIDE_Y = 32;
    /** Skip this many pixels from the sheet’s top-left before cell (0,0) starts (fixes “only see left edge”). */
    private static final int DUNGEON_KNIGHT_ORIGIN_X = 0;
    private static final int DUNGEON_KNIGHT_ORIGIN_Y = 0;
    /** Strip index while idle (usually the stand pose column). */
    private static final int DUNGEON_KNIGHT_IDLE_CELL = 0;
    /** First / last strip index used while moving (idle is not repeated mid-walk). Use -1 for “last column”. */
    private static final int DUNGEON_KNIGHT_WALK_FIRST = 1;
    private static final int DUNGEON_KNIGHT_WALK_LAST = -1;

    /** {@code raw < 0} means “use last column of strip”. */
    private static int clampWalkLastIndex(int raw, int frameCount) {
        if (raw < 0) {
            return frameCount - 1;
        }
        return Math.min(raw, frameCount - 1);
    }

    private static boolean imageHasPixels(PImage img) {
        if (img == null) {
            return false;
        }
        int w = img.pixelWidth > 0 ? img.pixelWidth : img.width;
        int h = img.pixelHeight > 0 ? img.pixelHeight : img.height;
        return w > 0 && h > 0;
    }

    private Engine engine;
    private AssetsManager assetsManager;
    private TileMapRenderer tileMapRenderer;
    private Player player;
    private InputManager inputManager;
    private PImage logo;
    private final SceneTransition transition = new SceneTransition();
    private final BGMusic overworldBgm = new BGMusic(this, "bgmusic1.mp3");
    private final BGMusic dungeonBgm = new BGMusic(this, "data/retromusic.mp3");
    /** Which track the on-screen slider and [ ] keys adjust while the HUD is visible. */
    private BGMusic activeBgmForVolume = overworldBgm;
    private final SoundFX walkingSound = new SoundFX(this, "walking.mp3");
    private SceneManager scenes;
    private Camera2D cam;

    private static final float VOLUME_UI_X = 8f;
    private static final float VOLUME_UI_H = 10f;
    private static final float VOLUME_UI_PAD_BOTTOM = 8f;
    private boolean volumeBarDragging;
    private boolean showVolumeHud;

    private static Drawable createDungeonKnightWalkLook(AssetsManager assets) {
        PImage dungeonKnightSheet = assets.getSprite("dungeon_knight");
        if (!imageHasPixels(dungeonKnightSheet)) {
            return new SpriteDrawable("knight");
        }
        int sw = dungeonKnightSheet.pixelWidth > 0 ? dungeonKnightSheet.pixelWidth : dungeonKnightSheet.width;
        int sh = dungeonKnightSheet.pixelHeight > 0 ? dungeonKnightSheet.pixelHeight : dungeonKnightSheet.height;
        int cw = Math.min(DUNGEON_KNIGHT_CELL_W, sw);
        int ch = Math.min(DUNGEON_KNIGHT_CELL_H, sh);
        if (cw < 1 || ch < 1) {
            return new SpriteDrawable("knight");
        }
        int cols = sw / cw;
        int rows = sh / ch;
        if (cols < 1) {
            cols = 1;
        }
        if (rows < 1) {
            rows = 1;
        }
        int frameCount = cols * rows;
        if (frameCount <= 1) {
            return new SpriteDrawable("dungeon_knight");
        }
        int sx = Math.max(1, DUNGEON_KNIGHT_STRIDE_X);
        int sy = Math.max(1, DUNGEON_KNIGHT_STRIDE_Y);
        SpriteSheetDrawable dkWalk = new SpriteSheetDrawable(
                "dungeon_knight", cw, ch, cols, sx, sy,
                DUNGEON_KNIGHT_ORIGIN_X, DUNGEON_KNIGHT_ORIGIN_Y);
        int idle = Math.min(DUNGEON_KNIGHT_IDLE_CELL, frameCount - 1);
        int wf = Math.min(DUNGEON_KNIGHT_WALK_FIRST, frameCount - 1);
        int wl = clampWalkLastIndex(DUNGEON_KNIGHT_WALK_LAST, frameCount);
        return new WalkingSpriteSheetDrawable(dkWalk, 10f, frameCount, idle, wf, wl);
    }

    private static void applyDungeonKnightDisplaySize(Player p) {
        TransformComponent t = p.getTransform();
        if (t != null) {
            t.setWidth(DUNGEON_KNIGHT_DISPLAY_W);
            t.setHeight(DUNGEON_KNIGHT_DISPLAY_H);
        }
        p.syncBodyFromTransform();
    }

    private float volumeBarY() {
        return height - VOLUME_UI_PAD_BOTTOM - VOLUME_UI_H - 14f;
    }

    private float volumeBarW() {
        return Math.min(220f, width - 2f * VOLUME_UI_X);
    }

    private boolean hitVolumeBar(int mx, int my) {
        float y = volumeBarY();
        float w = volumeBarW();
        return mx >= VOLUME_UI_X && mx <= VOLUME_UI_X + w && my >= y && my <= y + VOLUME_UI_H;
    }

    private void setVolumeFromMouseX(int mx) {
        float w = volumeBarW();
        float t = (mx - VOLUME_UI_X) / w;
        if (t < 0f) {
            t = 0f;
        }
        if (t > 1f) {
            t = 1f;
        }
        activeBgmForVolume.setVolume(t);
    }

    private void drawVolumeMeter() {
        if (!showVolumeHud) {
            return;
        }
        float barY = volumeBarY();
        float barW = volumeBarW();
        float v = activeBgmForVolume.getVolume();
        pushStyle();
        fill(40);
        noStroke();
        rect(VOLUME_UI_X, barY, barW, VOLUME_UI_H);
        fill(100, 180, 255);
        rect(VOLUME_UI_X, barY, barW * v, VOLUME_UI_H);
        stroke(200);
        strokeWeight(1);
        noFill();
        rect(VOLUME_UI_X - 0.5f, barY - 0.5f, barW + 1f, VOLUME_UI_H + 1f);
        fill(240);
        textAlign(LEFT, PApplet.BASELINE);
        textSize(11);
        text("Music volume — drag bar ( [ quieter  ] louder )", VOLUME_UI_X, barY - 3f);
        popStyle();
    }

    public static void main(String args[]) {
        
        PApplet.main("core.GameMain");
        
    }

    public void settings() {
        size(512, 512);
    }
    public void setup() {
        assetsManager = new AssetsManager(this);
        scenes = new SceneManager(this, transition);
        
        // Same resolver as tile sprites (handles cwd=bin vs project data/)
        assetsManager.loadSprite("_logo", "MDC.png");
        logo = assetsManager.getSprite("_logo");
        assetsManager.loadTileMap("test", "data/mapexample.png", 20, 20, 32, 32);
        assetsManager.loadTileMap("bridge", "data/Bridge.png", 18, 20, 32, 32 );
        assetsManager.loadTileMap("dungeon", "data/dungeon.png", 26, 26, 16, 16);
        assetsManager.loadTileMapFromRasterGrid("survival", "data/survival.png", 32, 32);
        assetsManager.loadSprite("knight", "knight.png");
        assetsManager.loadSprite("dungeon", "data/dungeon.png");
        assetsManager.loadSprite("dungeon_knight", "data/dungeon_knight.png");
        assetsManager.loadSprite("spikeball", "data/spikeball.png");
        TileMap testMap = assetsManager.getTilemap("test");
        if (testMap != null) {
            int w = testMap.getWidth();
            int h = testMap.getHeight();
            testMap.setSolidRect(0, 0, w-1, 0, true); //top wall
            testMap.setSolidRect(0, h-1, w-1, h-1, true); // bottom wall
            testMap.setSolidRect(0, 0, 0, h-1, true); // left wall
            testMap.setSolidRect(w-1, 0, w-1, h-1, true); // right wall
            testMap.setSolidRect(w-1, 6, w-1, 11, false); // doorway to bridge

            testMap.setSolidRect(5, 5, 8, 8, true); 
        }

        TileMap bridgeMap = assetsManager.getTilemap("bridge");
        if (bridgeMap != null) {
            int w = bridgeMap.getWidth();
            int h = bridgeMap.getHeight();
            bridgeMap.setSolidRect(0, 0, w-1, 0, true); //top wall
            bridgeMap.setSolidRect(0, h-1, w-1, h-1, true); // bottom wall
            bridgeMap.setSolidRect(0, 0, 0, h-1, true); // left wall
            bridgeMap.setSolidRect(w-1, 0, w-1, h-1, true); // right wall
            bridgeMap.setSolidRect(0, 6, 0, 11, false);
        }

        TileMap survivalMap = assetsManager.getTilemap("survival");
        if (survivalMap != null) {
            int w = survivalMap.getWidth();
            int h = survivalMap.getHeight();
            survivalMap.setSolidRect(0, 0, w - 1, 0, true);
            survivalMap.setSolidRect(0, h - 1, w - 1, h - 1, true);
            survivalMap.setSolidRect(0, 0, 0, h - 1, true);
            survivalMap.setSolidRect(w - 1, 0, w - 1, h - 1, true);
            // PNG art may look like floor obstacles; collision is border-only. Clear any interior flags.
            for (int x = 1; x < w - 1; x++) {
                for (int y = 1; y < h - 1; y++) {
                    survivalMap.setSolid(x, y, false);
                }
            }
        }
        SpriteRenderer spriteRenderer = new SpriteRenderer(this, assetsManager);
        cam = new Camera2D();
        spriteRenderer.setCamera(cam);

        engine = new Engine();
        engine.setRenderer(spriteRenderer);
        tileMapRenderer = new TileMapRenderer(spriteRenderer,
            new SpriteDrawable("test"), assetsManager.getTilemap("test"),
            assetsManager.getSprite("test"));
        
        engine.setTileMapRenderer(tileMapRenderer);

        TileMap dungeonMap = assetsManager.getTilemap("dungeon");
        PImage dungeonImg = assetsManager.getSprite("dungeon");
        if (dungeonMap != null && dungeonImg != null) {
            tileMapRenderer.setTileMap(dungeonMap);
            tileMapRenderer.setTileMapImage(dungeonImg);
        }

        Drawable overworldLook = new SpriteDrawable("knight");
        Drawable dungeonPlayerLook = createDungeonKnightWalkLook(assetsManager);

        player = new Player(100, 100, overworldLook);
        Player dungeonPlayer = new Player(100, 100, dungeonPlayerLook);
        applyDungeonKnightDisplaySize(dungeonPlayer);
        player.setPhysicsHull(60, 80);
        dungeonPlayer.setPhysicsHull(DUNGEON_KNIGHT_PHYSICS_W, DUNGEON_KNIGHT_PHYSICS_H);
        engine.registerEntity(player);
        
        inputManager = new InputManager();
        inputManager.bindKey(65, "MOVE_LEFT");   // A
        inputManager.bindKey(68, "MOVE_RIGHT");  // D
        inputManager.bindKey(87, "MOVE_UP");     // W
        inputManager.bindKey(83, "MOVE_DOWN");   // S
        player.setInput(inputManager);

        MainMenuScene mainMenu = new MainMenuScene();
        GameplayScene gameplayScene = new GameplayScene(
                engine,
                player,
                dungeonPlayer,
                inputManager,
                walkingSound,
                assetsManager,
                tileMapRenderer,
                transition,
                cam,
                scenes,
                mainMenu,
                overworldBgm,
                dungeonBgm,
                vis -> this.showVolumeHud = Boolean.TRUE.equals(vis),
                m -> this.activeBgmForVolume = m);
        mainMenu.bind(scenes, gameplayScene);

        scenes.start(new SplashScene(scenes, logo, mainMenu));
    }

    public void draw() {

        float dt = 1f / 60f;
        transition.update(dt);
        if (!transition.isBlocking()) {
            scenes.update(dt);
        }
        scenes.render();
        transition.render(this);
        drawVolumeMeter();
    }

    @Override
    public void keyPressed() {
        if (showVolumeHud) {
            if (key == '[') {
                activeBgmForVolume.setVolume(activeBgmForVolume.getVolume() - 0.06f);
            } else if (key == ']') {
                activeBgmForVolume.setVolume(activeBgmForVolume.getVolume() + 0.06f);
            }
        }
        scenes.keyPressed(key, keyCode);
    }

    @Override
    public void keyReleased() {
        scenes.keyReleased(key, keyCode);
    }

    @Override
    public void mousePressed(processing.event.MouseEvent event) {
        if (showVolumeHud && hitVolumeBar(event.getX(), event.getY())) {
            volumeBarDragging = true;
            setVolumeFromMouseX(event.getX());
        } else {
            scenes.mousePressed(event.getX(), event.getY());
        }
    }

    @Override
    public void mouseDragged(processing.event.MouseEvent event) {
        if (volumeBarDragging) {
            setVolumeFromMouseX(event.getX());
        }
    }

    @Override
    public void mouseReleased(processing.event.MouseEvent event) {
        volumeBarDragging = false;
        scenes.mouseReleased(event.getX(), event.getY());
    }
}
