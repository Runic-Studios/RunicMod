package com.runicrealms.runicspy.spy;

import com.runicrealms.plugin.RunicBank;
import com.runicrealms.plugin.model.BankHolder;
import com.runicrealms.runicitems.item.RunicItem;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A class that contains all information needed by the plugin for the spy
 *
 * @author BoBoBalloon
 * @since 6/24/23
 */
public class SpyInfo {
    private final Player target;
    private final Location origin;
    private final BukkitTask task;
    private Location center;
    private ItemStack[] contents;
    private ItemStack[] armor;
    private Map<Integer, RunicItem[]> bankPages;

    public SpyInfo(@NotNull Player target, @NotNull Location origin, @NotNull BukkitTask task, @NotNull Location center) {
        this.target = target;
        this.origin = origin;
        this.task = task;
        this.center = center;
        this.contents = null;
        this.armor = null;
        this.bankPages = null;
    }

    /**
     * A method that returns the user being spied on
     *
     * @return the user being spied on
     */
    @NotNull
    public Player getTarget() {
        return this.target;
    }

    /**
     * A method that returns the location of the spy before they were set into spy mode
     *
     * @return the location of the spy before they were set into spy mode
     */
    @NotNull
    public Location getOrigin() {
        return this.origin;
    }

    /**
     * Gets an instance of the repeating task that makes sure the spy is nearby the spied
     *
     * @return the repeating task that makes sure the spy is nearby the spied
     */
    @NotNull
    public BukkitTask getTask() {
        return this.task;
    }

    /**
     * A method that gets the current target location of the user being spied on
     *
     * @return the current target location of the user being spied on
     */
    @NotNull
    public Location getCenter() {
        return this.center;
    }

    /**
     * A method that sets the current target location of the user being spied on
     *
     * @param center the current target location of the user being spied on
     */
    public void setCenter(@NotNull Location center) {
        this.center = center;
    }

    /**
     * A method that gets the inventory associated with this player
     *
     * @return the inventory associated with this player
     */
    @NotNull
    public ItemStack[] getContents() {
        return this.contents == null || this.target.isOnline() ? this.target.getInventory().getContents() : this.contents;
    }

    /**
     * A method that sets the "cache" of inventory contents
     *
     * @param contents the "cache" of inventory contents
     */
    public void setContents(@Nullable ItemStack[] contents) {
        this.contents = contents;
    }

    /**
     * A method that gets the "cache" of armor contents
     *
     * @return the "cache" of armor contents
     */
    @NotNull
    public ItemStack[] getArmor() {
        return this.armor == null || this.target.isOnline() ? this.target.getInventory().getArmorContents() : this.armor;
    }

    /**
     * A method that sets the "cache" of armor contents
     *
     * @param armor the "cache" of armor contents
     */
    public void setArmor(@Nullable ItemStack[] armor) {
        this.armor = armor;
    }

    /**
     * A method that gets the "cache" of the target's bank pages
     *
     * @return the "cache" of the target's bank pages
     */
    @NotNull
    public Map<Integer, RunicItem[]> getBankPages() {
        BankHolder bank = RunicBank.getAPI().getBankHolderMap().get(this.target.getUniqueId());

        if (bank == null && this.bankPages == null) {
            throw new IllegalStateException("There was an error caching the item data for " + this.target.getName() + "'s bank");
        }

        //if this.target.isOnline() is true, the bank will not return null
        return this.bankPages == null || this.target.isOnline() ? bank.getRunicItemContents() : this.bankPages;
    }

    /**
     * A method that sets the "cache" of the target's bank pages
     *
     * @param bankPages the "cache" of the target's bank pages
     */
    public void setBankPages(@Nullable Map<Integer, RunicItem[]> bankPages) {
        this.bankPages = bankPages;
    }
}
