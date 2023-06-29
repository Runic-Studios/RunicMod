package com.runicrealms.runicspy.ui;

import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.util.BankUtil;
import com.runicrealms.runicitems.item.RunicItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * An bank preview of a given user
 *
 * @author BoBoBalloon
 * @since 6/29/23
 */
public class BankPreview implements InventoryHolder {
    private final Inventory inventory;
    private final Map<Integer, ItemStack[]> pages;
    private int currentPage;

    private static final ItemStack BLANK = BankPreview.blank();

    public BankPreview(@NotNull Map<Integer, RunicItem[]> pages) {
        this.inventory = Bukkit.createInventory(this, 6 * 9, ColorUtil.format("&r&d[&5Runic&2Spy&d] > &2Bank Preview"));
        this.pages = new HashMap<>();

        for (Map.Entry<Integer, RunicItem[]> entry : pages.entrySet()) {
            this.pages.put(entry.getKey(), Arrays.stream(entry.getValue())
                    .map(RunicItem::generateItem)
                    .toArray(ItemStack[]::new));
        }

        this.reload();
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
        if (!this.pages.containsKey(this.currentPage)) {
            this.currentPage = 0;
        }

        this.inventory.setContents(this.pages.get(this.currentPage));

        // fill top row with black panes
        for (int i = 0; i < 4; i++) {
            this.inventory.setItem(i, BankPreview.BLANK);
        }
        this.inventory.setItem(5, BankPreview.BLANK);

        // menu buttons
        this.inventory.setItem(4, BankUtil.menuItem(Material.YELLOW_STAINED_GLASS_PANE, "&6&lBank of Alterra", "&7Welcome to your bank\n&aPage: &f" + (this.currentPage + 1)));
        this.inventory.setItem(6, BankUtil.menuItem(Material.BARRIER, "&c&lYou may not purchase bank pages in a preview!", ""));
        this.inventory.setItem(7, BankUtil.menuItem(Material.GREEN_STAINED_GLASS_PANE, "&f&lPrevious Page", "&7Display the previous page in your bank"));
        this.inventory.setItem(8, BankUtil.menuItem(Material.RED_STAINED_GLASS_PANE, "&f&lNext Page", "&7Display the next page in your bank"));
    }

    /**
     * A method that sets this bank preview to the next page
     */
    public void nextPage() {
        if (this.pages.containsKey(this.currentPage + 1)) {
            this.currentPage++;
            this.reload();
        }
    }

    /**
     * A method that sets this bank preview to the next page
     */
    public void lastPage() {
        if (this.pages.containsKey(this.currentPage - 1)) {
            this.currentPage--;
            this.reload();
        }
    }

    /**
     * A method that returns a new instance of a blank itemstack icon
     *
     * @return a new instance of a blank itemstack icon
     */
    @NotNull
    private static ItemStack blank() {
        ItemStack blank = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = blank.getItemMeta();
        meta.setDisplayName("");
        blank.setItemMeta(meta);

        return blank;
    }
}
