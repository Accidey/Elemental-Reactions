package com.xulai.elementalcraft.util;

import com.xulai.elementalcraft.ElementalCraft;
import com.xulai.elementalcraft.config.ElementalConfig;
import com.xulai.elementalcraft.config.ForcedItemConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class ForcedItemHelper {

    private static final Map<Item, WeaponData> WEAPON_CACHE = new ConcurrentHashMap<>();

    private static final Map<Item, ArmorTemplate> ARMOR_CACHE = new ConcurrentHashMap<>();

    private static final Map<Item, ArmorData> ARMOR_ROLLED_CACHE = new ConcurrentHashMap<>();

    private static volatile boolean weaponsParsed;

    private static volatile boolean armorParsed;

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

    private static void parseWeapons() {
        for (String line : ForcedItemConfig.FORCED_WEAPONS.get()) {
            try {
                String[] parts = line.split(",");
                if (parts.length < 2) continue;

                Item item = resolveItem(parts[0]);
                if (item == null) continue;

                ElementType attack = parseElement(parts[1]);
                if (attack == null || attack == ElementType.NONE) {
                    ElementalCraft.LOGGER.warn("[ElementalCraft] Invalid attack element in forced weapon entry: {}", line);
                    continue;
                }
                WEAPON_CACHE.put(item, new WeaponData(attack));
            } catch (Exception e) {
                ElementalCraft.LOGGER.warn("[ElementalCraft] Failed to parse forced weapon entry: {}", line, e);
            }
        }
        weaponsParsed = true;
    }

    private static void parseArmor() {
        for (String line : ForcedItemConfig.FORCED_ARMOR.get()) {
            try {
                String[] parts = line.split(",");
                if (parts.length < 5) continue;

                Item item = resolveItem(parts[0]);
                if (item == null) continue;

                ElementType enhance = parseElement(parts[1]);
                RangeValue enhanceRange = parsePointsRange(parts[2]);

                ElementType resist = parseElement(parts[3]);
                RangeValue resistRange = parsePointsRange(parts[4]);

                if (enhanceRange.max > 0 || resistRange.max > 0) {
                    if (enhance == null || resist == null) {
                        ElementalCraft.LOGGER.warn("[ElementalCraft] Invalid element in forced armor entry: {}", line);
                    }
                    ARMOR_CACHE.put(item, new ArmorTemplate(enhance, enhanceRange, resist, resistRange));
                }
            } catch (Exception e) {
                ElementalCraft.LOGGER.warn("[ElementalCraft] Failed to parse forced armor entry: {}", line, e);
            }
        }
        armorParsed = true;
    }

    private static Item resolveItem(String raw) {
        String id = stripQuotes(raw);
        if (id.isEmpty()) return null;

        ResourceLocation itemId = ResourceLocation.parse(id);
        if (!BuiltInRegistries.ITEM.containsKey(itemId)) {
            ElementalCraft.LOGGER.warn("[ElementalCraft] Unknown item in forced item config: {}", itemId);
            return null;
        }
        Item item = BuiltInRegistries.ITEM.get(itemId);
        if (item == null || item == Items.AIR) return null;
        return item;
    }

    private static String stripQuotes(String s) {
        if (s == null) return "";
        String val = s.trim();
        if (val.length() >= 2 && val.startsWith("\"") && val.endsWith("\"")) {
            val = val.substring(1, val.length() - 1).trim();
        }
        return val;
    }

    private static ElementType parseElement(String s) {
        if (s == null || s.isBlank()) return ElementType.NONE;
        return ElementType.fromId(stripQuotes(s));
    }

    public static int maxPointsOf(String raw) {
        return parsePointsRange(raw).max();
    }

    private static RangeValue parsePointsRange(String s) {
        if (s == null || s.isBlank()) return new RangeValue(0, 0, true);
        String val = stripQuotes(s);
        if (val.isEmpty()) return new RangeValue(0, 0, true);

        try {
            if (val.contains("-")) {
                String[] range = val.split("-");
                if (range.length == 2) {
                    int min = Integer.parseInt(range[0].trim());
                    int max = Integer.parseInt(range[1].trim());
                    return new RangeValue(Math.min(min, max), Math.max(min, max), false);
                }
            }
            int fixed = Integer.parseInt(val);
            return new RangeValue(fixed, fixed, true);
        } catch (NumberFormatException e) {
            ElementalCraft.LOGGER.warn("[ElementalCraft] Invalid point range in forced item config: {}", s);
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
