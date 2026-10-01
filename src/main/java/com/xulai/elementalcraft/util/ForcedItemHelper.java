package com.xulai.elementalcraft.util;

import com.xulai.elementalcraft.ElementalCraft;
import com.xulai.elementalcraft.config.ElementalConfig;
import com.xulai.elementalcraft.config.ForcedItemConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class ForcedItemHelper {

    private static final Map<Item, WeaponData> WEAPON_CACHE = new ConcurrentHashMap<>();

    private static final Map<Item, ArmorTemplate> ARMOR_CACHE = new ConcurrentHashMap<>();

    private static final Map<Item, ArmorData> ARMOR_ROLLED_CACHE = new ConcurrentHashMap<>();

    private static volatile boolean weaponsParsed = false;
    private static volatile boolean armorParsed = false;

    private ForcedItemHelper() {}

    public static void clearCache() {
        WEAPON_CACHE.clear();
        ARMOR_CACHE.clear();
        ARMOR_ROLLED_CACHE.clear();
        weaponsParsed = false;
        armorParsed = false;
    }

    public record WeaponData(ElementType attackType) {}

    public record ArmorData(ElementType enhanceType, int enhancePoints, ElementType resistType, int resistPoints) {}

    private record ArmorTemplate(ElementType enhanceType, RangeValue enhanceRange, ElementType resistType, RangeValue resistRange) {}

    private record RangeValue(int min, int max, boolean isFixed) {
        public int roll() {
            if (isFixed) return min;
            return rollInRange(min, max);
        }
    }

    public static WeaponData getForcedWeapon(Item item) {
        if (!weaponsParsed) {
            parseWeapons();
        }
        return WEAPON_CACHE.get(item);
    }

    public static ArmorData getForcedArmor(Item item) {
        if (!armorParsed) {
            parseArmor();
        }

        ArmorTemplate template = ARMOR_CACHE.get(item);
        if (template == null) return null;

        ArmorData rolled = ARMOR_ROLLED_CACHE.get(item);
        if (rolled == null) {
            rolled = new ArmorData(template.enhanceType(), template.enhanceRange().roll(),
                    template.resistType(), template.resistRange().roll());
            ARMOR_ROLLED_CACHE.put(item, rolled);
        }
        return rolled;
    }

    @SuppressWarnings("deprecation")
    private static void parseWeapons() {
        for (String line : ForcedItemConfig.FORCED_WEAPONS.get()) {
            try {
                String[] parts = line.split(",");
                if (parts.length < 2) {
                    ElementalCraft.LOGGER.warn("[ElementalCraft] Skipped forced weapon entry with too few fields: {}", line);
                    continue;
                }

                Identifier itemId = Identifier.parse(parts[0].trim());
                Item item = BuiltInRegistries.ITEM.getValue(itemId);
                if (item == null) {
                    ElementalCraft.LOGGER.warn("[ElementalCraft] Unknown item id in forced weapon config: {}", itemId);
                    continue;
                }

                ElementType attack = ElementType.fromId(parts[1].trim());
                if (attack != null && attack != ElementType.NONE) {
                    WEAPON_CACHE.put(item, new WeaponData(attack));
                } else {
                    ElementalCraft.LOGGER.warn("[ElementalCraft] Unknown element id '{}' in forced weapon config for {}", parts[1].trim(), itemId);
                }
            } catch (Exception e) {
                ElementalCraft.LOGGER.warn("[ElementalCraft] Skipped invalid forced weapon entry: {}", line, e);
            }
        }
        weaponsParsed = true;
    }

    @SuppressWarnings("deprecation")
    private static void parseArmor() {
        for (String line : ForcedItemConfig.FORCED_ARMOR.get()) {
            try {
                String[] parts = line.split(",");
                if (parts.length < 5) {
                    ElementalCraft.LOGGER.warn("[ElementalCraft] Skipped forced armor entry with too few fields: {}", line);
                    continue;
                }

                Identifier itemId = Identifier.parse(parts[0].trim());
                Item item = BuiltInRegistries.ITEM.getValue(itemId);
                if (item == null) {
                    ElementalCraft.LOGGER.warn("[ElementalCraft] Unknown item id in forced armor config: {}", itemId);
                    continue;
                }

                ElementType enhance = parseElement(parts[1]);
                RangeValue enhanceRange = parsePointsRange(parts[2]);

                ElementType resist = parseElement(parts[3]);
                RangeValue resistRange = parsePointsRange(parts[4]);

                if (enhanceRange.max > 0 || resistRange.max > 0) {
                    ARMOR_CACHE.put(item, new ArmorTemplate(enhance, enhanceRange, resist, resistRange));
                }
            } catch (Exception e) {
                ElementalCraft.LOGGER.warn("[ElementalCraft] Skipped invalid forced armor entry: {}", line, e);
            }
        }
        armorParsed = true;
    }

    private static ElementType parseElement(String s) {
        if (s == null || s.isBlank()) return ElementType.NONE;
        ElementType type = ElementType.fromId(s.trim());
        if (type == null) {
            ElementalCraft.LOGGER.warn("[ElementalCraft] Unknown element id '{}' in forced armor config", s.trim());
        }
        return type;
    }

    public static int maxPointsOf(String raw) {
        return parsePointsRange(raw).max();
    }

    private static RangeValue parsePointsRange(String s) {
        if (s == null || s.isBlank()) return new RangeValue(0, 0, true);
        String val = s.trim();

        try {
            if (val.contains("-")) {
                String[] range = val.split("-");
                if (range.length == 2) {
                    int min = Integer.parseInt(range[0]);
                    int max = Integer.parseInt(range[1]);
                    return new RangeValue(Math.min(min, max), Math.max(min, max), false);
                }
            }
            int fixed = Integer.parseInt(val);
            return new RangeValue(fixed, fixed, true);
        } catch (NumberFormatException e) {
            return new RangeValue(0, 0, true);
        }
    }

    private static int rollInRange(int min, int max) {
        if (min < 0) min = 0;
        if (max < 0) max = 0;
        if (min > max) return min;
        if (min == max) return min;

        int rangeDiff = max - min;

        double c1 = ElementalConfig.chance0_20;
        double c2 = ElementalConfig.chance20_50;
        double c3 = ElementalConfig.chance50_80;

        double roll = ThreadLocalRandom.current().nextDouble();
        double s1 = c1;
        double s2 = s1 + c2;
        double s3 = s2 + c3;

        double minPct, maxPct;

        if (roll < s1) {
            minPct = 0.0;
            maxPct = 0.20;
        } else if (roll < s2) {
            minPct = 0.20;
            maxPct = 0.50;
        } else if (roll < s3) {
            minPct = 0.50;
            maxPct = 0.80;
        } else {
            minPct = 0.80;
            maxPct = 1.0;
        }

        int segmentMin = min + (int) (rangeDiff * minPct);
        int segmentMax = min + (int) (rangeDiff * maxPct);

        if (segmentMax < segmentMin) segmentMax = segmentMin;

        int result = segmentMin + ThreadLocalRandom.current().nextInt(segmentMax - segmentMin + 1);
        return (result / 10) * 10;
    }
}
