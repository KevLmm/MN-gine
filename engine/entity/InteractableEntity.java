package entity;

import rendering.CircleDrawable;

public class InteractableEntity extends Entity {

    public InteractableEntity(float x, float y) {
        super(x, y);
        addComponent(new TransformComponent(x, y, 50, 50));
        addComponent(new AppearanceComponent(new CircleDrawable(255, 100, 0)));
    }

    @Override
    public void onInteract() {
        System.out.println("You interacted with the orange circle!");
    }

}
