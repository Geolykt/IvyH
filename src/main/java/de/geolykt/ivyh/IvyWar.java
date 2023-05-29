package de.geolykt.ivyh;

import java.util.Collection;
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
import de.geolykt.starloader.api.empire.War;

import snoddasmannen.galimulator.Space;

public class IvyWar implements War {

    private final int startYear;
    private int lastActionYear;
    private int aggressorScore;

    @NotNull
    private ActiveEmpire initiator;
    @NotNull
    private ActiveEmpire target;

    @NotNull
    private final Set<@NotNull ActiveEmpire> allAggressors = ConcurrentHashMap.newKeySet();
    @NotNull
    public final Set<@NotNull ActiveEmpire> allAggressorsView = Collections.unmodifiableSet(this.allAggressors);
    @NotNull
    private final Set<@NotNull ActiveEmpire> allDefenders = ConcurrentHashMap.newKeySet();
    @NotNull
    public final Set<@NotNull ActiveEmpire> allDefendersView = Collections.unmodifiableSet(this.allDefenders);
    private boolean unloaded;

    public IvyWar(int startYear, @NotNull ActiveEmpire initiator, @NotNull ActiveEmpire target) {
        this.startYear = this.lastActionYear = startYear;
        this.initiator = Objects.requireNonNull(initiator);
        this.target = Objects.requireNonNull(target);
        this.allAggressors.add(initiator);
        this.allDefenders.add(target);
    }

    public void addAggressor(@NotNull ActiveEmpire e) {
        if (this.unloaded) {
            throw new IllegalStateException("IvyWar instance invalidated and may not be mutated further");
        }
        this.allAggressors.add(e);
    }

    public void addDefender(@NotNull ActiveEmpire e) {
        if (this.unloaded) {
            throw new IllegalStateException("IvyWar instance invalidated and may not be mutated further");
        }
        this.allDefenders.add(e);
    }

    public void leave(@NotNull ActiveEmpire e) {
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

    public void setInitiator(@NotNull ActiveEmpire initiator) {
        if (this.unloaded) {
            throw new IllegalStateException("IvyWar instance invalidated and may not be mutated further");
        }
        this.initiator = Objects.requireNonNull(initiator, "Initiator may not be null!");
    }

    public void setTarget(@NotNull ActiveEmpire target) {
        if (this.unloaded) {
            throw new IllegalStateException("IvyWar instance invalidated and may not be mutated further");
        }
        this.target = Objects.requireNonNull(target, "Target may not be null");
    }

    @NotNull
    public ActiveEmpire getInitiator() {
        return this.initiator;
    }

    @NotNull
    public ActiveEmpire getTarget() {
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

    public void incrementScore(@NotNull ActiveEmpire e) {
        if (this.allAggressors.contains(e)) {
            this.incrementAggressorScore();
        } else if (this.allDefenders.contains(e)) {
            this.decrementAggressorScore();
        } else {
            throw new IllegalStateException("Empire " + e.getUID() + "(" + e.getEmpireName() + ") does not participate in war " + this.getDisplayName());
        }
    }

    @Override
    @NotNull
    public Collection<@NotNull ActiveEmpire> getAggressorParty() {
        return this.allAggressorsView;
    }

    @Override
    public int getDateOfLastAction() {
        return this.lastActionYear;
    }

    @Override
    @NotNull
    public Collection<@NotNull ActiveEmpire> getDefenderParty() {
        return this.allDefendersView;
    }

    @Override
    public int getDestroyedShips() {
        return 0; // Not implemented
    }

    @Override
    public int getStarDelta() {
        return this.aggressorScore;
    }

    @Override
    public int getStartDate() {
        return this.startYear;
    }

    @Override
    public void noteShipDestruction() {
        // NOP (Not implemented)
    }

    @Override
    public void noteStarChange(@NotNull ActiveEmpire empire) throws IllegalArgumentException {
        this.incrementScore(empire);
    }

    @Override
    public void setDestroyedShips(int count) {
        // NOP (Not implemented)
    }

    @Override
    public void setStarDelta(int count) {
        this.aggressorScore = count;
    }
}
