package cz.yorick.screen;

import cz.yorick.data.ShadowData;
import cz.yorick.networking.ShadowInventoryInteractC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class ShadowDataWidget extends ClickableWidget {
    private final int id;
    private final ShadowDataDisplayWidget displayWidget;
    private final ShadowDataMenuWidget menuWidget;
    private ClickableWidget shownWidget;
    public ShadowDataWidget(int id, ShadowData shadowData, World world, int width, int height) {
        //x and y gets fixed by the grid
        super(0, 0, width, height, Text.empty());
        this.id = id;
        this.displayWidget = new ShadowDataDisplayWidget(shadowData, world, width, height, this::showMenu);
        this.menuWidget = new ShadowDataMenuWidget(width, height, this::summon, this::convert, this::showDisplay);
        showDisplay();
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        this.displayWidget.setX(x);
        this.menuWidget.setX(x);
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        this.displayWidget.setY(y);
        this.menuWidget.setY(y);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        this.shownWidget.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    protected boolean isValidClickButton(MouseInput input) {
        //let the other widgets filter out clicks
        return true;
    }

    //other widgets should decide if the sound should be played
    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        this.shownWidget.mouseClicked(click, doubled);
    }

    @Override
    protected void onDrag(Click click, double offsetX, double offsetY) {
        this.shownWidget.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return this.shownWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    private void showDisplay() {
        this.shownWidget = this.displayWidget;
    }

    private void showMenu() {
        this.shownWidget = this.menuWidget;
    }

    private void summon() {
        ClientPlayNetworking.send(new ShadowInventoryInteractC2SPacket(this.id, ShadowInventoryInteractC2SPacket.Action.TOGGLE_SUMMON));
        showDisplay();
    }

    private void convert() {
        ClientPlayNetworking.send(new ShadowInventoryInteractC2SPacket(this.id, ShadowInventoryInteractC2SPacket.Action.CONVERT));
        showDisplay();
    }
}
