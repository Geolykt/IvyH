package de.geolykt.ivyh.ui.warlist;

import org.jetbrains.annotations.NotNull;

import de.geolykt.ivyh.IvyWar;
import de.geolykt.ivyh.ui.warinspect.WarInspector;
import de.geolykt.ivyh.ui.warlist.Paginator.PaginatedElement;
import de.geolykt.starloader.api.gui.canvas.CanvasManager;
import de.geolykt.starloader.api.gui.canvas.CanvasPosition;

public class PaginatedWarButton implements PaginatedElement {

    @NotNull
    private final IvyWar war;

    public PaginatedWarButton(@NotNull IvyWar war) {
        this.war = war;
    }

    @Override
    public void execute() {
        CanvasManager.getInstance().openCanvas(new WarInspector(this.war).toCanvas(), CanvasPosition.BOTTOM_RIGHT);
    }

    @Override
    @NotNull
    public String getTitle() {
        return this.war.getDisplayName();
    }

    @Override
    @NotNull
    public String getSubtitle() {
        return this.war.formatDisplayScore(this.war.getAgressorScore()) + " " + this.war.getDisplayAge();
    }
}
