package cz.yorick.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

public class SilentButtonWidget extends AbstractWidget {
    private final Runnable onClicked;
    public SilentButtonWidget(int width, int height, Runnable onClicked) {
        super(0, 0, width, height, Component.empty());
        this.onClicked = onClicked;
    }

    @Override
    public void onClick(MouseButtonEvent click, boolean doubled) {
        this.onClicked.run();
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        if(isHovered()) {
            context.fill(getX() + 1, getY() + 1, getX() + 17, getY() + 17, -2130706433);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
    }
}
