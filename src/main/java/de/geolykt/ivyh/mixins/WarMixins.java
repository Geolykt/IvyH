package de.geolykt.ivyh.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.geolykt.starloader.api.empire.War;

@Mixin(priority = 1100, value = snoddasmannen.galimulator.War.class)
public abstract class WarMixins implements War {

    @SuppressWarnings("null")
    @Inject(at = @At("TAIL"), method = "<init>", require = 1)
    private void lateInit(CallbackInfo ci) {
        throw new UnsupportedOperationException("Due to IvyH the constructor may not be invoked. Use IvyWar instead.");
    }
}
