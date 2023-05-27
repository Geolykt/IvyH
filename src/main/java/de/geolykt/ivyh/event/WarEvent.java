package de.geolykt.ivyh.event;

import de.geolykt.starloader.api.empire.War;
import de.geolykt.starloader.api.event.Event;

public abstract class WarEvent extends Event {

    private final War war;

    public WarEvent(War war) {
        this.war = war;
    }

    public War getWar() {
        return war;
    }
}
