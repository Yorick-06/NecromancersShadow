package cz.yorick.screen.widget;

import cz.yorick.NecromancersShadow;
import cz.yorick.data.ShadowData;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class SoulSlotWidget extends AbstractWidget {
    public static final Identifier BACKGROUND_TEXTURE = Identifier.fromNamespaceAndPath(NecromancersShadow.MOD_ID, "textures/gui/soul_slot.png");
    public static final Identifier SOUL_TEXTURE = Identifier.fromNamespaceAndPath(NecromancersShadow.MOD_ID, "textures/gui/soul.png");
    private static final Identifier SELECTED_TEXTURE = Identifier.withDefaultNamespace("textures/gui/sprites/hud/hotbar_selection.png");
    private ShadowData shadowData;
    private final int id;
    private boolean pickedUp = false;
    public SoulSlotWidget(int id) {
        super(0, 0, 18, 18, Component.empty());
        this.id = id;
    }

    public void setShadowData(ShadowData shadowData) {
        this.shadowData = shadowData;
        this.setMessage(this.shadowData == null ? Component.empty() : this.shadowData.asText());
    }

    public int getId() {
        return this.id;
    }

    public ShadowData getShadowData() {
        return this.shadowData;
    }

    public boolean isEmpty() {
        return this.shadowData == null;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, this.getX(), this.getY(), 0, 0, 18, 18, 18, 18);
        if(this.shadowData != null && !this.pickedUp) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, SOUL_TEXTURE, this.getX() + 1, this.getY() + 1, 0, 0, 16, 16, 16, 16);
        }

        if(isHovered()) {
            graphics.fill(getX() + 1, getY() + 1, getX() + 17, getY() + 17, -2130706433);
        }
    }

    public void setPickedUp(boolean pickedUp) {
        this.pickedUp = pickedUp;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {

    }
}
