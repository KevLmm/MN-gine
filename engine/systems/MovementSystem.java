package systems;
import entity.Entity;
import entity.TransformComponent;
import input.InputListener;


public class MovementSystem {

    private char key;
    private int keyCode; 
    public void movement(Entity entity, float dt) {
        TransformComponent t = entity.getComponent(TransformComponent.class);
        if (t != null) {
            t.setX(t.getX() + t.getSpeedX() * dt);
            t.setY(t.getY() + t.getSpeedY() * dt);
            t.setSpeed(200, 200);
            if (entity instanceof InputListener) {
                ((InputListener) entity).onKeyPressed(key, keyCode);
                ((InputListener) entity).onKeyReleased(key, keyCode);
                
            }


        }
    }
}
