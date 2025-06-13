package fun.mntale.midnightPatch;

import org.bukkit.plugin.java.JavaPlugin;
import fun.mntale.midnightPatch.chunk.EnderPearlChunkManager;

public final class MidnightPatch extends JavaPlugin {
    public static MidnightPatch instance;
    private EnderPearlChunkManager chunkManager;
    private MossBlockManager mossBlockManager;
    private DragonEggManager dragonEggManager;

    @Override
    public void onEnable() {
        instance = this;
        chunkManager = new EnderPearlChunkManager(this);
        chunkManager.initialize();
        
        mossBlockManager = new MossBlockManager(this);
        mossBlockManager.initialize();
        
        dragonEggManager = new DragonEggManager(this);
        dragonEggManager.initialize();
    }

    @Override
    public void onDisable() {
        if (chunkManager != null) {
            chunkManager.shutdown();
        }
    }
}
