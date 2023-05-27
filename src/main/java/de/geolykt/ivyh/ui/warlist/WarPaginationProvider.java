package de.geolykt.ivyh.ui.warlist;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import de.geolykt.ivyh.IvyH;
import de.geolykt.ivyh.IvyWar;
import de.geolykt.ivyh.ui.warlist.Paginator.PaginatedElement;

public class WarPaginationProvider implements Supplier<@NotNull List<@NotNull PaginatedElement>> {

    @Override
    public @NotNull List<@NotNull PaginatedElement> get() {
        List<@NotNull PaginatedElement> elements = new ArrayList<>();
        for (IvyWar war : IvyH.CONTAINER.getAllWars()) {
            elements.add(new PaginatedWarButton(war));
        }
        return elements;
    }
}
