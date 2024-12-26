package me.dj1tjoo.customgui.guis;

import io.papermc.paper.datacomponent.DataComponentTypes;
import me.dj1tjoo.customgui.guis.modules.ClearPlayerInventoryModule;
import me.dj1tjoo.customgui.guis.modules.FillInventoryModule;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class CleanVentGUI extends BasicGUI implements FillInventoryModule.Filler {

    private final static  VentItem SMOKE = new VentItem("among_us", "tasks_clean_vent_smoke", "", false);
    private final static  VentItem TITLE_BLOCKER = new VentItem("among_us", "tasks_clean_vent_title_blocker", "", false);
    private final static List<VentItem> GARBAGE_ITEMS = List.of(
        new VentItem("among_us", "tasks_clean_vent_pizza", "Pizza", true),
        new VentItem("among_us", "tasks_clean_vent_tennis_bal", "Tennis Ball", true),
        new VentItem("among_us", "tasks_clean_vent_paper_clip", "Paper Clip", true)
    );

    public CleanVentGUI(Plugin plugin) {
        super(plugin, 54);
    }

    @Override
    protected void createModules() {
        registerModule(new ClearPlayerInventoryModule(this, ClearPlayerInventoryModule.Type.STORE));
        registerModule(new FillInventoryModule(this, FillInventoryModule.Type.SINGLE, this));
    }

    @Override
    protected void register() {
        super.register();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    protected void unregister() {
        super.unregister();

        InventoryClickEvent.getHandlerList().unregister(this);
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
        if (itemModel == null) {
            return;
        }

        if (itemModel.equals(TITLE_BLOCKER.getItemModel()) || itemModel.equals(SMOKE.getItemModel())) {
            event.setCancelled(true);
            return;
        }

        if (GARBAGE_ITEMS.stream().noneMatch(t -> itemModel.equals(t.getItemModel()))) {
            return;
        }

        event.setCancelled(true);
        event.getClickedInventory().setItem(event.getSlot(), SMOKE.createItemStack());
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            event.getClickedInventory().setItem(event.getSlot(), null);
        }, MinecraftHelpers.secondsToTicks(0.5));
    }


    @Override
    public void fill(UUID uuid) {
        FillInventoryModule module = (FillInventoryModule) getModule(uuid);

        Inventory inventory = module.getStoredInventory();
        inventory.setItem(18, TITLE_BLOCKER.createItemStack());

        Random random = new Random();

        int inventoryMin = 27, inventoryMax = 54;
        for (int i = 0; i < 8; i++) {
            int slot = random.nextInt(inventoryMax - inventoryMin) + inventoryMin;
            ItemStack item = GARBAGE_ITEMS.get(random.nextInt(GARBAGE_ITEMS.size())).createItemStack();
            inventory.setItem(slot, item);
        }

        Inventory playerInventory = module.getStoredPlayerInventory();
        int playerMin = 9, playerMax = 35;
        for (int i = 0; i < 8; i++) {
            int slot = random.nextInt(playerMax - playerMin) + playerMin;
            ItemStack item = GARBAGE_ITEMS.get(random.nextInt(GARBAGE_ITEMS.size())).createItemStack();
            playerInventory.setItem(slot, item);
        }
    }

    @Override
    protected Component createTitle() {
        Component gui = ComponentHelpers.offset(
            Component.translatable("among_us.tasks.clean_vent.vent").font(ComponentHelpers.AMONG_US_FONT)
                .color(TextColor.fromCSSHexString("#ffffff")), 256, -8 - 40);

        return ComponentHelpers.join(gui);
    }

    private record VentItem(String namespace, String model, String name, boolean garbage) {
        public Key getItemModel() {
            return Key.key(namespace, model);
        }

        public ItemStack createItemStack() {
            ItemStack item = ItemStack.of(Material.PAPER);

            item.setData(DataComponentTypes.ITEM_MODEL, getItemModel());

            item.setData(DataComponentTypes.ITEM_NAME, Component.text(name));
            item.setData(DataComponentTypes.HIDE_TOOLTIP);

            return item;
        }
    }
}
