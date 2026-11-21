package com.xulai.elementalcraft.event;

import com.xulai.elementalcraft.ElementalCraft;
import com.xulai.elementalcraft.config.ElementalConfig;
import com.xulai.elementalcraft.enchantment.ModEnchantments;
import com.xulai.elementalcraft.util.ElementType;
import com.xulai.elementalcraft.util.ForcedItemHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = ElementalCraft.MODID)
public class InventoryAutoForceEvents {

    private static final String TAG_FORCED = "elementalcraft_forced";

    private static final String TAG_FORCED_DATA = "elementalcraft_forced_data";

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player == null || player.level().isClientSide()) return;

        if (player.tickCount % 100 != 0) return;

        processList(player.getInventory().items, player);
        processList(player.getInventory().armor, player);
        processList(player.getInventory().offhand, player);
    }

    private static void processList(List<ItemStack> stacks, Player player) {
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) continue;

            boolean isForced =
                    ForcedItemHelper.getForcedWeapon(stack.getItem()) != null
                            || ForcedItemHelper.getForcedArmor(stack.getItem()) != null;

            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            CompoundTag tag = customData != null ? customData.copyTag() : null;
            boolean wasForced = tag != null && tag.getBoolean(TAG_FORCED);

            if (isForced) {
                applyForcedAttributes(stack, wasForced);
            } else if (wasForced) {
                removeForcedAttributes(stack);
                stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY,
                        cd -> cd.update(t -> {
                            t.remove(TAG_FORCED);
                            t.remove(TAG_FORCED_DATA);
                        }));
            }
        }
    }

    public static void applyForcedAttributes(ItemStack stack, boolean isTracked) {
        if (stack.isEmpty()) return;

        ForcedItemHelper.WeaponData weaponData = ForcedItemHelper.getForcedWeapon(stack.getItem());
        ForcedItemHelper.ArmorData armorData = ForcedItemHelper.getForcedArmor(stack.getItem());

        CompoundTag desired = new CompoundTag();
        int enhanceLevel = 0;
        int resistLevel = 0;

        if (weaponData != null && weaponData.attackType() != null && weaponData.attackType() != ElementType.NONE) {
            desired.putString("attack", weaponData.attackType().getId());
        }
        if (armorData != null && armorData.enhanceType() != null && armorData.enhancePoints() > 0) {
            desired.putString("enhance", armorData.enhanceType().getId());
            int enhancePerLevel = ElementalConfig.getStrengthPerLevel();
            int enhanceMaxLevel = Math.max(1, ElementalConfig.getMaxStatCap() / enhancePerLevel);
            enhanceLevel = Math.max(1, Math.min(enhanceMaxLevel, armorData.enhancePoints() / enhancePerLevel));
            desired.putInt("enhanceLevel", enhanceLevel);
        }
        if (armorData != null && armorData.resistType() != null && armorData.resistPoints() > 0) {
            desired.putString("resist", armorData.resistType().getId());
            int resistPerLevel = ElementalConfig.getResistPerLevel();
            int resistMaxLevel = Math.max(1, ElementalConfig.getMaxStatCap() / resistPerLevel);
            resistLevel = Math.max(1, Math.min(resistMaxLevel, armorData.resistPoints() / resistPerLevel));
            desired.putInt("resistLevel", resistLevel);
        }

        if (isTracked) {
            if (desired.equals(storedForcedData(stack))) return;
            removeForcedAttributes(stack);
        }

        boolean changed = false;
        var componentType = EnchantmentHelper.getComponentType(stack);
        ItemEnchantments currentEnchants = stack.getOrDefault(componentType, ItemEnchantments.EMPTY);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(currentEnchants);

        if (desired.contains("attack")) {
            ElementType type = ElementType.fromId(desired.getString("attack"));
            Holder<Enchantment> targetEnchant = getAttackEnchantment(type);
            if (targetEnchant != null && !hasElementalEnchant(mutable, true)) {
                mutable.set(targetEnchant, 1);
                changed = true;
            }
        }

        if (desired.contains("enhance") && enhanceLevel > 0) {
            ElementType type = ElementType.fromId(desired.getString("enhance"));
            Holder<Enchantment> targetEnhance = getEnhancementEnchantment(type);
            if (targetEnhance != null && !hasElementalEnchant(mutable, false)) {
                mutable.set(targetEnhance, enhanceLevel);
                changed = true;
            }
        }

        if (desired.contains("resist") && resistLevel > 0) {
            ElementType type = ElementType.fromId(desired.getString("resist"));
            Holder<Enchantment> targetResist = getResistanceEnchantment(type);
            if (targetResist != null && !hasElementalResistance(mutable)) {
                mutable.set(targetResist, resistLevel);
                changed = true;
            }
        }

        if (changed) {
            stack.set(componentType, mutable.toImmutable());
        }

        writeForcedTag(stack, desired);
    }

    private static boolean hasElementalEnchant(ItemEnchantments.Mutable mutable, boolean attack) {
        for (ElementType type : ElementType.values()) {
            if (type == ElementType.NONE) continue;
            Holder<Enchantment> e = attack ? getAttackEnchantment(type) : getEnhancementEnchantment(type);
            if (e != null && mutable.getLevel(e) > 0) return true;
        }
        return false;
    }

    private static boolean hasElementalResistance(ItemEnchantments.Mutable mutable) {
        for (ElementType type : ElementType.values()) {
            if (type == ElementType.NONE) continue;
            Holder<Enchantment> e = getResistanceEnchantment(type);
            if (e != null && mutable.getLevel(e) > 0) return true;
        }
        return false;
    }

    private static CompoundTag storedForcedData(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return new CompoundTag();
        CompoundTag tag = customData.copyTag();
        if (!tag.getBoolean(TAG_FORCED) || !tag.contains(TAG_FORCED_DATA)) return new CompoundTag();
        return tag.getCompound(TAG_FORCED_DATA);
    }

    private static void writeForcedTag(ItemStack stack, CompoundTag forcedData) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = customData != null ? customData.copyTag() : new CompoundTag();

        boolean marked = tag.getBoolean(TAG_FORCED);
        boolean dataUpToDate = forcedData.isEmpty()
                || (tag.contains(TAG_FORCED_DATA) && tag.getCompound(TAG_FORCED_DATA).equals(forcedData));
        if (marked && dataUpToDate) return;

        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY,
                cd -> cd.update(t -> {
                    t.putBoolean(TAG_FORCED, true);
                    if (!forcedData.isEmpty()) {
                        t.put(TAG_FORCED_DATA, forcedData);
                    }
                }));
    }

    private static void removeForcedAttributes(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return;
        CompoundTag tag = customData.copyTag();

        var componentType = EnchantmentHelper.getComponentType(stack);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(
                stack.getOrDefault(componentType, ItemEnchantments.EMPTY));
        boolean changed = false;

        if (tag.contains(TAG_FORCED_DATA)) {
            CompoundTag data = tag.getCompound(TAG_FORCED_DATA);

            if (data.contains("attack")) {
                ElementType type = ElementType.fromId(data.getString("attack"));
                Holder<Enchantment> ench = type != null ? getAttackEnchantment(type) : null;
                if (ench != null && mutable.getLevel(ench) > 0) {
                    mutable.set(ench, 0);
                    changed = true;
                }
            }
            if (data.contains("enhance")) {
                ElementType type = ElementType.fromId(data.getString("enhance"));
                Holder<Enchantment> ench = type != null ? getEnhancementEnchantment(type) : null;
                if (ench != null && mutable.getLevel(ench) > 0) {
                    mutable.set(ench, 0);
                    changed = true;
                }
            }
            if (data.contains("resist")) {
                ElementType type = ElementType.fromId(data.getString("resist"));
                Holder<Enchantment> ench = type != null ? getResistanceEnchantment(type) : null;
                if (ench != null && mutable.getLevel(ench) > 0) {
                    mutable.set(ench, 0);
                    changed = true;
                }
            }
        }

        if (changed) {
            stack.set(componentType, mutable.toImmutable());
        }
    }

    private static Holder<Enchantment> getAttackEnchantment(ElementType type) {
        if (type == null) return null;
        return switch (type) {
            case FIRE -> ModEnchantments.FIRE_STRIKE.get();
            case FROST -> ModEnchantments.FROST_STRIKE.get();
            case THUNDER -> ModEnchantments.THUNDER_STRIKE.get();
            case NATURE -> ModEnchantments.NATURE_STRIKE.get();
            default -> null;
        };
    }

    private static Holder<Enchantment> getEnhancementEnchantment(ElementType type) {
        if (type == null) return null;
        return switch (type) {
            case FIRE -> ModEnchantments.FIRE_ENHANCE.get();
            case FROST -> ModEnchantments.FROST_ENHANCE.get();
            case THUNDER -> ModEnchantments.THUNDER_ENHANCE.get();
            case NATURE -> ModEnchantments.NATURE_ENHANCE.get();
            default -> null;
        };
    }

    private static Holder<Enchantment> getResistanceEnchantment(ElementType type) {
        if (type == null) return null;
        return switch (type) {
            case FIRE -> ModEnchantments.FIRE_RESIST.get();
            case FROST -> ModEnchantments.FROST_RESIST.get();
            case THUNDER -> ModEnchantments.THUNDER_RESIST.get();
            case NATURE -> ModEnchantments.NATURE_RESIST.get();
            default -> null;
        };
    }
}
