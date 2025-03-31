package cz.yorick.entity;

import cz.yorick.NecromancersShadow;
import cz.yorick.data.NecromancerData;
import cz.yorick.data.ShadowData;
import cz.yorick.item.SculkEmeraldItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class SoulEntity extends Entity {
    private static final TrackedDataHandler<ShadowData> ENTITY_TYPE_HANDLER;
    private static final TrackedData<ShadowData> SHADOW;
    static {
        ENTITY_TYPE_HANDLER = TrackedDataHandler.create(PacketCodecs.registryCodec(ShadowData.SYNC_CODEC));
        TrackedDataHandlerRegistry.register(ENTITY_TYPE_HANDLER);
        SHADOW = DataTracker.registerData(SoulEntity.class, ENTITY_TYPE_HANDLER);
    }
    private static final String SOUL_KEY = "soul";
    public SoulEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(SHADOW, ShadowData.empty());
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected Box calculateDefaultBoundingBox(Vec3d pos) {
        return this.getDimensions(null).getBoxAt(pos);
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        if(!nbt.contains(SOUL_KEY)) {
            return;
        }

        ShadowData.CODEC.parse(NbtOps.INSTANCE, nbt.get(SOUL_KEY)).ifSuccess(shadow ->
                this.dataTracker.set(SHADOW, shadow)
        );
        this.lifespan = nbt.getInt("lifespan", 2400);
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        ShadowData.CODEC.encodeStart(NbtOps.INSTANCE, this.dataTracker.get(SHADOW)).ifSuccess(shadow ->
                nbt.put(SOUL_KEY, shadow)
        );
        nbt.putInt("lifespan", 0);
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        if(player instanceof ServerPlayerEntity serverPlayerEntity) {
            ItemStack heldStack = player.getStackInHand(hand);
            if(heldStack.get(NecromancersShadow.SHADOW_DATA_COMPONENT) != null) {
                List<ShadowData> storedShadows = SculkEmeraldItem.getMutableShadowData(heldStack);
                storedShadows.add(this.dataTracker.get(SHADOW));
                heldStack.set(NecromancersShadow.SHADOW_DATA_COMPONENT, storedShadows);
            } else {
                NecromancerData.addShadow(serverPlayerEntity, this.dataTracker.get(SHADOW));
            }

            this.remove(RemovalReason.KILLED);
            return ActionResult.SUCCESS_SERVER;
        }

        //cancels processing, sends packet to server
        return ActionResult.CONSUME;
    }

    public void setShadow(ShadowData shadow) {
        this.dataTracker.set(SHADOW, shadow);
    }

    public ShadowData getEntityType() {
        return this.dataTracker.get(SHADOW);
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

        if(getWorld() instanceof ServerWorld serverWorld && this.age % 20 == 0) {
            serverWorld.spawnParticles(ParticleTypes.SCULK_SOUL, this.getX(), this.getY() + 0.5, this.getZ(), 1, 0, 0, 0, 0);
        }
    }
}
