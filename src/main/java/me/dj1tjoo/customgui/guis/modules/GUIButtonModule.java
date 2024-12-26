package me.dj1tjoo.customgui.guis.modules;

import me.dj1tjoo.customgui.guis.BasicGUI;
import me.dj1tjoo.customgui.guis.ComponentHelpers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GUIButtonModule implements GUIModule, Listener {
    private final UUID uuid;
    private final BasicGUI gui;
    private Plugin plugin;

    private final List<ClickListener> clickListeners;
    private final List<Integer> slots;

    private final int width, x;
    private final String buttonImage, buttonPressedImage;

    private boolean isPressed;
    private BukkitTask animationTaskPressing, animationTaskReleasing;

    public GUIButtonModule(BasicGUI gui, List<Integer> slots, String buttonImage, int width) {
        this(gui, slots, buttonImage, buttonImage + "_pressed", width, 0);
    }

    public GUIButtonModule(BasicGUI gui, List<Integer> slots, String buttonImage, int width, int x) {
        this(gui, slots, buttonImage, buttonImage + "_pressed", width, x);
    }

    public GUIButtonModule(BasicGUI gui, List<Integer> slots, String buttonImage,
                           String buttonPressedImage, int width, int x) {
        uuid = UUID.randomUUID();
        this.gui = gui;

        clickListeners = new ArrayList<>();

        this.slots = slots;
        this.width = width;
        this.x = x;
        this.buttonImage = buttonImage;
        this.buttonPressedImage = buttonPressedImage;
    }

    @Override
    public void register(Plugin plugin) {
        this.plugin = plugin;

        isPressed = false;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unregister() {
        InventoryClickEvent.getHandlerList().unregister(this);

        if (animationTaskPressing != null) {
            animationTaskPressing.cancel();
        }

        if (animationTaskReleasing != null) {
            animationTaskReleasing.cancel();
        }
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (!gui.isInventoryOpened(event)) {
            return;
        }

        if (slots.contains(event.getRawSlot())) {
            pressButton(event.getWhoClicked());
        }
    }

    private void pressButton(HumanEntity humanEntity) {
        if (isPressed) {
            return;
        }

        isPressed = true;
        animationTaskPressing = Bukkit.getScheduler().runTask(plugin, () -> {
            fireClickEvent();
            gui.reopen(humanEntity);
        });

        animationTaskReleasing = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            isPressed = false;
            gui.reopen(humanEntity);
        }, 7);
    }

    public Component createTitle() {
        Component button = Component.translatable(isPressed ? buttonPressedImage : buttonImage)
            .font(ComponentHelpers.GUI_FONT).color(TextColor.fromCSSHexString("#ffffff"));

        return ComponentHelpers.offset(button, width, x);
    }

    public void registerClickListener(ClickListener listener) {
        clickListeners.add(listener);
    }

    public void unregisterClickListener(ClickListener listener) {
        clickListeners.remove(listener);
    }

    private void fireClickEvent() {
        for (ClickListener listener : clickListeners) {
            listener.onClick(uuid);
        }
    }

    public static List<Integer> generateSlots(int min, int max) {
        List<Integer> slots = new ArrayList<>();
        for (int i = min; i <= max; i++) {
            slots.add(i);
        }

        return slots;
    }

    public interface ClickListener {
        void onClick(UUID uuid);
    }
}
