package com.runicrealms.plugin.runicmod.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import com.runicrealms.plugin.chat.api.event.ChatChannelMessageEvent;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.runicmod.RunicMod;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.LinkedList;

/**
 * The command that will print out a log of chat messagess
 *
 * @author BoBoBalloon
 * @since 11/18/23
 */
@CommandAlias("chatlog")
@CommandPermission("runic.spy")
public class ChatLogCommand extends BaseCommand implements Listener {
    private static final int MESSAGES = 1000;
    private static final LinkedList<String> RECENT_MESSSAGES = new LinkedList<>();

    @CatchUnknown
    @Default
    @CommandCompletion("@nothing")
    private void execute(@NotNull CommandSender sender) {
        if (RECENT_MESSSAGES.isEmpty()) {
            sender.sendMessage(ColorUtil.format("&cThere are no chat messsages in the cache!"));
            return;
        }

        String timestamp = new Timestamp(System.currentTimeMillis()).toString();
        String trimmed = timestamp.substring(0, timestamp.length() - 4).replaceAll(":", "-");

        File parent = RunicMod.getInstance().getDataFolder();

        if (!parent.exists()) {
            parent.mkdirs();
        }

        File file = new File(parent, trimmed + ".txt");

        BufferedWriter writer;

        try {
            file.createNewFile();
            writer = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            sender.sendMessage(ColorUtil.format("&cThere was an error accessing a file on disk!"));
            e.printStackTrace();
            return;
        }

        try {
            for (String messsage : RECENT_MESSSAGES) {
                writer.write(messsage);
                writer.newLine();
            }

            writer.close();
        } catch (IOException e) {
            sender.sendMessage(ColorUtil.format("&cThere was an error writing to disk!"));
            e.printStackTrace();
            return;
        }

        RECENT_MESSSAGES.clear();

        sender.sendMessage(ColorUtil.format("&aLogs written to disk at: &f&l" + trimmed));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onChatChannelMessage(ChatChannelMessageEvent event) {
        if (RECENT_MESSSAGES.size() >= MESSAGES) {
            RECENT_MESSSAGES.removeFirst();
        }

        RECENT_MESSSAGES.add(new Timestamp(System.currentTimeMillis()) + " " + event.getMessageSender().getName() + ": " + event.getChatMessage());
    }
}
