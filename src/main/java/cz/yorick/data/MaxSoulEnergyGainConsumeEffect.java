package cz.yorick.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;

public record MaxSoulEnergyGainConsumeEffect(int soulEnergy) implements ConsumeEffect {
    private static final MapCodec<MaxSoulEnergyGainConsumeEffect> CODEC = Codec.INT.fieldOf("energy").xmap(MaxSoulEnergyGainConsumeEffect::new, MaxSoulEnergyGainConsumeEffect::soulEnergy);
    public static final Type<MaxSoulEnergyGainConsumeEffect> TYPE = new Type<>(CODEC, ByteBufCodecs.fromCodecWithRegistries(CODEC.codec()));

    @Override
    public Type<MaxSoulEnergyGainConsumeEffect> getType() {
        return TYPE;
    }

    @Override
    public boolean apply(Level world, ItemStack stack, LivingEntity user) {
        if(user instanceof ServerPlayer player) {
            DataAttachments.setMaxSoulEnergy(player, DataAttachments.getMaxSoulEnergy(player) + this.soulEnergy);
            return true;
        }
        return false;
    }
}
