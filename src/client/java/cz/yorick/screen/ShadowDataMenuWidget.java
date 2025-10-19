package cz.yorick.screen;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.List;

public class ShadowDataMenuWidget extends ClickableWidget {
    private final List<ButtonWidget> buttons;
    public ShadowDataMenuWidget(int width, int height, Runnable summonClicked, Runnable convertClicked, Runnable cancelClicked) {
        super(0, 0, width, height, Text.empty());
        this.buttons = List.of(
                ButtonWidget.builder(Text.literal("summon"), widget -> summonClicked.run()).build(),
                ButtonWidget.builder(Text.literal("convert"), widget -> convertClicked.run()).build(),
                ButtonWidget.builder(Text.literal("cancel"), widget -> cancelClicked.run()).build()
        );

        this.buttons.forEach(button -> {
            button.setWidth(this.width);
            button.setHeight(this.height/buttons.size());
        });
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        this.buttons.forEach(button -> button.setX(this.getX()));
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        int buttonOffset = this.height/this.buttons.size();
        for (int i = 0; i < this.buttons.size(); i++) {
            this.buttons.get(i).setY(this.getY() + i * buttonOffset);
        }
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        this.buttons.forEach(button -> button.render(context, mouseX, mouseY, deltaTicks));
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        this.buttons.forEach(button -> button.mouseClicked(click, doubled));
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
