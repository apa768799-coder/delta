package com.parallax.delta.module.modules;

import com.parallax.delta.module.Category;
import com.parallax.delta.module.Module;
import com.parallax.delta.module.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotemModule extends Module {
    private final Setting.Num hpThreshold = register(
            new Setting.Num("HP Threshold", 10.0, 1.0, 20.0));
    private final Setting.Num delayTicks = register(
            new Setting.Num("Delay", 2, 1, 10));
    private final Setting.Bool preferHotbar = register(
            new Setting.Bool("Prefer Hotbar", true));
    private final Setting.Bool onlyIfEmpty = register(
            new Setting.Bool("Only If Empty", true));

    private int cooldown = 0;

    public AutoTotemModule() {
        super("AutoTotem", Category.COMBAT);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (cooldown > 0) { cooldown--; return; }

        ItemStack offhand = mc.player.getOffHandStack();
        boolean hasTotem = offhand.isOf(Items.TOTEM_OF_UNDYING);

        if (onlyIfEmpty.get() && hasTotem) return;
        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() > hpThreshold.get()) return;

        int slot = findTotemSlot(mc);
        if (slot == -1) return;

        // 40 = слот оффхенда в player screen handler для SWAP
        mc.interactionManager.clickSlot(
                mc.player.playerScreenHandler.syncId,
                slot, 40, SlotActionType.SWAP, mc.player);
        cooldown = delayTicks.getInt();
    }

    private int findTotemSlot(MinecraftClient mc) {
        // приоритет оффхенду не нужен — там уже проверено
        if (preferHotbar.get()) {
            for (int i = 0; i < 9; i++) {
                ItemStack s = mc.player.getInventory().getStack(i);
                if (s.isOf(Items.TOTEM_OF_UNDYING)) return i;
            }
        }
        // инвентарь — слоты 9..35 в player inventory
        for (int i = 9; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.TOTEM_OF_UNDYING)) return i;
        }
        if (!preferHotbar.get()) {
            for (int i = 0; i < 9; i++) {
                ItemStack s = mc.player.getInventory().getStack(i);
                if (s.isOf(Items.TOTEM_OF_UNDYING)) return i;
            }
        }
        return -1;
    }

    @Override
    protected void onDisable() {
        cooldown = 0;
    }
}
