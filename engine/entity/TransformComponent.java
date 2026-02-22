package entity;

/**
 * Position and size of an entity. Attach to any entity that has a place in the world.
 */
public class TransformComponent extends Component {
    private float x, y, width, height;
    private float speedX, speedY;
    public TransformComponent(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speedX = 5;
        this.speedY = 5;
    }

    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public float getWidth() { return width; }
    public void setWidth(float width) { this.width = width; }
    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }
    public float getSpeedX() { return speedX; }
    public void setSpeedX(float speedX) { this.speedX = speedX; }
    public float getSpeedY() { return speedY; }
    public void setSpeedY(float speedY) { this.speedY = speedY;}
    public void setSpeed(float speedX, float speedY) { 
        this.speedX = speedX; this.speedY = speedY; }
}
