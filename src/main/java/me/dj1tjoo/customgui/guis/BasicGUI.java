package me.dj1tjoo.customgui.guis;

import me.dj1tjoo.customgui.guis.modules.GUIModule;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class BasicGUI implements InventoryHolder, Listener {
    protected final Plugin plugin;
    protected final UUID uuid;

    protected final int size;
    private final List<GUIModule> modules;

    protected Inventory inventory;

    private boolean closed;

    protected BasicGUI(Plugin plugin, int size) {
        this.plugin = plugin;
        this.uuid = UUID.randomUUID();
        this.size = size;
        this.modules = new ArrayList<>();

        createModules();

        inventory = createInventory();
        closed = true;
    }

    protected abstract void createModules();

    protected void register() {
        for (GUIModule module : modules) {
            module.register(plugin);
        }

        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    protected void unregister() {
        for (GUIModule module : modules.reversed()) {
            module.unregister();
        }

        InventoryCloseEvent.getHandlerList().unregister(this);
    }

    protected boolean registerModule(GUIModule module) {
        if (modules.contains(module)) return false;
        return modules.add(module);
    }

    protected boolean unregisterModule(GUIModule module) {
        return modules.remove(module);
    }

    protected GUIModule getModule(UUID uuid) {
        for(GUIModule c : modules) {
            if(c.getUUID().equals(uuid)) return c;
        }

        return null;
    }

    protected <T extends GUIModule> T getModule(Class<T> module) {
        for(GUIModule c : modules) {
            if(module.isInstance(c)) return (T) c;
        }

        return null;
    }

    protected <T extends GUIModule> List<T> getModules(Class<T> module) {
        List<T> modulesToGet = new ArrayList<>();
        for(GUIModule c : modules) {
            if(!module.isInstance(c)) continue;
            modulesToGet.add((T) c);
        }

        return modulesToGet;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) {
            return;
        }

        if (!isInventoryOpened(event)) {
            return;
        }

        // TODO: actually test with multiple users
        if (event.getViewers().size() > 1) {
            return;
        }

        closed = true;
        unregister();
    }

    public void open(HumanEntity humanEntity) {
        if (!isClosed()) {
            return;
        }

        closed = false;
        register();

        inventory = createInventory();
        //Bukkit.getScheduler().runTask(plugin, () ->
        humanEntity.openInventory(inventory);
    }

    public void reopen(HumanEntity humanEntity) {
        if (closed) {
            return;
        }

        inventory = createInventory();
        humanEntity.openInventory(inventory);
    }

    protected abstract Component createTitle();

    private Inventory createInventory() {
        return Bukkit.createInventory(this, size, createTitle());
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getSize() {
        return size;
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isInventory(InventoryEvent event) {
        Inventory inventory = event.getInventory();
        return isInventory(inventory);
    }

    public boolean isInventory(Inventory inventory) {
        InventoryHolder holder = inventory.getHolder(false);
        if (!(holder instanceof BasicGUI basicGUI)) {
            return false;
        }

        return basicGUI.getUuid().equals(uuid);
    }

    public boolean isInventoryOpened(InventoryEvent event) {
        Inventory inventory = event.getInventory();
        return isInventoryOpened(inventory);
    }

    public boolean isInventoryOpened(Inventory inventory) {
        return isInventory(inventory) && !isClosed();
    }
}
