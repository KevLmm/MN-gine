package ui;
import java.util.List;
import processing.core.PApplet;


public class ui_frame {
    private float x, y, width, height;
    private String title;
    private boolean isVisible;

    public ui_frame(float x, float y, float width, float height, String title) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
