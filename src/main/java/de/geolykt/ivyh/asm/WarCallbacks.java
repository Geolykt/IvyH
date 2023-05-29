package de.geolykt.ivyh.asm;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import de.geolykt.ivyh.IvyH;
import de.geolykt.ivyh.IvyWar;

public class WarCallbacks {

    // Note: Called via ASM
    public static final boolean isLoosingNoWars(de.geolykt.starloader.api.empire.@NotNull ActiveEmpire e) {
        Collection<IvyWar> wars = IvyH.CONTAINER.getWars(e);
        for (IvyWar war : wars) {
            if (war.allAggressorsView.contains(e)) {
                if (war.getAgressorScore() < 0) {
                    return true;
                }
            } else if (war.allDefendersView.contains(e)) {
                if (war.getAgressorScore() > 0) {
                    return true;
                }
            } else {
                LoggerFactory.getLogger(WarCallbacks.class).warn("Empire {} ({}) does not really participate in war {}.", e.getUID(), e.getEmpireName(), war.getDisplayName());
            }
        }
        return false;
    }
}
