package cz.yorick.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.yorick.NecromancersShadow;
import cz.yorick.imixin.IMobEntityMixin;
import cz.yorick.imixin.IServerPlayerEntityMixin;
import cz.yorick.util.Util;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public record ShadowData(EntityType<?> entityType, int cost, NbtCompound nbt) {
    public static final Codec<ShadowData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.ENTITY_TYPE.getCodec().fieldOf("type").forGetter(ShadowData::entityType),
            Codecs.NON_NEGATIVE_INT.optionalFieldOf("cost").forGetter(data -> Optional.of(data.cost)),
            NbtCompound.CODEC.optionalFieldOf("data", new NbtCompound()).forGetter(shadow -> shadow.nbt)
    ).apply(instance, (type, cost, data) -> new ShadowData(type, cost.orElse(Util.getAttribute(type, EntityAttributes.MAX_HEALTH)), data)));

    public static final Codec<ShadowData> SYNC_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.ENTITY_TYPE.getCodec().fieldOf("type").forGetter(ShadowData::entityType),
            Codecs.NON_NEGATIVE_INT.optionalFieldOf("cost").forGetter(data -> Optional.of(data.cost))
    ).apply(instance, (type, cost) -> new ShadowData(type, cost.orElse(Util.getAttribute(type, EntityAttributes.MAX_HEALTH)), new NbtCompound())));

    @Override
    public NbtCompound nbt() {
        return this.nbt.copy();
    }

    public static ShadowData fromEntity(MobEntity entity) {
        NbtWriteView entityNbt = NbtWriteView.create(NecromancersShadow.ERROR_REPORTER, entity.getRegistryManager());
        entity.writeData(entityNbt);
        //do not save some data
        entityNbt.remove("Pos");
        entityNbt.remove("Motion");
        entityNbt.remove("Rotation");
        entityNbt.remove("fall_distance");
        entityNbt.remove("Fire");
        entityNbt.remove("Air");
        entityNbt.remove("OnGround");
        entityNbt.remove("PortalCooldown");
        //do not want any duplicate UUID trouble
        entityNbt.remove("UUID");

        entityNbt.remove("TicksFrozen");
        entityNbt.remove("HasVisualFire");
        entityNbt.remove("Passengers");

        //makes the entity red
        entityNbt.remove("HurtTime");
        entityNbt.remove("DeathTime");

        //do not store the health, this way it gets set to the max every time the entity spawns
        entityNbt.remove("Health");
        return new ShadowData(entity.getType(), Math.round(entity.getMaxHealth()), entityNbt.getNbt());
    }

    public void applyTo(MobEntity vessel, ServerPlayerEntity owner) {
        //a trick to not change the spawned entities position
        Vec3d originalPos = vessel.getEntityPos();
        vessel.readData(NbtReadView.create(NecromancersShadow.ERROR_REPORTER, vessel.getRegistryManager(), this.nbt.copy()));
        vessel.setPosition(originalPos);

        ((IMobEntityMixin)vessel).necromancers_shadow$setShadow(new Instance(owner));
        DataAttachments.markAsShadow(vessel, true);
    }

    public Text typeName() {
        return this.entityType.getName();
    }

    public Text costText() {
        return Text.literal(String.valueOf(this.cost)).formatted(Formatting.AQUA);
    }

    public Text asText() {
        return typeName().copy().formatted(Formatting.GRAY)
                .append(Text.literal(": [").formatted(Formatting.GRAY))
                .append(costText())
                .append(Text.literal("]").formatted(Formatting.GRAY));
    }

    public record Instance(ServerPlayerEntity owner) {
        public LivingEntity getTarget() {
            return ((IServerPlayerEntityMixin)this.owner).necromancers_shadow$getTarget();
        }
    }

    public String simpleToString() {
        return "ShadowData{" +
                "entityType=" + this.entityType +
                ", cost=" + this.cost +
                '}';
    }
}
