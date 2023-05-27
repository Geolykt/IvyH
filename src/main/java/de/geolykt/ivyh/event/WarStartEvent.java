package de.geolykt.ivyh.event;

import java.util.Arrays;

import org.jetbrains.annotations.NotNull;

import de.geolykt.starloader.api.empire.ActiveEmpire;
import de.geolykt.starloader.api.empire.Empire;
import de.geolykt.starloader.api.empire.War;

public class WarStartEvent extends WarEmpireJoinEvent {

    @NotNull
    private final ActiveEmpire attacker;
    @NotNull
    private final ActiveEmpire target;

    @SuppressWarnings("null")
    public WarStartEvent(@NotNull War war, @NotNull ActiveEmpire attacker, @NotNull ActiveEmpire target) {
        super(war, Arrays.asList(attacker, target));
        this.attacker = attacker;
        this.target = target;
    }

    @NotNull
    public Empire getAttacker() {
        return attacker;
    }

    @NotNull
    public Empire getTarget() {
        return target;
    }
}
