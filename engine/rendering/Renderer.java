package rendering;

import core.Renderable;
public interface Renderer {

    void render(Renderable renderable);
    void clear();
    void drawRect(float x, float y, float width, float height);
    void drawCircle(float x, float y, float width, float height, int r, int g, int b);
    void drawSprite(String assetId, float x, float y, float w, float h, int frameIndex);
    void drawText(String text, float x, float y);
}
