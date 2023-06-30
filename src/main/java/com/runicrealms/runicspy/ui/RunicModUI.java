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
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * A class that represnets an interactable UI for RunicMOD
 *
 * @author BoBoBalloon
 * @since 6/29/23
 */
public abstract class RunicModUI implements InventoryHolder {
    private final Inventory inventory;
    private final Map<Integer, BiConsumer<Player, ItemStack>> clickEvents;
    private boolean destroyItemsOnEdge;

    protected static final ItemStack BLANK = RunicModUI.blank();

    public RunicModUI(@NotNull String title, int slots, boolean destroyItemsOnEdge) {
        this.inventory = Bukkit.createInventory(this, slots, ColorUtil.format(title));
        this.clickEvents = new HashMap<>();
        this.destroyItemsOnEdge = destroyItemsOnEdge;
    }

    public RunicModUI(@NotNull String title, int slots) {
        this(title, slots, false);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    /**
     * A method used to check if the viewer clicks outside the inventory view while holding an item if it should be destroyed
     *
     * @return if the viewer clicks outside the inventory view while holding an item if it should be destroyed
     */
    public boolean getDestroyItemsOnEdge() {
        return this.destroyItemsOnEdge;
    }

    /**
     * A method used to set if the viewer clicks outside the inventory view while holding an item if it should be destroyed
     *
     * @param destroyItemsOnEdge if the viewer clicks outside the inventory view while holding an item if it should be destroyed
     */
    public void setDestroyItemsOnEdge(boolean destroyItemsOnEdge) {
        this.destroyItemsOnEdge = destroyItemsOnEdge;
    }

    /**
     * A method that gets the click action for the given index or null if no click action exists
     *
     * @param index the slot index
     * @return the click action for the given index or null if no click action exists
     */
    @Nullable
    public BiConsumer<Player, ItemStack> getClickAction(int index) {
        return this.clickEvents.get(index);
    }

    /**
     * A method that sets a click event given a slot
     *
     * @param index  the slot index
     * @param action the click action
     */
    public void registerClickEvent(int index, @NotNull BiConsumer<Player, ItemStack> action) {
        this.clickEvents.put(index, action);
    }

    /**
     * A method called to reload the inventory
     */
    public abstract void reload();

    /**
     * A method called when the inventory is closed
     */
    public void onClose() {

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
