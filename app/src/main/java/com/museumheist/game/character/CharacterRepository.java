package com.museumheist.game.character;

import android.graphics.Color;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class CharacterRepository {
    private static CharacterConfig create(
            String id,
            String name,
            String description,
            String style,
            int primary,
            int secondary,
            int accent,
            int dark,
            int skin,
            int pants,
            int shoes,
            int variant
    ) {
        return new CharacterConfig(
                id,
                name,
                description,
                id + "_preview",
                id + "_home",
                id + "_game",
                style,
                1f,
                0f,
                1f,
                primary,
                secondary,
                accent,
                dark,
                skin,
                pants,
                shoes,
                variant
        );
    }

    private static final CharacterConfig[] DATA = {
            create(
                    "city_explorer",
                    "都市探员",
                    "轻装夹克与战术背包，适合第一次潜入行动。",
                    "均衡型",
                    Color.rgb(38, 111, 190),
                    Color.rgb(232, 238, 241),
                    Color.rgb(238, 128, 54),
                    Color.rgb(22, 28, 34),
                    Color.rgb(220, 174, 132),
                    Color.rgb(18, 28, 42),
                    Color.rgb(28, 35, 44),
                    0
            ),
            create(
                    "tech_scout",
                    "技术侦察员",
                    "搭载夜视护目镜和信号终端，科技感十足。",
                    "科技型",
                    Color.rgb(34, 52, 66),
                    Color.rgb(102, 196, 214),
                    Color.rgb(202, 216, 222),
                    Color.rgb(26, 34, 43),
                    Color.rgb(214, 169, 126),
                    Color.rgb(42, 48, 58),
                    Color.rgb(68, 86, 96),
                    1
            ),
            create(
                    "museum_researcher",
                    "文物研究员",
                    "熟悉展厅动线，长风衣与档案包更贴合博物馆氛围。",
                    "学者型",
                    Color.rgb(116, 91, 58),
                    Color.rgb(214, 197, 162),
                    Color.rgb(54, 97, 72),
                    Color.rgb(72, 45, 28),
                    Color.rgb(208, 162, 118),
                    Color.rgb(62, 72, 64),
                    Color.rgb(63, 48, 34),
                    2
            ),
            create(
                    "street_player",
                    "街头魔术师",
                    "用亮色连帽衫和耳机隐藏真实意图。",
                    "潮流型",
                    Color.rgb(92, 48, 150),
                    Color.rgb(30, 31, 42),
                    Color.rgb(236, 210, 56),
                    Color.rgb(31, 24, 36),
                    Color.rgb(218, 166, 124),
                    Color.rgb(36, 39, 48),
                    Color.rgb(245, 218, 71),
                    3
            ),
            create(
                    "fantasy_guardian",
                    "月影守望者",
                    "仪式披风与护肩构成独特的收藏级造型。",
                    "幻想型",
                    Color.rgb(226, 222, 200),
                    Color.rgb(174, 151, 216),
                    Color.rgb(124, 196, 222),
                    Color.rgb(220, 211, 180),
                    Color.rgb(225, 179, 137),
                    Color.rgb(82, 74, 116),
                    Color.rgb(236, 226, 192),
                    4
            )
    };

    private static final List<CharacterConfig> LIST =
            Collections.unmodifiableList(Arrays.asList(DATA));

    private CharacterRepository() {
    }

    public static List<CharacterConfig> getAll() {
        return LIST;
    }

    public static CharacterConfig getDefault() {
        return DATA[0];
    }

    public static CharacterConfig findById(String id) {
        for (CharacterConfig character : DATA) {
            if (character.getId().equals(id)) {
                return character;
            }
        }
        return getDefault();
    }

    public static int indexOf(String id) {
        for (int i = 0; i < DATA.length; i++) {
            if (DATA[i].getId().equals(id)) {
                return i;
            }
        }
        return 0;
    }
}