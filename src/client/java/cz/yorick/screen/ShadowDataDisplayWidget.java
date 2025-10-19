package cz.yorick.screen;

import cz.yorick.NecromancersShadow;
import cz.yorick.data.ShadowData;
import cz.yorick.mixin.EntityAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.storage.NbtReadView;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

class ShadowDataDisplayWidget extends ClickableWidget {
    private static final double ROTATION_CONSTANT = 0.05;
    private static final float SCALING_CONSTANT = 2;
    private static final float MIN_SCALE = 1F;
    private static final float DEFAULT_SCALE = 16F;
    private static final float MAX_SCALE = 64F;
    private final ShadowData shadowData;
    //a fake entity used for rendering, does not actually exist in the world
    private final Entity entity;
    private final Quaternionf rotation = new Quaternionf().rotateZ(3.1415927F).rotateY(2.6F);
    private float scale = 16.0F;
    private final Runnable rightClickCallback;
    //position gets fixed by the grid widget wrapper layout
    public ShadowDataDisplayWidget(ShadowData shadowData, World world, int width, int height, Runnable rightClickCallback) {
        super(0, 0, width, height, Text.empty());
        this.shadowData = shadowData;
        this.rightClickCallback = rightClickCallback;
        this.entity = shadowData.getEntityType().create(world, SpawnReason.TRIGGERED);
        ((EntityAccessor) this.entity).invokeReadCustomData(NbtReadView.create(NecromancersShadow.ERROR_REPORTER, MinecraftClient.getInstance().player.getRegistryManager(), shadowData.copyNbt()));
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        EntityRenderManager entityRenderManager = MinecraftClient.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super Entity, ?> entityRenderer = entityRenderManager.getRenderer(this.entity);
        EntityRenderState entityRenderState = entityRenderer.getAndUpdateRenderState(this.entity, 1.0F);
        entityRenderState.light = 15728880;
        entityRenderState.hitbox = null;
        entityRenderState.shadowPieces.clear();
        entityRenderState.outlineColor = 0;
        //context.state.addSpecialElement(new EntityGuiElementRenderState(entityRenderState, new Vector3f().add(0, 1.5F * (DEFAULT_SCALE/this.scale), 0), this.rotation, new Quaternionf(), this.getX(), this.getY(), this.getRight(), this.getBottom(), this.scale, null, null));
        context.addEntity(entityRenderState, this.scale, new Vector3f().add(0, 1.5F * (DEFAULT_SCALE/this.scale), 0), this.rotation, new Quaternionf(), this.getX(), this.getY(), this.getRight(), this.getBottom());

        if (this.isHovered()) {
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            Text tooltip = this.shadowData.asText();
            context.drawTooltip(textRenderer, List.of(tooltip.asOrderedText()), XCenteredTooltipPositioner.INSTANCE, this.getX() + (this.getWidth()/2), this.getBottom(), false);
        }
    }

    @Override
    protected boolean isValidClickButton(MouseInput input) {
        //react only to left/right mouse clicks
        return input.button() == 0 || input.button() == 1;
    }

    //don't play a sound when clicking, play it manually when right-clicking
    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    protected void onDrag(Click click, double offsetX, double offsetY) {
        //only rotate using left mouse button
        if (click.button() == 0) {
            this.rotation.rotateLocalY((float) (-offsetX * ROTATION_CONSTANT));//.rotateLocalX((float) (offsetY * ROTATION_CONSTANT));
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (isHovered()) {
            this.scale = (float) Math.clamp(this.scale + verticalAmount * SCALING_CONSTANT, MIN_SCALE, MAX_SCALE);
            return true;
        }

        return false;
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        //play sound and activate callback on right button
        if(click.button() == 1) {
            playClickSound(MinecraftClient.getInstance().getSoundManager());
            this.rightClickCallback.run();
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }
}
