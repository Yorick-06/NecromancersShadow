package cz.yorick.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import cz.yorick.NecromancersShadow;
import cz.yorick.imixin.IMobEntityMixin;
import cz.yorick.imixin.IServerPlayerEntityMixin;
import cz.yorick.mixin.EntityAccessor;
import cz.yorick.util.Util;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipAppender;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ShadowData implements TooltipAppender {
    public static final String STORED_SHADOWS_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".stored_shadows";
    private static final MapCodec<ShadowData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Registries.ENTITY_TYPE.getCodec().fieldOf("type").forGetter(ShadowData::getEntityType),
            Codecs.NON_NEGATIVE_INT.optionalFieldOf("energyCost").forGetter(data -> Optional.of(data.energyCost))
    ).apply(instance, (type, health) -> new ShadowData(type, health.orElse(Util.getAttribute(type, EntityAttributes.MAX_HEALTH)), new NbtCompound())));

    public static final Codec<ShadowData> SYNC_CODEC = MAP_CODEC.codec();

    //just adds nbt to the previous codec
    public static final Codec<ShadowData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MAP_CODEC.forGetter(data -> data),
            NbtCompound.CODEC.optionalFieldOf("data", new NbtCompound()).forGetter(shadow -> shadow.nbt)
    ).apply(instance, (data, nbt) -> {
        data.nbt = nbt;
        return data;
    }));

    private EntityType<?> entityType;
    private int energyCost;
    private NbtCompound nbt;
    private ShadowData(EntityType<?> entityType, int energyCost, NbtCompound nbt) {
        this.entityType = entityType;
        this.energyCost = energyCost;
        this.nbt = nbt;
    }

    public ShadowData(MobEntity entity) {
        updateFrom(entity);
    }

    public EntityType<?> getEntityType() {
        return this.entityType;
    }

    public int getEnergyCost() {
        return this.energyCost;
    }

    public void onSpawn(MobEntity vessel, ServerPlayerEntity owner) {
        ((EntityAccessor)vessel).invokeReadCustomData(NbtReadView.create(NecromancersShadow.ERROR_REPORTER, vessel.getRegistryManager(), this.nbt.copy()));
        ((IMobEntityMixin)vessel).necromancers_shadow$setShadow(new Instance(this, owner));
        NecromancyAttachments.markAsShadow(vessel, true);
    }

    public void updateFrom(MobEntity vessel) {
        NbtWriteView newNbt = NbtWriteView.create(NecromancersShadow.ERROR_REPORTER, vessel.getRegistryManager());
        ((EntityAccessor)vessel).invokeWriteCustomData(newNbt);
        //do not store the health, this way it gets set to the max every time the entity spawns
        newNbt.remove("Health");

        this.entityType = vessel.getType();
        this.energyCost = Math.round(vessel.getMaxHealth());
        this.nbt = newNbt.getNbt();
    }

    @Override
    public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        textConsumer.accept(
                MutableText.of(this.entityType.getName().getContent()).formatted(Formatting.GRAY)
                        .append(Text.literal(": [").formatted(Formatting.GRAY))
                                .append(Text.literal(String.valueOf(this.energyCost)).formatted(Formatting.AQUA))
                                        .append(Text.literal("]").formatted(Formatting.GRAY))
        );
    }

    public static ShadowData empty() {
        return new ShadowData(Registries.ENTITY_TYPE.get((Identifier)null), 0, new NbtCompound());
    }

    public static void appendShadowsTooltip(ComponentsAccess components, Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, List<ShadowData> storedShadows) {
        textConsumer.accept(Text.translatable(STORED_SHADOWS_TRANSLATION_KEY));
        storedShadows.forEach(shadow -> shadow.appendTooltip(context, textConsumer, type, components));
    }

    public record Instance(ShadowData shadow, ServerPlayerEntity owner) {
        public LivingEntity getTarget() {
            return ((IServerPlayerEntityMixin)this.owner).necromancers_shadow$getTarget();
        }
    }
}
