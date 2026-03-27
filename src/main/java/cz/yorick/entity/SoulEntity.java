package cz.yorick.entity;

import cz.yorick.NecromancersShadow;
import cz.yorick.data.DataAttachments;
import cz.yorick.data.ImmutableShadowStorage;
import cz.yorick.data.ShadowData;
import cz.yorick.data.MutableShadowStorage;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SoulEntity extends Entity {
    private static final EntityDataSerializer<ShadowData> ENTITY_TYPE_HANDLER;
    private static final EntityDataAccessor<ShadowData> SHADOW;
    private static final String SOUL_KEY = "soul";
    static {
        ENTITY_TYPE_HANDLER = EntityDataSerializer.forValueType(ByteBufCodecs.fromCodecWithRegistries(ShadowData.SYNC_CODEC));
        FabricEntityDataRegistry.register(Identifier.fromNamespaceAndPath(NecromancersShadow.MOD_ID, SOUL_KEY), ENTITY_TYPE_HANDLER);
        SHADOW = SynchedEntityData.defineId(SoulEntity.class, ENTITY_TYPE_HANDLER);
    }

    public SoulEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SHADOW, new ShadowData(EntityType.PIG, 10, new CompoundTag()));
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected AABB makeBoundingBox(Vec3 pos) {
        return this.getDimensions(null).makeBoundingBox(pos);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput view) {
        view.read(SOUL_KEY, ShadowData.CODEC).ifPresent(shadowData -> this.entityData.set(SHADOW, shadowData));
        this.lifespan = view.getIntOr("lifespan", 2400);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {
        view.store(SOUL_KEY, ShadowData.CODEC, this.entityData.get(SHADOW));
        view.putInt("lifespan", this.lifespan);
    }


    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        if(player instanceof ServerPlayer serverPlayerEntity) {
            ItemStack heldStack = player.getItemInHand(hand);
            ImmutableShadowStorage itemStorage = heldStack.get(NecromancersShadow.SHADOW_STORAGE_COMPONENT);
            if(itemStorage != null) {
                MutableShadowStorage mutableStorage = itemStorage.toMutable();
                mutableStorage.addShadow(this.entityData.get(SHADOW));
                heldStack.set(NecromancersShadow.SHADOW_STORAGE_COMPONENT, mutableStorage.toImmutable());
            } else {
                DataAttachments.mutateShadowStorage(serverPlayerEntity, mutableStorage -> mutableStorage.addShadow(this.entityData.get(SHADOW)));
            }

            this.remove(RemovalReason.KILLED);
            return InteractionResult.SUCCESS_SERVER;
        }

        //cancels processing, sends packet to server
        return InteractionResult.CONSUME;
    }


    public void setShadow(ShadowData shadow) {
        this.entityData.set(SHADOW, shadow);
    }

    public ShadowData getEntityType() {
        return this.entityData.get(SHADOW);
    }

    private int lifespan = 2400;
    @Override
    public void tick() {
        super.tick();
        if(this.lifespan != -1) {
            this.lifespan--;
            if (this.lifespan <= 0) {
                this.remove(RemovalReason.DISCARDED);
            }
        }

        if(level() instanceof ServerLevel serverWorld && this.tickCount % 20 == 0) {
            serverWorld.sendParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY() + 0.5, this.getZ(), 1, 0, 0, 0, 0);
        }
    }

    //to run clinit and register the tracked data
    public static void registerTrackedData() {
    }
}
