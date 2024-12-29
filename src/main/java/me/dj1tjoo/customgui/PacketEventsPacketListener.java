package me.dj1tjoo.customgui;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import me.dj1tjoo.customgui.titles.TitleShownManager;

public class PacketEventsPacketListener implements PacketListener {
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        User user = event.getUser();
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_ROTATION) {
            WrapperPlayClientPlayerRotation playerRotation = new WrapperPlayClientPlayerRotation(event);

//            Bukkit.getLogger().info("rotate");
            TitleShownManager.TitleShownEntry titleEntry = TitleShownManager.getInstance().getShowingTo(user.getUUID());
            if (titleEntry != null) {
                titleEntry.title().onRotationPacket(event, playerRotation);
            }
        } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_INPUT) {
//            Bukkit.getLogger().info("input");
            WrapperPlayClientPlayerInput playerInput = new WrapperPlayClientPlayerInput(event);
            TitleShownManager.TitleShownEntry titleEntry = TitleShownManager.getInstance().getShowingTo(user.getUUID());
            if (titleEntry != null) {
                titleEntry.title().close();
            }
        } else if (event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
            // Only gets called when item in hand
            WrapperPlayClientUseItem useItem = new WrapperPlayClientUseItem(event);
//            Bukkit.getLogger().info("use: " + useItem.getHand() +  " " + useItem.getSequence());
        } else if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            WrapperPlayClientAnimation interactEntity = new WrapperPlayClientAnimation(event);
            TitleShownManager.TitleShownEntry titleEntry = TitleShownManager.getInstance().getShowingTo(user.getUUID());
            if (titleEntry != null) {
                titleEntry.title().onAnimationPacket(event, interactEntity);
            }
        }

    }
}
