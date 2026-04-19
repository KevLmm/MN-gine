package core;
import entity.Player;
import processing.core.PApplet;
import rendering.SpriteRenderer;
import entity.Entity;
import input.InputListener;
import input.InputManager;
import entity.InteractableEntity;
import assets.AssetsManager;
import rendering.SpriteDrawable;
import rendering.TileMapRenderer;
import processing.core.PImage;
import scene.SceneTransition;
import music.BGMusic;
import music.SoundFX;


public class GameMain extends PApplet {

    private Engine engine;
    private AssetsManager assetsManager;
    private TileMapRenderer tileMapRenderer;
    private Player player;
    private InputManager inputManager;
    private PImage logo;
    private boolean showSplash = true;
    private int splashStartTime;
    private final int SPLASH_DURATION = 2000;
    private final SceneTransition transition = new SceneTransition();
    private final BGMusic bgMusic = new BGMusic(this, "bgmusic1.mp3");
    private final SoundFX walkingSound = new SoundFX(this, "walking.mp3");
    private boolean walkingSoundActive;


    public static void main(String args[]) {
        
        PApplet.main("core.GameMain");
        
    }

    public void settings() {
        size(512, 512);
    }
    public void setup() {
        assetsManager = new AssetsManager(this);
        // Same resolver as tile sprites (handles cwd=bin vs project data/)
        assetsManager.loadSprite("_logo", "MDC.png");
        logo = assetsManager.getSprite("_logo");
        splashStartTime = millis();

        assetsManager.loadTileMap("test", "data/mapexample.png", 16, 16, 32, 32);
        assetsManager.loadSprite("knight", "knight.png");
        SpriteRenderer spriteRenderer = new SpriteRenderer(this, assetsManager);
        engine = new Engine();
        engine.setRenderer(spriteRenderer);
        tileMapRenderer = new TileMapRenderer(spriteRenderer,
            new SpriteDrawable("test"), assetsManager.getTilemap("test"),
            assetsManager.getSprite("test"));

        engine.setTileMapRenderer(tileMapRenderer);
        
        SpriteDrawable knightDrawable = new SpriteDrawable("knight");
        player = new Player(100, 100, knightDrawable);
        engine.registerEntity(player);
        inputManager = new InputManager();
        inputManager.bindKey(65, "MOVE_LEFT");   // A
        inputManager.bindKey(68, "MOVE_RIGHT");  // D
        inputManager.bindKey(87, "MOVE_UP");     // W
        inputManager.bindKey(83, "MOVE_DOWN");   // S
        player.setInput(inputManager);
        InteractableEntity circleEntity = new InteractableEntity(200, 200);
        engine.registerEntity(circleEntity);
    }

    public void draw() {
        if (showSplash) {
            drawSplashScreen();

            if (millis() - splashStartTime >= SPLASH_DURATION) {
                showSplash = false;
                bgMusic.setVolume(0.5f);
                bgMusic.playLoop();
            }
            return;
        }
        engine.update(1/60f, width, height);
        updateWalkingSound();
        engine.render();
        transition.render(this);

        
    }

    private void updateWalkingSound() {
        boolean moving = inputManager.isActionActive("MOVE_LEFT")
                || inputManager.isActionActive("MOVE_RIGHT")
                || inputManager.isActionActive("MOVE_UP")
                || inputManager.isActionActive("MOVE_DOWN");
        if (moving) {
            if (!walkingSoundActive) {
                walkingSound.playLoop();
                walkingSoundActive = true;
            }
        } else {
            if (walkingSoundActive) {
                walkingSound.stop();
                walkingSoundActive = false;
            }
        }
    }

    private void drawSplashScreen() {
        background(0);
        if (logo == null) {
            logo = loadImage(dataPath("MDC.png"));
        }
        if (logo != null) {
            imageMode(CENTER);
            image(logo, width / 2.0f, height / 2.0f);
            fill(255);
            textAlign(CENTER, CENTER);
            text("Powered by ModuCore", width / 2.0f, height / 2.0f + 100);
        } else {
            fill(255);
            textAlign(CENTER, CENTER);
            text("Engine Logo", width / 2.0f, height / 2.0f);
        }
    }

    /** 'E' key for interact. Processing keyCode for E is 69. */
    private static final int INTERACT_KEY = 69;

    @Override
    public void keyPressed() {
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
    public void keyReleased() {
        for (Entity e : engine.getEntities()) {
            if (e instanceof InputListener) {
                ((InputListener) e).onKeyReleased(key, keyCode);
            }
        }
    }



}
