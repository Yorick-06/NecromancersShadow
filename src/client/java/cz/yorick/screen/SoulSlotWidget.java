package cz.yorick.screen;

import cz.yorick.NecromancersShadow;
import cz.yorick.data.ShadowData;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SoulSlotWidget extends ClickableWidget {
    public static final Identifier SOUL_TEXTURE = Identifier.of(NecromancersShadow.MOD_ID, "textures/item/soul.png");
    private static final Identifier SELECTED_TEXTURE = Identifier.ofVanilla("textures/gui/sprites/hud/hotbar_selection.png");
    private ShadowData shadowData;
    private final int id;
    private boolean pickedUp = false;
    public SoulSlotWidget(int id) {
        super(0, 0, 18, 18, Text.empty());
        this.id = id;
    }

    public void setShadowData(ShadowData shadowData) {
        this.shadowData = shadowData;
        this.setMessage(this.shadowData != null ? this.shadowData.asText() : Text.empty());
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
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if(this.shadowData != null && !this.pickedUp) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, SOUL_TEXTURE, this.getX() + 1, this.getY() + 1, 0, 0, 16, 16, 16, 16);
            //this.renderer.render(context, getX() + 1, getY() + 1, deltaTicks);
        }/*
        if(isFocused()) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, SELECTED_TEXTURE, this.getX() - 3, this.getY() - 3, 0, 0, 24, 23, 24, 23);
        }*/
    }

    public void setPickedUp(boolean pickedUp) {
        this.pickedUp = pickedUp;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
