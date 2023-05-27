package de.geolykt.ivyh.mixins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.geolykt.ivyh.IvyH;
import de.geolykt.ivyh.IvyWar;
import de.geolykt.ivyh.IvyWarAccess;
import de.geolykt.ivyh.event.WarEndEvent;
import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.NullUtils;
import de.geolykt.starloader.api.empire.Empire;
import de.geolykt.starloader.api.empire.War;
import de.geolykt.starloader.api.event.EventManager;

import snoddasmannen.galimulator.Settings;

@Mixin(priority = 1100, value = snoddasmannen.galimulator.War.class)
public abstract class WarMixins implements War, IvyWarAccess {

    @SuppressWarnings("null")
    @NotNull
    private transient IvyWar handler = NullUtils.provideNull();

    @SuppressWarnings("null")
    @Inject(at = @At("TAIL"), method = "<init>", require = 1)
    private void lateInit(snoddasmannen.galimulator.@NotNull Empire a, snoddasmannen.galimulator.@NotNull Empire b, CallbackInfo ci) {
        this.handler = new IvyWar(Galimulator.getGameYear(), (Empire) a, (Empire) b);
    }

    @Override
    @NotNull
    @Overwrite
    public Empire getEmpireA() {
        return this.handler.getInitiator();
    }

    @Override
    @NotNull
    @Overwrite
    public Empire getEmpireB() {
        return this.handler.getTarget();
    }

    @Override
    @NotNull
    @Overwrite
    public Collection<@NotNull Empire> getAggressorParty() {
        return this.handler.allAggressorsView;
    }

    @Override
    @NotNull
    @Overwrite
    public Collection<@NotNull Empire> getDefenderParty() {
        return this.handler.allDefendersView;
    }

    @Overwrite
    public void incrementScore(snoddasmannen.galimulator.@NotNull Empire empire) {
        if (this.handler.isUnloaded()) {
            throw new IllegalStateException("IvyWar instance invalidated and may not be mutated further");
        }
        this.handler.setLastActionYear(Galimulator.getGameYear());
        if (this.getAggressorParty().contains((Empire) empire)) {
            this.handler.incrementAggressorScore();
        } else {
            this.handler.decrementAggressorScore();
        }
    }

    @Overwrite
    public int getScore(snoddasmannen.galimulator.Empire empire) {
        // Vanilla galim has the conditions inverted for whatever reason, but this should be the right implementation of the method.
        if (this.getAggressorParty().contains((Empire) empire)) {
            return this.handler.getAgressorScore();
        } else if (this.getDefenderParty().contains((Empire) empire)) {
            return -this.handler.getAgressorScore();
        } else {
            LoggerFactory.getLogger(IvyH.class).warn("Score fetched for empire not in the war, that's weird huh");
            return 0;
        }
    }

    @Overwrite
    public boolean isActive() {
        if (this.handler.isUnloaded()) {
            return false;
        }
        boolean activeAggressor = false;
        for (Empire e : this.getAggressorParty()) {
            if (!e.hasCollapsed()) {
                activeAggressor = true;
                break;
            }
        }
        if (!activeAggressor) {
            IvyH.CONTAINER.unloadWar(this.handler);
            this.handler.unload();
            return false;
        }
        for (Empire e : this.getDefenderParty()) {
            if (!e.hasCollapsed()) {
                return true;
            }
        }
        IvyH.CONTAINER.unloadWar(this.handler);
        this.handler.unload();
        return false;
    }

    @Overwrite
    public void tick() {
        // TODO Use @Redirect instead
        if (Galimulator.getGameYear() - 2000 > this.handler.getLastActionYear()
                && ((Boolean) Settings.EnumSettings.ALLOW_PEACE.getValue()).booleanValue()) {
            Collection<@NotNull Empire> participants = new ArrayList<>();
            participants.addAll(this.handler.allAggressorsView);
            participants.addAll(this.handler.allDefendersView);
            WarEndEvent evt = new WarEndEvent(this, participants);
            EventManager.handleEvent(evt);
            if (evt.isCancelled()) {
                this.handler.setLastActionYear(Galimulator.getGameYear());
                return;
            }
            IvyH.CONTAINER.disbandWar(this.handler);
        }
    }

    @Override
    @NotNull
    public IvyWar getWarHandler() {
        return this.handler;
    }

    @Overwrite
    public String getDisplayScore() {
        return this.handler.formatDisplayScore(this.handler.getAgressorScore());
    }

    @Overwrite
    public String getWarName() {
        return this.handler.getDisplayName();
    }

    @Overwrite
    public ArrayList<?> getItems() {
        throw new UnsupportedOperationException("Due to IvyH, this method is not sensical.");
    }

    @Override
    public void setWarHandler(@NotNull IvyWar handler) {
        if (Objects.isNull(this.handler)) {
            this.handler = Objects.requireNonNull(handler);
        } else {
            throw new UnsupportedOperationException("This method may only be called if the handler has not been set up through the standard constructor.");
        }
    }
}
