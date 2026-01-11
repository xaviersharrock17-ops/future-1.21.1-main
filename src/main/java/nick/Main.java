package nick;

import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;
import java.util.Set;

public class Main implements IMixinConfigPlugin {

    public static boolean DEBUG = true; // Enable debug for mixins

    @Override
    public void onLoad(String mixinPackage) {
        if (DEBUG) {
            System.out.println("[Future Mod] Loading mixins from package: " + mixinPackage);
        }
        try {
            Patcher.initialize(); // Your mod’s initialization logic for modules
        } catch (Throwable t) {
            t.printStackTrace();
        }
        MixinEnvironment.getCurrentEnvironment().setSide(MixinEnvironment.Side.CLIENT); // Force client side
    }

    @Override
    public String getRefMapperConfig() {
        return null; // Not using a separate refmap
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true; // Allow all mixins by default
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
        // You can log targeted classes if needed
    }

    @Override
    public List<String> getMixins() {
        return List.of(); // Keep empty; mixins are loaded via separate JSONs
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // Optional: called before each mixin applies
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        // Optional: called after each mixin applies
    }
}
