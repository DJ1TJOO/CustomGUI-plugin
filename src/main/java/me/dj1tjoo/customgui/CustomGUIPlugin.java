package me.dj1tjoo.customgui;

import me.dj1tjoo.customgui.commands.OpenGUICommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomGUIPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        // Plugin startup logic
//        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        getCommand(OpenGUICommand.COMMAND).setExecutor(new OpenGUICommand(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
