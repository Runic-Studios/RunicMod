package com.runicrealms.runicspy.ui.preview;

import com.runicrealms.plugin.util.BankUtil;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicspy.ui.RunicModUI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
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
public class BankPreview extends RunicModUI {
    private final Map<Integer, ItemStack[]> pages;
    private int currentPage;

    public BankPreview(@NotNull Map<Integer, RunicItem[]> pages) {
        super("&r&d[&5Runic&2Spy&d] > &2Bank Preview", 54, true);
        this.pages = new HashMap<>();

        for (Map.Entry<Integer, RunicItem[]> entry : pages.entrySet()) {
            this.pages.put(entry.getKey(), Arrays.stream(entry.getValue())
                    .map(RunicItem::generateItem)
                    .toArray(ItemStack[]::new));
        }

        this.reload();
    }

    /**
     * A method that sets the content of this inventory
     */
    @Override
    public void reload() {
        if (!this.pages.containsKey(this.currentPage)) {
            this.currentPage = 0;
        }

        this.getInventory().setContents(this.pages.get(this.currentPage));

        // fill top row with black panes
        for (int i = 0; i < 4; i++) {
            this.getInventory().setItem(i, RunicModUI.BLANK);
        }
        this.getInventory().setItem(5, RunicModUI.BLANK);

        // menu buttons
        this.getInventory().setItem(4, BankUtil.menuItem(Material.YELLOW_STAINED_GLASS_PANE, "&6&lBank of Alterra", "&7Welcome to your bank\n&aPage: &f" + (this.currentPage + 1)));
        this.getInventory().setItem(6, BankUtil.menuItem(Material.BARRIER, "&c&lYou may not purchase bank pages in a preview!", ""));
        this.getInventory().setItem(7, BankUtil.menuItem(Material.GREEN_STAINED_GLASS_PANE, "&f&lPrevious Page", "&7Display the previous page in your bank"));
        this.registerClickEvent(7, (player, stack) -> this.lastPage());
        this.getInventory().setItem(8, BankUtil.menuItem(Material.RED_STAINED_GLASS_PANE, "&f&lNext Page", "&7Display the next page in your bank"));
        this.registerClickEvent(8, (player, stack) -> this.nextPage());
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
}
