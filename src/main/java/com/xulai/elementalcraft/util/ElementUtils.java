package com.xulai.elementalcraft.util;

import com.xulai.elementalcraft.ElementalCraft;
import com.xulai.elementalcraft.config.ElementalConfig;
import com.xulai.elementalcraft.enchantment.ModEnchantments;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.lang.reflect.Field;

public class ElementUtils {

    private static final Field TRIDENT_ITEM_FIELD;
    static {
        Field field = null;
        for (String name : new String[]{"tridentItem", "f_37555_"}) {
            try {
                field = ThrownTrident.class.getDeclaredField(name);
                field.setAccessible(true);
                break;
            } catch (NoSuchFieldException ignored) {}
        }
        if (field == null) {
            ElementalCraft.LOGGER.error("Failed to find ThrownTrident.tridentItem field");
        }
        TRIDENT_ITEM_FIELD = field;
    }


    public static ElementType getAttackElement(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return ElementType.NONE;

        if (EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.FIRE_STRIKE.get(), stack) > 0)    return ElementType.FIRE;
        if (EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.NATURE_STRIKE.get(), stack) > 0) return ElementType.NATURE;
        if (EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.FROST_STRIKE.get(), stack) > 0)  return ElementType.FROST;
        if (EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.THUNDER_STRIKE.get(), stack) > 0) return ElementType.THUNDER;
        return ElementType.NONE;
    }

    public static int getEnhancementLevel(ItemStack stack, ElementType type) {
        if (stack == null || stack.isEmpty() || type == ElementType.NONE) return 0;
        return switch (type) {
            case FIRE    -> EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.FIRE_ENHANCE.get(), stack);
            case NATURE  -> EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.NATURE_ENHANCE.get(), stack);
            case FROST   -> EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.FROST_ENHANCE.get(), stack);
            case THUNDER -> EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.THUNDER_ENHANCE.get(), stack);
            default      -> 0;
        };
    }

    public static int getResistanceLevel(ItemStack stack, ElementType type) {
        if (stack == null || stack.isEmpty() || type == ElementType.NONE) return 0;
        return switch (type) {
            case FIRE    -> EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.FIRE_RESIST.get(), stack);
            case NATURE  -> EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.NATURE_RESIST.get(), stack);
            case FROST   -> EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.FROST_RESIST.get(), stack);
            case THUNDER -> EnchantmentHelper.getTagEnchantmentLevel(ModEnchantments.THUNDER_RESIST.get(), stack);
            default      -> 0;
        };
    }

    public static ElementType getAttackElement(LivingEntity attacker) {
        if (attacker == null) return ElementType.NONE;
        return getAttackElement(attacker.getMainHandItem());
    }

    public static ItemStack getProjectileWeaponStack(Entity projectile, LivingEntity shooter) {
        if (projectile instanceof ThrownTrident trident) {
            if (TRIDENT_ITEM_FIELD != null) {
                try {
                    ItemStack tridentStack = (ItemStack) TRIDENT_ITEM_FIELD.get(trident);
                    if (tridentStack != null && !tridentStack.isEmpty()) return tridentStack;
                } catch (IllegalAccessException ignored) {}
            }
            return ItemStack.EMPTY;
        }
        if (projectile instanceof ThrowableItemProjectile throwable) {
            return throwable.getItem();
        }
        if (projectile instanceof AbstractArrow && shooter != null) {
            ItemStack mainHand = shooter.getMainHandItem();
            if (mainHand.getItem() instanceof ProjectileWeaponItem && getAttackElement(mainHand) != ElementType.NONE) {
                return mainHand;
            }
            ItemStack offHand = shooter.getOffhandItem();
            if (offHand.getItem() instanceof ProjectileWeaponItem && getAttackElement(offHand) != ElementType.NONE) {
                return offHand;
            }
        }
        return ItemStack.EMPTY;
    }

    public static int getDisplayEnhancement(LivingEntity entity, ElementType type) {
        if (entity == null || type == ElementType.NONE) return 0;

        int totalLevels = getEnhancementLevel(entity.getMainHandItem(), type);

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                totalLevels += getEnhancementLevel(entity.getItemBySlot(slot), type);
            }
        }

        return totalLevels * ElementalConfig.getStrengthPerLevel();
    }

    public static int getDisplayResistance(LivingEntity entity, ElementType type) {
        if (entity == null || type == ElementType.NONE) return 0;

        int totalLevels = 0;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                totalLevels += getResistanceLevel(entity.getItemBySlot(slot), type);
            }
        }

        return totalLevels * ElementalConfig.getResistPerLevel();
    }

    public static ElementType getDominantElement(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return ElementType.NONE;

        ElementType attack = getAttackElement(stack);
        if (attack != ElementType.NONE) return attack;

        for (ElementType t : ElementType.values()) {
            if (t == ElementType.NONE) continue;
            if (getEnhancementLevel(stack, t) > 0) return t;
        }

        for (ElementType t : ElementType.values()) {
            if (t == ElementType.NONE) continue;
            if (getResistanceLevel(stack, t) > 0) return t;
        }

        return ElementType.NONE;
    }

    public static ElementType getElementType(LivingEntity entity) {
        if (entity == null) return ElementType.NONE;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = entity.getItemBySlot(slot);
            ElementType type = getDominantElement(stack);
            if (type != ElementType.NONE) {
                return type;
            }
        }
        return ElementType.NONE;
    }

    public static ElementType getConsistentAttackElement(LivingEntity attacker) {
        if (attacker == null) return ElementType.NONE;

        ElementType weaponElement = getAttackElement(attacker);
        if (weaponElement == ElementType.NONE) return ElementType.NONE;

        int enhancementPoints = getDisplayEnhancement(attacker, weaponElement);

        if (enhancementPoints > 0) {
            return weaponElement;
        }

        return ElementType.NONE;
    }

    public static ElementType getConsistentAttackElement(DamageSource source, LivingEntity attacker) {
        if (attacker == null || source == null) return ElementType.NONE;
        Entity direct = source.getDirectEntity();
        if (direct instanceof Projectile projectile && direct != attacker) {
            ElementType element = getAttackElement(getProjectileWeaponStack(projectile, attacker));
            if (element == ElementType.NONE) return ElementType.NONE;
            return getDisplayEnhancement(attacker, element) > 0 ? element : ElementType.NONE;
        }
        return getConsistentAttackElement(attacker);
    }
}
