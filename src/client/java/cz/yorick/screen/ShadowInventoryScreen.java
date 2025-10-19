package cz.yorick.screen;

import cz.yorick.NecromancersShadow;
import cz.yorick.ShadowInventoryScreenHandler;
import cz.yorick.data.ShadowData;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public class ShadowInventoryScreen extends HandledScreen<ShadowInventoryScreenHandler> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/generic_54.png");
    private static final int ROW_ENTITIES = 3;
    private final PlayerEntity player;
    public ShadowInventoryScreen(ShadowInventoryScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.player = inventory.player;
        this.backgroundHeight = 114 + 6 * 18;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
        //refreshGridWidget();
        int index = 0;
        Slot slot = getScreenHandler().getShadowInventorySlot(index);
        while (slot != null) {
            if(slot.getStack() == null || slot.getStack().isEmpty()) {
                continue;
            }

            ShadowData data = slot.getStack().get(NecromancersShadow.SOUL_DATA_COMPONENT);
            if(data == null) {
                continue;
            }

            ShadowDataDisplayWidget widget = new ShadowDataDisplayWidget(data, this.player.getEntityWorld(), 54, 54, () -> {});
            this.addDrawableChild(widget);
            widget.setX(slot.x - 18);
            widget.setY(slot.y - 36);

            index++;
            slot = getScreenHandler().getShadowInventorySlot(index);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {/*
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0.0F, 0.0F, this.backgroundWidth, this.backgroundHeight, 256, 256);*/
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0.0F, 0.0F, this.backgroundWidth, 6 * 18 + 17, 256, 256);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j + 6 * 18 + 17, 0.0F, 126.0F, this.backgroundWidth, 96, 256, 256);
    }

    private void refreshGridWidget() {
        GridWidget gridWidget = new GridWidget();
        List<ShadowData> shadows = this.getScreenHandler().getShadowData();

        for (int y = 0; y * ROW_ENTITIES < shadows.size(); y++) {
            for (int x = 0; x < ROW_ENTITIES && (y * ROW_ENTITIES + x) < shadows.size(); x++) {
                int index = y * ROW_ENTITIES + x;
                ShadowDataWidget widget = new ShadowDataWidget(y * ROW_ENTITIES + x, shadows.get(index), this.player.getEntityWorld(), 54, 54);
                gridWidget.add(widget, y, x);
                this.addDrawableChild(widget);
            }
        }

        gridWidget.setX(this.x + 8 - 1);
        gridWidget.setY(this.y + 18 - 1);
        gridWidget.refreshPositions();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if(!super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return this.hoveredElement(mouseX, mouseY).filter(hovered -> hovered.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent();
        }

        return false;
    }
}
