package de.geolykt.ivyh.event;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.jetbrains.annotations.NotNull;

import de.geolykt.starloader.api.empire.Empire;
import de.geolykt.starloader.api.empire.War;
import de.geolykt.starloader.api.event.Cancellable;

public class WarEmpireLeaveEvent extends WarEvent implements Cancellable {

    @NotNull
    private final Collection<@NotNull Empire> leavingEmpires;

    private boolean cancelled = false;

    public WarEmpireLeaveEvent(@NotNull War war, @NotNull Collection<@NotNull Empire> leavingEmpires) {
        super(war);
        this.leavingEmpires = new HashSet<>(leavingEmpires);
    }

    @NotNull
    public Collection<@NotNull Empire> getLeavingEmpires() {
        return Collections.unmodifiableCollection(this.leavingEmpires);
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
