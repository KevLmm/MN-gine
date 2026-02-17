package input;

//* Interface for objects that want to receive input events */

public interface InputListener {
    void onKeyPressed(char key, int keyCode);
    void onKeyReleased(char key, int keyCode);
    void onMousePressed(int button, int x, int y);
    void onMouseReleased(int button, int x, int y);
    void onMouseMoved(int x, int y);
    void onMouseDragged(int x, int y);
    void onMouseWheel(int delta);
    void onTouchStarted(int x, int y);
    void onTouchMoved(int x, int y);
    void onTouchEnded(int x, int y);
}
