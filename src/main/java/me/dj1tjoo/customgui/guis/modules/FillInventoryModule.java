package me.dj1tjoo.customgui.guis.modules;

import me.dj1tjoo.customgui.guis.BasicGUI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class FillInventoryModule implements GUIModule, Listener {
    public enum Type {
        SINGLE, EVERY
    }

    private final UUID uuid;
    private final BasicGUI gui;
    private final Type type;
    private final Filler filler;

    private boolean alreadyFilled;
    private Inventory inventory;
    private Inventory playerInventory;

    public FillInventoryModule(BasicGUI gui, Type type, Filler filler) {
        uuid = UUID.randomUUID();
        this.gui = gui;
        this.type = type;
        this.filler = filler;

        this.inventory = Bukkit.createInventory(null, gui.getSize());
        this.playerInventory = Bukkit.createInventory(null, InventoryType.PLAYER);
    }

    @Override
    public void register(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unregister() {
        InventoryOpenEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!gui.isInventoryOpened(event)) {
            return;
        }

        if (type != Type.SINGLE || !alreadyFilled) {
            filler.fill(uuid);
            alreadyFilled = true;
        }

        event.getInventory().setContents(inventory.getContents());
        event.getPlayer().getInventory().setContents(playerInventory.getContents());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!gui.isInventoryOpened(event)) {
            return;
        }

        inventory.setContents(event.getInventory().getContents());
        playerInventory.setContents(event.getPlayer().getInventory().getContents());
    }

    public interface Filler {
        void fill(UUID uuid);
    }

    public void resetFilled() {
        alreadyFilled = false;
    }

    public boolean isAlreadyFilled() {
        return alreadyFilled;
    }

    public Inventory getStoredInventory() {
        return inventory;
    }

    public void storeInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public Inventory getStoredPlayerInventory() {
        return playerInventory;
    }

    public void storePlayerInventory(Inventory playerInventory) {
        this.playerInventory = playerInventory;
    }
}
