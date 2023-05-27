package de.geolykt.ivyh.asm;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.jetbrains.annotations.Nullable;

import de.geolykt.starloader.impl.GalimulatorImplementation;

public class WarCallbacks {

    @Nullable
    private static MethodHandle warCtorHandle;
    private static boolean errorWarCtor = false;

    @Nullable
    public static final snoddasmannen.galimulator.War createInstance() {
        if (WarCallbacks.errorWarCtor) {
            return null;
        }
        MethodHandle mh = WarCallbacks.warCtorHandle;
        if (mh == null) {
            try {
                mh = MethodHandles.publicLookup().findConstructor(snoddasmannen.galimulator.War.class, MethodType.methodType(void.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                WarCallbacks.errorWarCtor = true;
                GalimulatorImplementation.crash(e, "Unable to bypass the standard snoddasmannen/galimulator/War constructor. This indicates a mod incompatibility. Issue occured while obtaining the method handle.", false);
                return null;
            }
            warCtorHandle = mh;
        }
        try {
            return (snoddasmannen.galimulator.War) mh.invokeExact();
        } catch (Throwable e) {
            GalimulatorImplementation.crash(e, "Unable to bypass the standard snoddasmannen/galimulator/War constructor. This indicates a mod incompatibility. Issue occured while invoking the method handle.", false);
            return null;
        }
    }
}
