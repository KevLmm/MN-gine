package scene;

/**
 * One-way rule: standing on {@code fromMapId} at {@code fromEdge}, within the gate tile band,
 * triggers a fade swap to {@code toMapId}, then spawns the player on {@code spawnEdge} at
 * {@code spawnTileIndex} (tile row for LEFT/RIGHT spawns, tile column for TOP/BOTTOM).
 */

public final class MapExit {

    public final String fromMapId;
    public final SpawnEdge fromEdge;
    public final int gateStartTile;
    public final int gateEndTile;
    public final String toMapId;
    public final SpawnEdge spawnEdge;
    public final int spawnTileIndex;

    public MapExit(
        String fromMapId,
        SpawnEdge fromEdge,
        int gateStartTile,
        int gateEndTile,
        String toMapId,
        SpawnEdge spawnEdge,
        int spawnTileIndex) 
        
        {
        this.fromMapId = fromMapId;
        this.fromEdge = fromEdge;
        this.gateStartTile = gateStartTile;
        this.gateEndTile = gateEndTile;
        this.toMapId = toMapId;
        this.spawnEdge = spawnEdge;
        this.spawnTileIndex = spawnTileIndex;
    }

}
