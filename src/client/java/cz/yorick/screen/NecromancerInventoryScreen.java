package cz.yorick.screen;

import cz.yorick.NecromancersShadow;
import cz.yorick.networking.NecromancerInventorySwapC2SPacket;
import cz.yorick.screen.widget.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class NecromancerInventoryScreen extends AbstractContainerScreen<NecromancerInventoryScreenHandler> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(NecromancersShadow.MOD_ID, "textures/gui/player_inventory.png");
    private final ShadowAccessWidget storageWidget;
    private final ShadowPreviewWidget previewWidget = new ShadowPreviewWidget(70, 70);
    private final SilentButtonWidget deleteWidget = new SilentButtonWidget(18, 18, this::deletePickedUp);
    private final ToggleSummonWidget toggleSummonWidget = new ToggleSummonWidget(this::togglePreviewedSummon);
    public NecromancerInventoryScreen(NecromancerInventoryScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
        this.storageWidget = new ShadowAccessWidget(this.getMenu().getPlayerShadows(), this::onClicked);
        this.titleLabelY += 77;
    }

    @Override
    protected void init() {
        super.init();
        this.previewWidget.setPosition(this.leftPos + 8, this.topPos + 8);
        this.addRenderableWidget(this.previewWidget);

        this.deleteWidget.setPosition(this.leftPos + this.imageWidth - 7 - 18, this.topPos + 7);
        this.addRenderableWidget(this.deleteWidget);

        //added only when a preview is active
        this.toggleSummonWidget.setPosition(this.leftPos + this.imageWidth - 7 - 18 - 1, this.topPos + 58);

        this.storageWidget.updateSizeAndPosition(this.imageWidth - 14 - 18, 4 * 18 - 9, this.leftPos + 7, this.topPos + 18 + 70 + 7);
        this.addRenderableWidget(this.storageWidget);
    }

    private int pickedUp = -1;
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        super.extractRenderState(graphics, mouseX, mouseY, deltaTicks);
        if(this.pickedUp >= 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, SoulSlotWidget.SOUL_TEXTURE, mouseX - 8, mouseY - 8, 0, 0, 16, 16, 16, 16);
            graphics.setTooltipForNextFrame(this.storageWidget.getWidgetFor(this.pickedUp).getMessage(), mouseX, mouseY);
            return;
        }

        getChildAt(mouseX, mouseY).ifPresent(hoveredMain -> {
            if(hoveredMain instanceof ShadowAccessWidget hoveredStorage) {
                hoveredStorage.getChildAt(mouseX, mouseY).ifPresent(hoveredEntry -> {
                    if(hoveredEntry instanceof ShadowAccessWidget.Entry entry) {
                        entry.getChildAt(mouseX, mouseY).ifPresent(hoveredElement -> {
                            if(hoveredElement instanceof SoulSlotWidget dataWidget && dataWidget.getShadowData() != null) {
                                graphics.setTooltipForNextFrame(dataWidget.getMessage(), mouseX, mouseY);
                            }
                        });
                    }
                });
            } else if(hoveredMain instanceof ToggleSummonWidget toggleWidget && toggleWidget.getMessage() != null) {
                graphics.setTooltipForNextFrame(toggleWidget.getMessage(), mouseX, mouseY);
            }
        });
    }

    //do not draw the "Inventory" title
    @Override
    protected void extractLabels(GuiGraphicsExtractor extractor, int mouseX, int mouseY) {
        extractor.text(this.font, this.title, this.titleLabelX, this.titleLabelY, -12566464, false);
    }

    private void onClicked(MouseButtonEvent click, SoulSlotWidget clicked) {
        if(click.button() == 0) {
            onLeftClick(clicked);
            return;
        }

        if(click.button() == 1) {
            if(clicked.isEmpty()) {
                //remove button when deselecting
                this.removeWidget(this.toggleSummonWidget);
            } else if(!this.previewWidget.isActive()) {
                //add button when swapping from a non-active to an active state
                this.addRenderableWidget(this.toggleSummonWidget);
            }

            this.previewWidget.setPreviewed(clicked.getId(), clicked.getShadowData(), this.minecraft.level);
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

        this.getMenu().getPlayerShadows().swapShadows(this.pickedUp, clicked.getId());
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
        this.storageWidget.refreshEntries(this.getMenu().getPlayerShadows());
        //keep the picked up after refresh
        if(this.pickedUp >= 0) {
            this.storageWidget.getWidgetFor(this.pickedUp).setPickedUp(true);
        }
    }

    private void deletePickedUp() {
        if(this.pickedUp >= 0) {
            this.getMenu().getPlayerShadows().setShadow(this.pickedUp, null);
            //swapping to -1 means deleting
            ClientPlayNetworking.send(new NecromancerInventorySwapC2SPacket(this.pickedUp, -2));
            if(this.pickedUp == this.previewWidget.getPreviewedId()) {
                this.previewWidget.clearPreview();
            }

            this.pickedUp = -1;
            this.storageWidget.refreshEntries(this.getMenu().getPlayerShadows());
        }
    }

    private void togglePreviewedSummon() {
        ClientPlayNetworking.send(new NecromancerInventorySwapC2SPacket(this.previewWidget.getPreviewedId(), -1));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return getChildAt(mouseX, mouseY)
                .map(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount))
                .orElseGet(() -> super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount));
    }
}
