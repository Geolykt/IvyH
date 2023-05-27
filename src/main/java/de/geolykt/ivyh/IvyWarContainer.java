package de.geolykt.ivyh;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import de.geolykt.ivyh.asm.WarCallbacks;
import de.geolykt.ivyh.codec.IvyWarContainerState;
import de.geolykt.ivyh.codec.IvyWarContainerState.WarState;
import de.geolykt.ivyh.event.WarEmpireLeaveEvent;
import de.geolykt.ivyh.event.WarEndEvent;
import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.empire.ActiveEmpire;
import de.geolykt.starloader.api.empire.Empire;
import de.geolykt.starloader.api.empire.War;
import de.geolykt.starloader.api.event.EventManager;

import snoddasmannen.galimulator.Space;

public class IvyWarContainer {

    private final Map<Integer, Set<@NotNull IvyWar>> wars = new ConcurrentHashMap<>();
    private final Map<IvyWar, War> reverseLookup = new ConcurrentHashMap<>();
    private final NavigableSet<@NotNull IvyWar> allWars = new ConcurrentSkipListSet<>((w1, w2) -> {
        return Integer.compareUnsigned(w1.getStartYear(), w2.getStartYear());
    });

    @Nullable
    public IvyWar get(@NotNull Empire a, @NotNull Empire b) {
        Set<IvyWar> aWars = this.wars.get(a.getUID());
        if (aWars == null) {
            return null;
        }
        for (IvyWar war : aWars) {
            if (war.allAggressorsView.contains(b) || war.allDefendersView.contains(b)) {
                return war;
            }
        }
        return null;
    }

    @NotNull
    public synchronized IvyWar getWarOrInitiate(@NotNull ActiveEmpire aggressor, @NotNull ActiveEmpire defender) {
        IvyWar alreadyExisting = this.get(aggressor, defender);
        if (alreadyExisting != null) {
            return alreadyExisting;
        }
        alreadyExisting = this.tryMergeWar(aggressor, defender);
        if (alreadyExisting != null) {
            return alreadyExisting;
        }
        return this.createWar(aggressor, defender);
    }

    @NotNull
    public IvyWar createWar(@NotNull ActiveEmpire aggressor, @NotNull ActiveEmpire defender) {
        Set<@NotNull IvyWar> agressorWars = this.wars.computeIfAbsent(aggressor.getUID(), (ignore) -> ConcurrentHashMap.newKeySet());
        Set<@NotNull IvyWar> defenderWars = this.wars.computeIfAbsent(defender.getUID(), (ignore) -> ConcurrentHashMap.newKeySet());
        snoddasmannen.galimulator.Empire a = (snoddasmannen.galimulator.Empire) aggressor;
        snoddasmannen.galimulator.Empire b = (snoddasmannen.galimulator.Empire) defender;
        snoddasmannen.galimulator.War war = new snoddasmannen.galimulator.War(a, b);
        IvyWar ivyWar = ((IvyWarAccess) war).getWarHandler();
        agressorWars.add(ivyWar);
        defenderWars.add(ivyWar);
        this.reverseLookup.put(ivyWar, (War) war);
        this.addWarUnsafely((War) war);
        this.allWars.add(ivyWar);
        return ivyWar;
    }

    @SuppressWarnings("deprecation")
    private void addWarUnsafely(@NotNull War war) {
        Galimulator.getImplementation().getUnsafe().getWarsUnsafe().add(war);
    }

    @SuppressWarnings("deprecation")
    private void clearWarsUnsafely() {
        Galimulator.getImplementation().getUnsafe().getWarsUnsafe().clear();
    }

    @Nullable
    public IvyWar tryMergeWar(@NotNull ActiveEmpire a, @NotNull ActiveEmpire b) {
        IvyWar mergeable = getMergeableWar(a, b);
        if (mergeable == null) {
            return null;
        }
        if (this.couldJoinSide(mergeable.allAggressorsView, a)) {
            mergeable.addAggressor(a);
            mergeable.addDefender(b);
        } else {
            mergeable.addAggressor(b);
            mergeable.addDefender(a);
        }
        return mergeable;
    }

    @Nullable
    public IvyWar getMergeableWar(@NotNull ActiveEmpire a, @NotNull ActiveEmpire b) {
        if (a.getAlliance() == null && b.getAlliance() == null) {
            return null;
        }
        for (IvyWar war : this.reverseLookup.keySet()) {
            if (this.couldJoinSide(war.allAggressorsView, a)) {
                if (this.couldJoinSide(war.allDefendersView, b)) {
                    return war;
                }
            } else if (this.couldJoinSide(war.allAggressorsView, b)) {
                if (this.couldJoinSide(war.allDefendersView, a)) {
                    return war;
                }
            }
        }

        return null;
    }

    private boolean couldJoinSide(@NotNull Collection<Empire> participantsView, @NotNull ActiveEmpire empire) {
        for (Empire participant : participantsView) {
            if (!(participant instanceof ActiveEmpire)) {
                continue;
            }
            if (participant == empire || ((ActiveEmpire) participant).getAlliance() == empire.getAlliance()) {
                return true;
            }
        }
        return false;
    }

    public void leaveAllWars(@NotNull Empire empire, boolean silent) {
        Set<@NotNull IvyWar> wars = this.wars.get(empire.getUID());
        if (wars != null) {
            for (IvyWar war : wars) {
                this.leaveWar(empire, war, silent);
            }
        }
    }

    private boolean handEmpireLeaveEvent(@NotNull Empire empire, @NotNull IvyWar war, boolean endWar) {
        War slWar = this.reverseLookup.get(war);
        if (slWar == null) {
            throw new IllegalStateException("No SLAPI `War` instance registered for IvyH `IvyWar` instance " + war);
        }
        WarEmpireLeaveEvent evt;
        if (endWar) {
            evt = new WarEndEvent(slWar, Collections.singleton(empire));
        } else {
            evt = new WarEmpireLeaveEvent(slWar, Collections.singleton(empire));
        }
        EventManager.handleEvent(evt);
        return evt.isCancelled();
    }

    public boolean leaveWar(@NotNull Empire empire, @NotNull IvyWar war, boolean silent) {
        if (war.allAggressorsView.contains(empire)) {
            if (war.allAggressorsView.size() == 1) {
                if (!silent && this.handEmpireLeaveEvent(empire, war, true)) {
                    return false;
                }
                this.reverseLookup.remove(war);
                for (Empire defender : war.allDefendersView) {
                    this.removeFromWar(defender, war);
                }
                this.removeFromWar(empire, war);
                war.leave(empire);
                return true;
            }
            if (!silent && this.handEmpireLeaveEvent(empire, war, false)) {
                return false;
            }
            if (war.getInitiator() == empire) {
                for (Empire attacker : war.allAggressorsView) {
                    if (attacker != empire) {
                        war.setInitiator(attacker);
                        break;
                    }
                }
            }
            this.removeFromWar(empire, war);
            war.leave(empire);
            return true;
        } else if (war.allDefendersView.contains(empire)) {
            if (war.allDefendersView.size() == 1) {
                if (!silent && this.handEmpireLeaveEvent(empire, war, true)) {
                    return false;
                }
                this.reverseLookup.remove(war);
                for (Empire aggressor : war.allAggressorsView) {
                    this.removeFromWar(aggressor, war);
                }
                this.removeFromWar(empire, war);
                war.leave(empire);
                return true;
            }
            if (!silent && this.handEmpireLeaveEvent(empire, war, false)) {
                return false;
            }
            if (war.getTarget() == empire) {
                for (Empire defender : war.allDefendersView) {
                    if (defender != empire) {
                        war.setTarget(defender);
                        break;
                    }
                }
            }
            this.removeFromWar(empire, war);
            war.leave(empire);
            return true;
        } else {
            throw new IllegalStateException("Empire not involved in war!");
        }
    }

    private void removeFromWar(@NotNull Empire empire, @NotNull IvyWar war) {
        if (Objects.requireNonNull(this.wars.get(empire.getUID())).remove(war)) {
            throw new IllegalStateException("Empire did not participate in this war!");
        }
    }

    @SuppressWarnings("null")
    @NotNull
    public Collection<@NotNull IvyWar> getWars(@NotNull ActiveEmpire targetEmpire) {
        Set<IvyWar> wars = this.wars.get(targetEmpire.getUID());
        if (wars == null) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableCollection(wars);
        }
    }

    @NotNull
    public War toSLAPIWar(@NotNull IvyWar war) {
        War slWar = this.reverseLookup.get(war);
        if (slWar == null) {
            throw new IllegalStateException("No SLAPI `War` instance registered for IvyH `IvyWar` instance " + war);
        }
        return slWar;
    }

    public synchronized void unloadWar(@NotNull IvyWar war) {
        this.reverseLookup.remove(war);
        this.allWars.remove(war);
        for (Empire e : war.allAggressorsView) {
            Set<IvyWar> wars = this.wars.get(e.getUID());
            if (wars != null) {
                wars.remove(war);
            }
        }
        for (Empire e : war.allDefendersView) {
            Set<IvyWar> wars = this.wars.get(e.getUID());
            if (wars != null) {
                wars.remove(war);
            }
        }
        war.unload();
    }

    public void disbandWar(@NotNull IvyWar war) {
        for (Empire a : war.allAggressorsView) {
            if (!(a instanceof snoddasmannen.galimulator.Empire)) {
                continue;
            }
            for (Empire d : war.allDefendersView) {
                if (!(d instanceof snoddasmannen.galimulator.Empire)) {
                    continue;
                }
                Space.signPeace((snoddasmannen.galimulator.Empire) a, (snoddasmannen.galimulator.Empire) d);
            }
        }
        this.unloadWar(war);
    }

    @SuppressWarnings("null")
    @NotNull
    public NavigableSet<@NotNull IvyWar> getAllWars() {
        return Collections.unmodifiableNavigableSet(this.allWars);
    }

    public synchronized void apply(@NotNull IvyWarContainerState state) {
        this.allWars.clear();
        this.reverseLookup.clear();
        this.wars.clear();
        this.clearWarsUnsafely();
        for (WarState war : state.wars) {
            snoddasmannen.galimulator.War galimWar = WarCallbacks.createInstance();
            if (galimWar == null) {
                LoggerFactory.getLogger(IvyWarContainer.class).error("Unable to create `War` instance. Dropping war.");
                continue;
            }
            Empire initiator = Galimulator.getEmpireByUID(war.initiator);
            Empire target = Galimulator.getEmpireByUID(war.target);
            if (initiator == null) {
                LoggerFactory.getLogger(IvyWarContainer.class).error("Unknown empire UID {}. Dropping war as there is no initiator.", war.initiator);
                continue;
            }
            if (target == null) {
                LoggerFactory.getLogger(IvyWarContainer.class).error("Unknown empire UID {}. Dropping war as there is no target.", war.target);
                continue;
            }
            IvyWar ivyWar = new IvyWar(war.warStart, initiator, target);
            ivyWar.setAggressorScore(war.aggressorScore);
            ivyWar.setLastActionYear(war.lastActionYear);
            ((IvyWarAccess) galimWar).setWarHandler(ivyWar);
            this.addWarUnsafely((War) galimWar);
            this.allWars.add(ivyWar);
            this.reverseLookup.put(ivyWar, (War) galimWar);
            for (int aggressor : war.aggressors) {
                Empire e = Galimulator.getEmpireByUID(aggressor);
                if (e != null) {
                    ivyWar.addAggressor(e);
                    this.wars.computeIfAbsent(aggressor, (ignore) -> ConcurrentHashMap.newKeySet()).add(ivyWar);
                } else {
                    LoggerFactory.getLogger(IvyWarContainer.class).error("Unknown empire UID {}. Dropping aggressor.", aggressor);
                }
            }
            for (int defender : war.defenders) {
                Empire e = Galimulator.getEmpireByUID(defender);
                if (e != null) {
                    ivyWar.addDefender(e);
                    this.wars.computeIfAbsent(defender, (ignore) -> ConcurrentHashMap.newKeySet()).add(ivyWar);
                } else {
                    LoggerFactory.getLogger(IvyWarContainer.class).error("Unknown empire UID {}. Dropping defender.", defender);
                }
            }
        }
    }

    @NotNull
    public synchronized IvyWarContainerState store() {
        List<@NotNull WarState> wars = new ArrayList<>();
        for (IvyWar war : this.allWars) {
            if (war.isUnloaded()) {
                // Whatever happened here
                continue;
            }
            int[] aggressors = new int[war.allAggressorsView.size()];
            int[] defenders = new int[war.allDefendersView.size()];
            Iterator<Empire> it = war.allAggressorsView.iterator();
            for (int i = 0; it.hasNext(); i++) {
                aggressors[i] = it.next().getUID();
            }
            it = war.allDefendersView.iterator();
            for (int i = 0; it.hasNext(); i++) {
                defenders[i] = it.next().getUID();
            }
            wars.add(new WarState(war.getStartYear(), war.getLastActionYear(),
                    war.getAgressorScore(), war.getInitiator().getUID(), war.getTarget().getUID(),
                    aggressors, defenders));
        }
        return new IvyWarContainerState(wars);
    }
}
