package cz.yorick.screen;

import cz.yorick.NecromancersShadow;
import cz.yorick.data.ShadowData;
import cz.yorick.mixin.EntityAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.storage.NbtReadView;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ShadowStorageTooltipRenderer {
    private static final int PREVIEW_SIZE = 72;
    private static final float ROTATION_CONSTANT = 0.05F;
    //a fake entity used for rendering, does not actually exist in the world
    private Entity entity;
    private Quaternionf rotation;
    private float scale;
    private Text tooltip;
    public void onFocusChanged(SoulSlotWidget focusedWidget) {
        if(focusedWidget == null) {
            resetDisplayed();
            return;
        }

        ShadowData data = focusedWidget.getShadowData();
        if(data == null) {
            resetDisplayed();
            return;
        }

        setDisplayed(data, MinecraftClient.getInstance().world);
    }

    public void setDisplayed(ShadowData data, World world) {
        this.rotation =  new Quaternionf().rotateZ(3.1415927F).rotateY(2.6F);
        this.tooltip = data.asText();
        this.entity = data.entityType().create(world, SpawnReason.TRIGGERED);
        ((EntityAccessor) this.entity).invokeReadCustomData(NbtReadView.create(NecromancersShadow.ERROR_REPORTER, MinecraftClient.getInstance().player.getRegistryManager(), data.nbt()));

        float entityWidth = this.entity.getWidth();
        float entityHeight = this.entity.getHeight();
        this.scale = PREVIEW_SIZE/Math.max(entityWidth, entityHeight);
    }

    public void resetDisplayed() {
        this.entity = null;
    }

    public void render(DrawContext context, int x, int y, float deltaTicks) {
        if(this.entity == null) {
            return;
        }

        //8 extra height for text, 2 for padding
        TooltipBackgroundRenderer.render(context, x, y, PREVIEW_SIZE, PREVIEW_SIZE + 10, null);
        this.rotation.rotateLocalY(ROTATION_CONSTANT * deltaTicks);

        EntityRenderManager entityRenderManager = MinecraftClient.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super Entity, ?> entityRenderer = entityRenderManager.getRenderer(this.entity);
        EntityRenderState entityRenderState = entityRenderer.getAndUpdateRenderState(this.entity, 1.0F);
        entityRenderState.light = 15728880;
        entityRenderState.hitbox = null;
        entityRenderState.shadowPieces.clear();
        entityRenderState.outlineColor = 0;
        context.addEntity(entityRenderState, this.scale, new Vector3f().add(0, 1, 0), this.rotation, new Quaternionf(), x, y, x + PREVIEW_SIZE, y + PREVIEW_SIZE);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        context.drawText(textRenderer, this.tooltip, x + (PREVIEW_SIZE - textRenderer.getWidth(this.tooltip))/2 , y + PREVIEW_SIZE + 2, -1, true);
    }
}
