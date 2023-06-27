package com.runicrealms.runicspy.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.SpyAPI;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.runicspy.spy.SpyInfo;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
                + "\t&9- /spy bank - &6opens a preview of the target's bank");
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

        SpyAPI api = RunicCore.getSpyAPI();

        SpyInfo info = api.getInfo(player);

        if (info != null && info.getTarget().getUniqueId().equals(target.getUniqueId())) {
            api.removeSpy(player);
            this.send(player, "&9Stopped spying on " + arg + ".");
            return;
        }

        api.setSpy(player, target);
        this.send(player, "&9Started spying on " + arg + ".");
    }

    @Subcommand("stop")
    @CommandCompletion("@nothing")
    private void onStop(@NotNull Player player) {
        SpyAPI api = RunicCore.getSpyAPI();

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
        SpyAPI api = RunicCore.getSpyAPI();

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
        SpyAPI api = RunicCore.getSpyAPI();

        SpyInfo info = api.getInfo(player);

        if (info == null) {
            this.send(player, "&cYou are not currently in spy mode!");
            return;
        }

        api.previewBank(player);
        this.send(player, "&9Previewing " + info.getTarget().getName() + "'s bank!");
    }

    /**
     * Shorthand to send a message to a user
     *
     * @param sender the user
     * @param text   the message
     */
    private void send(@NotNull CommandSender sender, @NotNull String text) {
        sender.sendMessage(ColorUtil.format("&r&d[&5Runic&2Spy&d] > &r" + text));
    }
}
