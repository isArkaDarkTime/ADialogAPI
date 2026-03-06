package me.arkadarktime.aDialogAPI.actions.impl;

import me.arkadarktime.aDialogAPI.ADialogAPI;
import me.arkadarktime.aDialogAPI.actions.ButtonAction;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class GiveItemAction implements ButtonAction {

    private final Material material;
    private final int count;

    public GiveItemAction(Material material, int count) {
        this.material = material;
        this.count = count;
    }

    @Override
    public String getTypeName() {
        return "GIVE_ITEM";
    }

    @Override
    public void execute(Player player, Map<String, String> inputs, ADialogAPI plugin) {
        Map<Integer, ItemStack> overflow = player.getInventory().addItem(new ItemStack(material, count));
        overflow.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
    }
}
