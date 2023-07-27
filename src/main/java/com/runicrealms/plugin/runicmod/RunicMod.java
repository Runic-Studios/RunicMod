package com.runicrealms.plugin.runicmod;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChainFactory;
import com.runicrealms.plugin.runicitems.TemplateManager;
import com.runicrealms.plugin.runicmod.api.SpyAPI;
import com.runicrealms.plugin.runicmod.spy.SpyManager;
import com.runicrealms.plugin.runicmod.command.SpyCommand;
import com.runicrealms.plugin.runicmod.ui.RunicModUIListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public final class RunicMod extends JavaPlugin {
    private static RunicMod plugin;
    private SpyManager spyManager;
    private PaperCommandManager commandManager;
    private TaskChainFactory taskChainFactory;

    @Override
    public void onEnable() {
        RunicMod.plugin = this;
        this.spyManager = new SpyManager();
        this.commandManager = new PaperCommandManager(this);
        this.taskChainFactory = BukkitTaskChainFactory.create(this);

        this.commandManager.getCommandConditions().addCondition("is-player", context -> {
            if (!(context.getIssuer().getIssuer() instanceof Player)) {
                throw new ConditionFailedException("This command cannot be run from console!");
            }
        });

        this.commandManager.getCommandCompletions().registerAsyncCompletion("item-ids", context -> {
            if (!context.getSender().isOp()) return new HashSet<>();
            return TemplateManager.getTemplates().keySet();
        });

        Bukkit.getPluginManager().registerEvents(this.spyManager, this);
        Bukkit.getPluginManager().registerEvents(new RunicModUIListener(), this);
        this.commandManager.registerCommand(new SpyCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    /**
     * A method that returns the singleton instance of the plugin
     *
     * @return the singleton instance of the plugin
     */
    @NotNull
    public static RunicMod getInstance() {
        if (RunicMod.plugin == null) {
            throw new IllegalStateException("You called the getInstance() method before RunicMod was enabled!");
        }

        return RunicMod.plugin;
    }

    /**
     * A method that returns the spy api
     *
     * @return the spy api
     */
    @NotNull
    public SpyAPI getSpyAPI() {
        return this.spyManager;
    }

    /**
     * A method that returns this plugin's instance of a task chain factory
     *
     * @return this plugin's instance of a task chain factory
     */
    @NotNull
    public TaskChainFactory getTaskChainFactory() {
        return this.taskChainFactory;
    }
}
