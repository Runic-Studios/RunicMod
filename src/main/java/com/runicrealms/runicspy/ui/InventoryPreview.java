package com.runicrealms.runicspy.ui;

import com.runicrealms.plugin.common.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * An inventory preview of a given user
 */
public class InventoryPreview implements InventoryHolder {
    private final Inventory inventory;
    private final ItemStack[] contents;
    private final ItemStack[] armor;

    private static final ItemStack BLANK = InventoryPreview.blank();

    public InventoryPreview(@NotNull ItemStack[] contents, @NotNull ItemStack[] armor) {
        this.inventory = Bukkit.createInventory(this, 6 * 9, ColorUtil.format("&r&d[&5Runic&2Spy&d] > &2Inventory Preview"));
        this.contents = contents;
        this.armor = armor;
        this.reload();
    }

    public InventoryPreview(@NotNull Player target) {
        this(target.getInventory().getContents(), target.getInventory().getArmorContents());
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    /**
     * A method that sets the content of this inventory
     */
    public void reload() {
        for (int i = 0; i < this.inventory.getSize(); i++) {
            if (i > 18) {
                this.inventory.setItem(i + 18, this.contents[i]);
                continue;
            }

            if (i > 8) {
                this.inventory.setItem(i, InventoryPreview.BLANK);
                continue;
            }

            if (i > this.armor.length - 1) {
                continue;
            }

            this.inventory.setItem(i, this.armor[i]);
        }
    }

    /**
     * A method that returns a new instance of a blank itemstack icon
     *
     * @return a new instance of a blank itemstack icon
     */
    @NotNull
    private static ItemStack blank() {
        ItemStack blank = new ItemStack(Material.IRON_BARS);
        ItemMeta meta = blank.getItemMeta();
        meta.setDisplayName("");
        blank.setItemMeta(meta);

        return blank;
    }
}
