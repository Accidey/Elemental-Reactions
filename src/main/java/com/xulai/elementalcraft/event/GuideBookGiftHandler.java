package com.xulai.elementalcraft.event;

import com.xulai.elementalcraft.ElementalCraft;
import com.xulai.elementalcraft.config.ElementalConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = ElementalCraft.MODID)
public class GuideBookGiftHandler {

    private static final String NBT_GUIDE_BOOK_GIVEN = "EC_GuideBookGiven";
    private static final ResourceLocation GUIDE_BOOK_ITEM = new ResourceLocation("patchouli", "guide_book");
    private static final String GUIDE_BOOK_TAG = "patchouli:book";
    private static final String GUIDE_BOOK_ID = ElementalCraft.MODID + ":guide";

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

    private static ItemStack createGuideBook() {
        Item item = ForgeRegistries.ITEMS.getValue(GUIDE_BOOK_ITEM);
        if (item == null || item == Items.AIR) return ItemStack.EMPTY;

        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putString(GUIDE_BOOK_TAG, GUIDE_BOOK_ID);
        return stack;
    }
}
