package com.runicrealms.runicspy.ui.preview;

import com.runicrealms.plugin.RunicBank;
import com.runicrealms.plugin.model.BankHolder;
import com.runicrealms.plugin.util.BankUtil;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicspy.ui.RunicModUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private final Player target;
    private final Map<Integer, ItemStack[]> pages;
    private int currentPage;

    public BankPreview(@Nullable Player target, @NotNull Map<Integer, RunicItem[]> pages) {
        super("&r&d[&5Runic&2Spy&d] > &2Bank Preview", 54, true);
        this.target = target;
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

        for (int i = 9; i < this.getInventory().getSize(); i++) {
            int index = i; //can't use a changing variable in a lambda

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
                    //edit bank data so that item on cursor is set to the index
                    player.setItemOnCursor(null);
                } else { //if item should be taken
                    this.getInventory().setItem(index, null);
                    //edit bank data so that index is set to null/air
                    player.setItemOnCursor(clicked);
                }
            });
        }

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

    @Override
    public void onClose() {
        this.updateHolder();
    }

    /**
     * A method that sets this bank preview to the next page
     */
    public void nextPage() {
        if (this.pages.containsKey(this.currentPage + 1)) {
            this.currentPage++;
            this.updateHolder();
            this.reload();
        }
    }

    /**
     * A method that sets this bank preview to the next page
     */
    public void lastPage() {
        if (this.pages.containsKey(this.currentPage - 1)) {
            this.currentPage--;
            this.updateHolder();
            this.reload();
        }
    }

    /**
     * Sets the underlying {@link BankHolder} inventory in local memory to match this preview
     */
    public void updateHolder() {
        if (this.target == null) {
            return;
        }

        if (!this.target.isOnline()) {
            //RunicBank.getBankWriteOperation().updatePlayerBankData();
            return;
        }

        BankHolder holder = RunicBank.getAPI().getBankHolderMap().get(this.target.getUniqueId());

        if (holder == null) {
            throw new IllegalStateException("Player " + this.target.getName() + " is online but has no bank loaded in local memory!?");
        }

        int currentPage = holder.getCurrentPage();

        holder.setCurrentPage(this.currentPage);
        holder.setInventoryContents(this.pages.get(this.currentPage));
        holder.savePage();

        holder.setCurrentPage(currentPage);
    }
}
