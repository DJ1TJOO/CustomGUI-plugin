package me.dj1tjoo.customgui.guis.modules;

import me.dj1tjoo.customgui.guis.BasicGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClearPlayerInventoryModule implements GUIModule, Listener {

    public enum Type {
        PREVENT, DISPOSE, STORE
    }

    private final UUID uuid;
    private final BasicGUI gui;
    private final Type type;

    private final Map<UUID, @NotNull Inventory> playerInventories;

    public ClearPlayerInventoryModule(BasicGUI gui, Type type) {
        uuid = UUID.randomUUID();
        this.gui = gui;
        this.type = type;

        playerInventories = new HashMap<>();
    }

    @Override
    public void register(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unregister() {
        InventoryOpenEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
        EntityPickupItemEvent.getHandlerList().unregister(this);
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) {
            return;
        }

        if (!gui.isInventory(event)) {
            return;
        }

        HumanEntity player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!playerInventories.containsKey(uuid)) {
            return;
        }

        event.getPlayer().getInventory().setContents(playerInventories.get(uuid).getContents());
        playerInventories.remove(uuid);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!gui.isInventoryOpened(event)) {
            return;
        }

        HumanEntity player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (playerInventories.containsKey(uuid)) {
            return;
        }

        Inventory newPlayerInventory = Bukkit.createInventory(null, InventoryType.PLAYER);
        newPlayerInventory.setContents(player.getInventory().getContents());
        playerInventories.put(uuid, newPlayerInventory);

        player.getInventory()
            .setContents(new ItemStack[player.getInventory().getContents().length]);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!(event.getEntity() instanceof HumanEntity player)) {
            return;
        }

        Inventory inventory = player.getOpenInventory().getTopInventory();
        if (!gui.isInventoryOpened(inventory)) {
            return;
        }

        event.setCancelled(true);

        if (type == Type.PREVENT) {
            return;
        }

        if (type == Type.DISPOSE) {
            event.getItem().remove();
            return;
        }

        UUID uuid = player.getUniqueId();
        if (!playerInventories.containsKey(uuid)) throw new NullPointerException("Player inventory was not stored before hand"); // This should never happen

        ItemStack stack = event.getItem().getItemStack();
        HashMap<Integer, ItemStack> leftOverItems = playerInventories.get(uuid).addItem(stack);
        if (leftOverItems.containsKey(0)) {
            event.getItem().setItemStack(leftOverItems.get(0));
        } else {
            event.getItem().remove();
        }
    }
}
