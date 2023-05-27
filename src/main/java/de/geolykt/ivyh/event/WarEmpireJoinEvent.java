package de.geolykt.ivyh.event;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.jetbrains.annotations.NotNull;

import de.geolykt.starloader.api.empire.ActiveEmpire;
import de.geolykt.starloader.api.empire.War;

public class WarEmpireJoinEvent extends WarEvent {

    @NotNull
    private final Collection<@NotNull ActiveEmpire> joiningEmpires;

    public WarEmpireJoinEvent(@NotNull War war, @NotNull Collection<@NotNull ActiveEmpire> joiningEmpires) {
        super(war);
        this.joiningEmpires = new HashSet<>(joiningEmpires);
    }

    @NotNull
    public Collection<@NotNull ActiveEmpire> getJoiningEmpires() {
        return Collections.unmodifiableCollection(this.joiningEmpires);
    }
}
