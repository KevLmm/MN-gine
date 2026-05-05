package scene;

import core.Engine;
import entity.CollisionRect;
import entity.DungeonInteractable;
import entity.DungeonMessageSink;
import entity.Entity;
import entity.Player;
import entity.TileMap;
import entity.TransformComponent;
import systems.CollisionSystem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Builds wall solids and invisible interactables for the dungeon tile map. Linear tile indices follow
 * row-major order: {@code index = row * width + column}.
 */
public final class DungeonLayout {

    private DungeonLayout() {
    }

    private static int col(int linear, int w) {
        return linear % w;
    }

    private static int row(int linear, int w) {
        return linear / w;
    }

    /** Same tile coverage as {@link systems.CollisionSystem#resolveTileMapCollisions}. */
    private static boolean hullOverlapsSolid(TileMap map, float x, float y, float w, float h) {
        int tw = map.getTileWidth();
        int th = map.getTileHeight();
        int tx0 = (int) Math.floor(x / tw);
        int ty0 = (int) Math.floor(y / th);
        int tx1 = (int) Math.floor((x + w - 1f) / tw);
        int ty1 = (int) Math.floor((y + h - 1f) / th);
        for (int ty = ty0; ty <= ty1; ty++) {
            for (int tx = tx0; tx <= tx1; tx++) {
                if (map.isSolid(tx, ty)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Places the physics hull (see {@link systems.CollisionSystem#hullOf}) centered in tile
     * {@code (c,r)} when it fits, then converts to sprite transform top-left using sprite size
     * {@code spriteW}×{@code spriteH}.
     */
    private static void positionInTileForHull(TileMap map, int c, int r, float spriteW, float spriteH,
            float hullW, float hullH, float[] out) {
        float tw = map.getTileWidth();
        float th = map.getTileHeight();
        float hx = c * tw;
        float hy = r * th;
        if (hullW <= tw) {
            hx += (tw - hullW) * 0.5f;
        } else {
            hx += 1f;
        }
        if (hullH <= th) {
            hy += (th - hullH) * 0.5f;
        } else {
            hy += 1f;
        }
        float tx = hx - (spriteW - hullW) * 0.5f;
        float ty = hy - (spriteH - hullH) * 0.5f;
        float maxX = Math.max(0f, map.getPixelWidth() - spriteW);
        float maxY = Math.max(0f, map.getPixelHeight() - spriteH);
        out[0] = Math.max(0f, Math.min(tx, maxX));
        out[1] = Math.max(0f, Math.min(ty, maxY));
    }

    /**
     * If the hull overlaps any solid, pick a nearby free tile using the same placement rules as
     * {@link #placePlayerAtIndex}. Tiles are tried in order of squared offset from the anchor
     * tile so we do not run east along a corridor to the opposite corner (the old 2px slide did).
     */
    private static void snapSpawnClearOfSolids(Player player, TileMap map, int anchorLinear, int mapWidthTiles) {
        TransformComponent t = player.getTransform();
        if (t == null) {
            return;
        }
        float spriteW = t.getWidth();
        float spriteH = t.getHeight();
        CollisionRect h0 = CollisionSystem.hullOf(player, t);
        float hullW = h0.width;
        float hullH = h0.height;
        if (!hullOverlapsSolid(map, h0.x, h0.y, hullW, hullH)) {
            return;
        }
        int ac = col(anchorLinear, mapWidthTiles);
        int ar = row(anchorLinear, mapWidthTiles);
        int mh = map.getHeight();
        int radius = Math.max(mapWidthTiles, mh);
        List<int[]> offsets = new ArrayList<>((2 * radius + 1) * (2 * radius + 1));
        for (int dc = -radius; dc <= radius; dc++) {
            for (int dr = -radius; dr <= radius; dr++) {
                offsets.add(new int[] { dc, dr });
            }
        }
        offsets.sort(Comparator
                .comparingInt((int[] o) -> o[0] * o[0] + o[1] * o[1])
                .thenComparingInt(o -> Math.abs(o[0]) + Math.abs(o[1])));
        float[] tmp = new float[2];
        for (int[] o : offsets) {
            int nc = ac + o[0];
            int nr = ar + o[1];
            if (nc < 0 || nr < 0 || nc >= mapWidthTiles || nr >= mh) {
                continue;
            }
            positionInTileForHull(map, nc, nr, spriteW, spriteH, hullW, hullH, tmp);
            t.setX(tmp[0]);
            t.setY(tmp[1]);
            player.syncBodyFromTransform();
            CollisionRect chk = CollisionSystem.hullOf(player, t);
            if (!hullOverlapsSolid(map, chk.x, chk.y, chk.width, chk.height)) {
                return;
            }
        }
    }

    /**
     * Places the player on {@code linearIndex}. Centers in the tile when the hull fits in one tile;
     * otherwise uses a small inset. Then snaps so the hull does not overlap solids (centering can
     * overlap the column to the left, e.g. dungeon border in column 1).
     */
    public static void placePlayerAtIndex(Player player, TileMap map, int linearIndex) {
        int w = map.getWidth();
        TransformComponent t = player.getTransform();
        if (t == null) {
            return;
        }
        int c = col(linearIndex, w);
        int r = row(linearIndex, w);
        float spriteW = t.getWidth();
        float spriteH = t.getHeight();
        CollisionRect ph = CollisionSystem.hullOf(player, t);
        float[] pos = new float[2];
        positionInTileForHull(map, c, r, spriteW, spriteH, ph.width, ph.height, pos);
        float px = pos[0];
        float py = pos[1];

        t.setX(px);
        t.setY(py);
        player.syncBodyFromTransform();
        snapSpawnClearOfSolids(player, map, linearIndex, w);
    }

    private static void solidLinears(TileMap map, int[] cells) {
        int w = map.getWidth();
        for (int id : cells) {
            map.setSolid(col(id, w), row(id, w), true);
        }
    }

    /** Invisible interact zone over the 2×2 block spanned by four linear indices. */
    private static DungeonInteractable registerDungeonTrigger(Engine engine, TileMap map,
            DungeonMessageSink sink, int a, int b, int c, int d, DungeonInteractable.Kind kind) {
        int w = map.getWidth();
        int c0 = col(a, w), c1 = col(b, w), c2 = col(c, w), c3 = col(d, w);
        int r0 = row(a, w), r1 = row(b, w), r2 = row(c, w), r3 = row(d, w);
        int minC = Math.min(Math.min(c0, c1), Math.min(c2, c3));
        int maxC = Math.max(Math.max(c0, c1), Math.max(c2, c3));
        int minR = Math.min(Math.min(r0, r1), Math.min(r2, r3));
        int maxR = Math.max(Math.max(r0, r1), Math.max(r2, r3));

        float tw = map.getTileWidth();
        float th = map.getTileHeight();
        float x = minC * tw;
        float y = minR * th;
        float bw = (maxC - minC + 1) * tw;
        float bh = (maxR - minR + 1) * th;

        DungeonInteractable trigger = new DungeonInteractable(
                engine, x, y, bw, bh, kind, map, sink, minC, minR, maxC, maxR);
        engine.registerEntity(trigger);
        return trigger;
    }

    /**
     * Applies solids, optional default player spawn, and dungeon interactables. Returns every
     * entity registered here so the scene can {@link Engine#removeEntity} them when leaving.
     *
     * @param placePlayerAtDefaultSpawn when true, moves the player to the demo tile index; when
     *        false, leaves position unchanged (e.g. after an edge transition spawned the player).
     * @param messageSink receives modal text for chests, gates, and NPC (never null).
     */
    public static List<Entity> applyDungeonDemo(Engine engine, Player player, TileMap map,
            boolean placePlayerAtDefaultSpawn, DungeonMessageSink messageSink) {
        List<Entity> spawned = new ArrayList<>();
        int w = map.getWidth();
        int h = map.getHeight();

        // Borders
        for (int r = 0; r < h; r++) {
            map.setSolid(1, r, true);
            map.setSolid(24, r, true);
        }
        // Bottom edge solid row (index h - 1); matches the bottom border in the 26×26 dungeon art.
        for (int c = 0; c < w; c++) {
            map.setSolid(c, h - 1, true);
        }

        // Middle walls
        for (int r = 0; r < h; r++) {
            map.setSolid(12, r, true);
            map.setSolid(13, r, true);
        }
        int hr1 = row(312, w);
        int hr2 = row(338, w);
        if (hr1 >= 0 && hr1 < h) {
            for (int c = 0; c < w; c++) map.setSolid(c, hr1, true);
        }
        if (hr2 >= 0 && hr2 < h) {
            for (int c = 0; c < w; c++) map.setSolid(c, hr2, true);
        }

        // Extra wall tiles
        solidLinears(map, new int[] {
            524, 525, 550, 551, 528, 529, 554, 555, 420, 421, 446, 447, 424, 425, 450, 451,
            108, 109, 134, 135, 112, 113, 138, 139, 216, 217, 242, 243, 120, 121, 146, 147,
            224, 225, 250, 251, 124, 150, 151, 228, 229, 254, 255, 432, 433, 458, 459, 536,
            537, 562, 563, 436, 437, 462, 463, 540, 541, 566, 567
        });

        // Urns = solid cells (art already drawn)
        solidLinears(map, new int[] { 496, 497, 470, 471, 372, 373, 398, 399 });

        // Gates (open on interact; tile art is in the map)
        spawned.add(registerDungeonTrigger(engine, map, messageSink, 168, 169, 194, 195, DungeonInteractable.Kind.GATE));
        spawned.add(registerDungeonTrigger(engine, map, messageSink, 480, 481, 506, 507, DungeonInteractable.Kind.GATE)); // bottom vertical gate
        spawned.add(registerDungeonTrigger(engine, map, messageSink, 318, 319, 344, 345, DungeonInteractable.Kind.GATE));
        spawned.add(registerDungeonTrigger(engine, map, messageSink, 330, 331, 356, 357, DungeonInteractable.Kind.GATE));

        spawned.add(registerDungeonTrigger(engine, map, messageSink, 418, 419, 444, 445, DungeonInteractable.Kind.CHEST));
        spawned.add(registerDungeonTrigger(engine, map, messageSink, 374, 375, 400, 401, DungeonInteractable.Kind.CHEST));
        spawned.add(registerDungeonTrigger(engine, map, messageSink, 430, 431, 456, 457, DungeonInteractable.Kind.CHEST));
        spawned.add(registerDungeonTrigger(engine, map, messageSink, 380, 381, 406, 407, DungeonInteractable.Kind.CHEST));
        spawned.add(registerDungeonTrigger(engine, map, messageSink, 474, 475, 500, 501, DungeonInteractable.Kind.NPC));

        // After all solids exist: spawn uses final geometry (wide hulls overlap fewer walls).
        if (placePlayerAtDefaultSpawn) {
            placePlayerAtIndex(player, map, 522);
        }
        return spawned;
    }
}