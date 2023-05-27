package de.geolykt.ivyh.event;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;

import de.geolykt.starloader.api.empire.Empire;
import de.geolykt.starloader.api.empire.War;

public class WarEndEvent extends WarEmpireLeaveEvent {
    public WarEndEvent(@NotNull War war, @NotNull Collection<@NotNull Empire> leavingEmpires) {
        super(war, leavingEmpires);
    }
}
