package cz.yorick.data;

import com.mojang.serialization.Codec;
import cz.yorick.NecromancersShadow;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class DataAttachments {
    public static final String SOUL_ENERGY_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".soul_energy";
    private static final AttachmentType<Boolean> IS_SHADOW = AttachmentRegistry.create(Identifier.of(NecromancersShadow.MOD_ID, "is_shadow"),
            builder -> builder
                    .copyOnDeath()
                    .persistent(Codec.BOOL)
                    .syncWith(PacketCodecs.BOOLEAN, AttachmentSyncPredicate.all())
    );

    private static final AttachmentType<ImmutableShadowStorage> SHADOW_STORAGE = AttachmentRegistry.create(Identifier.of(NecromancersShadow.MOD_ID, "shadow_storage"),
            builder -> builder
                    .copyOnDeath()
                    .initializer(ImmutableShadowStorage::empty)
                    .persistent(ImmutableShadowStorage.CODEC)
                    .syncWith(PacketCodecs.registryCodec(ImmutableShadowStorage.SYNC_CODEC), AttachmentSyncPredicate.targetOnly())
    );

    private static final AttachmentType<ServerShadowManager> SHADOW_MANAGER = AttachmentRegistry.create(Identifier.of(NecromancersShadow.MOD_ID, "shadow_manager"));

    private static final AttachmentType<Double> SOUL_ENERGY = AttachmentRegistry.create(Identifier.of(NecromancersShadow.MOD_ID, "soul_energy"),
            builder -> builder
                    .copyOnDeath()
                    .initializer(() -> 0.0)
                    .persistent(Codec.DOUBLE)
                    .syncWith(PacketCodecs.DOUBLE, AttachmentSyncPredicate.targetOnly())
    );

    private static final AttachmentType<Integer> MAX_SOUL_ENERGY = AttachmentRegistry.create(Identifier.of(NecromancersShadow.MOD_ID, "max_soul_energy"),
            builder -> builder
                    .copyOnDeath()
                    .initializer(() -> 0)
                    .persistent(Codec.INT)
                    .syncWith(PacketCodecs.INTEGER, AttachmentSyncPredicate.targetOnly())
    );

    public static boolean isMarkedAsShadow(Entity entity) {
        return entity.hasAttached(IS_SHADOW);
    }

    public static void markAsShadow(Entity entity, boolean shadow) {
        if(shadow) {
            entity.setAttached(IS_SHADOW, true);
        } else {
            entity.removeAttached(IS_SHADOW);
        }
    }

    public static ImmutableShadowStorage getShadowStorage(PlayerEntity player) {
        return player.getAttachedOrCreate(SHADOW_STORAGE);
    }

    public static void mutateShadowStorage(ServerPlayerEntity player, Consumer<MutableShadowStorage> mutator) {
        MutableShadowStorage mutable = getShadowStorage(player).toMutable();
        mutator.accept(mutable);
        player.setAttached(SHADOW_STORAGE, mutable.toImmutable());
    }

    public static ServerShadowManager getShadowManager(ServerPlayerEntity player) {
        return player.getAttachedOrCreate(SHADOW_MANAGER, () -> new ServerShadowManager(player));
    }

    public static double getSoulEnergy(PlayerEntity player) {
        return player.getAttachedOrCreate(SOUL_ENERGY);
    }

    public static void setSoulEnergy(ServerPlayerEntity player, double amount) {
        player.setAttached(SOUL_ENERGY, amount);
    }

    public static void addSoulEnergy(ServerPlayerEntity player, double amount) {
        player.modifyAttached(SOUL_ENERGY, originalAmount -> originalAmount + amount);
    }

    public static void removeSoulEnergy(ServerPlayerEntity player, double amount) {
        addSoulEnergy(player, -amount);
    }

    public static int getMaxSoulEnergy(PlayerEntity player) {
        return player.getAttachedOrCreate(MAX_SOUL_ENERGY);
    }

    public static void setMaxSoulEnergy(ServerPlayerEntity player, int amount) {
        player.setAttached(MAX_SOUL_ENERGY, amount);
    }

    public static int getSoulEnergyItemBarStep() {
        return NecromancersShadow.LOCAL_PLAYER.get().map(player -> {
            double percentage = getSoulEnergy(player)/(double)getMaxSoulEnergy(player);
            //item bar has 13 steps
            return Math.clamp(Math.round(percentage * 13), 0, 13);
        }).orElse(0);
    }

    public static void appendEnergyTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
        NecromancersShadow.LOCAL_PLAYER.get().ifPresent(player -> {
            textConsumer.accept(Text.translatable(SOUL_ENERGY_TRANSLATION_KEY).formatted(Formatting.GRAY)
                    .append(Text.literal(NecromancersShadow.DECIMAL_FORMAT.format(getSoulEnergy(player))).formatted(Formatting.AQUA))
                    .append(Text.literal("/").formatted(Formatting.GRAY))
                    .append(Text.literal(String.valueOf(getMaxSoulEnergy(player))).formatted(Formatting.AQUA))
            );

            getShadowStorage(player).appendTooltip(context, textConsumer, type, components);
        });
    }

    public static void init() {
    }
}
