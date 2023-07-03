package com.runicrealms.runicspy.spy;

import com.runicrealms.RunicChat;
import com.runicrealms.api.chat.ChatChannel;
import com.runicrealms.api.event.ChatChannelMessageEvent;
import com.runicrealms.channels.StaffChannel;
import com.runicrealms.plugin.RunicBank;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.BankOpenEvent;
import com.runicrealms.plugin.common.util.ChatUtils;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.model.BankHolder;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.rdb.event.CharacterHasQuitEvent;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicspy.RunicMod;
import com.runicrealms.runicspy.api.SpyAPI;
import com.runicrealms.runicspy.ui.preview.BankPreview;
import com.runicrealms.runicspy.ui.preview.InventoryPreview;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
                if (info.getTarget().isOnline() && RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(info.getTarget().getUniqueId()) != -1) {
                    info.setCenter(target.getLocation());
                }

                if (info.getCenter().distance(spy.getLocation()) >= 50) {
                    spy.teleport(info.getCenter(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            });
        }, 20, 100);

        if (!RunicCore.getVanishAPI().getVanishedPlayers().contains(spy)) {
            RunicCore.getVanishAPI().hidePlayer(spy);
        }

        RunicChat.getRunicChatAPI().setWhisperSpy(spy, target, true);

        spy.setFlying(true);

        this.spies.put(spy.getUniqueId(), new SpyInfo(target, spy.getLocation(), task));

        spy.teleport(target.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);

        ChatChannel staffChannel = this.getStaffChannel();

        RunicChat.getRunicChatAPI().setPlayerChatChannel(spy, staffChannel);

        for (Player player : staffChannel.getRecipients(spy)) {
            ChatUtils.sendCenteredMessage(player, ColorUtil.format("&r&9&l" + spy.getName() + " is spying on " + target.getName()));
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

        spy.setFlying(false);
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

        if (RunicBank.getAPI().isViewingBank(info.getTarget().getUniqueId())) {
            info.getTarget().closeInventory();
        }

        spy.closeInventory();

        Map<Integer, RunicItem[]> bankPages = info.getBankPages();

        if (bankPages == null) {
            spy.sendMessage(ColorUtil.format("&cThe player does not have a bank loaded!"));
            return;
        }

        BankPreview preview = new BankPreview(bankPages);
        spy.openInventory(preview.getInventory());
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
    private void onCharacterLeave(@NotNull CharacterHasQuitEvent event) {
        this.removeSpy(event.getPlayer()); //remove spy if they exist

        for (Map.Entry<UUID, SpyInfo> pair : this.spies.entrySet()) {
            SpyInfo info = pair.getValue();

            if (!info.getTarget().getUniqueId().equals(event.getPlayer().getUniqueId())) {
                continue;
            }

            info.setContents(event.getPlayer().getInventory().getContents());
            info.setArmor(event.getPlayer().getInventory().getArmorContents());

            BankHolder bank = RunicBank.getAPI().getBankHolderMap().get(info.getTarget().getUniqueId());

            if (bank == null) {
                return;
            }

            info.setBankPages(bank.getRunicItemContents());
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerTeleport(@NotNull PlayerTeleportEvent event) {
        if (event.getTo() == null) {
            return;
        }

        for (Map.Entry<UUID, SpyInfo> entry : this.spies.entrySet()) {
            if (!entry.getValue().getTarget().getUniqueId().equals(event.getPlayer().getUniqueId())) {
                continue;
            }

            Player spy = Bukkit.getPlayer(entry.getKey());

            if (spy == null) {
                continue;
            }

            spy.teleport(event.getTo(), PlayerTeleportEvent.TeleportCause.PLUGIN);
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
    private void onBankOpen(@NotNull BankOpenEvent event) {
        for (Map.Entry<UUID, SpyInfo> entry : this.spies.entrySet()) {
            if (entry.getValue().getTarget().getUniqueId().equals(entry.getValue().getTarget().getUniqueId()) && Bukkit.getPlayer(entry.getKey()).getOpenInventory().getTopInventory().getHolder() instanceof BankPreview) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (this.spies.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void onPlayerInteractAtEntity(@NotNull PlayerInteractAtEntityEvent event) {
        if (this.spies.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onEntityTarget(@NotNull EntityTargetEvent event) {
        if (event.getTarget() != null && this.spies.containsKey(event.getTarget().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onEntityPickup(@NotNull EntityPickupItemEvent event) {
        if (this.spies.containsKey(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onPlayerDropItem(@NotNull PlayerDropItemEvent event) {
        if (this.spies.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
