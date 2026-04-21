package scene;

import processing.core.PApplet;
import processing.core.PImage;
import music.BGMusic;

public final class SplashScene extends Scene {

    private final SceneManager scenes;
    private final Scene nextScene;
    private PApplet p;
    private PImage logo;
    private final BGMusic bgMusic;

    private int startMs;
    private static final int SPLASH_MS = 2000;
    
    private boolean transitioned = false;
    

    public SplashScene(SceneManager scenes, PImage logo, BGMusic bgMusic, 
        Scene nextScene) {
        this.scenes = scenes;
        this.logo = logo;
        this.bgMusic = bgMusic;
        this.nextScene = nextScene;
    }

    @Override
    public void enter(PApplet p) {
        this.p = p;
        this.startMs = p.millis();
        this.transitioned = false;

    }
    @Override
    public void update(float dt) {
        if (transitioned) return;
        
        if (p.millis() - startMs >= SPLASH_MS) {
            bgMusic.setVolume(0.5f);
            bgMusic.playLoop();
            transitioned = true;
            scenes.replaceWithTransition(nextScene);
        }
        
    }

    @Override
    public void render() {
        p.background(0);
        if (logo == null) {
            logo = p.loadImage(p.dataPath("MDC.png"));
        }
        if (logo != null) {
            p.imageMode(PApplet.CENTER);
            p.image(logo, p.width / 2.0f, p.height / 2.0f);
            p.fill(255);
            p.textAlign(PApplet.CENTER, PApplet.CENTER);
            p.text("Powered by ModuCore", p.width / 2.0f, p.height / 2.0f + 100);
        } else {
            p.fill(255);
            p.textAlign(PApplet.CENTER, PApplet.CENTER);
            p.text("Engine Logo", p.width / 2.0f, p.height / 2.0f);
        }
    }
    
}
