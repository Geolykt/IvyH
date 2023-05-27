package de.geolykt.ivyh.codec;

import java.util.List;

import org.jetbrains.annotations.NotNull;

public class IvyWarContainerState {

    public static class WarState {
        public final int warStart;
        public final int lastActionYear;
        public final int aggressorScore;
        public final int initiator;
        public final int target;
        public final int @NotNull[] aggressors;
        public final int @NotNull[] defenders;

        public WarState(int warStart, int lastActionYear, int aggressorScore, int initator, int target, int @NotNull[] aggressors, int @NotNull[] defenders) {
            this.warStart = warStart;
            this.lastActionYear = lastActionYear;
            this.aggressorScore = aggressorScore;
            this.initiator = initator;
            this.target = target;
            this.aggressors = aggressors;
            this.defenders = defenders;
        }
    }

    @NotNull
    public final List<@NotNull WarState> wars;

    public IvyWarContainerState(@NotNull List<@NotNull WarState> wars) {
        this.wars = wars;
    }
}
