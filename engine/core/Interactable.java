package core;

/**
 * Optional interaction hooks for entities. {@link entity.Entity} supplies empty defaults;
 * gameplay normally overrides {@link #onInteract()} for the interact key.
 */
public interface Interactable {

    void interact();

    void onInteract();

    void onInteractEnd();

    void onInteractCancel();

    void onInteractStart();

    void onInteractUpdate();

}
