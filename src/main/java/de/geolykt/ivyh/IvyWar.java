package de.geolykt.ivyh;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.Color;

import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.empire.ActiveEmpire;
import de.geolykt.starloader.api.empire.Alliance;
import de.geolykt.starloader.api.empire.Empire;

import snoddasmannen.galimulator.Space;

public class IvyWar {

    private final int startYear;
    private int lastActionYear;
    private int aggressorScore;

    @NotNull
    private Empire initiator;
    @NotNull
    private Empire target;

    @NotNull
    private final Set<@NotNull Empire> allAggressors = ConcurrentHashMap.newKeySet();
    @NotNull
    public final Set<@NotNull Empire> allAggressorsView = Collections.unmodifiableSet(this.allAggressors);
    @NotNull
    private final Set<@NotNull Empire> allDefenders = ConcurrentHashMap.newKeySet();
    @NotNull
    public final Set<@NotNull Empire> allDefendersView = Collections.unmodifiableSet(this.allDefenders);
    private boolean unloaded;

    public IvyWar(int startYear, @NotNull Empire initiator, @NotNull Empire target) {
        this.startYear = this.lastActionYear = startYear;
        this.initiator = Objects.requireNonNull(initiator);
        this.target = Objects.requireNonNull(target);
        this.allAggressors.add(initiator);
        this.allDefenders.add(target);
    }

    public void addAggressor(@NotNull Empire e) {
        if (this.unloaded) {
            throw new IllegalStateException("IvyWar instance invalidated and may not be mutated further");
        }
        this.allAggressors.add(e);
    }

    public void addDefender(@NotNull Empire e) {
        if (this.unloaded) {
            throw new IllegalStateException("IvyWar instance invalidated and may not be mutated further");
        }
        this.allDefenders.add(e);
    }

    public void leave(@NotNull Empire e) {
        if (this.unloaded) {
            throw new IllegalStateException("IvyWar instance invalidated and may not be mutated further");
        }
        if (this.initiator == e || this.target == e) {
            throw new UnsupportedOperationException("Initiating empires cannot leave a war (API limitation).");
        }
        if (!this.allAggressors.remove(e) && !this.allDefenders.remove(e)) {
            throw new IllegalStateException("Empire not part of war.");
        }
    }

    public void setInitiator(@NotNull Empire initiator) {
        if (this.unloaded) {
            throw new IllegalStateException("IvyWar instance invalidated and may not be mutated further");
        }
        this.initiator = Objects.requireNonNull(initiator, "Initiator may not be null!");
    }

    public void setTarget(@NotNull Empire target) {
        if (this.unloaded) {
            throw new IllegalStateException("IvyWar instance invalidated and may not be mutated further");
        }
        this.target = Objects.requireNonNull(target, "Target may not be null");
    }

    @NotNull
    public Empire getInitiator() {
        return this.initiator;
    }

    @NotNull
    public Empire getTarget() {
        return this.target;
    }

    public boolean isUnloaded() {
        return this.unloaded;
    }

    public void unload() {
        this.unloaded = true;
    }

    @NotNull
    public String getDisplayName() {
        StringBuilder builder = new StringBuilder();
        Empire initiator = this.initiator;
        if (this.allAggressors.size() != 1 && initiator instanceof ActiveEmpire) {
            Alliance alliance = ((ActiveEmpire) initiator).getAlliance();
            if (alliance == null) {
                IvyUtil.appendColorString(builder, initiator.getEmpireName(), initiator.getGDXColor().toIntBits());
                builder.append(" and others");
            } else {
                IvyUtil.appendColorString(builder, alliance.getAbbreviation(), alliance.getGDXColor().toIntBits());
            }
        } else {
            IvyUtil.appendColorString(builder, initiator.getEmpireName(), initiator.getGDXColor().toIntBits());
        }
        IvyUtil.appendColorString(builder, " vs ", Color.GRAY.toIntBits());
        Empire target = this.target;
        if (this.allDefenders.size() != 1 && target instanceof ActiveEmpire) {
            Alliance alliance = ((ActiveEmpire) target).getAlliance();
            if (alliance == null) {
                IvyUtil.appendColorString(builder, target.getEmpireName(), target.getGDXColor().toIntBits());
                builder.append(" and others");
            } else {
                IvyUtil.appendColorString(builder, alliance.getAbbreviation(), alliance.getGDXColor().toIntBits());
            }
        } else {
            IvyUtil.appendColorString(builder, target.getEmpireName(), target.getGDXColor().toIntBits());
        }
        return builder.toString();
    }

    @NotNull
    public String formatDisplayScore(int score) {
        StringBuilder builder = new StringBuilder();
        builder.append("[GRAY]Score:[] +(");
        if (score > 0) {
            Empire initiator = this.initiator;
            if (this.allAggressors.size() != 1 && initiator instanceof ActiveEmpire) {
                Alliance alliance = ((ActiveEmpire) initiator).getAlliance();
                if (alliance == null) {
                    IvyUtil.appendColorString(builder, Integer.toString(score), initiator.getGDXColor().toIntBits());
                } else {
                    IvyUtil.appendColorString(builder, Integer.toString(score), alliance.getGDXColor().toIntBits());
                }
            } else {
                IvyUtil.appendColorString(builder, Integer.toString(score), initiator.getGDXColor().toIntBits());
            }
        } else {
            Empire target = this.target;
            if (this.allDefenders.size() != 1 && target instanceof ActiveEmpire) {
                Alliance alliance = ((ActiveEmpire) target).getAlliance();
                if (alliance == null) {
                    IvyUtil.appendColorString(builder, Integer.toString(Math.abs(score)), target.getGDXColor().toIntBits());
                } else {
                    IvyUtil.appendColorString(builder, Integer.toString(Math.abs(score)), alliance.getGDXColor().toIntBits());
                }
            } else {
                IvyUtil.appendColorString(builder, Integer.toString(Math.abs(score)), target.getGDXColor().toIntBits());
            }
        }
        return builder.appendCodePoint(')').toString();
    }

    public int getStartYear() {
        return this.startYear;
    }

    public int getLastActionYear() {
        return lastActionYear;
    }

    public void setLastActionYear(int lastActionYear) {
        this.lastActionYear = lastActionYear;
    }

    @NotNull
    public String getDisplayAge() {
        if (this.isUnloaded()) {
            return "[GRAY]Age:[] " + (this.lastActionYear - this.startYear) / 1000 + " " + Space.getMapData().getTimeNoun();
        } else {
            return "[GRAY]Age:[] " + (Galimulator.getGameYear() - this.startYear) / 1000 + " " + Space.getMapData().getTimeNoun();
        }
    }

    public int getAgressorScore() {
        return this.aggressorScore;
    }

    public void incrementAggressorScore() {
        this.aggressorScore++;
    }

    public void decrementAggressorScore() {
        this.aggressorScore--;
    }

    public void setAggressorScore(int aggressorScore) {
        this.aggressorScore = aggressorScore;
    }
}
