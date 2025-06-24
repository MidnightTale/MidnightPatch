package fun.mntale.midnightPatch.module.entity.player.task.effect;

import org.bukkit.block.Block;

public class BlockAnimationUtil {
    
    public static void sendBlockCrackAnimation(Block block, int entityId, int progress) {
        try {
            if (block == null) {
                return;
            }
            
            org.bukkit.World world = block.getWorld();
            if (world == null) {
                return;
            }
            
            if (!(world instanceof org.bukkit.craftbukkit.CraftWorld)) {
                return;
            }
            
            net.minecraft.server.level.ServerLevel serverLevel = ((org.bukkit.craftbukkit.CraftWorld) world).getHandle();
            if (serverLevel == null) {
                return;
            }
            
            net.minecraft.core.BlockPos blockPos = ((org.bukkit.craftbukkit.block.CraftBlock) block).getPosition();
            if (blockPos == null) {
                return;
            }
            
            if (progress < -1 || progress > 10) {
                progress = Math.max(-1, Math.min(10, progress));
            }
            
            serverLevel.destroyBlockProgress(entityId, blockPos, progress);
            
        } catch (Exception e) {
            System.err.println("Error sending block crack animation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void sendBlockDestructionPacket(int entityId, net.minecraft.core.BlockPos blockPos, int progress) {
        try {
            for (org.bukkit.entity.Player onlinePlayer : org.bukkit.Bukkit.getOnlinePlayers()) {
                net.minecraft.server.level.ServerPlayer serverPlayer = ((org.bukkit.craftbukkit.entity.CraftPlayer) onlinePlayer).getHandle();
                net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket packet = 
                    new net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket(entityId, blockPos, progress);
                serverPlayer.connection.send(packet);
            }
            
        } catch (Exception e) {
            System.err.println("Error sending block destruction packet: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 