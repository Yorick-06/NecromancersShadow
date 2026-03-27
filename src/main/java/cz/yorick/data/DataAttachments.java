package cz.yorick.data;

import com.mojang.serialization.Codec;
import cz.yorick.NecromancersShadow;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import java.util.function.Consumer;

public class DataAttachments {
    public static final String SOUL_ENERGY_TRANSLATION_KEY = "tooltip." + NecromancersShadow.MOD_ID + ".soul_energy";
    private static final AttachmentType<Boolean> IS_SHADOW = AttachmentRegistry.create(Identifier.fromNamespaceAndPath(NecromancersShadow.MOD_ID, "is_shadow"),
            builder -> builder
                    .copyOnDeath()
                    .persistent(Codec.BOOL)
                    .syncWith(ByteBufCodecs.BOOL, AttachmentSyncPredicate.all())
    );

    private static final AttachmentType<ImmutableShadowStorage> SHADOW_STORAGE = AttachmentRegistry.create(Identifier.fromNamespaceAndPath(NecromancersShadow.MOD_ID, "shadow_storage"),
            builder -> builder
                    .copyOnDeath()
                    .initializer(ImmutableShadowStorage::empty)
                    .persistent(ImmutableShadowStorage.CODEC)
                    .syncWith(ByteBufCodecs.fromCodecWithRegistries(ImmutableShadowStorage.SYNC_CODEC), AttachmentSyncPredicate.targetOnly())
    );

    private static final AttachmentType<ServerShadowManager> SHADOW_MANAGER = AttachmentRegistry.create(Identifier.fromNamespaceAndPath(NecromancersShadow.MOD_ID, "shadow_manager"));

    private static final AttachmentType<Double> SOUL_ENERGY = AttachmentRegistry.create(Identifier.fromNamespaceAndPath(NecromancersShadow.MOD_ID, "soul_energy"),
            builder -> builder
                    .copyOnDeath()
                    .initializer(() -> 0.0)
                    .persistent(Codec.DOUBLE)
                    .syncWith(ByteBufCodecs.DOUBLE, AttachmentSyncPredicate.targetOnly())
    );

    private static final AttachmentType<Integer> MAX_SOUL_ENERGY = AttachmentRegistry.create(Identifier.fromNamespaceAndPath(NecromancersShadow.MOD_ID, "max_soul_energy"),
            builder -> builder
                    .copyOnDeath()
                    .initializer(() -> 0)
                    .persistent(Codec.INT)
                    .syncWith(ByteBufCodecs.INT, AttachmentSyncPredicate.targetOnly())
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

    public static ImmutableShadowStorage getShadowStorage(Player player) {
        return player.getAttachedOrCreate(SHADOW_STORAGE);
    }

    public static void mutateShadowStorage(ServerPlayer player, Consumer<MutableShadowStorage> mutator) {
        MutableShadowStorage mutable = getShadowStorage(player).toMutable();
        mutator.accept(mutable);
        player.setAttached(SHADOW_STORAGE, mutable.toImmutable());
    }

    public static ServerShadowManager getShadowManager(ServerPlayer player) {
        return player.getAttachedOrCreate(SHADOW_MANAGER, () -> new ServerShadowManager(player));
    }

    public static double getSoulEnergy(Player player) {
        return player.getAttachedOrCreate(SOUL_ENERGY);
    }

    public static void setSoulEnergy(ServerPlayer player, double amount) {
        player.setAttached(SOUL_ENERGY, Math.clamp(amount, 0, getMaxSoulEnergy(player)));
    }

    public static void addSoulEnergy(ServerPlayer player, double amount) {
        setSoulEnergy(player, getSoulEnergy(player) + amount);
    }

    public static void removeSoulEnergy(ServerPlayer player, double amount) {
        addSoulEnergy(player, -amount);
    }

    public static int getMaxSoulEnergy(Player player) {
        return player.getAttachedOrCreate(MAX_SOUL_ENERGY);
    }

    public static void setMaxSoulEnergy(ServerPlayer player, int amount) {
        double currentEnergy = getMaxSoulEnergy(player);
        if(amount < currentEnergy) {
            setSoulEnergy(player, amount);
        }

        player.setAttached(MAX_SOUL_ENERGY, amount);
    }

    public static int getSoulEnergyItemBarStep() {
        return NecromancersShadow.LOCAL_PLAYER.get().map(player -> {
            double percentage = getSoulEnergy(player)/(double)getMaxSoulEnergy(player);
            //item bar has 13 steps
            return Math.clamp(Math.round(percentage * 13), 0, 13);
        }).orElse(0);
    }

    public static void appendEnergyTooltip(Item.TooltipContext context, Consumer<Component> textConsumer, TooltipFlag type, DataComponentGetter components) {
        NecromancersShadow.LOCAL_PLAYER.get().ifPresent(player -> {
            textConsumer.accept(Component.translatable(SOUL_ENERGY_TRANSLATION_KEY).withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(NecromancersShadow.DECIMAL_FORMAT.format(getSoulEnergy(player))).withStyle(ChatFormatting.AQUA))
                    .append(Component.literal("/").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(String.valueOf(getMaxSoulEnergy(player))).withStyle(ChatFormatting.AQUA))
            );

            getShadowStorage(player).addToTooltip(context, textConsumer, type, components);
        });
    }

    public static void init() {
    }
}
