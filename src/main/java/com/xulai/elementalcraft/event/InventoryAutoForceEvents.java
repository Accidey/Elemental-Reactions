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

        processList(player.getInventory().getNonEquipmentItems(), player);
        for (var slot : new net.minecraft.world.entity.EquipmentSlot[]{
                net.minecraft.world.entity.EquipmentSlot.HEAD,
                net.minecraft.world.entity.EquipmentSlot.CHEST,
                net.minecraft.world.entity.EquipmentSlot.LEGS,
                net.minecraft.world.entity.EquipmentSlot.FEET,
                net.minecraft.world.entity.EquipmentSlot.OFFHAND}) {
            processList(java.util.List.of(player.getItemBySlot(slot)), player);
        }
    }

    private static void processList(List<ItemStack> stacks, Player player) {
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) continue;

            boolean isForced =
                    ForcedItemHelper.getForcedWeapon(stack.getItem()) != null
                            || ForcedItemHelper.getForcedArmor(stack.getItem()) != null;

            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            CompoundTag tag = customData != null ? customData.copyTag() : null;
            boolean wasForced = tag != null && tag.getBooleanOr(TAG_FORCED, false);

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

        boolean changed = false;
        var componentType = EnchantmentHelper.getComponentType(stack);
        ItemEnchantments currentEnchants = stack.getOrDefault(componentType, ItemEnchantments.EMPTY);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(currentEnchants);
        CompoundTag forcedData = new CompoundTag();

        ForcedItemHelper.WeaponData weaponData = ForcedItemHelper.getForcedWeapon(stack.getItem());
        if (weaponData != null && weaponData.attackType() != null) {
            Holder<Enchantment> targetEnchant = getAttackEnchantment(weaponData.attackType());

            if (targetEnchant != null) {
                forcedData.putString("attack", weaponData.attackType().getId());

                Holder<Enchantment> currentAttackEnchant = null;
                for (ElementType type : ElementType.values()) {
                    if (type == ElementType.NONE) continue;
                    Holder<Enchantment> e = getAttackEnchantment(type);
                    if (e != null && mutable.getLevel(e) > 0) {
                        currentAttackEnchant = e;
                        break;
                    }
                }

                if (isTracked) {
                    if (currentAttackEnchant != targetEnchant) {
                        if (currentAttackEnchant != null) mutable.set(currentAttackEnchant, 0);
                        mutable.set(targetEnchant, 1);
                        changed = true;
                    }
                } else {
                    if (currentAttackEnchant == null) {
                        mutable.set(targetEnchant, 1);
                        changed = true;
                    }
                }
            }
        }

        ForcedItemHelper.ArmorData armorData = ForcedItemHelper.getForcedArmor(stack.getItem());
        if (armorData != null) {
            if (armorData.enhanceType() != null && armorData.enhancePoints() > 0) {
                Holder<Enchantment> targetEnhance = getEnhancementEnchantment(armorData.enhanceType());
                int level = Math.max(1, Math.min(5, armorData.enhancePoints() / ElementalConfig.getStrengthPerLevel()));

                if (targetEnhance != null) {
                    forcedData.putString("enhance", armorData.enhanceType().getId());

                    if (isTracked) {
                        for (ElementType type : ElementType.values()) {
                            if (type == ElementType.NONE) continue;
                            Holder<Enchantment> e = getEnhancementEnchantment(type);
                            if (e != null && e != targetEnhance && mutable.getLevel(e) > 0) {
                                mutable.set(e, 0);
                                changed = true;
                            }
                        }
                        if (mutable.getLevel(targetEnhance) != level) {
                            mutable.set(targetEnhance, level);
                            changed = true;
                        }
                    } else {
                        boolean hasEnhance = false;
                        for (ElementType type : ElementType.values()) {
                            if (type == ElementType.NONE) continue;
                            Holder<Enchantment> e = getEnhancementEnchantment(type);
                            if (e != null && mutable.getLevel(e) > 0) {
                                hasEnhance = true;
                                break;
                            }
                        }
                        if (!hasEnhance) {
                            mutable.set(targetEnhance, level);
                            changed = true;
                        }
                    }
                }
            }

            if (armorData.resistType() != null && armorData.resistPoints() > 0) {
                Holder<Enchantment> targetResist = getResistanceEnchantment(armorData.resistType());
                int level = Math.max(1, Math.min(5, armorData.resistPoints() / ElementalConfig.getResistPerLevel()));

                if (targetResist != null) {
                    forcedData.putString("resist", armorData.resistType().getId());

                    if (isTracked) {
                        for (ElementType type : ElementType.values()) {
                            if (type == ElementType.NONE) continue;
                            Holder<Enchantment> e = getResistanceEnchantment(type);
                            if (e != null && e != targetResist && mutable.getLevel(e) > 0) {
                                mutable.set(e, 0);
                                changed = true;
                            }
                        }
                        if (mutable.getLevel(targetResist) != level) {
                            mutable.set(targetResist, level);
                            changed = true;
                        }
                    } else {
                        boolean hasResist = false;
                        for (ElementType type : ElementType.values()) {
                            if (type == ElementType.NONE) continue;
                            Holder<Enchantment> e = getResistanceEnchantment(type);
                            if (e != null && mutable.getLevel(e) > 0) {
                                hasResist = true;
                                break;
                            }
                        }
                        if (!hasResist) {
                            mutable.set(targetResist, level);
                            changed = true;
                        }
                    }
                }
            }
        }

        if (changed) {
            stack.set(componentType, mutable.toImmutable());
        }

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
            CompoundTag data = tag.getCompound(TAG_FORCED_DATA).orElse(new CompoundTag());

            if (data.contains("attack")) {
                ElementType type = ElementType.fromId(data.getStringOr("attack", ""));
                Holder<Enchantment> ench = type != null ? getAttackEnchantment(type) : null;
                if (ench != null && mutable.getLevel(ench) > 0) {
                    mutable.set(ench, 0);
                    changed = true;
                }
            }
            if (data.contains("enhance")) {
                ElementType type = ElementType.fromId(data.getStringOr("enhance", ""));
                Holder<Enchantment> ench = type != null ? getEnhancementEnchantment(type) : null;
                if (ench != null && mutable.getLevel(ench) > 0) {
                    mutable.set(ench, 0);
                    changed = true;
                }
            }
            if (data.contains("resist")) {
                ElementType type = ElementType.fromId(data.getStringOr("resist", ""));
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
