package com.runicrealms.runicspy.ui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

/**
 * A class used to listen for basic UI events
 */
public class RunicModUIListener implements Listener {
    @EventHandler
    private void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof RunicModUI modUI)) {
            return;
        }

        Inventory inventory = event.getClickedInventory();

        if (inventory == null) {
            return;
        }

        event.setCancelled(true);

        BiConsumer<Player, ItemStack> action = modUI.getClickAction(event.getSlot());

        if (action == null) {
            return;
        }

        action.accept((Player) event.getWhoClicked(), event.getCurrentItem());
    }

    @EventHandler(ignoreCancelled = true)
    private void onInventoryDrag(@NotNull InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof RunicModUI) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof RunicModUI ui) {
            ui.onClose();
        }
    }
}
