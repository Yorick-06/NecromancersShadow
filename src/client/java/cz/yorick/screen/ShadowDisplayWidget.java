package cz.yorick.screen;

import cz.yorick.NecromancersShadow;
import cz.yorick.data.ShadowData;
import cz.yorick.mixin.EntityAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.gui.widget.ClickableWidget;
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

public class ShadowDisplayWidget extends ClickableWidget {
    private static final float ROTATION_CONSTANT = 0.05F;
    private static final float SCALING_CONSTANT = 20;
    private static final float MIN_SCALE = 1F;
    private static final float DEFAULT_SCALE = 16F;
    private static final float MAX_SCALE = 64F;
    private static final int PREVIEW_SIZE = 54;

    //a fake entity used for rendering, does not actually exist in the world
    private Entity entity;
    private Quaternionf rotation;
    private float scale;
    public ShadowDisplayWidget(int width, int height) {
        super(0, 0, width, height, Text.empty());
    }

    public void setDisplayed(ShadowData data, World world) {
        this.rotation =  new Quaternionf().rotateZ(3.1415927F).rotateY(2.6F);
        setMessage(data.asText());
        this.entity = data.entityType().create(world, SpawnReason.TRIGGERED);
        ((EntityAccessor) this.entity).invokeReadCustomData(NbtReadView.create(NecromancersShadow.ERROR_REPORTER, MinecraftClient.getInstance().player.getRegistryManager(), data.nbt()));

        float entityWidth = this.entity.getWidth();
        float entityHeight = this.entity.getHeight();
        this.scale = PREVIEW_SIZE/Math.max(entityWidth, entityHeight);
    }

    public void resetDisplayed() {
        this.entity = null;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if(this.entity == null) {
            return;
        }

        TooltipBackgroundRenderer.render(context, mouseX, mouseY, PREVIEW_SIZE, PREVIEW_SIZE, null);
        this.rotation.rotateLocalY(ROTATION_CONSTANT * deltaTicks);

        EntityRenderManager entityRenderManager = MinecraftClient.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super Entity, ?> entityRenderer = entityRenderManager.getRenderer(this.entity);
        EntityRenderState entityRenderState = entityRenderer.getAndUpdateRenderState(this.entity, 1.0F);
        entityRenderState.light = 15728880;
        entityRenderState.hitbox = null;
        entityRenderState.shadowPieces.clear();
        entityRenderState.outlineColor = 0;
        //context.state.addSpecialElement(new EntityGuiElementRenderState(entityRenderState, new Vector3f().add(0, 1.5F * (DEFAULT_SCALE/this.scale), 0), this.rotation, new Quaternionf(), this.getX(), this.getY(), this.getRight(), this.getBottom(), this.scale, null, null));
        context.addEntity(entityRenderState, this.scale, new Vector3f().add(0, 1, 0), this.rotation, new Quaternionf(), mouseX, mouseY, mouseX + PREVIEW_SIZE, mouseY + PREVIEW_SIZE);

        if (this.isHovered()) {
            //TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            context.drawTooltip(getMessage(), this.getX() + (this.getWidth()/2), this.getBottom());
            //context.drawTooltip(textRenderer, List.of(this.getMessage().asOrderedText()), XCenteredTooltipPositioner.INSTANCE, this.getX() + (this.getWidth()/2), this.getBottom(), false);
        }
    }

    //only accepted click is left to rotate, do not play a sound
    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }
}
