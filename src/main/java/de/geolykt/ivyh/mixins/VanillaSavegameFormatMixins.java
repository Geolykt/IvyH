package de.geolykt.ivyh.mixins;

import java.io.InputStream;
import java.util.Vector;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.impl.serial.VanillaSavegameFormat;

@Mixin(VanillaSavegameFormat.class)
public class VanillaSavegameFormatMixins {
    @Redirect(target = @Desc(value = "loadVanillaState", args = InputStream.class),
            at = @At(value = "INVOKE", target = "setWarsUnsafe"))
    private static void redirectSetWars(@SuppressWarnings("deprecation") @Coerce Galimulator.Unsafe unsafe, @NotNull Vector<Object> v) {
        // NOP
    }
}
