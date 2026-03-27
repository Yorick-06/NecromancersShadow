package cz.yorick.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.yorick.NecromancersShadow;
import cz.yorick.imixin.IMobEntityMixin;
import cz.yorick.imixin.IServerPlayerEntityMixin;
import cz.yorick.util.Util;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

public record ShadowData(EntityType<?> entityType, int cost, CompoundTag nbt) {
    public static final Codec<ShadowData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(ShadowData::entityType),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("cost").forGetter(data -> Optional.of(data.cost)),
            CompoundTag.CODEC.optionalFieldOf("data", new CompoundTag()).forGetter(shadow -> shadow.nbt)
    ).apply(instance, (type, cost, data) -> new ShadowData(type, cost.orElse(Util.getAttribute(type, Attributes.MAX_HEALTH)), data)));

    public static final Codec<ShadowData> SYNC_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(ShadowData::entityType),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("cost").forGetter(data -> Optional.of(data.cost))
    ).apply(instance, (type, cost) -> new ShadowData(type, cost.orElse(Util.getAttribute(type, Attributes.MAX_HEALTH)), new CompoundTag())));

    @Override
    public CompoundTag nbt() {
        return this.nbt.copy();
    }

    public static ShadowData fromEntity(Mob entity) {
        TagValueOutput entityNbt = TagValueOutput.createWithContext(NecromancersShadow.ERROR_REPORTER, entity.registryAccess());
        entity.saveWithoutId(entityNbt);
        //do not save some data
        entityNbt.discard("Pos");
        entityNbt.discard("Motion");
        entityNbt.discard("Rotation");
        entityNbt.discard("fall_distance");
        entityNbt.discard("Fire");
        entityNbt.discard("Air");
        entityNbt.discard("OnGround");
        entityNbt.discard("PortalCooldown");
        //do not want any duplicate UUID trouble
        entityNbt.discard("UUID");

        entityNbt.discard("TicksFrozen");
        entityNbt.discard("HasVisualFire");
        entityNbt.discard("Passengers");

        //makes the entity red
        entityNbt.discard("HurtTime");
        entityNbt.discard("DeathTime");

        //do not store the health, this way it gets set to the max every time the entity spawns
        entityNbt.discard("Health");
        return new ShadowData(entity.getType(), Math.round(entity.getMaxHealth()), entityNbt.buildResult());
    }

    public void applyTo(Mob vessel, ServerPlayer owner) {
        //a trick to not change the spawned entities position
        Vec3 originalPos = vessel.position();
        vessel.load(TagValueInput.create(NecromancersShadow.ERROR_REPORTER, vessel.registryAccess(), this.nbt.copy()));
        vessel.setPos(originalPos);

        ((IMobEntityMixin)vessel).necromancers_shadow$setShadow(new Instance(owner));
        DataAttachments.markAsShadow(vessel, true);
    }

    public Component typeName() {
        return this.entityType.getDescription();
    }

    public Component costText() {
        return Component.literal(String.valueOf(this.cost)).withStyle(ChatFormatting.AQUA);
    }

    public Component asText() {
        return typeName().copy().withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": [").withStyle(ChatFormatting.GRAY))
                .append(costText())
                .append(Component.literal("]").withStyle(ChatFormatting.GRAY));
    }

    public record Instance(ServerPlayer owner) {
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
