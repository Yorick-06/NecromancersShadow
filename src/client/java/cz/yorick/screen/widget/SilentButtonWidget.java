package cz.yorick.screen.widget;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;

public class SilentButtonWidget extends ClickableWidget {
    private final Runnable onClicked;
    public SilentButtonWidget(int width, int height, Runnable onClicked) {
        super(0, 0, width, height, Text.empty());
        this.onClicked = onClicked;
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        this.onClicked.run();
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if(isHovered()) {
            context.fill(getX() + 1, getY() + 1, getX() + 17, getY() + 17, -2130706433);
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }
}
