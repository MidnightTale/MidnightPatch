package fun.mntale.midnightPatch;

import org.bukkit.plugin.java.JavaPlugin;
import fun.mntale.midnightPatch.chunk.EnderPearlChunkManager;
import fun.mntale.midnightPatch.skin.SkinManager;
import fun.mntale.midnightPatch.skin.SkinCommand;
import io.papermc.paper.command.brigadier.BasicCommand;

public final class MidnightPatch extends JavaPlugin {
    public static MidnightPatch instance;
    private EnderPearlChunkManager chunkManager;
    private MossBlockManager mossBlockManager;
    private SkinManager skinManager;

    @Override
    public void onEnable() {
        instance = this;
        chunkManager = new EnderPearlChunkManager(this);
        chunkManager.initialize();
        
        mossBlockManager = new MossBlockManager(this);
        mossBlockManager.initialize();
        
        // Initialize skin manager and register command
        skinManager = new SkinManager(this);
        BasicCommand skinCommand = new SkinCommand(skinManager);
        
        // Register command using Paper's command system
        registerCommand("midnightpatch", skinCommand);
    }

    @Override
    public void onDisable() {
        if (chunkManager != null) {
            chunkManager.shutdown();
        }
    }
}
