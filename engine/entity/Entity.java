package entity;

import java.util.ArrayList;
import java.util.List;
import core.Interactable;

/**
 * Base type for game objects. Entities bundle {@link Component}s; systems query components for
 * updates and rendering. Subclasses extend behavior or add components.
 */
public abstract class Entity implements Interactable {

    private float x, y;

    public Entity(float x, float y) {
        this.x = x;
        this.y = y;
    }

    private final List<Component> components = new ArrayList<>();

    public void addComponent(Component component) {
        components.add(component);
    }

    public <T extends Component> T getComponent(Class<T> componentClass) {
        for (Component c : components) {
            if (componentClass.isInstance(c)) {
                return componentClass.cast(c);
            }
        }
        return null;
    }

    public List<Component> getComponents() {
        return new ArrayList<>(components);
    }

    /**
     * When true, this entity is drawn after others so large props do not cover the hero.
     *
     * @see systems.RenderSystem
     */
    public boolean shouldDrawAfterOthers() {
        return false;
    }

    /** Called each frame; subclasses override for motion or logic. */
    public void update(float dt) {
    }

    /**
     * Default interaction pipeline. Most gameplay overrides {@link #onInteract()} only.
     */
    @Override
    public void interact() {
        onInteract();
        onInteractEnd();
        onInteractCancel();
        onInteractStart();
        onInteractUpdate();
    }

    @Override
    public void onInteract() {
    }

    @Override
    public void onInteractEnd() {
    }

    @Override
    public void onInteractCancel() {
    }

    @Override
    public void onInteractStart() {
    }

    @Override
    public void onInteractUpdate() {
    }

}
