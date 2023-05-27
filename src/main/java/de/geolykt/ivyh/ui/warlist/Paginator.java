package de.geolykt.ivyh.ui.warlist;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;

import de.geolykt.starloader.api.NullUtils;
import de.geolykt.starloader.api.gui.AsyncRenderer;
import de.geolykt.starloader.api.gui.Drawing;
import de.geolykt.starloader.api.gui.canvas.Canvas;
import de.geolykt.starloader.api.gui.canvas.CanvasContext;
import de.geolykt.starloader.api.gui.canvas.CanvasManager;
import de.geolykt.starloader.api.gui.canvas.CanvasSettings;
import de.geolykt.starloader.api.gui.canvas.ChildObjectOrientation;

public class Paginator {

    public interface PaginatedElement {
        @NotNull
        public String getTitle();
        @NotNull
        public String getSubtitle();
        public void execute();
    }

    @NotNull
    private List<PaginatedElement> displayElements;
    @NotNull
    private final Supplier<@NotNull List<PaginatedElement>> elementSupplier;
    private final int width;
    private final int height;
    @NotNull
    private final List<@NotNull Button> buttons = new ArrayList<>();
    private int page;

    public Paginator(int width, int height, @NotNull Supplier<@NotNull List<PaginatedElement>> supplier) {
        this.width = width;
        this.height = height;
        this.elementSupplier = supplier;
        this.displayElements = supplier.get();
        int buttonCount = (height - 10) / 60;
        for (int i = 0; i < buttonCount; i++) {
            this.buttons.add(new Button(i, width - 10, 60));
        }
    }

    private class Button implements CanvasContext {
        private final int pageIndex;
        private final int height;
        private final int width;

        public Button(int pageIndex, int width, int height) {
            this.pageIndex = pageIndex;
            this.width = width;
            this.height = height;
        }

        @Override
        public void render(@NotNull SpriteBatch surface, @NotNull Camera camera) {
            int idx = this.pageIndex + Paginator.this.buttons.size() * Paginator.this.page;
            if (Paginator.this.displayElements.size() <= idx) {
                return;
            }
            PaginatedElement element = Paginator.this.displayElements.get(idx);
            if (element == null) {
                return;
            }
            NinePatch texture = Drawing.getTextureProvider().getBoxButtonNinePatch();
            AsyncRenderer.drawNinepatch(texture, 0, 0, this.width, this.height, NullUtils.requireNotNull(Color.WHITE), camera);
            AsyncRenderer.drawText(0, this.height / 3 * 2 + 10, this.width, element.getTitle(), NullUtils.requireNotNull(Color.WHITE), camera, Align.center);
            AsyncRenderer.drawText(0, this.height / 3 + 10, this.width, element.getSubtitle(), NullUtils.requireNotNull(Color.WHITE), camera, Align.center);
        }

        @Override
        public int getHeight() {
            return this.height;
        }

        @Override
        public int getWidth() {
            return this.width;
        }

        @Override
        public void onClick(int canvasX, int canvasY, @NotNull Camera camera,
                @NotNull Canvas canvas) {
            int idx = this.pageIndex + Paginator.this.buttons.size() * Paginator.this.page;
            if (Paginator.this.displayElements.size() <= idx) {
                return;
            }
            PaginatedElement element = Paginator.this.displayElements.get(idx);
            if (element == null) {
                return;
            }
            canvas.displaySelectionEffect();
            //canvas.closeCanvas();
            element.execute();
        }
    }

    private class PageContext implements CanvasContext {
        @Override
        public int getHeight() {
            return Paginator.this.height;
        }

        @Override
        public int getWidth() {
            return Paginator.this.width;
        }

        @Override
        public void render(@NotNull SpriteBatch surface, @NotNull Camera camera) {
            Paginator.this.displayElements = Paginator.this.elementSupplier.get();
        }
    }

    @NotNull
    public Canvas asCanvas() {
        CanvasManager cm = CanvasManager.getInstance();
        @NotNull CanvasContext[] centerColumnContexts = new @NotNull CanvasContext [this.buttons.size() + 2];
        centerColumnContexts[0] = cm.dummyContext(this.width - 10, 5);
        for (int i = this.buttons.size(); i > 0; i--) {
            centerColumnContexts[i] = this.buttons.get(i - 1);
        }
        centerColumnContexts[centerColumnContexts.length - 1] = cm.dummyContext(this.width - 10, 5);
        Canvas centerColumn = cm.multiCanvas(cm.dummyContext(this.width - 10, this.height), CanvasSettings.CHILD_TRANSPARENT, ChildObjectOrientation.BOTTOM_TO_TOP, centerColumnContexts);
        Canvas leftMargin = cm.newCanvas(cm.dummyContext(5, this.height), CanvasSettings.CHILD_TRANSPARENT);
        Canvas rightMargin = cm.newCanvas(cm.dummyContext(5, this.height), CanvasSettings.CHILD_TRANSPARENT);
        return cm.multiCanvas(new PageContext(), new CanvasSettings(NullUtils.requireNotNull(Color.DARK_GRAY)), ChildObjectOrientation.LEFT_TO_RIGHT, leftMargin, centerColumn, rightMargin);
    }

    public void setPage(int page) {
        this.page = Math.min(Math.max(page, 0), getRenderedPages());
    }

    public int getCurrentPage() {
        return this.page;
    }

    public int getRenderedPages() {
        return this.displayElements.size() / this.buttons.size();
    }
}
