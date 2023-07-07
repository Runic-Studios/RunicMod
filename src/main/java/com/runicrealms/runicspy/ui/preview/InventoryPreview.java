package com.runicrealms.runicspy.ui.preview;

import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.runicspy.spy.SpyInfo;
import com.runicrealms.runicspy.ui.RunicModUI;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * An inventory preview of a given user
 *
 * @author BoBoBalloon
 */
public class InventoryPreview extends RunicModUI {
    private final ItemStack[] contents;
    private final ItemStack[] armor;

    private static final List<ItemStack> ICONS = InventoryPreview.getEquipmentSlots();

    public InventoryPreview(@Nullable ItemStack[] contents, @Nullable ItemStack[] armor) {
        super("&r&d[&5Runic&2Spy&d] > &2Inventory Preview", 54);
        this.contents = contents;
        this.armor = armor;
        this.reload();
    }

    /**
     * A method that sets the content of this inventory
     */
    @Override
    public void reload() {
        for (int i = 0; i < this.getInventory().getSize(); i++) {
            if (i < 9 && i < SpyInfo.ARMOR_SLOTS.size()) {
                this.getInventory().setItem(i, this.armor[i] != null && this.armor[i].getType() != Material.AIR ? this.armor[i] : InventoryPreview.ICONS.get(i));
                continue;
            } else if (i < 9) {
                this.getInventory().setItem(i, null);
                continue;
            }

            if (i < 18) {
                this.getInventory().setItem(i, InventoryPreview.BLANK);
                continue;
            }

            this.getInventory().setItem(((6 - i / 9) * 9 + (i % 9)) + 9, this.contents[i - 18]);
        }
    }

    /**
     * A method that premakes empty armor icons
     *
     * @return empty armor icons
     */
    @NotNull
    private static List<ItemStack> getEquipmentSlots() {
        List<ItemStack> stacks = new ArrayList<>();

        for (EquipmentSlot slot : SpyInfo.ARMOR_SLOTS) {
            ItemStack stack = new ItemStack(Material.BARRIER);
            ItemMeta meta = stack.getItemMeta();

            meta.setDisplayName(ColorUtil.format("&c" + slot.name()));
            stack.setItemMeta(meta);
            stacks.add(stack);
        }

        return stacks;
    }
}
