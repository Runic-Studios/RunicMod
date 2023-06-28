package com.runicrealms.runicspy.spy;

import com.runicrealms.RunicChat;
import com.runicrealms.api.chat.ChatChannel;
import com.runicrealms.api.event.ChatChannelMessageEvent;
import com.runicrealms.channels.StaffChannel;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.ChatUtils;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.runicspy.RunicMod;
import com.runicrealms.runicspy.api.SpyAPI;
import com.runicrealms.runicspy.ui.InventoryPreview;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A class that manages all mods in spy mode
 *
 * @author BoBoBalloon
 * @since 6/24/23
 */
public final class SpyManager implements SpyAPI, Listener {
    private final Map<UUID, SpyInfo> spies;

    public SpyManager() {
        this.spies = new HashMap<>();
    }

    /**
     * A method that returns all the info needed by the plugin for the spy
     *
     * @param spy the moderator spying on another user
     * @return the necessary info or null if the player is not in spy mode
     */
    @Override
    @Nullable
    public SpyInfo getInfo(@NotNull Player spy) {
        return this.spies.get(spy.getUniqueId());
    }

    /**
     * A method used to set a player into spy mode
     *
     * @param spy    the player in spy mode
     * @param target the player being spied on
     */
    @Override
    public void setSpy(@NotNull Player spy, @NotNull Player target) {
        if (spy.getUniqueId().equals(target.getUniqueId())) {
            return;
        }

        if (this.spies.containsKey(target.getUniqueId())) {
            this.removeSpy(target);
        }

        if (this.spies.containsKey(spy.getUniqueId())) {
            this.removeSpy(spy);
        }

        BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(RunicMod.getInstance(), () -> {
            SpyInfo info = this.getInfo(spy);

            if (info == null) {
                throw new IllegalStateException("This cannot be run until the spy is registered!");
            }

            Bukkit.getScheduler().runTask(RunicMod.getInstance(), () -> {
                if (info.getTarget().isOnline()) {
                    info.setCenter(target.getLocation());
                }

                if (info.getCenter().distance(spy.getLocation()) >= 200) {
                    spy.teleport(info.getCenter(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            });
        }, 20, 100);

        if (!RunicCore.getVanishAPI().getVanishedPlayers().contains(spy)) {
            RunicCore.getVanishAPI().hidePlayer(spy);
        }

        RunicChat.getRunicChatAPI().setWhisperSpy(spy, target, true);

        spy.setGameMode(GameMode.SPECTATOR);
        spy.teleport(target.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);

        this.spies.put(spy.getUniqueId(), new SpyInfo(target, spy.getLocation(), task, target.getLocation()));

        ChatChannel staffChannel = this.getStaffChannel();

        RunicChat.getRunicChatAPI().setPlayerChatChannel(spy, staffChannel);

        for (Player player : staffChannel.getRecipients(spy)) {
            ChatUtils.sendCenteredMessage(player, "&r&9&l" + spy.getName() + " is spying on " + target.getName());
        }
    }

    /**
     * A method that removes the player from the spy list
     *
     * @param spy the moderator spying on another player
     */
    @Override
    public void removeSpy(@NotNull Player spy) {
        SpyInfo info = this.spies.remove(spy.getUniqueId());

        if (info == null) {
            return;
        }

        info.getTask().cancel();

        if (RunicCore.getVanishAPI().getVanishedPlayers().contains(spy)) {
            RunicCore.getVanishAPI().showPlayer(spy);
        }

        spy.setGameMode(GameMode.ADVENTURE);
        spy.teleport(info.getOrigin(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        RunicChat.getRunicChatAPI().setWhisperSpy(spy, info.getTarget(), false);
    }

    /**
     * A method that starts a preview on the targeter user's inventory
     *
     * @param spy the spy looking to preview their target's inventory
     */
    @Override
    public void previewInventory(@NotNull Player spy) {
        SpyInfo info = this.spies.get(spy.getUniqueId());

        if (info == null) {
            return;
        }

        spy.closeInventory();

        InventoryPreview preview = new InventoryPreview(info.getContents(), info.getArmor());
        spy.openInventory(preview.getInventory());
    }

    /**
     * A method that starts a preview on the targeter user's bank
     *
     * @param spy the spy looking to preview their target's bank
     */
    @Override
    public void previewBank(@NotNull Player spy) {
        SpyInfo info = this.spies.get(spy.getUniqueId());

        if (info == null) {
            return;
        }

        spy.closeInventory();
    }

    /**
     * A method that returns the staff channel
     *
     * @return the staff channel
     */
    @NotNull
    private ChatChannel getStaffChannel() {
        Optional<ChatChannel> optional = RunicChat.getRunicChatAPI().getChatChannels().stream()
                .filter(channel -> channel instanceof StaffChannel)
                .findAny();

        if (optional.isEmpty()) {
            throw new IllegalStateException("There must be a staff channel registered!");
        }

        return optional.get();
    }

    @EventHandler
    private void onCharacterLeave(@NotNull CharacterQuitEvent event) {
        this.removeSpy(event.getPlayer()); //remove spy if they exist

        for (Map.Entry<UUID, SpyInfo> pair : this.spies.entrySet()) {
            SpyInfo info = pair.getValue();

            if (info.getTarget().getUniqueId().equals(event.getPlayer().getUniqueId())) {
                info.setContents(event.getPlayer().getInventory().getContents());
                info.setArmor(event.getPlayer().getInventory().getArmorContents());
                //set bank pages preview here
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerTeleport(@NotNull PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE && this.spies.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onChatChannelMessage(@NotNull ChatChannelMessageEvent event) {
        for (UUID uuid : this.spies.keySet()) {
            SpyInfo info = this.spies.get(uuid);

            List<UUID> recipients = event.getRecipients().stream().map(Player::getUniqueId).toList();

            if (recipients.contains(info.getTarget().getUniqueId()) &&
                    !recipients.contains(uuid) &&
                    event.getSpies().stream().map(Player::getUniqueId).noneMatch(id -> id.equals(uuid))) {
                event.getSpies().add(Bukkit.getPlayer(uuid));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerCommandPreprocess(@NotNull PlayerCommandPreprocessEvent event) {
        if (this.spies.containsKey(event.getPlayer().getUniqueId()) && !event.getMessage().startsWith("/spy") && !event.getMessage().startsWith("/whois")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ColorUtil.format("&r&cYou can only execute the spy and whois command while in spy mode!"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onInventoryClick(@NotNull InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();

        if (inventory != null && inventory.getHolder() instanceof InventoryPreview) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onInventoryDrag(@NotNull InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof InventoryPreview) {
            event.setCancelled(true);
        }
    }
}
