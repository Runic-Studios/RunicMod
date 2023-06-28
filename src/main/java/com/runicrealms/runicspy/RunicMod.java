package com.runicrealms.runicspy;

import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.PaperCommandManager;
import com.runicrealms.runicspy.api.SpyAPI;
import com.runicrealms.runicspy.command.SpyCommand;
import com.runicrealms.runicspy.spy.SpyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class RunicMod extends JavaPlugin {
    private static RunicMod plugin;
    private static SpyManager spyManager;
    private PaperCommandManager commandManager;

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
     * @return
     */
    @NotNull
    public static SpyAPI getSpyAPI() {
        return spyManager;
    }

    @Override
    public void onEnable() {
        RunicMod.plugin = this;
        spyManager = new SpyManager();
        this.commandManager = new PaperCommandManager(this);

        this.commandManager.getCommandConditions().addCondition("is-player", context -> {
            if (!(context.getIssuer().getIssuer() instanceof Player)) {
                throw new ConditionFailedException("This command cannot be run from console!");
            }
        });

        Bukkit.getPluginManager().registerEvents(spyManager, this);
        this.commandManager.registerCommand(new SpyCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
