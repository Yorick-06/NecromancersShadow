package cz.yorick.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import cz.yorick.data.NecromancerData;
import cz.yorick.util.Util;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ExperienceOrbEntity.class)
public class ExperienceOrbEntityMixin {
    @Unique
    private static final double XP_MULTIPLIER = 30D;

    @WrapMethod(method = "repairPlayerGears")
    private int necromancers_shadow$repairPlayerGears(ServerPlayerEntity player, int amount, Operation<Integer> original) {
        //if the player is holding a totem in either hand
        if(Util.isHoldingTotem(player)) {
            double soulEnergy = NecromancerData.getEnergy(player);
            double maxSoulEnergy = NecromancerData.getMaxEnergy(player);

            double toAdd = Math.clamp(amount/XP_MULTIPLIER, 0, maxSoulEnergy - soulEnergy);
            NecromancerData.setEnergy(player, NecromancerData.getEnergy(player) + toAdd);

            double leftover = amount - toAdd;
            amount = (int)Math.round(leftover * XP_MULTIPLIER);
        }

        return original.call(player, amount);
    }
}
