package entity;

public class InteractableComponent {

    private String actionId;


    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public InteractableComponent(String actionId) {
        this.actionId = actionId;
    }
}
