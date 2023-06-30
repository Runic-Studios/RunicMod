package com.runicrealms.runicspy.ui.preview;

import com.runicrealms.runicspy.ui.RunicModUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An inventory preview of a given user
 *
 * @author BoBoBalloon
 */
public class InventoryPreview extends RunicModUI {
    private final Player target;
    private final ItemStack[] contents;
    private final ItemStack[] armor;

    public InventoryPreview(@Nullable Player target, @NotNull ItemStack[] contents, @NotNull ItemStack[] armor) {
        super("&r&d[&5Runic&2Spy&d] > &2Inventory Preview", 54);
        this.target = target;
        this.contents = contents;
        this.armor = armor;
        this.reload();
    }

    public InventoryPreview(@NotNull Player target) {
        this(target, target.getInventory().getContents(), target.getInventory().getArmorContents());
    }

    /**
     * A method that sets the content of this inventory
     */
    @Override
    public void reload() {
        for (int i = 0; i < this.getInventory().getSize(); i++) {
            int index = i; //cant use changing variables in lambda

            if (i > 18) {
                this.getInventory().setItem(i + 18, this.contents[i]);
                this.registerClickEvent(i + 18, (player, stack) -> {
                    if ((index == 36 || index == 43 || index == 44) || (!player.isOp() && !player.hasPermission("runic.spy.edit") && (this.target == null || !this.target.isOnline()))) {
                        return;
                    }

                    ItemStack clicked = this.getInventory().getItem(index);

                    if (clicked == null && player.getItemOnCursor().getAmount() == 0) {
                        return;
                    }

                    if (player.getItemOnCursor().getAmount() != 0) { //if item should be placed
                        this.getInventory().setItem(index + 18, player.getItemOnCursor());
                        this.target.getInventory().setItem(index, player.getItemOnCursor());
                        player.setItemOnCursor(null);
                    } else { //if item should be taken
                        this.getInventory().setItem(index + 18, null);
                        this.target.getInventory().setItem(index, null);
                        player.setItemOnCursor(clicked);
                    }
                });
                continue;
            }

            if (i > 8) {
                this.getInventory().setItem(i, InventoryPreview.BLANK);
                continue;
            }

            if (i > this.armor.length - 1) {
                continue;
            }

            this.getInventory().setItem(i, this.armor[i]);
            this.registerClickEvent(i, (player, stack) -> {
                if (!player.isOp() && !player.hasPermission("runic.spy.edit") && (this.target == null || !this.target.isOnline())) {
                    return;
                }

                ItemStack clicked = this.getInventory().getItem(index);

                if (clicked == null && player.getItemOnCursor().getAmount() == 0) {
                    return;
                }

                if (player.getItemOnCursor().getAmount() != 0) { //if item should be placed
                    this.getInventory().setItem(index, player.getItemOnCursor());
                    ItemStack[] armor = this.target.getInventory().getArmorContents();
                    armor[index] = null;
                    this.target.getInventory().setArmorContents(armor);
                    player.setItemOnCursor(null);
                } else { //if item should be taken
                    this.getInventory().setItem(index, null);
                    ItemStack[] armor = this.target.getInventory().getArmorContents();
                    armor[index] = null;
                    this.target.getInventory().setArmorContents(armor);
                    player.setItemOnCursor(clicked);
                }
            });
        }
    }
}
