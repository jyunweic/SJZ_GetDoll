package com.jyunChen.sjzgetdoll;

import com.jyunChen.sjzgetdoll.commands.DollCommand;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;

public final class SJZ_GetDoll extends JavaPlugin {

    private static final Logger log = Logger.getLogger("Minecraft");

    @Override
    public void onEnable() {
        log.info("[" + this.getDescription().getName() + "] Plugin Enabled! Final Alias Attempt by jyunChen.");

        DollCommand dollCommand = new DollCommand(this);
        this.getCommand("doll").setExecutor(dollCommand);
        this.getCommand("doll").setTabCompleter(dollCommand);

        log.info("[" + this.getDescription().getName() + "] Doll command registered.");
    }

    @Override
    public void onDisable() {
        log.info("[" + this.getDescription().getName() + "] Plugin Disabled!");
    }
}