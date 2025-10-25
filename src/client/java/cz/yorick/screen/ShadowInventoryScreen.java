package cz.yorick.screen;

import cz.yorick.data.ShadowData;
import cz.yorick.networking.ShadowStorageSwapC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class ShadowInventoryScreen extends HandledScreen<ShadowInventoryScreenHandler> {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/generic_54.png");
    private final ShadowStorageTooltipRenderer tooltipRenderer = new ShadowStorageTooltipRenderer();
    private final ShadowStorageWidget storageWidget;
    public ShadowInventoryScreen(ShadowInventoryScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.storageWidget = new ShadowStorageWidget(this.getScreenHandler().getShadowManager());
        this.backgroundHeight = 114 + 6 * 18;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.storageWidget.position(this.backgroundWidth - 16, 6 * 18, this.x + 8, this.y + 18);
        this.addDrawableChild(this.storageWidget);
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        int i = (this.width - this.backgroundWidth) / 2;
        int j = (this.height - this.backgroundHeight) / 2;
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0.0F, 0.0F, this.backgroundWidth, 6 * 18 + 17, 256, 256);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j + 6 * 18 + 17, 0.0F, 126.0F, this.backgroundWidth, 96, 256, 256);
    }

    private SoulSlotWidget pickedUp = null;
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        if(this.pickedUp != null) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, SoulSlotWidget.SOUL_TEXTURE, mouseX - 8, mouseY - 8, 0, 0, 16, 16, 16, 16);
            context.drawTooltip(this.pickedUp.getMessage(), mouseX, mouseY);
            return;
        }

        hoveredElement(mouseX, mouseY).ifPresent(hoveredMain -> {
            if(hoveredMain instanceof ShadowStorageWidget storageWidget) {
                storageWidget.hoveredElement(mouseX, mouseY).ifPresent(hoveredEntry -> {
                    if(hoveredEntry instanceof ShadowStorageWidget.Entry entry) {
                        entry.hoveredElement(mouseX, mouseY).ifPresent(hoveredElement -> {
                            if(hoveredElement instanceof SoulSlotWidget dataWidget && dataWidget.getShadowData() != null) {
                                context.drawTooltip(dataWidget.getMessage(), mouseX, mouseY);
                            }
                        });
                    }
                });
            }
        });
        /*
        hoveredElement(mouseX, mouseY).ifPresent(hovered -> {
            if(hovered instanceof ShadowStorageWidget shadowStorageWidget) {
                ShadowStorageWidget.Entry focusedRow = shadowStorageWidget.getFocused();
                //nothing is focused but something was focused
                if(focusedRow == null) {
                    if(this.previousFocusedWidget != null) {
                        this.tooltipRenderer.onFocusChanged(null);
                        this.previousFocusedWidget = null;
                    }
                    return;
                }

                //only ShadowDataWidget elements are present, should never fail
                ShadowDataWidget focusedWidget = (ShadowDataWidget)focusedRow.getFocused();
                if(focusedWidget != this.previousFocusedWidget) {
                    this.tooltipRenderer.onFocusChanged(focusedWidget);
                    this.previousFocusedWidget = focusedWidget;
                }
            }
        });*/

        //this.tooltipRenderer.render(context, this.x, this.y  + 8 * 18, deltaTicks);
    }

    @Override
    public void setFocused(@Nullable Element focused) {
        super.setFocused(focused);
        if(!(focused instanceof ShadowStorageWidget shadowStorageWidget)) {
            //this.tooltipRenderer.onFocusChanged(null);
            return;
        }

        if(shadowStorageWidget.getFocused() instanceof ShadowStorageWidget.Entry entry && entry.getFocused() instanceof SoulSlotWidget cliked) {
            //not holding anything, try to pick up the target
            if(this.pickedUp == null) {
                if(!cliked.isEmpty()) {
                    cliked.setPickedUp(true);
                    this.pickedUp = cliked;
                }
                return;
            }

            //clicking again into the slot, put down the target
            if(this.pickedUp == cliked) {
                this.pickedUp.setPickedUp(false);
                this.pickedUp = null;
                return;
            }

            //clicking into another slot while holding something - execute a swap
            this.pickedUp.setPickedUp(false);
            ShadowData clickedShadowData = cliked.getShadowData();
            cliked.setShadowData(this.pickedUp.getShadowData());
            this.pickedUp.setShadowData(clickedShadowData);
            ClientPlayNetworking.send(new ShadowStorageSwapC2SPacket(this.pickedUp.getId(), cliked.getId()));

            //only pick up the new slot if it is not empty
            if(!cliked.isEmpty()) {
                cliked.setPickedUp(true);
                this.pickedUp = cliked;
            } else {
                this.pickedUp = null;
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return hoveredElement(mouseX, mouseY)
                .map(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount))
                .orElseGet(() -> super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount));
    }
}
