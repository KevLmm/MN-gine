package entity;

import core.Drawable;
import rendering.DefaultDrawable;
import input.InputListener;

/**
 * The player entity. Uses Transform + Appearance so it can be drawn and moved.
 * To use custom visuals, get AppearanceComponent and setDrawable(yourDrawable).
 */
public class Player extends Entity implements InputListener {

    public static final float DEFAULT_WIDTH = 40;
    public static final float DEFAULT_HEIGHT = 60;

    public Player(float x, float y) {
        this(x, y, new DefaultDrawable());
    }

    /** Create a player with custom drawable so others can "upload their own visuals and behaviors". */
    public Player(float x, float y, Drawable drawable) {
        addComponent(new TransformComponent(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT));
        addComponent(new AppearanceComponent(drawable));
    }

    public TransformComponent getTransform() {
        return getComponent(TransformComponent.class);
    }

    public AppearanceComponent getAppearance() {
        return getComponent(AppearanceComponent.class);
    }
    @Override
    public void onKeyPressed(char key, int keyCode) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onKeyPressed'");
    }
    @Override
    public void onKeyReleased(char key, int keyCode) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onKeyReleased'");
    }
    @Override
    public void onMousePressed(int button, int x, int y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onMousePressed'");
    }
    @Override
    public void onMouseReleased(int button, int x, int y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onMouseReleased'");
    }
    @Override
    public void onMouseMoved(int x, int y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onMouseMoved'");
    }
    @Override
    public void onMouseDragged(int x, int y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onMouseDragged'");
    }
    @Override
    public void onMouseWheel(int delta) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onMouseWheel'");
    }
    @Override
    public void onTouchStarted(int x, int y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onTouchStarted'");
    }
    @Override
    public void onTouchMoved(int x, int y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onTouchMoved'");
    }
    @Override
    public void onTouchEnded(int x, int y) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'onTouchEnded'");
    }
}
