package cz.yorick.screen;

import cz.yorick.NecromancersShadow;
import cz.yorick.networking.NecromancerInventorySwapC2SPacket;
import cz.yorick.screen.widget.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class NecromancerInventoryScreen extends HandledScreen<NecromancerInventoryScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of(NecromancersShadow.MOD_ID, "textures/gui/player_inventory.png");
    private final ShadowAccessWidget storageWidget;
    private final ShadowPreviewWidget previewWidget = new ShadowPreviewWidget(70, 70);
    private final SilentButtonWidget deleteWidget = new SilentButtonWidget(18, 18, this::deletePickedUp);
    private final ToggleSummonWidget toggleSummonWidget = new ToggleSummonWidget(this::togglePreviewedSummon);
    public NecromancerInventoryScreen(NecromancerInventoryScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.storageWidget = new ShadowAccessWidget(this.getScreenHandler().getPlayerShadows(), this::onClicked);
        this.titleY += 77;
    }

    @Override
    protected void init() {
        super.init();
        this.previewWidget.setPosition(this.x + 8, this.y + 8);
        this.addDrawableChild(this.previewWidget);

        this.deleteWidget.setPosition(this.x + this.backgroundWidth - 7 - 18, this.y + 7);
        this.addDrawableChild(this.deleteWidget);

        //added only when a preview is active
        this.toggleSummonWidget.setPosition(this.x + this.backgroundWidth - 7 - 18 - 1, this.y + 58);

        this.storageWidget.position(this.backgroundWidth - 14 - 18, 4 * 18 - 9, this.x + 7, this.y + 18 + 70 + 7);
        this.addDrawableChild(this.storageWidget);
    }

    @Override
    protected void drawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight, 256, 256);
    }

    //do not draw the "Inventory" title
    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.title, this.titleX, this.titleY, -12566464, false);
    }

    private int pickedUp = -1;
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        if(this.pickedUp >= 0) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, SoulSlotWidget.SOUL_TEXTURE, mouseX - 8, mouseY - 8, 0, 0, 16, 16, 16, 16);
            context.drawTooltip(this.storageWidget.getWidgetFor(this.pickedUp).getMessage(), mouseX, mouseY);
            return;
        }

        hoveredElement(mouseX, mouseY).ifPresent(hoveredMain -> {
            if(hoveredMain instanceof ShadowAccessWidget hoveredStorage) {
                hoveredStorage.hoveredElement(mouseX, mouseY).ifPresent(hoveredEntry -> {
                    if(hoveredEntry instanceof ShadowAccessWidget.Entry entry) {
                        entry.hoveredElement(mouseX, mouseY).ifPresent(hoveredElement -> {
                            if(hoveredElement instanceof SoulSlotWidget dataWidget && dataWidget.getShadowData() != null) {
                                context.drawTooltip(dataWidget.getMessage(), mouseX, mouseY);
                            }
                        });
                    }
                });
            } else if(hoveredMain instanceof ToggleSummonWidget toggleWidget && toggleWidget.getMessage() != null) {
                context.drawTooltip(toggleWidget.getMessage(), mouseX, mouseY);
            }
        });
    }

    private void onClicked(Click click, SoulSlotWidget clicked) {
        if(click.button() == 0) {
            onLeftClick(clicked);
            return;
        }

        if(click.button() == 1) {
            if(clicked.isEmpty()) {
                //remove button when deselecting
                this.remove(this.toggleSummonWidget);
            } else if(!this.previewWidget.isActive()) {
                //add button when swapping from a non-active to an active state
                this.addDrawableChild(this.toggleSummonWidget);
            }

            this.previewWidget.setPreviewed(clicked.getId(), clicked.getShadowData(), this.client.world);
        }
    }

    private void onLeftClick(SoulSlotWidget clicked) {
        //not holding anything, try to pick up the target
        if(this.pickedUp < 0) {
            if(!clicked.isEmpty()) {
                clicked.setPickedUp(true);
                this.pickedUp = clicked.getId();
            }
            return;
        }

        //clicking again into the slot, put down the target
        if(this.pickedUp == clicked.getId()) {
            this.storageWidget.getWidgetFor(this.pickedUp).setPickedUp(false);
            this.pickedUp = -1;
            return;
        }

        //clicking into another slot while holding something - execute a swap
        SoulSlotWidget pickedUpWidget = this.storageWidget.getWidgetFor(this.pickedUp);

        this.getScreenHandler().getPlayerShadows().swapShadows(this.pickedUp, clicked.getId());
        ClientPlayNetworking.send(new NecromancerInventorySwapC2SPacket(this.pickedUp, clicked.getId()));

        //swap reference if the previewed shadow got swapped
        this.previewWidget.onSwap(this.pickedUp, clicked.getId());

        //if there was something, do nothing
        //the new slot should be picked up, but the new slot soul gets swapped to the old slot which is already marked as picked up
        if(clicked.isEmpty()) {
            //nothing was in the clicked slot
            pickedUpWidget.setPickedUp(false);
            this.pickedUp = -1;
        }

        //refresh the inventory, this updates the displayed widgets and adds/removes rows based on the change
        this.storageWidget.refreshEntries(this.getScreenHandler().getPlayerShadows());
        //keep the picked up after refresh
        if(this.pickedUp >= 0) {
            this.storageWidget.getWidgetFor(this.pickedUp).setPickedUp(true);
        }
    }

    private void deletePickedUp() {
        if(this.pickedUp >= 0) {
            this.getScreenHandler().getPlayerShadows().setShadow(this.pickedUp, null);
            //swapping to -1 means deleting
            ClientPlayNetworking.send(new NecromancerInventorySwapC2SPacket(this.pickedUp, -2));
            if(this.pickedUp == this.previewWidget.getPreviewedId()) {
                this.previewWidget.clearPreview();
            }

            this.pickedUp = -1;
            this.storageWidget.refreshEntries(this.getScreenHandler().getPlayerShadows());
        }
    }

    private void togglePreviewedSummon() {
        ClientPlayNetworking.send(new NecromancerInventorySwapC2SPacket(this.previewWidget.getPreviewedId(), -1));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return hoveredElement(mouseX, mouseY)
                .map(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount))
                .orElseGet(() -> super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount));
    }
}
