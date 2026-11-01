package com.parallax.delta.module.modules;

import com.parallax.delta.module.Category;
import com.parallax.delta.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotemModule extends Module {
    public AutoTotemModule() {
        super("AutoTotem", Category.COMBAT);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        if (client.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return;
        if (client.player.getHealth() > 10.0f) return; // экипируем только при низком HP

        int totemSlot = findTotem(client);
        if (totemSlot == -1) return;

        client.interactionManager.clickSlot(
                client.player.playerScreenHandler.syncId,
                totemSlot, 40, SlotActionType.SWAP, client.player
        );
    }

    private int findTotem(MinecraftClient client) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (stack.isOf(Items.TOTEM_OF_UNDYING)) return i;
        }
        return -1;
    }
}
