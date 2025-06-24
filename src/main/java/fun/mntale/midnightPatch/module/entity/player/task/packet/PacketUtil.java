package fun.mntale.midnightPatch.module.entity.player.task.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.entity.Player;

public class PacketUtil {
    
    public static void sendSwingArmPacket(Player player, int hand) {
        try {
            ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
            PacketContainer packet = protocolManager.createPacket(PacketType.Play.Client.ARM_ANIMATION);
            EnumWrappers.Hand protocolHand =
                (hand == 0) ?
                EnumWrappers.Hand.MAIN_HAND :
                EnumWrappers.Hand.OFF_HAND;
            packet.getHands().write(0, protocolHand);
            protocolManager.receiveClientPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 