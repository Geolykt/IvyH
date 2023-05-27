package de.geolykt.ivyh.ui.warinspect;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;

import de.geolykt.ivyh.IvyWar;
import de.geolykt.ivyh.ui.AbstractTextContext;
import de.geolykt.ivyh.ui.ConstantTextContext;
import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.NullUtils;
import de.geolykt.starloader.api.gui.canvas.Canvas;
import de.geolykt.starloader.api.gui.canvas.CanvasManager;
import de.geolykt.starloader.api.gui.canvas.CanvasSettings;
import de.geolykt.starloader.api.gui.canvas.ChildObjectOrientation;
import de.geolykt.starloader.api.gui.canvas.MultiCanvas;

import snoddasmannen.galimulator.Space;

public class WarInspector {

    @NotNull
    private final IvyWar war;

    public WarInspector(@NotNull IvyWar war) {
        this.war = war;
    }

    @NotNull
    public Canvas toCanvas() {
        CanvasManager cm = CanvasManager.getInstance();
        ConstantTextContext activeKey = new ConstantTextContext(150, 25, 5, 0, Align.left, "Active");
        AbstractTextContext activeValue = new AbstractTextContext(350, 25, 5, 0, Align.left) {
            @SuppressWarnings("null")
            @Override
            @NotNull
            public String getText() {
                return Boolean.toString(!WarInspector.this.war.isUnloaded());
            }
        };
        ConstantTextContext ageKey = new ConstantTextContext(150, 25, 5, 0, Align.left, "Age");
        AbstractTextContext ageValue = new AbstractTextContext(350, 25, 5, 0, Align.left) {
            @Override
            @NotNull
            public String getText() {
                if (WarInspector.this.war.isUnloaded()) {
                    return (WarInspector.this.war.getLastActionYear() - WarInspector.this.war.getStartYear()) / 1000.0F + " " + Space.getMapData().getTimeNoun();
                } else {
                    return (Galimulator.getGameYear() - WarInspector.this.war.getStartYear()) / 1000.0F + " " + Space.getMapData().getTimeNoun();
                }
            }
        };
        ConstantTextContext scoreKey = new ConstantTextContext(150, 25, 5, 0, Align.left, "Star score");
        AbstractTextContext scoreValue = new AbstractTextContext(350, 25, 5, 0, Align.left) {
            @SuppressWarnings("null")
            @Override
            @NotNull
            public String getText() {
                return Integer.toString(WarInspector.this.war.getAgressorScore());
            }
        };
        ConstantTextContext aggressorHeader = new ConstantTextContext(500, 25, 5, 0, Align.center, "Aggressors");
        ConstantTextContext defenderHeader = new ConstantTextContext(500, 25, 5, 0, Align.center, "Defender");
        Canvas aggressors = cm.withMargins(5, 5, 5, 5, cm.newCanvas(new WarParticipantsList(490, 690, this.war.allAggressorsView), CanvasSettings.CHILD_TRANSPARENT), CanvasSettings.CHILD_TRANSPARENT);
        Canvas defenders = cm.withMargins(5, 5, 5, 5, cm.newCanvas(new WarParticipantsList(490, 690, this.war.allDefendersView), CanvasSettings.CHILD_TRANSPARENT), CanvasSettings.CHILD_TRANSPARENT);
        MultiCanvas age = cm.multiCanvas(cm.dummyContext(500, 25), CanvasSettings.CHILD_TRANSPARENT, ChildObjectOrientation.LEFT_TO_RIGHT, ageKey, ageValue);
        MultiCanvas active = cm.multiCanvas(cm.dummyContext(500, 25), CanvasSettings.CHILD_TRANSPARENT, ChildObjectOrientation.LEFT_TO_RIGHT, activeKey, activeValue);
        MultiCanvas score = cm.multiCanvas(cm.dummyContext(500, 25), CanvasSettings.CHILD_TRANSPARENT, ChildObjectOrientation.LEFT_TO_RIGHT, scoreKey, scoreValue);
        MultiCanvas columnHeader = cm.multiCanvas(cm.dummyContext(1000, 25), CanvasSettings.CHILD_TRANSPARENT, ChildObjectOrientation.LEFT_TO_RIGHT, aggressorHeader, defenderHeader);
        MultiCanvas participants = cm.multiCanvas(cm.dummyContext(1000, 700), CanvasSettings.CHILD_TRANSPARENT, ChildObjectOrientation.LEFT_TO_RIGHT, aggressors, defenders);
        return cm.multiCanvas(cm.dummyContext(1000, 800), new CanvasSettings(CanvasSettings.NEAR_SOLID_COLOR, this.war.getDisplayName(), NullUtils.requireNotNull(Color.RED)), ChildObjectOrientation.BOTTOM_TO_TOP, participants, columnHeader, active, age, score);
    }
}
