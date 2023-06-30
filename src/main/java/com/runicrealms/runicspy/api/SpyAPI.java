package com.runicrealms.runicspy.api;

import com.runicrealms.runicspy.spy.SpyInfo;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An API interface to interact with the underlying spy system
 *
 * @author BoBoBalloon
 * @since 6/25/23
 */
public interface SpyAPI {
    /**
     * A method that returns all the info needed by the plugin for the spy
     *
     * @param spy the moderator spying on another user
     * @return the necessary info or null if the player is not in spy mode
     */
    @Nullable
    SpyInfo getInfo(@NotNull Player spy);

    /**
     * A method used to set a player into spy mode
     *
     * @param spy    the player in spy mode
     * @param target the player being spied on
     */
    void setSpy(@NotNull Player spy, @NotNull Player target);

    /**
     * A method that removes the player from the spy list
     *
     * @param spy the moderator spying on another player
     */
    void removeSpy(@NotNull Player spy);

    /**
     * A method that starts a preview on the targeter user's inventory
     *
     * @param spy the spy looking to preview their target's inventory
     */
    void previewInventory(@NotNull Player spy);

    /**
     * A method that starts a preview on the targeter user's bank
     *
     * @param spy the spy looking to preview their target's bank
     * @return if the operation was a success
     */
    boolean previewBank(@NotNull Player spy);
}
