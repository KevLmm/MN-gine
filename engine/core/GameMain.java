package core;
import entity.Player;
import processing.core.PApplet;
import rendering.SpriteRenderer;
import input.InputManager;
import entity.InteractableEntity;
import assets.AssetsManager;
import rendering.SpriteDrawable;
import rendering.TileMapRenderer;
import processing.core.PImage;
import scene.SceneTransition;
import scene.SceneManager;
import music.BGMusic;
import music.SoundFX;
import scene.GameplayScene;
import scene.SplashScene;
import core.Camera2D;



public class GameMain extends PApplet {

    private Engine engine;
    private AssetsManager assetsManager;
    private TileMapRenderer tileMapRenderer;
    private Player player;
    private InputManager inputManager;
    private PImage logo;
    private final SceneTransition transition = new SceneTransition();
    private final BGMusic bgMusic = new BGMusic(this, "bgmusic1.mp3");
    private final SoundFX walkingSound = new SoundFX(this, "walking.mp3");
    private SceneManager scenes;
    private Camera2D cam;



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
        assetsManager.loadSprite("knight", "knight.png");
        SpriteRenderer spriteRenderer = new SpriteRenderer(this, assetsManager);
        cam = new Camera2D();
        spriteRenderer.setCamera(cam);

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

        GameplayScene gameplayScene = new GameplayScene(engine, player, inputManager, walkingSound, assetsManager, tileMapRenderer, transition, cam);
        scenes.start(new SplashScene(scenes, logo, bgMusic, gameplayScene));
    }

    public void draw() {

        float dt = 1f / 60f;
        transition.update(dt);
        if (!transition.isBlocking()) {
            scenes.update(dt);
        }
        scenes.render();
        transition.render(this);

        
    }

    

    @Override
    public void keyPressed() {
        scenes.keyPressed(key, keyCode);
    }

    @Override
    public void keyReleased() {
        scenes.keyReleased(key, keyCode);


    }
}
