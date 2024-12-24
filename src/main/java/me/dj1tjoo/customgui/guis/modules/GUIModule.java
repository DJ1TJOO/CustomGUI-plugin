package me.dj1tjoo.customgui.guis.modules;

import org.bukkit.plugin.Plugin;

import java.util.UUID;

public interface GUIModule {
    void register(Plugin plugin);
    void unregister();
    UUID getUUID();
}
