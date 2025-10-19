package cz.yorick.screen;

import net.minecraft.client.gui.tooltip.TooltipPositioner;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public class XCenteredTooltipPositioner implements TooltipPositioner {
    public static final XCenteredTooltipPositioner INSTANCE = new XCenteredTooltipPositioner();

    @Override
    public Vector2ic getPosition(int screenWidth, int screenHeight, int x, int y, int width, int height) {
        return new Vector2i(x - (width/2), y);
    }
}
