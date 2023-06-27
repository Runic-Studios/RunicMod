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

public final class RunicSpy extends JavaPlugin {
    private static RunicSpy plugin;
    private SpyManager spyManager;
    private PaperCommandManager commandManager;

    @Override
    public void onEnable() {
        RunicSpy.plugin = this;
        this.spyManager = new SpyManager();
        this.commandManager = new PaperCommandManager(this);

        this.commandManager.getCommandConditions().addCondition("is-player", context -> {
            if (!(context.getIssuer().getIssuer() instanceof Player)) {
                throw new ConditionFailedException("This command cannot be run from console!");
            }
        });

        Bukkit.getPluginManager().registerEvents(this.spyManager, this);
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
    public static RunicSpy getInstance() {
        if (RunicSpy.plugin == null) {
            throw new IllegalStateException("You called the getInstance() method before RunicSpy was enabled!");
        }

        return RunicSpy.plugin;
    }

    /**
     * A method that returns the spy api
     *
     * @return
     */
    @NotNull
    public SpyAPI getSpyAPI() {
        return this.spyManager;
    }
}
