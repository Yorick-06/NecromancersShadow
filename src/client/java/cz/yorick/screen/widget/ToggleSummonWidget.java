package cz.yorick.screen.widget;

import cz.yorick.NecromancersShadow;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ToggleSummonWidget extends ClickableWidget {
    public static final String TOGGLE_SUMMON_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".toggle_summon";
    private static final Identifier TEXTURE = Identifier.of(NecromancersShadow.MOD_ID, "textures/gui/toggle_summon_button.png");
    private static final Identifier HOVERED_TEXTURE = Identifier.of(NecromancersShadow.MOD_ID, "textures/gui/toggle_summon_button_hovered.png");
    private final Runnable onClicked;
    public ToggleSummonWidget(Runnable onClicked) {
        super(0, 0, 20, 20, Text.translatable(TOGGLE_SUMMON_TRANSLATION_KEY));
        this.onClicked = onClicked;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, this.hovered ? HOVERED_TEXTURE : TEXTURE, this.getX(), this.getY(), 0, 0, 20, 20, 20, 20);
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        this.onClicked.run();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }
}
