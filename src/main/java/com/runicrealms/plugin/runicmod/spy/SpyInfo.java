package com.runicrealms.plugin.runicmod.spy;

import com.google.common.collect.ImmutableList;
import com.runicrealms.plugin.bank.RunicBank;
import com.runicrealms.plugin.bank.model.BankHolder;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.runicitems.item.RunicItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

/**
 * A class that contains all information needed by the plugin for the spy
 *
 * @author BoBoBalloon
 * @since 6/24/23
 */
public class SpyInfo {
    private final UUID target;
    private final Location origin;
    private final BukkitTask task;
    private final int characterSlot;
    private Player targetReference;
    private Location center;
    private ItemStack[] contents;
    private ItemStack[] armor;
    private Map<Integer, RunicItem[]> bankPages;

    public static final ImmutableList<EquipmentSlot> ARMOR_SLOTS = ImmutableList.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.OFF_HAND);

    public SpyInfo(@NotNull Player target, @NotNull Location origin, @NotNull BukkitTask task) {
        this.target = target.getUniqueId();
        this.origin = origin;
        this.task = task;
        this.characterSlot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(target.getUniqueId());
        this.targetReference = target;
        this.center = target.getLocation();
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
    public UUID getTargetUUID() {
        return this.target;
    }

    /**
     * A method that returns the user being spied on
     *
     * @return the user being spied on
     */
    @NotNull
    public Player getTarget() {
        Player player = Bukkit.getPlayer(this.target);

        if (player != null) {
            this.targetReference = player;
        }

        return this.targetReference;
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
     * A method that returns the selected character slot of the player being spied on
     *
     * @return the selected character slot of the player being spied on
     */
    public int getCharacterSlot() {
        return this.characterSlot;
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
    @Nullable
    public ItemStack[] getContents() {
        return this.contents == null || this.isTargetOnline() ? this.getTarget().getInventory().getStorageContents() : this.contents;
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
    @Nullable
    public ItemStack[] getArmor() {
        if (this.armor != null && !this.isTargetOnline()) {
            return this.armor;
        }

        ItemStack[] armor = new ItemStack[EquipmentSlot.values().length - 1];
        for (int i = 0; i < SpyInfo.ARMOR_SLOTS.size(); i++) {
            armor[i] = this.getTarget().getInventory().getItem(SpyInfo.ARMOR_SLOTS.get(i));
        }

        return armor;
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
    @Nullable
    public Map<Integer, RunicItem[]> getBankPages() {
        BankHolder bank = RunicBank.getAPI().getBankHolderMap().get(this.target);

        if (bank == null && this.bankPages == null) {
            return null;
        }

        //if this.target.isOnline() is true, the bank will not return null
        return bank != null && (this.bankPages == null || this.isTargetOnline()) ? bank.getRunicItemContents() : this.bankPages;
    }

    /**
     * A method that sets the "cache" of the target's bank pages
     *
     * @param bankPages the "cache" of the target's bank pages
     */
    public void setBankPages(@Nullable Map<Integer, RunicItem[]> bankPages) {
        this.bankPages = bankPages;
    }

    /**
     * A method used to check if the target is online
     *
     * @return if the target is online
     */
    public boolean isTargetOnline() {
        return this.getTarget().isOnline() && RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(this.target) == this.characterSlot;
    }
}
