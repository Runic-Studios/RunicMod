package com.runicrealms.runicspy.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.runicrealms.plugin.RunicBank;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.model.BankHolder;
import com.runicrealms.runicitems.RunicItems;
import com.runicrealms.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.runicspy.api.RunicModAPI;
import com.runicrealms.runicspy.api.SpyAPI;
import com.runicrealms.runicspy.spy.SpyInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The command that acts as an interface to the spy system
 *
 * @author BoBoBalloon
 * @since 6/25/23
 */
@CommandAlias("spy")
@CommandPermission("runic.spy")
@Conditions("is-player")
public class SpyCommand extends BaseCommand {
    @CatchUnknown
    @Default
    private void onHelp(@NotNull CommandSender sender) {
        this.send(sender, "&cImproper arguments:\n"
                + "\t&9- /spy on <player> - &6enabled/disables spy mode on the given target\n"
                + "\t&9- /spy stop - &6disables spy mode\n"
                + "\t&9- /spy inventory - &6opens a preview of the target's inventory\n"
                + "\t&9- /spy bank - &6opens a preview of the target's bank\n"
                + (sender.hasPermission("runic.spy.wipe") ? "\t&9- /spy wipe <item-id> - &6wipes all items of the given ID from the target's bank and inventory" : ""));
    }

    @Subcommand("on")
    @CommandCompletion("@players @nothing")
    private void onSpy(@NotNull Player player, @NotNull String[] args) {
        if (args.length != 1) {
            this.onHelp(player);
            return;
        }

        String arg = args[0];

        Player target = Bukkit.getPlayerExact(arg);

        if (target == null) {
            this.send(player, "&cCould not find player " + arg + "!");
            return;
        }

        if (player.getUniqueId().equals(target.getUniqueId())) {
            this.send(player, "&cYou cannot spy on yourself!");
            return;
        }

        if (target.hasPermission("runic.spy")) {
            this.send(player, "&cYou cannot spy on someone else who has spy permissions!");
            return;
        }

        SpyAPI api = RunicModAPI.getSpyAPI();

        SpyInfo info = api.getInfo(player);

        if (info != null && info.getTarget().getUniqueId().equals(target.getUniqueId())) {
            api.removeSpy(player);
            this.send(player, "&9Stopped spying on " + arg + ".");
            return;
        }

        api.setSpy(player, target);
        this.send(player, "&9Started spying on " + arg + ".");
    }

    @Subcommand("stop|off")
    @CommandCompletion("@nothing")
    private void onStop(@NotNull Player player) {
        SpyAPI api = RunicModAPI.getSpyAPI();

        SpyInfo info = api.getInfo(player);

        if (info == null) {
            this.send(player, "&cYou are not currently in spy mode!");
            return;
        }

        api.removeSpy(player);

        this.send(player, "&9Stopped spying on " + info.getTarget().getName() + ".");
    }

    @Subcommand("inventory|inv")
    @CommandCompletion("@nothing")
    private void onInventory(@NotNull Player player) {
        SpyAPI api = RunicModAPI.getSpyAPI();

        SpyInfo info = api.getInfo(player);

        if (info == null) {
            this.send(player, "&cYou are not currently in spy mode!");
            return;
        }

        api.previewInventory(player);
        this.send(player, "&9Previewing " + info.getTarget().getName() + "'s inventory!");
    }

    @Subcommand("bank")
    @CommandCompletion("@nothing")
    private void onBank(@NotNull Player player) {
        SpyAPI api = RunicModAPI.getSpyAPI();

        SpyInfo info = api.getInfo(player);

        if (info == null) {
            this.send(player, "&cYou are not currently in spy mode!");
            return;
        }

        api.previewBank(player);
        this.send(player, "&9Previewing " + info.getTarget().getName() + "'s bank!");
    }

    @Subcommand("wipe")
    @CommandCompletion("@item-ids @nothing")
    @CommandPermission("runic.spy.wipe")
    private void onWipe(@NotNull Player player, @NotNull String[] args) {
        player.sendMessage(ColorUtil.format("&r&cNot implemented yet... Coming soon!"));
        //CODE BELOW DOES NOT WIPE BANK CORRECTLY FOR OFFLINE PLAYERS, IT'S BECAUSE THEY ARE NOT IN THE CACHE (refer to first line of implementation of updateBankData)
        /*
        if (args.length != 1) {
            this.onHelp(player);
            return;
        }

        SpyAPI api = RunicModAPI.getSpyAPI();

        SpyInfo info = api.getInfo(player);

        if (info == null) {
            this.send(player, "&cYou are not currently in spy mode!");
            return;
        }

        RunicItemTemplate template = TemplateManager.getTemplateFromId(args[0]);

        if (template == null) {
            this.send(player, "&cThat is an invalid item template id!");
            return;
        }

        if (info.isTargetOnline()) {
            RunicItems.getInventoryAPI().clearInventory(info.getTarget().getInventory(), template, null, true);
            this.clearBankHolder(RunicBank.getAPI().getBankHolderMap().get(info.getTarget().getUniqueId()), info, template);
            return;
        }

        //online but not same character slot
        if (info.getTarget().isOnline()) {
            RunicBank.getAPI().getLockedOutPlayers().add(info.getTarget().getUniqueId());
        }

        RunicMod.getInstance().getTaskChainFactory().newChain()
                .asyncFirst(() -> RunicBank.getAPI().loadPlayerBankData(info.getTarget().getUniqueId()))
                .sync(playerBankData -> {
                    if (playerBankData == null && info.getTarget().isOnline()) {
                        RunicBank.getAPI().getLockedOutPlayers().remove(info.getTarget().getUniqueId());
                    }

                    return playerBankData;
                })
                .abortIfNull(BankManager.CONSOLE_LOG, info.getTarget(), "RunicMod failed to load bank data on onWipe()!")
                .syncLast(playerBankData -> {
                    this.clearBankHolder(playerBankData.getBankHolder(), info, template);
                    //RunicBank.getAPI().getLockedOutPlayers().remove(info.getTarget().getUniqueId());
                })
                .execute();
        RunicMod.getInstance().getTaskChainFactory().newChain()
                .asyncFirst(() -> RunicItems.getDataAPI().loadInventoryData(info.getTarget().getUniqueId(), info.getCharacterSlot()))
                .abortIfNull(BankManager.CONSOLE_LOG, info.getTarget(), "RunicMod failed to load inventory data on onWipe()!")
                .syncLast(inventoryData -> {
                    RunicDatabase.getAPI().getDataAPI().preventLogin(info.getTarget().getUniqueId());
                    RunicItem[] inventory = inventoryData.getContentsMap().get(info.getCharacterSlot());

                    for (int i = 0; i < inventory.length; i++) {
                        if (inventory[i] != null && inventory[i].getTemplateId().equals(template.getId())) {
                            inventory[i] = null;
                        }
                    }

                    ((ItemWriteOperation) RunicItems.getDataAPI()).updateInventoryData(info.getTarget().getUniqueId(), info.getCharacterSlot(), inventory, () -> {
                    });
                })
                .execute();

        //make memory in SpyInfo match changes
        for (int i = 0; i < info.getArmor().length; i++) {
            RunicItem item = ItemManager.getRunicItemFromItemStack(info.getArmor()[i]);

            if (item == null || !item.getTemplateId().equals(template.getId())) {
                continue;
            }

            info.getArmor()[i] = null;
        }

        for (int i = 0; i < info.getContents().length; i++) {
            RunicItem item = ItemManager.getRunicItemFromItemStack(info.getContents()[i]);

            if (item == null || !item.getTemplateId().equals(template.getId())) {
                continue;
            }

            info.getContents()[i] = null;
        }

        Map<Integer, RunicItem[]> bankPages = info.getBankPages();

        if (bankPages == null || RunicBank.getAPI().getBankHolderMap().get(info.getTarget().getUniqueId()) == null) {
            return;
        }

        for (RunicItem[] page : bankPages.values()) {
            for (int i = 0; i < page.length; i++) {
                RunicItem item = page[i];

                if (item == null || !item.getTemplateId().equals(template.getId())) {
                    continue;
                }

                page[i] = null;
            }
        }
         */
    }

    /**
     * A method used to clear a given item from a bank
     *
     * @param holder   the bank inventory holder
     * @param info     the spy info
     * @param template the item to be removed
     */
    private void clearBankHolder(@Nullable BankHolder holder, @NotNull SpyInfo info, @NotNull RunicItemTemplate template) {
        if (holder == null) {
            return;
        }

        RunicBank.getAPI().getLockedOutPlayers().add(info.getTarget().getUniqueId());

        if (RunicBank.getAPI().isViewingBank(info.getTarget().getUniqueId())) {
            info.getTarget().closeInventory();
            RunicBank.getAPI().getBankHolderMap().get(info.getTarget().getUniqueId()).setOpen(false);
        }

        for (int i = 0; i <= holder.getMaxPageIndex(); i++) {
            holder.setCurrentPage(i);
            RunicItems.getInventoryAPI().clearInventory(holder.getInventory(), template, null, true);
            holder.savePage();
        }

        RunicBank.getBankWriteOperation().updatePlayerBankData
                (
                        info.getTarget().getUniqueId(),
                        holder.getRunicItemContents(),
                        holder.getMaxPageIndex(),
                        true, //TODO MAKE REMOVELOCKOUT WORK
                        () -> RunicBank.getAPI().getLockedOutPlayers().remove(info.getTarget().getUniqueId())
                );
    }

    /**
     * Shorthand to send a message to a user
     *
     * @param sender the user
     * @param text   the message
     */
    private void send(@NotNull CommandSender sender, @NotNull String text) {
        sender.sendMessage(ColorUtil.format("&r&d[&5Runic&2Spy&d] » &r" + text));
    }
}
