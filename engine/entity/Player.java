package entity;

import javax.xml.crypto.dsig.Transform;

import core.Drawable;
import rendering.DefaultDrawable;
import input.InputListener;
import input.InputManager;

/**
 * The player entity. Uses Transform + Appearance so it can be drawn and moved.
 * To use custom visuals, get AppearanceComponent and setDrawable(yourDrawable).
 */
public class Player extends Entity implements InputListener {
    private InputManager input;
    public static final float DEFAULT_WIDTH = 40;
    public static final float DEFAULT_HEIGHT = 60;
    private float x, y;
    

    public Player(float x, float y) {
        this(x, y, new DefaultDrawable());
    }

    /** Create a player with custom drawable so others can "upload their own visuals and behaviors". */
    public Player(float x, float y, Drawable drawable) {
        addComponent(new TransformComponent(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT));
        addComponent(new AppearanceComponent(drawable));
        getTransform().setSpeed(200, 200);
        this.x = x;
        this.y = y;
        
    }

    public TransformComponent getTransform() {
        return getComponent(TransformComponent.class);
    }

    public AppearanceComponent getAppearance() {
        return getComponent(AppearanceComponent.class);
    }

    public void update(float dt) {
        float dx = 0, dy = 0;
        if (input.isActionActive("MOVE_LEFT")) dx -= 1;
        if (input.isActionActive("MOVE_RIGHT")) dx += 1;
        if (input.isActionActive("MOVE_UP")) dy -= 1;
        if (input.isActionActive("MOVE_DOWN")) dy += 1;

        if (dx != 0 || dy != 0) {
            float inv = (float)(1.0 / Math.sqrt(2));
            dx *= inv; dy *= inv;
        }
        TransformComponent t = getTransform();

        x = t.getX() + dx * t.getSpeedX() * dt;
        y = t.getY() + dy * t.getSpeedY() * dt;
        t.setX(x);
        t.setY(y);

    }

    public void setInput(InputManager input) {
        this.input = input;
        
    }

    @Override
    public void onKeyPressed(char key, int keyCode) {
        input.onKeyPressed(key, keyCode);
        
    }
    @Override
    public void onKeyReleased(char key, int keyCode) {
        input.onKeyReleased(key, keyCode);
    }
    @Override
    public void onMousePressed(int button, int x, int y) {
        input.onMousePressed(button, x, y);
    }
    @Override
    public void onMouseReleased(int button, int x, int y) {
        input.onMouseReleased(button, x, y);
    }
    @Override
    public void onMouseMoved(int x, int y) {
        input.onMouseMoved(x, y);
    }
    @Override
    public void onMouseDragged(int x, int y) {
        input.onMouseDragged(x, y);
    }
    @Override
    public void onMouseWheel(int delta) {
        input.onMouseWheel(delta);
    }
    @Override
    public void onTouchStarted(int x, int y) {
        input.onTouchStarted(x, y);
    }
    @Override
    public void onTouchMoved(int x, int y) {
        input.onTouchMoved(x, y);
    }
    @Override
    public void onTouchEnded(int x, int y) {
        input.onTouchEnded(x, y);
    }
}
