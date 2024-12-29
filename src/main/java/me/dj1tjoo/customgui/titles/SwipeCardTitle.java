package me.dj1tjoo.customgui.titles;

import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAnimation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerRotation;
import me.dj1tjoo.customgui.ComponentHelpers;
import me.dj1tjoo.customgui.MinecraftHelpers;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.time.Instant;

public class SwipeCardTitle extends BasicTitle implements Listener {

    public enum Status {
        IN_WALLET, PICKED_UP, FAILED, COMPLETED,
    }

    private Status status = Status.IN_WALLET;
    private double cardOffset;
    private Instant initialMove;

    private float initialYaw;
    private float initialPitch;

    public SwipeCardTitle(Plugin plugin) {
        super(plugin);
    }


    @Override
    protected void createModules() {

    }

    @Override
    protected void register() {
        super.register();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    protected void unregister() {
        super.unregister();

        PlayerQuitEvent.getHandlerList().unregister(this);
        PlayerInteractEntityEvent.getHandlerList().unregister(this);
        PlayerInteractEvent.getHandlerList().unregister(this);
    }

    @Override
    public void open(HumanEntity humanEntity) {
        super.open(humanEntity);
        humanEntity.playSound(
            Sound.sound(Key.key("among_us", "card_swipe.card_swipe_open"), Sound.Source.NEUTRAL,
                1.0f, 1.0f));
    }

    @Override
    public void closed(HumanEntity humanEntity) {
        super.closed(humanEntity);
        humanEntity.playSound(
            Sound.sound(Key.key("among_us", "card_swipe.card_swipe_close"), Sound.Source.NEUTRAL,
                1.0f, 1.0f));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!isShowingTo(event.getPlayer())) {
            return;
        }

        close();
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!isShowingTo(event.getPlayer())) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (isShowingTo(event.getPlayer())) {
            return;
        }
        event.setCancelled(true);
    }

    @Override
    public void onRotationPacket(PacketReceiveEvent event,
                                 WrapperPlayClientPlayerRotation playerRotation) {
        event.setCancelled(true);

        float yaw = playerRotation.getYaw();
        float pitch = playerRotation.getPitch();

        float normalizedYaw = ((-yaw + 180) % 360) - 180;
        float normalizedPitch = Math.clamp(pitch, -90, 90);

        if (initialYaw == 0) {
            initialYaw = yaw;
        }
        if (initialPitch == 0) {
            initialPitch = pitch;
        }

        float normalizedInitialYaw = ((-initialYaw + 180) % 360) - 180;
        float normalizedInitialPitch = Math.clamp(initialPitch, -90, 90);

        HumanEntity humanEntity = event.getPlayer();

        if (Math.abs(normalizedInitialYaw - normalizedYaw) > 1 ||
            Math.abs(normalizedInitialPitch - normalizedPitch) > 1) {

            WrapperPlayServerPlayerRotation packet =
                new WrapperPlayServerPlayerRotation(initialYaw, initialPitch);
            event.getUser().sendPacket(packet);

            if (status == Status.IN_WALLET || status == Status.COMPLETED ||
                status == Status.FAILED) {
                return;
            }

            float diff = normalizedInitialYaw - normalizedYaw;

            if (diff / 3 < -2) {
                cardOffset = 0;
                status = Status.FAILED;
                humanEntity.playSound(Sound.sound(Key.key("among_us", "card_swipe.card_swipe_fail"),
                    Sound.Source.NEUTRAL, 1.0f, 1.0f));
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    cardOffset = 0;
                    status = Status.PICKED_UP;
                    reopen(event.getPlayer());
                }, MinecraftHelpers.secondsToTicks(0.5));
            } else if (diff > 0) {
                if (cardOffset == 0) {
                    initialMove = Instant.now();
                    humanEntity.playSound(
                        Sound.sound(Key.key("among_us", "card_swipe.card_swipe_swipe"),
                            Sound.Source.NEUTRAL, 1.0f, 1.0f));
                }

                cardOffset += diff / 3;
                if (cardOffset >= 45) {
                    cardOffset = 45;
                    Instant currenTime = Instant.now();
                    double delta =
                        (currenTime.toEpochMilli() - initialMove.toEpochMilli()) / 1000.0;

                    double speed = cardOffset / delta;
                    if (speed < 90 || speed > 110) {
                        status = Status.FAILED;
                        humanEntity.playSound(
                            Sound.sound(Key.key("among_us", "card_swipe.card_swipe_fail"),
                                Sound.Source.NEUTRAL, 1.0f, 1.0f));
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            cardOffset = 0;
                            status = Status.PICKED_UP;
                            reopen(event.getPlayer());
                        }, MinecraftHelpers.secondsToTicks(0.5));
                    } else {
                        status = Status.COMPLETED;
                        humanEntity.playSound(
                            Sound.sound(Key.key("among_us", "card_swipe.card_swipe_completed"),
                                Sound.Source.NEUTRAL, 1.0f, 1.0f));
                        Bukkit.getScheduler().runTaskLater(plugin, this::close,
                            MinecraftHelpers.secondsToTicks(0.5));
                    }
                }
            }

            reopen(event.getPlayer());
        }
    }

    @Override
    public void onAnimationPacket(PacketReceiveEvent event, WrapperPlayClientAnimation animation) {
        event.setCancelled(true);

        if (status != Status.IN_WALLET) {
            return;
        }

        status = Status.PICKED_UP;

        ((HumanEntity) event.getPlayer()).playSound(
            Sound.sound(Key.key("among_us", "card_swipe.card_swipe_pickup"), Sound.Source.NEUTRAL,
                1.0f, 1.0f));
        reopen(event.getPlayer());
    }

    @Override
    protected Title createTitle() {
        return Title.title(createMainTitleComponent(), Component.empty(),
            Title.Times.times(Duration.ZERO, Duration.ofDays(1), Duration.ZERO));
    }

    private Component createMainTitleComponent() {
        final Component walletTable = ComponentHelpers.inPlace(
            ComponentHelpers.bitmap("among_us.tasks.swipe_card.wallet_table",
                ComponentHelpers.AMONG_US_FONT), 62, 0);

        Component card;
        Component text;

        if (status == null || status.equals(Status.IN_WALLET)) {
            final Component cardInWallet = ComponentHelpers.inPlace(
                ComponentHelpers.bitmap("among_us.tasks.swipe_card.card_in_wallet",
                    ComponentHelpers.AMONG_US_FONT), 28, 0);
            final Component walletOverlay = ComponentHelpers.inPlace(
                ComponentHelpers.bitmap("among_us.tasks.swipe_card.wallet_overlay",
                    ComponentHelpers.AMONG_US_FONT), 34, 0);
            card = ComponentHelpers.join(cardInWallet, walletOverlay);

            text = ComponentHelpers.inPlace(
                ComponentHelpers.bitmap("among_us.tasks.swipe_card.scanner_insert_text",
                    ComponentHelpers.AMONG_US_FONT), 32, 0);
        } else {
            card = ComponentHelpers.inPlace(
                ComponentHelpers.bitmap("among_us.tasks.swipe_card.card_in_scanner",
                    ComponentHelpers.AMONG_US_FONT), 20, cardOffset);

            text = ComponentHelpers.inPlace(
                ComponentHelpers.bitmap("among_us.tasks.swipe_card.scanner_swipe_text",
                    ComponentHelpers.AMONG_US_FONT), 31, 0);
        }

        final Component scannerTop = ComponentHelpers.inPlace(
            ComponentHelpers.bitmap("among_us.tasks.swipe_card.scanner_top",
                ComponentHelpers.AMONG_US_FONT), 61, 0);

        Component statusLight = null;
        if (status != null && status.equals(Status.FAILED)) {
            statusLight = ComponentHelpers.inPlace(
                ComponentHelpers.bitmap("among_us.tasks.swipe_card.scanner_red",
                    ComponentHelpers.AMONG_US_FONT), 54, 2);
        } else if (status != null && status.equals(Status.COMPLETED)) {
            statusLight = ComponentHelpers.inPlace(
                ComponentHelpers.bitmap("among_us.tasks.swipe_card.scanner_green",
                    ComponentHelpers.AMONG_US_FONT), 59, 2);
        }

        final Component space = ComponentHelpers.space(64);

        return ComponentHelpers.join(walletTable, card, scannerTop, text, statusLight, space)
            .shadowColor(ShadowColor.none());
    }
}
