package me.arkadarktime.aDialogAPI.actions.impl;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.actions.ButtonAction;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class TakeItemAction extends ButtonAction {

    private final Material material;
    private final int count;

    public TakeItemAction(Material material, int count) {
        this.material = material;
        this.count = count;
    }

    @Override
    public String getTypeName() {
        return "TAKE_ITEM";
    }

    @Override
    public void execute(Player player, Map<String, String> inputs, ADialogAPI plugin) {
        int remaining = count;
        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack slot = contents[i];
            if (slot == null || slot.getType() != material) continue;

            if (slot.getAmount() <= remaining) {
                remaining -= slot.getAmount();
                player.getInventory().setItem(i, null);
            } else {
                slot.setAmount(slot.getAmount() - remaining);
                remaining = 0;
            }
        }

        if (remaining > 0 && plugin.isDebugEnabled()) {
            plugin.getLogger().warning("[TakeItemAction] " + player.getName()
                    + " didn't have enough " + material
                    + " (missing " + remaining + ")");
        }
    }
}
