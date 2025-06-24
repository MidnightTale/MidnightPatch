package fun.mntale.midnightPatch.module.entity.player.fakeplayer;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class DummyConnection extends ServerGamePacketListenerImpl {
    public DummyConnection(MinecraftServer server, Connection networkManager, ServerPlayer player, CommonListenerCookie cookie) {
        super(server, networkManager, player, cookie);
    }

    @Override
    public void send(@javax.annotation.Nonnull Packet<?> packet) {
        // Do nothing
    }
} 