package com.museumheist.game.world;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;

import com.museumheist.game.entity.Door;
import com.museumheist.game.entity.Guard;
import com.museumheist.game.entity.KeyItem;
import com.museumheist.game.entity.Laser;
import com.museumheist.game.entity.SecurityCamera;
import com.museumheist.game.logic.PatrolPath;

import java.util.ArrayList;
import java.util.List;

/** Builds ten larger galleries with distinct routes and an escalating security plan. */
public final class LevelFactory {
    private static final float[] TREASURE_X = {0.15f, 0.36f, 0.46f, 0.73f, 0.25f, 0.58f, 0.87f};
    private static final float[] TREASURE_Y = {0.30f, 0.17f, 0.52f, 0.26f, 0.69f, 0.58f, 0.66f};
    private static final float[] PATROL_BAND_Y = {0.16f, 0.81f, 0.19f, 0.89f, 0.205f, 0.92f, 0.175f};

    private LevelFactory() {
    }

    public static List<Level> createLevels() {
        String[] titles = {
                "夜色前厅", "青铜长廊", "古卷密库", "机械回廊", "雕塑中庭",
                "海洋展区", "镜影展厅", "天文穹顶", "核心安防层", "终极珍藏馆"
        };
        String[] objectives = {
                "穿过交错巡逻线，至少取得一件藏品后自行选择撤离时机。",
                "利用展墙盲区避开双层巡逻，并判断激光关闭窗口。",
                "找到门禁钥匙，规划一条不被摄像头覆盖的折返路线。",
                "在机器人与旋转镜头之间切换掩体，控制行动节奏。",
                "在多名守卫的交叉视野中清空中央展区或快速撤离。",
                "识别工作人员与诱饵路线，安全带走目标展品。",
                "先取得蓝钥匙，再突破双重门禁抵达远端撤离点。",
                "利用遮挡、巡逻盲区与激光间歇穿过镜影展区。",
                "干扰摄像头与机器人，逐层瓦解核心安防网络。",
                "综合运用钥匙、道具、路线记忆与潜行技巧完成最终行动。"
        };

        List<Level> levels = new ArrayList<>();
        for (int index = 0; index < titles.length; index++) {
            levels.add(create(index, titles[index], objectives[index]));
        }
        return levels;
    }

    private static Level create(int index, String title, String objective) {
        float width = 2400f + index * 90f;
        float height = 1350f + index * 50f;
        float edge = 52f;
        RectF bounds = new RectF(edge, edge, width - edge, height - edge);
        RectF exit = new RectF(width - 180f, height - 220f, width - 72f, height - 92f);

        List<Wall> walls = createWalls(index, width, height);
        List<RectF> treasures = createTreasures(index, width, height);
        List<Guard> guards = createGuards(index, width, height);
        List<Laser> lasers = createLasers(index, width, height);
        List<Door> doors = new ArrayList<>();
        List<KeyItem> keys = new ArrayList<>();
        createDoorsAndKeys(index, width, height, doors, keys);
        List<SecurityCamera> cameras = createCameras(index, width, height);
        List<PointF> spawns = createSpawnPoints(width, height);

        return new Level(
                title,
                objective,
                width,
                height,
                bounds,
                treasures,
                exit,
                118f,
                height - 118f,
                walls,
                guards,
                lasers,
                doors,
                keys,
                cameras,
                spawns
        );
    }

    private static List<Wall> createWalls(int index, float width, float height) {
        List<Wall> walls = new ArrayList<>();
        float edge = 46f;
        walls.add(new Wall(0f, 0f, width, edge));
        walls.add(new Wall(0f, height - edge, width, height));
        walls.add(new Wall(0f, 0f, edge, height));
        walls.add(new Wall(width - edge, 0f, width, height));

        int islands = 4 + index % 2;
        float wallThickness = 46f + index * 1.2f;
        for (int island = 0; island < islands; island++) {
            float x = width * (0.22f + island * (0.56f / Math.max(1, islands - 1)));
            boolean upper = (island + index) % 2 == 0;
            float top = height * (upper ? 0.23f : 0.51f);
            float bottom = height * (upper ? 0.49f : 0.77f);
            walls.add(new Wall(x - wallThickness * 0.5f, top, x + wallThickness * 0.5f, bottom));

            // Short display wings create cover without sealing the surrounding corridor.
            float wingY = upper ? bottom : top;
            float direction = island % 2 == 0 ? 1f : -1f;
            float wingLeft = direction > 0f ? x : x - width * 0.085f;
            float wingRight = direction > 0f ? x + width * 0.085f : x;
            walls.add(new Wall(wingLeft, wingY - wallThickness * 0.5f,
                    wingRight, wingY + wallThickness * 0.5f));
        }

        // Side galleries break long sightlines and create meaningful route choices.
        walls.add(new Wall(width * 0.08f, height * 0.39f,
                width * (0.23f + index * 0.002f), height * 0.39f + wallThickness));
        walls.add(new Wall(width * 0.72f, height * 0.60f,
                width * 0.91f, height * 0.60f + wallThickness));
        if (index >= 3) {
            walls.add(new Wall(width * 0.36f, height * 0.83f,
                    width * 0.56f, height * 0.83f + wallThickness));
        }
        if (index >= 6) {
            walls.add(new Wall(width * 0.39f, height * 0.09f,
                    width * 0.62f, height * 0.09f + wallThickness));
        }
        return walls;
    }

    private static List<RectF> createTreasures(int index, float width, float height) {
        int count = Math.min(TREASURE_X.length, 4 + index / 2);
        List<RectF> treasures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            float x = width * TREASURE_X[i];
            float y = height * TREASURE_Y[i];
            float halfWidth = 40f + (i % 2) * 5f;
            float halfHeight = 31f + (i % 3) * 3f;
            treasures.add(new RectF(x - halfWidth, y - halfHeight, x + halfWidth, y + halfHeight));
        }
        return treasures;
    }

    private static List<Guard> createGuards(int index, float width, float height) {
        List<Guard> guards = new ArrayList<>();
        int count = Math.min(8, 3 + index / 2);
        float baseSpeed = 118f + index * 5.5f;
        float viewDistance = 410f + index * 18f;
        float viewAngle = (float) Math.toRadians(72f + Math.min(10f, index * 1.1f));

        // The outside loop stays beyond the side-gallery walls and clears every late-game wall band.
        guards.add(new Guard(
                Guard.Kind.GUARD,
                new PatrolPath(
                        new PointF(width * 0.055f, height * 0.145f),
                        new PointF(width * 0.945f, height * 0.145f),
                        new PointF(width * 0.945f, height * 0.925f),
                        new PointF(width * 0.055f, height * 0.925f)
                ),
                baseSpeed,
                viewDistance,
                viewAngle
        ));

        // Remaining actors sweep open upper/lower corridors. Their complete segments avoid display walls,
        // so visual patrol motion never clips through architecture even though guards do not use wall collision.
        for (int n = 1; n < count; n++) {
            float bandY = PATROL_BAND_Y[(n - 1) % PATROL_BAND_Y.length];
            float left = 0.085f + (n % 3) * 0.012f;
            float right = 0.915f - (n % 2) * 0.014f;
            PatrolPath path = new PatrolPath(
                    new PointF(width * left, height * bandY),
                    new PointF(width * right, height * bandY)
            );
            Guard.Kind kind;
            if (index >= 4 && n % 3 == 0) {
                kind = Guard.Kind.ROBOT;
            } else if (n % 3 == 1) {
                kind = Guard.Kind.STAFF;
            } else {
                kind = Guard.Kind.GUARD;
            }
            float kindSpeed = kind == Guard.Kind.STAFF ? baseSpeed * 0.92f
                    : kind == Guard.Kind.ROBOT ? baseSpeed * 1.08f : baseSpeed;
            guards.add(new Guard(kind, path, kindSpeed,
                    viewDistance + (kind == Guard.Kind.ROBOT ? 65f : 0f),
                    viewAngle + (kind == Guard.Kind.ROBOT ? 0.10f : 0f)));
        }
        return guards;
    }

    private static List<Laser> createLasers(int index, float width, float height) {
        List<Laser> lasers = new ArrayList<>();
        int count = Math.min(6, 1 + index / 2);
        for (int n = 0; n < count; n++) {
            boolean vertical = (n + index) % 2 == 0;
            float phase = n * 0.63f + index * 0.18f;
            if (vertical) {
                float x = width * (0.31f + n * 0.105f);
                lasers.add(new Laser(x, height * 0.16f, x, height * 0.84f,
                        3.7f - Math.min(0.6f, index * 0.045f), 1.72f + index * 0.025f, phase));
            } else {
                float y = height * (0.31f + n * 0.10f);
                lasers.add(new Laser(width * 0.13f, y, width * 0.87f, y,
                        3.9f - Math.min(0.7f, index * 0.05f), 1.65f + index * 0.03f, phase));
            }
        }
        return lasers;
    }

    private static void createDoorsAndKeys(int index, float width, float height,
                                           List<Door> doors, List<KeyItem> keys) {
        if (index >= 1) {
            int gold = Color.rgb(226, 194, 75);
            doors.add(new Door(new RectF(width * 0.65f, height * 0.43f,
                    width * 0.65f + 50f, height * 0.57f), "gold", "金色门", gold));
            keys.add(new KeyItem("gold", "金钥匙", gold, width * 0.19f, height * 0.80f, 24f));
        }
        if (index >= 5) {
            int blue = Color.rgb(77, 135, 220);
            doors.add(new Door(new RectF(width * 0.81f, height * 0.22f,
                    width * 0.81f + 50f, height * 0.38f), "blue", "蓝色门", blue));
            keys.add(new KeyItem("blue", "蓝钥匙", blue, width * 0.52f, height * 0.46f, 24f));
        }
        if (index >= 8) {
            int crimson = Color.rgb(198, 68, 76);
            doors.add(new Door(new RectF(width * 0.46f, height * 0.68f,
                    width * 0.46f + 52f, height * 0.84f), "crimson", "赤色门", crimson));
            keys.add(new KeyItem("crimson", "赤钥匙", crimson, width * 0.12f, height * 0.48f, 24f));
        }
    }

    private static List<SecurityCamera> createCameras(int index, float width, float height) {
        List<SecurityCamera> cameras = new ArrayList<>();
        int count = Math.min(6, 1 + index / 2);
        int islands = 4 + index % 2;
        for (int n = 0; n < count; n++) {
            int island = n % islands;
            float x = width * (0.22f + island * (0.56f / Math.max(1, islands - 1)));
            boolean upper = (island + index) % 2 == 0;
            float y = height * (upper ? 0.19f : 0.81f);
            float base = upper ? (float) Math.PI * 0.5f : (float) -Math.PI * 0.5f;
            cameras.add(new SecurityCamera(
                    x,
                    y,
                    base,
                    0.82f + Math.min(0.22f, index * 0.025f),
                    0.78f + index * 0.035f,
                    470f + index * 18f,
                    (float) Math.toRadians(68f + Math.min(8f, index))
            ));
        }
        return cameras;
    }

    private static List<PointF> createSpawnPoints(float width, float height) {
        List<PointF> spawns = new ArrayList<>();
        for (float y = 150f; y <= height - 150f; y += 180f) {
            for (float x = 150f; x <= width - 150f; x += 220f) {
                spawns.add(new PointF(x, y));
            }
        }
        return spawns;
    }
}
