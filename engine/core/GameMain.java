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


public class GameMain extends PApplet {

    private Engine engine;
    private AssetsManager assetsManager;
    private Player player;
    private InputManager inputManager;

    public static void main(String args[]) {
        
        PApplet.main("core.GameMain");
        
    }

    public void settings() {
        size(600, 600);
    }
    public void setup() {
        assetsManager = new AssetsManager(this);
        assetsManager.loadSprite("knight", "data/knight.png");
        engine = new Engine();
        engine.setRenderer(new SpriteRenderer(this, assetsManager));
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
        background(0);
        engine.update(1/60f, width, height);
        engine.render();
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
