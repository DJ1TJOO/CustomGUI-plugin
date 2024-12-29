package me.dj1tjoo.customgui.guis;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import me.dj1tjoo.customgui.ComponentHelpers;
import me.dj1tjoo.customgui.guis.modules.ClearPlayerInventoryModule;
import me.dj1tjoo.customgui.guis.modules.FillInventoryModule;
import me.dj1tjoo.customgui.guis.modules.GUIButtonModule;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class FirstGUI extends BasicGUI
    implements GUIButtonModule.ClickListener, FillInventoryModule.Filler {
    private GUIButtonModule button;
    private int count;

    public FirstGUI(Plugin plugin) {
        super(plugin, 54);

        count = 0;
    }

    @Override
    protected void createModules() {
        button =
            new GUIButtonModule(this, GUIButtonModule.generateSlots(38, 42), "custom_gui.opslaan",
                132, -8);
        registerModule(button);

        registerModule(new ClearPlayerInventoryModule(this, ClearPlayerInventoryModule.Type.STORE));
        registerModule(new FillInventoryModule(this, FillInventoryModule.Type.SINGLE, this));
    }

    @Override
    protected void register() {
        super.register();

        button.registerClickListener(this);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    protected void unregister() {
        super.unregister();

        button.unregisterClickListener(this);

        InventoryClickEvent.getHandlerList().unregister(this);
    }

    public void onClick(UUID uuid) {
        count++;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isInventoryOpened(event)) {
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (item == null) {
            return;
        }

        Key itemModel = item.getData(DataComponentTypes.ITEM_MODEL);
        if (itemModel == null || !itemModel.equals(Key.key("custom_gui", "title_blocker"))) {
            return;
        }

        event.setCancelled(true);
    }

    @Override
    public void fill(UUID uuid) {
        FillInventoryModule module = (FillInventoryModule) getModule(uuid);

        ItemStack titleBlocker = ItemStack.of(Material.PAPER);

        CustomModelData customModelData =
            CustomModelData.customModelData().addColor(Color.fromRGB(56, 63, 196)).build();
        titleBlocker.setData(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData);

        Key key = Key.key("custom_gui", "title_blocker");
        titleBlocker.setData(DataComponentTypes.ITEM_MODEL, key);

        titleBlocker.setData(DataComponentTypes.HIDE_TOOLTIP);

        Inventory inventory = module.getStoredInventory();
        inventory.setItem(45, titleBlocker);
    }

    @Override
    protected Component createTitle() {
        Component gui = ComponentHelpers.inPlace(
            ComponentHelpers.bitmap("custom_gui.gui", ComponentHelpers.GUI_FONT), 175, -8);

        Component buttonTitle = button.createTitle();

        Component title =
            Component.text("Count: " + count).color(TextColor.fromCSSHexString("#15ff00"));

        return ComponentHelpers.join(gui, buttonTitle, title);
    }
}
