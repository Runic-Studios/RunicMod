package com.runicrealms.plugin.runicmod.spy;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.runicrealms.plugin.chat.RunicChat;
import com.runicrealms.plugin.chat.api.chat.ChatChannel;
import com.runicrealms.plugin.chat.api.event.ChatChannelMessageEvent;
import com.runicrealms.plugin.chat.channels.StaffChannel;
import com.runicrealms.plugin.bank.BankManager;
import com.runicrealms.plugin.bank.RunicBank;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.npcs.api.NpcClickEvent;
import com.runicrealms.plugin.bank.api.event.BankOpenEvent;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.bank.model.BankHolder;
import com.runicrealms.plugin.party.Party;
import com.runicrealms.plugin.party.event.LeaveReason;
import com.runicrealms.plugin.party.event.PartyLeaveEvent;
import com.runicrealms.plugin.rdb.RunicDatabase;
import com.runicrealms.plugin.runicitems.item.RunicItem;
import com.runicrealms.plugin.runicmod.api.SpyAPI;
import com.runicrealms.plugin.runicmod.ui.preview.InventoryPreview;
import com.runicrealms.plugin.runicmod.RunicMod;
import com.runicrealms.plugin.runicmod.ui.preview.BankPreview;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

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
                if (info.isTargetOnline()) {
                    info.setCenter(info.getTarget().getLocation());
                }

                if (info.getCenter().distance(spy.getLocation()) >= 75) {
                    spy.teleport(info.getCenter(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                }
            });
        }, 20, 100);

        Party party = RunicCore.getPartyAPI().getParty(spy.getUniqueId());
        if (party != null) {
            party.sendMessageInChannel(spy.getName() + " has been removed this party &7Reason: left");
            PartyLeaveEvent partyLeaveEvent = new PartyLeaveEvent(party, spy, LeaveReason.LEAVE);
            Bukkit.getPluginManager().callEvent(partyLeaveEvent);
            party.getMembers().remove(spy);
            RunicCore.getPartyAPI().updatePlayerParty(spy.getUniqueId(), null);
        }

        if (!RunicCore.getVanishAPI().getVanishedPlayers().contains(spy)) {
            RunicCore.getVanishAPI().hidePlayer(spy);
        }

        RunicChat.getRunicChatAPI().setWhisperSpy(spy, target, true);

        this.spies.put(spy.getUniqueId(), new SpyInfo(target, spy.getLocation(), task));

        spy.setGameMode(GameMode.SPECTATOR);
        spy.teleport(target.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);

        RunicChat.getRunicChatAPI().setPlayerChatChannel(spy, this.getStaffChannel());
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

        InventoryPreview preview = new InventoryPreview(info);
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
            RunicBank.getAPI().getBankHolderMap().get(info.getTarget().getUniqueId()).setOpen(false);
        }

        spy.closeInventory();

        Map<Integer, RunicItem[]> bankPages = info.getBankPages();

        if (bankPages != null) {
            BankPreview preview = new BankPreview(bankPages);
            spy.openInventory(preview.getInventory());
            return;
        }

        RunicBank.getAPI().getLockedOutPlayers().add(info.getTarget().getUniqueId());

        RunicMod.getInstance().getTaskChainFactory().newChain()
                .asyncFirst(() -> RunicBank.getAPI().loadPlayerBankData(info.getTarget().getUniqueId()))
                .abortIfNull(BankManager.CONSOLE_LOG, info.getTarget(), "RunicMod failed to load bank data on previewBank()!")
                .syncLast(playerBankData -> {
                    if (info.getTarget().isOnline()) { //banks are account wide so it does not matterRunicBank.getAPI().getLockedOutPlayers().remove(info.getTarget().getUniqueId());
                        RunicBank.getAPI().getBankHolderMap().put(info.getTarget().getUniqueId(), playerBankData.getBankHolder());
                        RunicBank.getAPI().getBankHolderMap().get(info.getTarget().getUniqueId()).setCurrentPage(0); //will not ever be null
                    } else {
                        info.setBankPages(playerBankData.getPagesMap());
                    }

                    RunicBank.getAPI().getLockedOutPlayers().remove(info.getTarget().getUniqueId());

                    BankPreview preview = new BankPreview(info.getBankPages());
                    spy.openInventory(preview.getInventory());
                })
                .execute();
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

    @EventHandler(priority = EventPriority.LOWEST) //just listening, I need it to be called ASAP
    private void onCharacterQuit(@NotNull PlayerQuitEvent event) { //CharacterQuitEvent runs too late as it is async, come back to this later
        this.removeSpy(event.getPlayer()); //remove spy if they exist

        for (Map.Entry<UUID, SpyInfo> pair : this.spies.entrySet()) {
            SpyInfo info = pair.getValue();

            if (!info.getTarget().getUniqueId().equals(event.getPlayer().getUniqueId())) {
                continue;
            }

            info.setContents(event.getPlayer().getInventory().getStorageContents());

            ItemStack[] armor = new ItemStack[SpyInfo.ARMOR_SLOTS.size()];
            for (int i = 0; i < SpyInfo.ARMOR_SLOTS.size(); i++) {
                armor[i] = event.getPlayer().getInventory().getItem(SpyInfo.ARMOR_SLOTS.get(i));
            }

            info.setArmor(armor);

            BankHolder bank = RunicBank.getAPI().getBankHolderMap().get(info.getTarget().getUniqueId());

            if (bank == null) {
                RunicMod.getInstance().getLogger().log(Level.SEVERE, "Was unable to save bank data to SpyInfo for " + event.getPlayer().getName());
                return;
            }

            info.setBankPages(bank.getRunicItemContents());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void onPlayerTeleport(@NotNull PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE && this.spies.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }

        if (event.getTo() == null || RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(event.getPlayer().getUniqueId()) == -1) {
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void onBankOpen(@NotNull BankOpenEvent event) {
        for (Map.Entry<UUID, SpyInfo> entry : this.spies.entrySet()) {
            if (entry.getValue().getTarget().getUniqueId().equals(entry.getValue().getTarget().getUniqueId()) && Bukkit.getPlayer(entry.getKey()).getOpenInventory().getTopInventory().getHolder() instanceof BankPreview) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    private void onPlayerStartSpectatingEntity(@NotNull PlayerStartSpectatingEntityEvent event) {
        if (this.spies.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().setSpectatorTarget(null);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST) //I want first say in this
    private void onNpcClick(@NotNull NpcClickEvent event) {
        if (this.spies.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST) //I want first say in this
    private void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (this.spies.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
