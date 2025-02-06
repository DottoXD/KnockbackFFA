package me.dotto.CopperKnockbackFFA;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class KnockbackFFA extends JavaPlugin {
    Database db;
    FileConfiguration config = getConfig();
    KnockbackFFAListener listener;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        getLogger().info("CopperKnockbackFFA [1.8x] got loaded!");
        listener = new KnockbackFFAListener(config, this);
        getServer().getPluginManager().registerEvents(listener, this);

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("Just hooked into PlaceholderAPI!");
            new PlaceholderAPI(listener).register();
        }

        db = new Database(config, getLogger());
    }

    @Override
    public void onDisable() {
        db.Disconnect();
    }
}
