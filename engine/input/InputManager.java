package input;

import java.util.*;

public class InputManager implements InputListener {
    private final Set<Integer> pressedKeys = new HashSet<>();
    private final Map<Integer, String> keyBindings = new HashMap<>();

    public void bindKey(int keyCode, String action) {
        keyBindings.put(keyCode, action);
    }

    public boolean isActionActive(String action) {
        for (int key : pressedKeys) {
            if (action.equals(keyBindings.get(key))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onKeyPressed(char key, int keyCode) {
        pressedKeys.add(keyCode);
    }
    
    @Override
    public void onKeyReleased(char key, int keyCode) {
        pressedKeys.remove(keyCode);
    }

    @Override public void onMousePressed(int b, int x, int y) {}
    @Override public void onMouseReleased(int b, int x, int y) {}
    @Override public void onMouseMoved(int x, int y) {}
    @Override public void onMouseDragged(int x, int y) {}
    @Override public void onMouseWheel(int delta) {}
    @Override public void onTouchStarted(int x, int y) {}
    @Override public void onTouchMoved(int x, int y) {}
    @Override public void onTouchEnded(int x, int y) {}
}
