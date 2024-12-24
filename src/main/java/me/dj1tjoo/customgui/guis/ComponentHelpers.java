package me.dj1tjoo.customgui.guis;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;

public class ComponentHelpers {
    public static Key MINECRAFT_FONT = Key.key("minecraft", "default");
    public static Key SPACE_FONT = Key.key("space", "default");
    public static Key GUI_FONT = Key.key("custom_gui", "default");

    public static int MINECRAFT_CHARACTER_SPACING = 1;

    public static Component offset(Component component, int width, int offset) {
        Component offsetComponent = Component.translatable("offset." + offset).arguments(component).font(SPACE_FONT);
        Component resetComponent = Component.translatable("space." + (-width - MINECRAFT_CHARACTER_SPACING * 2)).font(SPACE_FONT);

        return join(offsetComponent, resetComponent);
    }

    public static Component join(ComponentLike... components) {
        return Component.join(JoinConfiguration.builder().build(), components);
    }
}
