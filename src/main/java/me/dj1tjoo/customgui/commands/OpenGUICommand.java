package me.dj1tjoo.customgui.commands;

import me.dj1tjoo.customgui.CustomGUIPlugin;
import me.dj1tjoo.customgui.guis.BasicGUI;
import me.dj1tjoo.customgui.guis.FirstGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OpenGUICommand implements CommandExecutor {

    public static String COMMAND = "open-gui";
    private final CustomGUIPlugin plugin;

    private final Map<UUID, BasicGUI> guis;

    public OpenGUICommand(CustomGUIPlugin plugin) {
        this.plugin = plugin;
        this.guis = new HashMap<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command,
                             @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            return true;
        }

        UUID uuid = player.getUniqueId();
        if (!guis.containsKey(uuid)) {
            guis.put(uuid, new FirstGUI(plugin));
        }

        guis.get(uuid).open(player);
//        new FirstGUI(plugin).open(player::openInventory);
        // /give DJ1TJOO minecraft:oak_button[minecraft:item_model='custom_gui:empty',minecraft:hide_tooltip={},minecraft:item_name='Opslaan',minecraft:max_stack_size=1]
        // /give DJ1TJOO minecraft:oak_button[minecraft:item_model='custom_gui:opslaan',minecraft:hide_tooltip={},minecraft:item_name='Opslaan',minecraft:max_stack_size=1]
        return true;
    }
}
