package com.museumheist.game.progress;

public enum UpgradeType {
    SHOES("静音鞋底", "鞋", "每级提升 5% 移动速度。"),
    GLOVES("精密手套", "套", "每级缩短 10% 展品收取时间。"),
    BATTERY("高能电池", "电", "每级提升 10% 冲刺恢复速度。"),
    MAGNET("藏品磁扣", "磁", "每级扩大 12% 金币拾取范围。");

    private final String label;
    private final String badge;
    private final String description;

    UpgradeType(String label, String badge, String description) {
        this.label = label;
        this.badge = badge;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getBadge() {
        return badge;
    }

    public String getDescription() {
        return description;
    }
}