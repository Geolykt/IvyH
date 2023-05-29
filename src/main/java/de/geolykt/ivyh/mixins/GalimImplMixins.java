package de.geolykt.ivyh.mixins;

import java.util.Vector;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Redirect;

import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.empire.War;
import de.geolykt.starloader.impl.GalimulatorImplementation;

import snoddasmannen.galimulator.SpaceState;

@Mixin(GalimulatorImplementation.class)
public class GalimImplMixins {
    @Overwrite
    public void setWarsUnsafe(Vector<War> wars) {
        throw new UnsupportedOperationException("IvyH reimplements warfare and diplomacy, rendering this method useless.");
    }

    @Overwrite
    public Vector<War> getWarsUnsafe() {
        throw new UnsupportedOperationException("IvyH reimplements warfare and diplomacy, rendering this method useless.");
    }

    @Redirect(target = @Desc(value = "createState", ret = SpaceState.class),
            at = @At(value = "INVOKE", target = "getWarsUnsafe"))
    private Vector<?> redirectGetWars(@SuppressWarnings("deprecation") @Coerce Galimulator.Unsafe unsafe) {
        return null;
    }
}
