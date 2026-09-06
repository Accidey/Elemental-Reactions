package com.xulai.elementalcraft.util;

import com.xulai.elementalcraft.config.ElementalConfig;
import com.xulai.elementalcraft.enchantment.ModEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.*;

public class AttributeEquipUtils {

    private static final Random RANDOM = new Random();

    public static int[] distributePointsToLevels(int totalPoints, int pointsPerLevel, int pieceCount) {
        if (pointsPerLevel <= 0) pointsPerLevel = 1;

        int totalLevelsNeeded = totalPoints / pointsPerLevel;
        int[] levels = new int[pieceCount];

        Arrays.fill(levels, 0);

        if (totalLevelsNeeded <= 0) {
            if (totalPoints > 0) {
                levels[RANDOM.nextInt(pieceCount)] = 1;
            }
            return levels;
        }

        int baseLevel = totalLevelsNeeded / pieceCount;
        int remainingLevels = totalLevelsNeeded % pieceCount;

        Arrays.fill(levels, baseLevel);

        for (int i = 0; i < remainingLevels; i++) {
            int chosen = RANDOM.nextInt(pieceCount);
            levels[chosen]++;
        }

        int maxConfigLevel = ElementalConfig.getMaxStatCap() / pointsPerLevel;
        if (maxConfigLevel <= 0) maxConfigLevel = 1;

        for (int i = 0; i < pieceCount; i++) {
            if (levels[i] > maxConfigLevel) levels[i] = maxConfigLevel;
        }

        return levels;
    }

    public static void applyAttackEnchant(ItemStack stack, ElementType type) {
        if (stack.isEmpty() || type == null || type == ElementType.NONE) return;
        Holder<Enchantment> ench = getAttackEnchantment(type);
        if (ench != null) stack.enchant(ench, 1);
    }

    public static void applyUnbreaking(ItemStack stack, int level) {
        if (stack.isEmpty()) return;
        stack.enchant(ModEnchantments.vanilla(Enchantments.UNBREAKING), level);
    }

    public static void applyArmorEnchants(ItemStack stack, ElementType enhType, int enhPoints, ElementType resType, int resPoints, int pointsPerLevelDivider) {
        if (stack.isEmpty()) return;

        if (pointsPerLevelDivider <= 0) pointsPerLevelDivider = 1;

        int maxConfigLevel = ElementalConfig.getMaxStatCap() / pointsPerLevelDivider;
        if (maxConfigLevel <= 0) maxConfigLevel = 1;

        int enhLv = (enhPoints > 0) ? Math.max(1, Math.min(maxConfigLevel, enhPoints / pointsPerLevelDivider)) : 0;
        int resLv = (resPoints > 0) ? Math.max(1, Math.min(maxConfigLevel, resPoints / pointsPerLevelDivider)) : 0;

        applyArmorEnchantsLevel(stack, enhType, enhLv, resType, resLv);
    }

    public static void applyArmorEnchantsLevel(ItemStack stack, ElementType enhType, int enhLv, ElementType resType, int resLv) {
        if (stack.isEmpty()) return;

        Holder<Enchantment> enh = (enhType != null && enhType != ElementType.NONE && enhLv > 0) ? getEnhancementEnchantment(enhType) : null;
        Holder<Enchantment> res = (resType != null && resType != ElementType.NONE && resLv > 0) ? getResistanceEnchantment(resType) : null;

        if (enh == null && res == null) return;

        EnchantmentHelper.updateEnchantments(stack, mutable -> {
            if (enh != null) {
                mutable.set(enh, Math.max(enhLv, mutable.getLevel(enh)));
            }
            if (res != null) {
                mutable.set(res, Math.max(resLv, mutable.getLevel(res)));
            }
        });
    }

    private static final Item[][] RANDOM_ARMOR_POOL = {
            {Items.LEATHER_HELMET, Items.CHAINMAIL_HELMET, Items.IRON_HELMET, Items.GOLDEN_HELMET, Items.DIAMOND_HELMET, Items.NETHERITE_HELMET},
            {Items.LEATHER_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE},
            {Items.LEATHER_LEGGINGS, Items.CHAINMAIL_LEGGINGS, Items.IRON_LEGGINGS, Items.GOLDEN_LEGGINGS, Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS},
            {Items.LEATHER_BOOTS, Items.CHAINMAIL_BOOTS, Items.IRON_BOOTS, Items.GOLDEN_BOOTS, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS}
    };

    private static final Item[] RANDOM_WEAPON_POOL = {
            Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD, Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD,
            Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE,
            Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE, Items.GOLDEN_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE,
            Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.IRON_SHOVEL, Items.GOLDEN_SHOVEL, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL,
            Items.WOODEN_HOE, Items.STONE_HOE, Items.IRON_HOE, Items.GOLDEN_HOE, Items.DIAMOND_HOE, Items.NETHERITE_HOE
    };

    public static ItemStack createRandomArmor(int slotIndex) {
        return createRandomArmor(slotIndex, false);
    }

    private static final Item[] GOLD_ARMOR = { Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS };

    public static ItemStack createRandomArmor(int slotIndex, boolean forceGold) {
        if (slotIndex < 0 || slotIndex >= RANDOM_ARMOR_POOL.length) return ItemStack.EMPTY;
        if (forceGold) return new ItemStack(GOLD_ARMOR[slotIndex]);
        Item[] pool = RANDOM_ARMOR_POOL[slotIndex];
        return new ItemStack(pool[RANDOM.nextInt(pool.length)]);
    }

    public static ItemStack createRandomWeapon() {
        return createRandomWeapon(false);
    }

    public static ItemStack createRandomWeapon(boolean forceGold) {
        if (forceGold) return new ItemStack(Items.GOLDEN_SWORD);
        return new ItemStack(RANDOM_WEAPON_POOL[RANDOM.nextInt(RANDOM_WEAPON_POOL.length)]);
    }

    public static ElementType getCounterElement(ElementType type) {
        if (type == null || type == ElementType.NONE) return ElementType.NONE;

        List<? extends String> restraints = ElementalConfig.cachedRestraints;
        if (restraints == null || restraints.isEmpty()) return ElementType.NONE;

        for (String relation : restraints) {
            String[] split = relation.split("->");
            if (split.length == 2) {
                String attackerId = split[0].trim();
                String victimId = split[1].trim();

                if (victimId.equalsIgnoreCase(type.getId())) {
                    return ElementType.fromId(attackerId);
                }
            }
        }

        return ElementType.NONE;
    }

    public static ElementType randomNonNoneElement() {
        ElementType[] valid = {ElementType.FIRE, ElementType.NATURE, ElementType.FROST, ElementType.THUNDER};
        return valid[RANDOM.nextInt(valid.length)];
    }

    private static Holder<Enchantment> getAttackEnchantment(ElementType type) {
        return switch (type) {
            case FIRE -> ModEnchantments.FIRE_STRIKE.get();
            case NATURE -> ModEnchantments.NATURE_STRIKE.get();
            case FROST -> ModEnchantments.FROST_STRIKE.get();
            case THUNDER -> ModEnchantments.THUNDER_STRIKE.get();
            default -> null;
        };
    }

    private static Holder<Enchantment> getEnhancementEnchantment(ElementType type) {
        return switch (type) {
            case FIRE -> ModEnchantments.FIRE_ENHANCE.get();
            case NATURE -> ModEnchantments.NATURE_ENHANCE.get();
            case FROST -> ModEnchantments.FROST_ENHANCE.get();
            case THUNDER -> ModEnchantments.THUNDER_ENHANCE.get();
            default -> null;
        };
    }

    private static Holder<Enchantment> getResistanceEnchantment(ElementType type) {
        return switch (type) {
            case FIRE -> ModEnchantments.FIRE_RESIST.get();
            case NATURE -> ModEnchantments.NATURE_RESIST.get();
            case FROST -> ModEnchantments.FROST_RESIST.get();
            case THUNDER -> ModEnchantments.THUNDER_RESIST.get();
            default -> null;
        };
    }
}
