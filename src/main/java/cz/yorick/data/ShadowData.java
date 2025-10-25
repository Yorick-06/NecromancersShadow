package cz.yorick.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.yorick.NecromancersShadow;
import cz.yorick.imixin.IMobEntityMixin;
import cz.yorick.imixin.IServerPlayerEntityMixin;
import cz.yorick.mixin.EntityAccessor;
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
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.dynamic.Codecs;

import java.util.Optional;

public record ShadowData(EntityType<?> entityType, int cost, NbtCompound nbt) {
    public static final String STORED_SHADOWS_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".stored_shadows";
    public static final Codec<ShadowData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.ENTITY_TYPE.getCodec().fieldOf("type").forGetter(ShadowData::entityType),
            Codecs.NON_NEGATIVE_INT.optionalFieldOf("cost").forGetter(data -> Optional.of(data.cost)),
            NbtCompound.CODEC.optionalFieldOf("data", new NbtCompound()).forGetter(shadow -> shadow.nbt)
    ).apply(instance, (type, cost, data) -> new ShadowData(type, cost.orElse(Util.getAttribute(type, EntityAttributes.MAX_HEALTH)), new NbtCompound())));

    public static final Codec<ShadowData> SYNC_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.ENTITY_TYPE.getCodec().fieldOf("type").forGetter(ShadowData::entityType),
            Codecs.NON_NEGATIVE_INT.optionalFieldOf("cost").forGetter(data -> Optional.of(data.cost))
    ).apply(instance, (type, cost) -> new ShadowData(type, cost.orElse(Util.getAttribute(type, EntityAttributes.MAX_HEALTH)), new NbtCompound())));

    @Override
    public NbtCompound nbt() {
        return this.nbt.copy();
    }

    //TODO save some extra data (name, components) and dont save some (the thing making the entity red)
    public static ShadowData fromEntity(MobEntity entity) {
        NbtWriteView entityNbt = NbtWriteView.create(NecromancersShadow.ERROR_REPORTER, entity.getRegistryManager());
        ((EntityAccessor)entity).invokeWriteCustomData(entityNbt);
        //do not store the health, this way it gets set to the max every time the entity spawns
        entityNbt.remove("Health");
        return new ShadowData(entity.getType(), Math.round(entity.getMaxHealth()), entityNbt.getNbt());
    }

    public void applyTo(MobEntity vessel, ServerPlayerEntity owner) {
        ((EntityAccessor)vessel).invokeReadCustomData(NbtReadView.create(NecromancersShadow.ERROR_REPORTER, vessel.getRegistryManager(), this.nbt.copy()));
        ((IMobEntityMixin)vessel).necromancers_shadow$setShadow(new Instance(owner));
        DataAttachments.markAsShadow(vessel, true);
    }

    public MutableText asText() {
        return MutableText.of(this.entityType.getName().getContent()).formatted(Formatting.GRAY)
                .append(Text.literal(": [").formatted(Formatting.GRAY))
                .append(Text.literal(String.valueOf(this.cost)).formatted(Formatting.AQUA))
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
