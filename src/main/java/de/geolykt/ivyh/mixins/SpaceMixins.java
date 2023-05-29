package de.geolykt.ivyh.mixins;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import de.geolykt.ivyh.IvyH;
import de.geolykt.ivyh.IvyWar;
import de.geolykt.ivyh.ui.warlist.PaginationButton;
import de.geolykt.ivyh.ui.warlist.PaginationButton.PaginationButtonType;
import de.geolykt.ivyh.ui.warlist.Paginator;
import de.geolykt.ivyh.ui.warlist.WarPaginationProvider;
import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.empire.ActiveEmpire;
import de.geolykt.starloader.api.gui.Drawing;
import de.geolykt.starloader.api.gui.canvas.Canvas;
import de.geolykt.starloader.api.gui.canvas.CanvasManager;
import de.geolykt.starloader.api.gui.canvas.CanvasSettings;
import de.geolykt.starloader.api.gui.canvas.ChildObjectOrientation;

import snoddasmannen.galimulator.Empire;
import snoddasmannen.galimulator.Space;
import snoddasmannen.galimulator.War;

@Mixin(priority = 1100, value = Space.class)
public class SpaceMixins {

    @Overwrite
    public static void onTakeStarFrom(@NotNull Empire empire0, @NotNull Empire empire1) {
        if (empire0 != Galimulator.getNeutralEmpire() && empire1 != Galimulator.getNeutralEmpire()) {
            if (empire0 != empire1) {
                IvyWar ivyWar = IvyH.CONTAINER.getWarOrInitiate((ActiveEmpire) empire0, (ActiveEmpire) empire1);
                ivyWar.incrementScore((ActiveEmpire) empire0);
            }
        }
    }

    @Overwrite
    @NotNull
    public static War getOrCreateWar(@NotNull Empire empire0, @NotNull Empire empire1) {
        throw new UnsupportedOperationException("IvyH reimplements warfare and diplomacy, rendering this method useless.");
    }

    @Overwrite
    @NotNull
    public static List<War> getParticipatingWars(@NotNull Empire empire) {
        throw new UnsupportedOperationException("IvyH reimplements warfare and diplomacy, rendering this method useless.");
    }

    @Overwrite
    public static void openActiveWarList() {
        Paginator paginator = new Paginator(700, 800, new WarPaginationProvider());
        CanvasManager cm = CanvasManager.getInstance();
        PaginationButton prevButton = new PaginationButton(Drawing.getSpaceFont(), "Previous page", 325, 50, PaginationButtonType.PREVIOUS_PAGE, paginator);
        PaginationButton nextButton = new PaginationButton(Drawing.getSpaceFont(), "Next page", 325, 50, PaginationButtonType.NEXT_PAGE, paginator);
        Canvas navigationCanvas = cm.multiCanvas(cm.dummyContext(700, 50), CanvasSettings.CHILD_TRANSPARENT, ChildObjectOrientation.LEFT_TO_RIGHT, prevButton, cm.dummyContext(50, 50), nextButton);
        Canvas paginatorCanvas = paginator.asCanvas();
        Canvas allCanvas = cm.multiCanvas(cm.dummyContext(700, 850), new CanvasSettings("Wars"), ChildObjectOrientation.BOTTOM_TO_TOP, navigationCanvas, paginatorCanvas);
        allCanvas.openCanvas();
    }
}
