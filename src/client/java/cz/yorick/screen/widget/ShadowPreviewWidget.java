package cz.yorick.screen.widget;

import cz.yorick.NecromancersShadow;
import cz.yorick.NecromancersShadowClient;
import cz.yorick.data.DataAttachments;
import cz.yorick.data.ShadowData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;

public class ShadowPreviewWidget extends AbstractWidget {
    public static final String HELP_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".preview_help";
    public static final String TYPE_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".type";
    public static final String SUMMON_COST_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".summon_cost";
    public static final String HEALTH_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".health";
    public static final String DAMAGE_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".damage";
    private static final double ROTATION_CONSTANT = 0.05;
    private static final float SCALING_CONSTANT = 2;
    private static final float MIN_SCALE = 1F;
    private static final float DEFAULT_SCALE = 16F;
    private static final float MAX_SCALE = 64F;
    private int previewedId = -1;
    //a fake entity used for rendering, does not actually exist in the world
    private Entity entity = null;
    private Quaternionf rotation;
    private float scale = 16.0F;
    private final ArrayList<Component> aboutText = new ArrayList<>();
    //position gets fixed by the grid widget wrapper layout
    public ShadowPreviewWidget(int width, int height) {
        super(0, 0, width, height, Component.empty());
        clearPreview();
    }

    public void clearPreview() {
        setPreviewed(-1, null, null);
    }

    public boolean isActive() {
        return this.entity != null;
    }

    public void onSwap(int from, int to) {
        if(this.previewedId == from) {
            this.previewedId = to;
        } else if (this.previewedId == to) {
            this.previewedId = from;
        }
    }

    //TODO figure out why the dataTracker data do not get synced
    public void setPreviewed(int id, ShadowData shadowData, Level world) {
        //clearing the entity stops everything from rendering
        this.aboutText.clear();
        if(shadowData == null) {
            this.entity = null;
            this.previewedId = 1;
            NecromancersShadowClient.decodeMultiline(HELP_TRANSLATION_KEY, this.aboutText::add);
            return;
        }

        this.rotation = new Quaternionf().rotateZ(3.1415927F).rotateY(2.6F);
        this.previewedId = id;
        this.entity = shadowData.entityType().create(world, EntitySpawnReason.TRIGGERED);
        ValueInput readView = TagValueInput.create(NecromancersShadow.ERROR_REPORTER, Minecraft.getInstance().player.registryAccess(), shadowData.nbt());
        this.entity.load(readView);
        //attributes are blocked using world.isClient() check, read manually
        if(this.entity instanceof LivingEntity livingEntity) {
            readView.read("attributes", AttributeInstance.Packed.LIST_CODEC).ifPresent(packedAttributes -> livingEntity.getAttributes().apply(packedAttributes));
        }
        //render the entity normally
        DataAttachments.markAsShadow(this.entity, false);

        //try to autoscale it so it looks decent (leave a bit of space)
        float widthScale = (this.getWidth() - 10)/this.entity.getBbWidth();
        float heightScale = (this.getHeight() - 10)/this.entity.getBbHeight();
        this.scale = Math.min(widthScale, heightScale);

        this.aboutText.add(this.entity.getName());
        this.aboutText.add(Component.literal(""));
        this.aboutText.add(Component.translatable(TYPE_TRANSLATION_KEY).append(shadowData.typeName()));
        this.aboutText.add(Component.translatable(SUMMON_COST_TRANSLATION_KEY).append(shadowData.costText()));
        if(this.entity instanceof LivingEntity livingEntity) {
            this.aboutText.add(Component.translatable(HEALTH_TRANSLATION_KEY).append(Component.literal(NecromancersShadow.DECIMAL_FORMAT.format(livingEntity.getMaxHealth())).withStyle(ChatFormatting.RED)));
            if(livingEntity.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
                this.aboutText.add(Component.translatable(DAMAGE_TRANSLATION_KEY).append(Component.literal(NecromancersShadow.DECIMAL_FORMAT.format(livingEntity.getAttributeValue(Attributes.ATTACK_DAMAGE))).withStyle(ChatFormatting.GREEN)));
            }
        }
    }

    public int getPreviewedId() {
        return this.previewedId;
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        if(this.entity != null) {
            EntityRenderDispatcher entityRenderManager = Minecraft.getInstance().getEntityRenderDispatcher();
            EntityRenderer<? super Entity, ?> entityRenderer = entityRenderManager.getRenderer(this.entity);
            EntityRenderState entityRenderState = entityRenderer.createRenderState(this.entity, 1.0F);
            entityRenderState.lightCoords = 15728880;
            entityRenderState.shadowPieces.clear();
            entityRenderState.outlineColor = 0;
            context.submitEntityRenderState(entityRenderState, this.scale, new Vector3f().add(0, 2.2F * (DEFAULT_SCALE / this.scale), 0), this.rotation, new Quaternionf(), this.getX(), this.getY(), this.getRight(), this.getBottom());
        }

        for (int i = 0; i < this.aboutText.size(); i++) {
            context.drawString(Minecraft.getInstance().font, this.aboutText.get(i), this.getRight() + 4, this.getY() + 10 + (8 * i), -12566464, false);
        }
    }

    //don't play a sound when clicking
    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    protected void onDrag(MouseButtonEvent click, double offsetX, double offsetY) {
        this.rotation.rotateLocalY((float) (-offsetX * ROTATION_CONSTANT));
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
    protected void updateWidgetNarration(NarrationElementOutput builder) {
    }
}
