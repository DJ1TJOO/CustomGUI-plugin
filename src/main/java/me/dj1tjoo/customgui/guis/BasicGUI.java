package me.dj1tjoo.customgui.guis;

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

import java.util.UUID;

public abstract class BasicGUI implements InventoryHolder, Listener {
    protected final Plugin plugin;

    protected final UUID uuid;
    protected Inventory inventory;

    private boolean closed;

    protected BasicGUI(Plugin plugin) {
        this.plugin = plugin;
        this.uuid = UUID.randomUUID();
        closed = true;

        createModules();

        inventory = createInventory();
    }

    abstract void createModules();

    protected void register() {
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }

    protected void unregister() {
        InventoryCloseEvent.getHandlerList().unregister(this);
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
        Bukkit.getScheduler().runTask(plugin, () -> humanEntity.openInventory(inventory));
    }

    public void reopen(HumanEntity humanEntity) {
        if (closed) {
            return;
        }

        inventory = createInventory();
        humanEntity.openInventory(inventory);
    }

    abstract Inventory createInventory();
    abstract Component createTitle();

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public UUID getUuid() {
        return uuid;
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
