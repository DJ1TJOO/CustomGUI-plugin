package me.dj1tjoo.customgui.titles;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAnimation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerRotation;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import me.dj1tjoo.customgui.titles.modules.TitleModule;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.boat.OakBoat;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class BasicTitle {
    protected final Plugin plugin;
    protected final UUID uuid;
    private final List<TitleModule> modules;
    private org.bukkit.entity.ArmorStand armorStand;
    private OakBoat boat;
    private Location location;

    protected Title title;

    protected BasicTitle(Plugin plugin) {
        this.plugin = plugin;
        this.uuid = UUID.randomUUID();
        this.modules = new ArrayList<>();

        title = createTitle();
    }

    protected abstract void createModules();

    protected boolean registerModule(TitleModule module) {
        if (modules.contains(module)) {
            return false;
        }
        return modules.add(module);
    }

    protected boolean unregisterModule(TitleModule module) {
        return modules.remove(module);
    }

    protected TitleModule getModule(UUID uuid) {
        for (TitleModule c : modules) {
            if (c.getUUID().equals(uuid)) {
                return c;
            }
        }

        return null;
    }

    protected <T extends TitleModule> T getModule(Class<T> module) {
        for (TitleModule c : modules) {
            if (module.isInstance(c)) {
                return module.cast(c);
            }
        }

        return null;
    }

    protected <T extends TitleModule> List<T> getModules(Class<T> module) {
        List<T> modulesToGet = new ArrayList<>();
        for (TitleModule c : modules) {
            if (!module.isInstance(c)) {
                continue;
            }
            modulesToGet.add(module.cast(c));
        }

        return modulesToGet;
    }

    protected void register() {
        for (TitleModule module : modules) {
            module.register(plugin);
        }
    }

    protected void unregister() {
        for (TitleModule module : modules.reversed()) {
            module.unregister();
        }
    }

    public void closed(@Nullable HumanEntity humanEntity) {
        unregister();

        if (humanEntity == null) {
            return;
        }

        Bukkit.getLogger().info("Closing " + humanEntity.getName());

        if (armorStand != null) {
            WrapperPlayServerDestroyEntities removeArmorStand =
                new WrapperPlayServerDestroyEntities(armorStand.getEntityId());
            PacketEvents.getAPI().getPlayerManager().sendPacket(humanEntity, removeArmorStand);
        }

        if (boat != null) {
            WrapperPlayServerSetPassengers setPassengers =
                new WrapperPlayServerSetPassengers(boat.getEntityId(),
                    new int[] {});
            PacketEvents.getAPI().getPlayerManager().sendPacket(humanEntity, setPassengers);

            WrapperPlayServerDestroyEntities removeBoat =
                new WrapperPlayServerDestroyEntities(boat.getEntityId());
            PacketEvents.getAPI().getPlayerManager().sendPacket(humanEntity, removeBoat);
        }

        if (location != null) {
            WrapperPlayServerEntityTeleport teleport =
                new WrapperPlayServerEntityTeleport(humanEntity.getEntityId(), new Vector3d(location.getX(), location.getY(),
                    location.getZ()), location.getYaw(), location.getPitch(), humanEntity.isOnGround());
            PacketEvents.getAPI().getPlayerManager().sendPacket(humanEntity, teleport);
        }

        WrapperPlayServerChangeGameState packet = new WrapperPlayServerChangeGameState(
            WrapperPlayServerChangeGameState.Reason.CHANGE_GAME_MODE,
            humanEntity.getGameMode().getValue());
        PacketEvents.getAPI().getPlayerManager().sendPacket(humanEntity, packet);

        WrapperPlayServerCamera camera = new WrapperPlayServerCamera(humanEntity.getEntityId());
        PacketEvents.getAPI().getPlayerManager().sendPacket(humanEntity, camera);

    }

    public void close() {
        TitleShownManager.getInstance().remove(this);
    }

    public void onRotationPacket(PacketReceiveEvent event,
                                 WrapperPlayClientPlayerRotation playerRotation) {

    }

    public void onAnimationPacket(PacketReceiveEvent event, WrapperPlayClientAnimation animation) {

    }

    public void open(HumanEntity humanEntity) {
        if (isShowingTo(humanEntity)) {
            return;
        }

        register();

        title = createTitle();
        TitleShownManager.getInstance().showTitleTo(humanEntity.getUniqueId(),
            TitleShownManager.TitleShownEntry.fromTitle(this, title),
            plugin);
        humanEntity.showTitle(title);

        location = humanEntity.getLocation();

        WrapperPlayServerChangeGameState packet = new WrapperPlayServerChangeGameState(
            WrapperPlayServerChangeGameState.Reason.CHANGE_GAME_MODE, GameMode.SPECTATOR.getId());

        // Finally, send it to the player.
        PacketEvents.getAPI().getPlayerManager().sendPacket(humanEntity, packet);

//        boat = (OakBoat) humanEntity.getWorld().spawnEntity(humanEntity.getLocation(), EntityType.OAK_BOAT);
//        boat.addPassenger(humanEntity);
//        boat.setInvisible(true);
//        boat.setInvulnerable(true);
//        boat.setNoPhysics(true);
//        boat.setGravity(false);
//        boat.setSilent(true);


        boat = humanEntity.getWorld()
            .createEntity(new Location(humanEntity.getWorld(), humanEntity.getX(), 500, humanEntity.getZ()), OakBoat.class);

//        if (humanEntity.getLocation().getDirection().getY() > )

        WrapperPlayServerSpawnEntity spawnBoat =
            new WrapperPlayServerSpawnEntity(boat.getEntityId(), boat.getUniqueId(),
                EntityTypes.OAK_BOAT, new com.github.retrooper.packetevents.protocol.world.Location(
                new Vector3d(boat.getLocation().getX(), boat.getLocation().getY(),
                    boat.getLocation().getZ()), boat.getYaw(), boat.getPitch()),
                humanEntity.getYaw(), 0, null);

        WrapperPlayServerEntityMetadata updateBoatAttributes =
            new WrapperPlayServerEntityMetadata(boat.getEntityId(), List.of(
                new EntityData(5, EntityDataTypes.BOOLEAN, true) // Has no gravity
            ));

        PacketEvents.getAPI().getPlayerManager().sendPacket(humanEntity, spawnBoat);
        PacketEvents.getAPI().getPlayerManager().sendPacket(humanEntity, updateBoatAttributes);

        WrapperPlayServerSetPassengers setPassengers =
            new WrapperPlayServerSetPassengers(boat.getEntityId(),
                new int[] {humanEntity.getEntityId()});
        PacketEvents.getAPI().getPlayerManager().sendPacket(humanEntity, setPassengers);

        armorStand = humanEntity.getWorld()
            .createEntity(humanEntity.getLocation(), ArmorStand.class);

        WrapperPlayServerSpawnEntity spawnArmorStand =
            new WrapperPlayServerSpawnEntity(armorStand.getEntityId(), armorStand.getUniqueId(),
                EntityTypes.ARMOR_STAND, new com.github.retrooper.packetevents.protocol.world.Location(
                new Vector3d(armorStand.getLocation().getX(), armorStand.getLocation().getY(),
                    armorStand.getLocation().getZ()), armorStand.getYaw(), armorStand.getPitch()),
                humanEntity.getYaw(), 0, null);

        WrapperPlayServerEntityMetadata updateArmorStandAttributes =
            new WrapperPlayServerEntityMetadata(spawnArmorStand.getEntityId(), List.of(
                new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20), // Invisible
                new EntityData(5, EntityDataTypes.BOOLEAN, true) // Has no gravity
            ));

        PacketEvents.getAPI().getPlayerManager().sendPacket(humanEntity, spawnArmorStand);
        PacketEvents.getAPI().getPlayerManager().sendPacket(humanEntity, updateArmorStandAttributes);

        // ServerboundMovePlayerPacket
        // ServerboundInteractPacket
//        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin,
//            <ServerboundMovePlayerPacket channel name>, new PluginMessageListener() {
//                @Override
//                public void onPluginMessageReceived(@NotNull String s, @NotNull Player player,
//                                                    @NotNull byte[] bytes) {
//
//                }
//            });

//        ArmorStand armorStandNMS = ((CraftArmorStand) armorStand).getHandle();
//        ClientboundSetCameraPacket setCameraPacket = new ClientboundSetCameraPacket(armorStandNMS);
//        listener.send(setCameraPacket);

        WrapperPlayServerCamera camera = new WrapperPlayServerCamera(armorStand.getEntityId());
        PacketEvents.getAPI().getPlayerManager().sendPacket(humanEntity, camera);
    }

    public void reopen(HumanEntity humanEntity) {
        if (!isShowingTo(humanEntity)) {
            return;
        }

        title = createTitle();
        TitleShownManager.getInstance().showTitleTo(humanEntity.getUniqueId(),
            TitleShownManager.TitleShownEntry.fromTitle(this, title),
            plugin);
        humanEntity.showTitle(title);
    }

    protected abstract Title createTitle();

    public UUID getUuid() {
        return uuid;
    }

    public boolean isShowingTo(HumanEntity humanEntity) {
        return TitleShownManager.getInstance().isShowingTo(humanEntity.getUniqueId(), this);
    }
}
