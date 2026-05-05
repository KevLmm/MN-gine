package scene;

import processing.core.PApplet;

/**
 * Root menu: pick overworld, dungeon, or survival. {@link GameplayScene} transitions back here via Esc / M;
 * the splash scene chains into this menu after the logo.
 */
public final class MainMenuScene extends Scene {

    private SceneManager scenes;
    private GameplayScene gameplay;
    private PApplet p;

    public void bind(SceneManager scenes, GameplayScene gameplay) {
        this.scenes = scenes;
        this.gameplay = gameplay;
    }

    @Override
    public void enter(PApplet p) {
        this.p = p;
    }

    @Override
    public void render() {
        if (p == null) {
            return;
        }
        p.background(36, 40, 48);
        p.fill(235);
        p.textAlign(PApplet.LEFT, PApplet.TOP);
        p.textSize(22);
        p.text("ModuCore — choose scene", 40, 36);
        p.textSize(16);
        p.text("1 — Overworld (skeleton)", 48, 88);
        p.text("2 — Dungeon", 48, 118);
        p.text("3 — Survival", 48, 148);
        p.textSize(13);
        p.fill(180);
        p.text("In gameplay: Esc or M — back to this menu", 48, 200);
    }

    @Override
    public void keyPressed(char key, int keyCode) {
        if (scenes == null || gameplay == null) {
            return;
        }
        char k = Character.toLowerCase(key);
        if (k == '1') {
            gameplay.configureStart("test");
            scenes.replaceWithTransition(gameplay);
        } else if (k == '2') {
            gameplay.configureStart("dungeon");
            scenes.replaceWithTransition(gameplay);
        } else if (k == '3') {
            gameplay.configureStart("survival");
            scenes.replaceWithTransition(gameplay);
        }
    }
}
