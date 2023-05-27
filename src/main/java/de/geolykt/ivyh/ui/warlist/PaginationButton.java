package de.geolykt.ivyh.ui.warlist;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.g2d.BitmapFont;

import de.geolykt.starloader.api.NullUtils;
import de.geolykt.starloader.api.gui.canvas.prefab.AbstractCanvasButton;

public class PaginationButton extends AbstractCanvasButton {

    @NotNull
    private final PaginationButtonType type;
    @NotNull
    private final Paginator paginator;

    public PaginationButton(@NotNull BitmapFont font, @NotNull CharSequence text, int width, int height, @NotNull PaginationButtonType type, @NotNull Paginator paginator) {
        super(font, text, width, height);
        this.type = NullUtils.requireNotNull(type, "type");
        this.paginator = NullUtils.requireNotNull(paginator, "paginator");
    }

    public enum PaginationButtonType {
        NEXT_PAGE,
        PREVIOUS_PAGE;
    }

    @Override
    public void onClick() {
        if (this.type == PaginationButtonType.NEXT_PAGE) {
            this.paginator.setPage(this.paginator.getCurrentPage() + 1);
        } else {
            this.paginator.setPage(this.paginator.getCurrentPage() - 1);
        }
    }
}
