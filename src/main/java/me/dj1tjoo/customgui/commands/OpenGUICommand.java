package me.dj1tjoo.customgui.commands;

import me.dj1tjoo.customgui.CustomGUIPlugin;
import me.dj1tjoo.customgui.titles.SwipeCardTitle;
import me.dj1tjoo.customgui.guis.tasks.CleanVentGUI;
import me.dj1tjoo.customgui.guis.FirstGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OpenGUICommand implements CommandExecutor, TabCompleter {

    public static String COMMAND = "open-gui";
    private static List<String> GUIS = List.of("first", "clean_vent", "swipe_card");
    private final CustomGUIPlugin plugin;

    private final Map<UUID, FirstGUI> firstGUIMap;
    private final Map<UUID, CleanVentGUI> cleanVentGUIMap;

    public OpenGUICommand(CustomGUIPlugin plugin) {
        this.plugin = plugin;
        this.firstGUIMap = new HashMap<>();
        this.cleanVentGUIMap = new HashMap<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command,
                             @NotNull String alias, @NotNull String[] args) {
        if (!(commandSender instanceof Player player) || !command.getName().equals(COMMAND)) {
            return true;
        }

        if (args.length == 0) {
            return false;
        }

        if (!GUIS.contains(args[0])) {
            commandSender.sendMessage("GUI not found");
            return true;
        }

        UUID uuid = player.getUniqueId();

        switch (args[0]) {
            case "first":
                if (!firstGUIMap.containsKey(uuid)) {
                    firstGUIMap.put(uuid, new FirstGUI(plugin));
                }

                firstGUIMap.get(uuid).open(player);
                break;

            case "clean_vent":
                if (!cleanVentGUIMap.containsKey(uuid)) {
                    cleanVentGUIMap.put(uuid, new CleanVentGUI(plugin));
                }

                cleanVentGUIMap.get(uuid).open(player);
                break;

            case "swipe_card":
                new SwipeCardTitle(plugin).open(player);
                break;
        }

        // /give DJ1TJOO minecraft:oak_button[minecraft:item_model='custom_gui:empty',minecraft:hide_tooltip={},minecraft:item_name='Opslaan',minecraft:max_stack_size=1]
        // /give DJ1TJOO minecraft:oak_button[minecraft:item_model='custom_gui:opslaan',minecraft:hide_tooltip={},minecraft:item_name='Opslaan',minecraft:max_stack_size=1]
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender,
                                                @NotNull Command command, @NotNull String alias,
                                                @NotNull String[] args) {
        if (!(commandSender instanceof Player player) || !command.getName().equals(COMMAND)) {
            return null;
        }

        if (args.length > 1) {
            return null;
        }

        if (args.length == 1) {
            return GUIS.stream().filter(string -> string.startsWith(args[0])).toList();
        }

        return GUIS;
    }
}
