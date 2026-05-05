package entity;

/**
 * Callback from dungeon props into the active scene for modal text and optional gold totals.
 *
 * @see scene.GameplayScene
 */
@FunctionalInterface
public interface DungeonMessageSink {
    void showModal(String message);

    /** Adds loot gold when a chest grants it; default implementation does nothing. */
    default void awardGold(int amount) {
    }
}
