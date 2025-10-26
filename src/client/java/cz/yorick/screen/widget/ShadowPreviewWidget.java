package cz.yorick.screen.widget;

import cz.yorick.NecromancersShadow;
import cz.yorick.NecromancersShadowClient;
import cz.yorick.data.DataAttachments;
import cz.yorick.data.ShadowData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadView;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;

public class ShadowPreviewWidget extends ClickableWidget {
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
    private final ArrayList<Text> aboutText = new ArrayList<>();
    //position gets fixed by the grid widget wrapper layout
    public ShadowPreviewWidget(int width, int height) {
        super(0, 0, width, height, Text.empty());
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
    public void setPreviewed(int id, ShadowData shadowData, World world) {
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
        this.entity = shadowData.entityType().create(world, SpawnReason.TRIGGERED);
        ReadView readView = NbtReadView.create(NecromancersShadow.ERROR_REPORTER, MinecraftClient.getInstance().player.getRegistryManager(), shadowData.nbt());
        this.entity.readData(readView);
        //attributes are blocked using world.isClient() check, read manually
        if(this.entity instanceof LivingEntity livingEntity) {
            readView.read("attributes", EntityAttributeInstance.Packed.LIST_CODEC).ifPresent(packedAttributes -> livingEntity.getAttributes().unpack(packedAttributes));
        }
        //render the entity normally
        DataAttachments.markAsShadow(this.entity, false);

        //try to autoscale it so it looks decent (leave a bit of space)
        float widthScale = (this.getWidth() - 10)/this.entity.getWidth();
        float heightScale = (this.getHeight() - 10)/this.entity.getHeight();
        this.scale = Math.min(widthScale, heightScale);

        this.aboutText.add(this.entity.getName());
        this.aboutText.add(Text.literal(""));
        this.aboutText.add(Text.translatable(TYPE_TRANSLATION_KEY).append(shadowData.typeName()));
        this.aboutText.add(Text.translatable(SUMMON_COST_TRANSLATION_KEY).append(shadowData.costText()));
        if(this.entity instanceof LivingEntity livingEntity) {
            this.aboutText.add(Text.translatable(HEALTH_TRANSLATION_KEY).append(Text.literal(NecromancersShadow.DECIMAL_FORMAT.format(livingEntity.getMaxHealth())).formatted(Formatting.RED)));
            if(livingEntity.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE) != null) {
                this.aboutText.add(Text.translatable(DAMAGE_TRANSLATION_KEY).append(Text.literal(NecromancersShadow.DECIMAL_FORMAT.format(livingEntity.getAttributeValue(EntityAttributes.ATTACK_DAMAGE))).formatted(Formatting.GREEN)));
            }
        }
    }

    public int getPreviewedId() {
        return this.previewedId;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if(this.entity != null) {
            EntityRenderManager entityRenderManager = MinecraftClient.getInstance().getEntityRenderDispatcher();
            EntityRenderer<? super Entity, ?> entityRenderer = entityRenderManager.getRenderer(this.entity);
            EntityRenderState entityRenderState = entityRenderer.getAndUpdateRenderState(this.entity, 1.0F);
            entityRenderState.light = 15728880;
            entityRenderState.hitbox = null;
            entityRenderState.shadowPieces.clear();
            entityRenderState.outlineColor = 0;
            context.addEntity(entityRenderState, this.scale, new Vector3f().add(0, 2.2F * (DEFAULT_SCALE / this.scale), 0), this.rotation, new Quaternionf(), this.getX(), this.getY(), this.getRight(), this.getBottom());
        }

        for (int i = 0; i < this.aboutText.size(); i++) {
            context.drawText(MinecraftClient.getInstance().textRenderer, this.aboutText.get(i), this.getRight() + 4, this.getY() + 10 + (8 * i), -12566464, false);
        }
    }

    //don't play a sound when clicking
    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    protected void onDrag(Click click, double offsetX, double offsetY) {
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
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }
}
