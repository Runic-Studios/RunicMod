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
    private final ItemStack[] contents;
    private final ItemStack[] armor;

    public InventoryPreview(@Nullable ItemStack[] contents, @Nullable ItemStack[] armor) {
        super("&r&d[&5Runic&2Spy&d] > &2Inventory Preview", 54);
        this.contents = contents;
        this.armor = armor;
        this.reload();
    }

    public InventoryPreview(@NotNull Player target) {
        this(target.getInventory().getContents(), target.getInventory().getArmorContents());
    }

    /**
     * A method that sets the content of this inventory
     */
    @Override
    public void reload() {
        for (int i = 0; i < this.getInventory().getSize(); i++) {
            if (i > 18) {
                this.getInventory().setItem(i + 18, this.contents[i]);
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
        }
    }
}
