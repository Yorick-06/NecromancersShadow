package cz.yorick.screen;

import cz.yorick.NecromancersShadow;
import cz.yorick.data.DataAttachments;
import cz.yorick.data.MutableShadowStorage;
import cz.yorick.item.SculkTotemItem;
import cz.yorick.networking.SculkEmeraldInventorySwapC2SPacket;
import cz.yorick.screen.widget.ShadowAccessWidget;
import cz.yorick.screen.widget.SoulSlotWidget;
import cz.yorick.util.UiId;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SculkEmeraldScreen extends Screen {
    public static String TITLE_TRANSLATION_KEY = "title." + NecromancersShadow.MOD_ID + ".sculk_emerald_inventory";
    private static final Identifier TEXTURE = Identifier.of(NecromancersShadow.MOD_ID, "textures/gui/sculk_emerald_inventory.png");
    private static final int backgroundWidth = 176;
    private static final int backgroundHeight = 114 + 6 * 18;
    private static final int titleX = 8;
    private static final int titleY = 6;
    private static final int title2Y = 6 * 18 + 6;
    private int x = 0;
    private int y = 0;
    private final MutableShadowStorage playerStorage;
    private final MutableShadowStorage itemStorage;
    private final ShadowAccessWidget playerStorageWidget;
    private final ShadowAccessWidget itemStorageWidget;
    private final Text title2;
    public SculkEmeraldScreen(MutableShadowStorage itemStorage) {
        super(Text.translatable(TITLE_TRANSLATION_KEY));
        this.playerStorage = DataAttachments.getShadowStorage(MinecraftClient.getInstance().player).toMutable();
        this.itemStorage = itemStorage;
        this.playerStorageWidget = new ShadowAccessWidget(this.playerStorage, (click, soulSlotWidget) -> onClicked(soulSlotWidget, UiId.Ui.PLAYER, click.hasShift()));
        this.itemStorageWidget = new ShadowAccessWidget(this.itemStorage, (click, soulSlotWidget) -> onClicked(soulSlotWidget, UiId.Ui.ITEM, click.hasShift()));
        this.title2 = Text.translatable(SculkTotemItem.NECROMANCER_INVENTORY_TRANSLATION_KEY);
    }

    @Override
    protected void init() {
        this.x = (this.width - backgroundWidth)/2;
        this.y = (this.height - backgroundHeight)/2;
        this.itemStorageWidget.position(backgroundWidth - 14 - 18, 5 * 18, this.x + 7, this.y + 18);
        this.addDrawableChild(this.itemStorageWidget);
        this.playerStorageWidget.position(backgroundWidth - 14 - 18, 5 * 18, this.x + 7, this.y + 18 + 5 * 18 + 18);
        this.addDrawableChild(this.playerStorageWidget);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.renderBackground(context, mouseX, mouseY, deltaTicks);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, this.x, this.y, 0, 0, backgroundWidth, backgroundHeight, 256, 256);
    }

    private UiId pickedUp = null;
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        context.drawText(this.textRenderer, this.title, this.x + titleX, this.y + titleY, -12566464, false);
        context.drawText(this.textRenderer, this.title2, this.x + titleX, this.y + title2Y, -12566464, false);

        if(this.pickedUp != null) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, SoulSlotWidget.SOUL_TEXTURE, mouseX - 8, mouseY - 8, 0, 0, 16, 16, 16, 16);
            context.drawTooltip(getWidget(this.pickedUp).getMessage(), mouseX, mouseY);
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
            }
        });
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (super.keyPressed(input)) {
            return true;
        } else if (this.client.options.inventoryKey.matchesKey(input)) {
            this.close();
            return true;
        }

        return false;
    }

    private void onClicked(SoulSlotWidget clickedWidget, UiId.Ui ui, boolean hasShift) {
        UiId clicked = new UiId(ui, clickedWidget.getId());

        //shift clicking has priority
        if(!clickedWidget.isEmpty() && hasShift) {
            //get the opposite
            UiId.Ui otherUi = clicked.ui().choose(UiId.Ui.ITEM, UiId.Ui.PLAYER);
            MutableShadowStorage otherStorage = otherUi.choose(this.playerStorage, this.itemStorage);
            UiId destination = new UiId(otherUi, otherStorage.firstFreeSlot());
            UiId.swap(clicked, destination, this.playerStorage, this.itemStorage);

            ClientPlayNetworking.send(new SculkEmeraldInventorySwapC2SPacket(clicked, destination));
            refreshStorageWidgets();
            return;
        }


        //not holding anything, try to pick up the target
        if(this.pickedUp == null) {
            if(!clickedWidget.isEmpty()) {
                clickedWidget.setPickedUp(true);
                this.pickedUp =  clicked;
            }
            return;
        }

        //clicking again into the slot, put down the target
        if(clicked.equals(this.pickedUp)) {
            getWidget(this.pickedUp).setPickedUp(false);
            this.pickedUp = null;
            return;
        }

        //clicking into another slot while holding something - execute a swap
        SoulSlotWidget pickedUpWidget = getWidget(this.pickedUp);
        pickedUpWidget.setPickedUp(false);

        UiId.swap(this.pickedUp, clicked, this.playerStorage, this.itemStorage);
        ClientPlayNetworking.send(new SculkEmeraldInventorySwapC2SPacket(this.pickedUp, clicked));

        //if there was something, do nothing
        //the new slot should be picked up, but the new slot soul gets swapped to the old slot which is already marked as picked up
        if(clickedWidget.isEmpty()) {
            clickedWidget.setPickedUp(false);
            this.pickedUp = null;
        }

        refreshStorageWidgets();
    }

    private void refreshStorageWidgets() {
        //refresh the inventory, this updates the displayed widgets and adds/removes rows based on the change
        this.playerStorageWidget.refreshEntries(this.playerStorage);
        this.itemStorageWidget.refreshEntries(this.itemStorage);
        //keep the picked up after refresh
        if(this.pickedUp != null) {
            getWidget(this.pickedUp).setPickedUp(true);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return hoveredElement(mouseX, mouseY)
                .map(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount))
                .orElseGet(() -> super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount));
    }

    private SoulSlotWidget getWidget(UiId uiId) {
        return switch (uiId.ui()) {
            case PLAYER -> this.playerStorageWidget.getWidgetFor(uiId.slot());
            case ITEM -> this.itemStorageWidget.getWidgetFor(uiId.slot());
        };
    }
}
