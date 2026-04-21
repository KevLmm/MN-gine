package scene;
import processing.core.PApplet;

public final class SceneManager {
    private Scene curr;
    private final PApplet p;
    private final SceneTransition transition;

    public SceneManager(PApplet p) {
        this(p, null);
    }

    public SceneManager(PApplet p, SceneTransition transition) {
        this.p = p;
        this.transition = transition;
    }

    public Scene getCurr() {
        return curr;
    }

    /*Call once from main game setup after assets are ready */
    public void start(Scene first) {
        if (first == null) {
            throw new IllegalArgumentException("First scene cannot be null");
        }
        curr = first;
        curr.enter(p);
    }

    /**
     * Leave old scene and move onto next
     * 
     */

    public void replace(Scene next) {
        if (next == null) {
            throw new IllegalArgumentException("Next scene cannot be null");
        }

        replaceNow(next);
    }

    /** Replace scene at mid-black if a transition exists, otherwise replace immediately. */
    public void replaceWithTransition(Scene next) {
        if (next == null) {
            throw new IllegalArgumentException("Next scene cannot be null");
        }
        if (transition == null) {
            replaceNow(next);
            return;
        }
        if (transition.isBlocking()) {
            return;
        }
        transition.begin(() -> replaceNow(next));
    }

    private void replaceNow(Scene next) {
        if (curr != null) {
            curr.exit();
        }

        curr = next;
        curr.enter(p);
    }

    public void update(float dt) {
        if (curr != null) {
            curr.update(dt);
        }
    }

    public void render() {
        if (curr != null) {
            curr.render();
        }
    }

    public void keyPressed(char key, int keyCode) {
        if (curr != null) {
            curr.keyPressed(key, keyCode);
        }
    }

    public void keyReleased(char key, int keyCode) {
        if (curr != null) {
            curr.keyReleased(key, keyCode);
        }
        
    }
}
