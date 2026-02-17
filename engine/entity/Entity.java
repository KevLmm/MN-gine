package entity;

import java.util.ArrayList;
import java.util.List;

//* Base for all game objects. Composed of components; 
    // systems query components to update and render. Subclass or add 
    // components to create own entities */
public abstract class Entity {

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
    
    
    
}
