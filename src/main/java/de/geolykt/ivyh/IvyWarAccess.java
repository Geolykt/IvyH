package de.geolykt.ivyh;

import org.jetbrains.annotations.NotNull;

public interface IvyWarAccess {
    @NotNull
    public IvyWar getWarHandler();
    public void setWarHandler(@NotNull IvyWar handler);
}
