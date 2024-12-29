package me.dj1tjoo.customgui;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.dj1tjoo.customgui.commands.OpenGUICommand;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomGUIPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        // Plugin startup logic

        PacketEvents.getAPI().getEventManager().registerListener(
            new PacketEventsPacketListener(), PacketListenerPriority.NORMAL);

//        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        PluginCommand
            openGUI = getCommand(OpenGUICommand.COMMAND);
        openGUI.setTabCompleter(new OpenGUICommand(this));
        openGUI.setExecutor(new OpenGUICommand(this));

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
