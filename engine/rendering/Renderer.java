package rendering;


public interface Renderer {
    void drawRect(float x, float y, float width, float height);
    void drawCircle(float x, float y, float radius);
    void drawText(String text, float x, float y);
}
