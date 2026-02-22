package entity;

import java.util.ArrayList;
import java.util.List;
import core.Interactable;

//* Base for all game objects. Composed of components; 
    // systems query components to update and render. Subclass or add 
    // components to create own entities */
public abstract class Entity implements Interactable{

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

    /** Override in subclasses for per-frame logic. */
    public void update(float dt) {
    }

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
        System.out.println("Interacting with " + this.getClass().getName());
    }

    @Override
    public void onInteractEnd() {
        System.out.println("Interact ended with " + this.getClass().getName());
    }

    @Override
    public void onInteractCancel() {
        System.out.println("Interact canceled with " + this.getClass().getName());
    }

    @Override
    public void onInteractStart() {
        System.out.println("Interact started with " + this.getClass().getName());
    }

    @Override
    public void onInteractUpdate() {
        System.out.println("Interact updated with " + this.getClass().getName());
    }

}
