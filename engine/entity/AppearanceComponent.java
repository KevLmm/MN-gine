package entity;

import core.Drawable;

/**
 * How the entity is drawn. Use setDrawable() to plug in custom visuals
 * (sprites, images, etc.) so others can upload their own content.
 */
public class AppearanceComponent extends Component {
    private Drawable drawable;

    public AppearanceComponent(Drawable drawable) {
        this.drawable = drawable;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    /** Replace with a custom Drawable to use your own sprite/visual. */
    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }
}
