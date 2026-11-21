package com.xulai.elementalcraft.event;

import com.xulai.elementalcraft.ElementalCraft;
import com.xulai.elementalcraft.config.ElementalConfig;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = ElementalCraft.MODID)
public class GuideBookGiftHandler {

    private static final String NBT_GUIDE_BOOK_GIVEN = "EC_GuideBookGiven";
    private static final ResourceLocation GUIDE_BOOK_ITEM = ResourceLocation.fromNamespaceAndPath("patchouli", "guide_book");
    private static final ResourceLocation GUIDE_BOOK_COMPONENT = ResourceLocation.fromNamespaceAndPath("patchouli", "book");
    private static final ResourceLocation GUIDE_BOOK_ID = ResourceLocation.fromNamespaceAndPath(ElementalCraft.MODID, "guide");

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!ElementalConfig.isGuideBookOnFirstJoin()) return;

        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (persisted.getBoolean(NBT_GUIDE_BOOK_GIVEN)) return;

        ItemStack book = createGuideBook();
        if (book.isEmpty()) return;

        persisted.putBoolean(NBT_GUIDE_BOOK_GIVEN, true);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);

        if (!player.getInventory().add(book)) {
            player.drop(book, false);
        }
    }

    @SuppressWarnings("unchecked")
    private static ItemStack createGuideBook() {
        Item item = BuiltInRegistries.ITEM.get(GUIDE_BOOK_ITEM);
        if (item == Items.AIR) return ItemStack.EMPTY;

        DataComponentType<ResourceLocation> bookComponent =
                (DataComponentType<ResourceLocation>) BuiltInRegistries.DATA_COMPONENT_TYPE.get(GUIDE_BOOK_COMPONENT);
        if (bookComponent == null) return ItemStack.EMPTY;

        ItemStack stack = new ItemStack(item);
        stack.set(bookComponent, GUIDE_BOOK_ID);
        return stack;
    }
}
