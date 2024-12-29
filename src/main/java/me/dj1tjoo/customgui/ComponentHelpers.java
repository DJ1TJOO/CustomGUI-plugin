package me.dj1tjoo.customgui;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.TextColor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ComponentHelpers {
    public static Key MINECRAFT_FONT = Key.key("minecraft", "default");
    public static Key SPACE_FONT = Key.key("space", "default");
    public static Key GUI_FONT = Key.key("custom_gui", "default");
    public static Key AMONG_US_FONT = Key.key("among_us", "default");

    public static int MINECRAFT_CHARACTER_SPACING = 1;

    public static Component inPlace(Component component, int width, int offset) {
        Component offsetComponent = offset(component, offset);
        Component resetComponent = space(-width - MINECRAFT_CHARACTER_SPACING * 2);

        return join(offsetComponent, resetComponent);
    }

    public static Component inPlace(Component component, double width, double offset) {
        Component offsetComponent = offset(component, offset);
        Component resetComponent = space(-width - MINECRAFT_CHARACTER_SPACING * 2);

        return join(offsetComponent, resetComponent);
    }

    public static Component inPlace(Component component, int width, double offset) {
        Component offsetComponent = offset(component, offset);
        Component resetComponent = space(-width - MINECRAFT_CHARACTER_SPACING * 2);

        return join(offsetComponent, resetComponent);
    }

    public static Component inPlace(Component component, double width, int offset) {
        Component offsetComponent = offset(component, offset);
        Component resetComponent = space(-width - MINECRAFT_CHARACTER_SPACING * 2);

        return join(offsetComponent, resetComponent);
    }

    public static Component bitmap(String translatableKey, Key font) {
        return Component.translatable(translatableKey).font(font)
            .color(TextColor.fromCSSHexString("#ffffff"));
    }

    public static Component space(int space) {
        return spaceInternal(String.valueOf(space));
    }

    public static Component space(double space) {
        return space(toSpaceWidthFraction(space));
    }

    public static Component space(SpaceWidthFraction widthFraction) {
        if (widthFraction.fraction == null && widthFraction.integer == null) {
            return Component.empty();
        }

        if (widthFraction.integer == null) {
            return spaceInternal(widthFraction.fraction);
        }

        if (widthFraction.fraction == null) {
            return spaceInternal(widthFraction.integer);
        }

        return spaceInternal(widthFraction.integer).append(spaceInternal(widthFraction.fraction));
    }

    private static Component spaceInternal(String space) {
        return Component.translatable("space." + space).font(SPACE_FONT);
    }

    public static Component offset(Component component, int offset) {
        return offsetInternal(component, String.valueOf(offset));
    }

    public static Component offset(Component component, double offset) {
        return offset(component, toSpaceWidthFraction(offset));
    }

    public static Component offset(Component component, SpaceWidthFraction widthFraction) {
        if (widthFraction.fraction == null && widthFraction.integer == null) {
            return component;
        }

        if (widthFraction.integer == null) {
            return offsetInternal(component, widthFraction.fraction);
        }

        if (widthFraction.fraction == null) {
            return offsetInternal(component, widthFraction.integer);
        }

        return offsetInternal(offsetInternal(component, widthFraction.fraction), widthFraction.integer);
    }

    private static Component offsetInternal(Component component, String offset) {
        return Component.translatable("offset." + offset).arguments(component).font(SPACE_FONT);
    }

    public static SpaceWidthFraction toSpaceWidthFraction(double width) {
        if (width == 0) {
            return new SpaceWidthFraction("0", null);
        }

        boolean isNegative = width < 0;
        if (isNegative) {
            width = -width;
        }

        long integer = (long) width;
        width -= integer;

        double error = Math.abs(width);
        int bestDenominator = 1;
        List<Integer> factors = List.of(2, 3, 4, 5, 6, 8, 10, 12, 15, 16, 20, 24, 25, 30, 32, 40, 48, 50, 60, 64, 75, 80, 96, 100);
        for(int factor : factors) {
            double error2 = Math.abs(width - (double) Math.round(width * factor) / factor);
            if (error2 < error) {
                error = error2;
                bestDenominator = factor;
            }
        }

        String integerString = integer != 0 ? String.valueOf(integer) : null;
        if (bestDenominator > 1) {
            return new SpaceWidthFraction(integerString, Math.round(width * bestDenominator) + "/" + bestDenominator);
        }

        return new SpaceWidthFraction(integerString, null);
    }

    public static Component join(ComponentLike... components) {
        return Component.join(JoinConfiguration.builder().build(), Arrays.stream(components).filter(
            Objects::nonNull).toArray(ComponentLike[]::new));
    }

    public record SpaceWidthFraction(String integer, String fraction) {}
}
