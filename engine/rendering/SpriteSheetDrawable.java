package rendering;

import core.Drawable;
import core.Renderable;

public final class SpriteSheetDrawable implements Drawable {

    private final String sheetId;
    private final int frameW;
    private final int frameH;
    private final int strideX;
    private final int strideY;
    private final int originX; // set 1 if sheet has a left padding column
    private final int originY; // set 1 if sheet has a top padding row
    private final int columns;

    private int frameIndex;

    public SpriteSheetDrawable(String sheetId, int frameW, int frameH, int columns,
                                int strideX, int strideY, int originX, int originY) {
        this.sheetId = sheetId;
        this.frameW = frameW;
        this.frameH = frameH;
        this.strideX = strideX;
        this.strideY = strideY;
        this.originX = originX;
        this.originY = originY;
        this.columns = columns = Math.max(1, columns);
        
    }

    public void setFrameIndex(int frameIndex) {
        this.frameIndex = Math.max(0, frameIndex);
    }

    public int getFrameIndex() {
        return frameIndex;
    }

    @Override
    public void draw(Renderable r, float x, float y, float width, float height) {
        int col = frameIndex % columns;
        int row = frameIndex / columns;

        int srcX = originX + col * strideX;
        int srcY = originY + row * strideY;

        r.drawSpriteRegion(sheetId, x, y, width, height, srcX, srcY, frameW, frameH);

    }

}
