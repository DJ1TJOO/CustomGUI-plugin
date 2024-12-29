package me.dj1tjoo.customgui.titles.modules;

import org.bukkit.plugin.Plugin;

import java.util.UUID;

public interface TitleModule {
    void register(Plugin plugin);
    void unregister();
    UUID getUUID();
}
