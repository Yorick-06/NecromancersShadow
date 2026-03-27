package cz.yorick.screen.widget;

import cz.yorick.NecromancersShadow;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ToggleSummonWidget extends AbstractWidget {
    public static final String TOGGLE_SUMMON_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".toggle_summon";
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(NecromancersShadow.MOD_ID, "textures/gui/toggle_summon_button.png");
    private static final Identifier HOVERED_TEXTURE = Identifier.fromNamespaceAndPath(NecromancersShadow.MOD_ID, "textures/gui/toggle_summon_button_hovered.png");
    private final Runnable onClicked;
    public ToggleSummonWidget(Runnable onClicked) {
        super(0, 0, 20, 20, Component.translatable(TOGGLE_SUMMON_TRANSLATION_KEY));
        this.onClicked = onClicked;
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        context.blit(RenderPipelines.GUI_TEXTURED, this.isHovered ? HOVERED_TEXTURE : TEXTURE, this.getX(), this.getY(), 0, 0, 20, 20, 20, 20);
    }

    @Override
    public void onClick(MouseButtonEvent click, boolean doubled) {
        this.onClicked.run();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
    }
}
