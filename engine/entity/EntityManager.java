package entity;
import java.util.*;

public class EntityManager {

    private List<Entity> entities = new ArrayList<>();

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public List<Entity> getEntities() {
        // Return a copy of the entities list to prevent external modification
        return new ArrayList<>(entities);
    }
}
