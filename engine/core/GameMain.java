package core;
import entity.Player;
import processing.core.PApplet;
import rendering.ShapeRenderer;
import entity.Entity;
import input.InputListener;

public class GameMain extends PApplet {

    Engine engine = new Engine();
    public static void main(String args[]) {
        
        PApplet.main("core.GameMain");
        
    }

    public void settings() {
        size(600, 600);
    }
    public void setup() {
        engine.setRenderer(new ShapeRenderer(this));
        // Create player at (100, 100). Others can replace its drawable via getAppearance().setDrawable(...)
        engine.registerEntity(new Player(100, 100));
    }

    public void draw() {
        background(0);
        engine.update(1/60f);
        engine.render();
    }

    public void keyPressed(char key, int keyCode) {
        for (Entity e : engine.getEntities()) {
            if (e instanceof InputListener) {
                ((InputListener) e).onKeyPressed(key, keyCode);
            }
        }
    }

    public void keyReleased(char key, int keyCode) {
        for (Entity e : engine.getEntities()) {
            if (e instanceof InputListener) {
                ((InputListener) e).onKeyReleased(key, keyCode);
            }
        }
    }

}
