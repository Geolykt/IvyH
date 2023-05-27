package de.geolykt.ivyh;

import java.util.Collections;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import net.minestom.server.extras.selfmodification.MinestomRootClassLoader;

import de.geolykt.ivyh.asm.WarASMTransformer;
import de.geolykt.ivyh.codec.IvyWarContainerState;
import de.geolykt.ivyh.codec.WarCodec;
import de.geolykt.starloader.api.NamespacedKey;
import de.geolykt.starloader.api.event.EventHandler;
import de.geolykt.starloader.api.event.EventManager;
import de.geolykt.starloader.api.event.EventPriority;
import de.geolykt.starloader.api.event.Listener;
import de.geolykt.starloader.api.event.empire.EmpireCollapseEvent;
import de.geolykt.starloader.api.event.lifecycle.GalaxyLoadingEndEvent;
import de.geolykt.starloader.api.event.lifecycle.GalaxySavingEvent;
import de.geolykt.starloader.api.registry.Registry;
import de.geolykt.starloader.impl.GalimulatorImplementation;
import de.geolykt.starloader.mod.Extension;

public class IvyH extends Extension {

    @NotNull
    public static final IvyWarContainer CONTAINER = new IvyWarContainer();
    @NotNull
    private final NamespacedKey ivyHWars = new NamespacedKey(this, "wars");

    @Override
    public void initialize() {
        EventManager.registerListener(new Listener() {
            @EventHandler(EventPriority.MONITOR)
            public void onEmpireDestroyEvent(@NotNull EmpireCollapseEvent evt) {
                if (evt.isCancelled()) {
                    return;
                }
                CONTAINER.leaveAllWars(evt.getTargetEmpire(), false);
                if (!CONTAINER.getWars(evt.getTargetEmpire()).isEmpty()) {
                    GalimulatorImplementation.crash(new IllegalStateException("Unable to remove dead empire from all of it's wars"), "Unable to remove dead empire from wars. This suggests an issue with a mod improperly cancelling WarEmpireLeave events.", true);
                }
            }

            @EventHandler
            public void onSave(@NotNull GalaxySavingEvent evt) {
                evt.getMetadataCollector().put(IvyH.this.ivyHWars, IvyH.CONTAINER.store());
            }

            @EventHandler
            public void onLoad(@NotNull GalaxyLoadingEndEvent evt) {
                Optional<@NotNull IvyWarContainerState> state = evt.getState().getDeserializedForm(IvyH.this.ivyHWars);
                if (state.isPresent()) {
                    IvyH.CONTAINER.apply(state.get());
                } else {
                    IvyH.this.getLogger().warn("Unable to load wars from savegame. Defaulting to no wars.");
                    IvyH.CONTAINER.apply(new IvyWarContainerState(Collections.emptyList()));
                }
            }
        });
    }

    @Override
    public void preInitialize() {
        NamespacedKey key = new NamespacedKey(this, "war_container_state");
        Registry.CODECS.register(key, new WarCodec(key), IvyWarContainerState.class);
    }

    static {
        MinestomRootClassLoader.getInstance().addTransformer(new WarASMTransformer());
    }
}
