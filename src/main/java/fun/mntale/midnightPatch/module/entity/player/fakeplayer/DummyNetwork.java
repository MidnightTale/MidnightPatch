package fun.mntale.midnightPatch.module.entity.player.fakeplayer;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import java.net.SocketAddress;
import io.netty.channel.local.LocalAddress;

public class DummyNetwork extends Connection {
    public DummyNetwork() {
        super(PacketFlow.CLIENTBOUND);
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return new LocalAddress("dummy");
    }

    // Optionally override other methods to do nothing or return safe defaults
}