package scene;
import processing.core.PApplet;

public abstract class Scene {
    
    /*Called once when this scene becomes active */
    public void enter(PApplet p) {

    }

    /*Called once when leaving this scene(next scene about to enter) */
    public void exit() {

    }

    /*Every frame while active. */
    public void update(float dt) {

    }

    /*Every frame after update. */
    public void render() {

    }

    public void keyPressed(char key, int keyCode) {

    }

    public void keyReleased(char key, int keyCode) {
        
    }
}
