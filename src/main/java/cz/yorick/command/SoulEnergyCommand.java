package cz.yorick.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import cz.yorick.NecromancersShadow;
import cz.yorick.data.DataAttachments;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

public class SoulEnergyCommand {
    public static final String MODIFIED_TRANSLATION_KEY = "command." + NecromancersShadow.MOD_ID + ".soul_energy_modified";
    public static final String GET_ENERGY_TRANSLATION_KEY = "command." + NecromancersShadow.MOD_ID + ".soul_energy_get";
    public static final String GET_MAX_ENERGY_TRANSLATION_KEY = "command." + NecromancersShadow.MOD_ID + ".max_soul_energy_get";
    private static final Permission LEVEL_2_PERM = new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS);
    public static void init(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("soulEnergy").requires(source -> source.permissions().hasPermission(LEVEL_2_PERM))
                .then(Commands.literal("get")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.literal("amount").executes(context -> executeGet(context, Type.AMOUNT)))
                                .then(Commands.literal("max").executes(context -> executeGet(context, Type.MAX)))
                        )
                ).then(Commands.literal("set")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.literal("amount").then(amount().executes(context -> execute(context, Type.AMOUNT, Operation.SET))))
                                .then(Commands.literal("max").then(amount().executes(context -> execute(context, Type.MAX, Operation.SET))))
                        )
                ).then(Commands.literal("add")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.literal("amount").then(amount().executes(context -> execute(context, Type.AMOUNT, Operation.ADD))))
                                .then(Commands.literal("max").then(amount().executes(context -> execute(context, Type.MAX, Operation.ADD))))
                        )
                ).then(Commands.literal("remove")
                        .then(Commands.argument("players", EntityArgument.players())
                                .then(Commands.literal("amount").then(amount().executes(context -> execute(context, Type.AMOUNT, Operation.REMOVE))))
                                .then(Commands.literal("max").then(amount().executes(context -> execute(context, Type.MAX, Operation.REMOVE))))
                        )
                )
        );
    }

    private static RequiredArgumentBuilder<CommandSourceStack, ?> amount() {
        return Commands.argument("amount", DoubleArgumentType.doubleArg(0, Integer.MAX_VALUE));
    }

    private static int execute(CommandContext<CommandSourceStack> context, Type type, Operation operation) throws CommandSyntaxException {
        return execute(context.getSource(), EntityArgument.getPlayers(context, "players"), type, operation, DoubleArgumentType.getDouble(context, "amount"));
    }

    private static int execute(CommandSourceStack source, Collection<ServerPlayer> targets, Type type, Operation operation, double amount) {
        for (ServerPlayer target : targets) {
            type.apply(target, operation, amount);
        }

        source.sendSystemMessage(Component.translatable(MODIFIED_TRANSLATION_KEY, targets.size()).withStyle(ChatFormatting.GREEN));
        return 1;
    }

    private static int executeGet(CommandContext<CommandSourceStack> context, Type type) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        double amount = type.getter.apply(player);
        context.getSource().sendSystemMessage(Component.translatable(type == Type.MAX ? GET_MAX_ENERGY_TRANSLATION_KEY : GET_ENERGY_TRANSLATION_KEY, player.getDisplayName(), amount));
        return (int)Math.round(amount);
    }

    private enum Operation {
        ADD(Double::sum),
        SET((original, amount) -> amount),
        REMOVE((original, amount) -> original - amount);
        private final BiFunction<Double, Double, Double> function;
        Operation(BiFunction<Double, Double, Double> function) {
            this.function = function;
        }
    }

    private enum Type {
        AMOUNT(DataAttachments::getSoulEnergy, DataAttachments::setSoulEnergy),
        MAX(player -> (double)DataAttachments.getMaxSoulEnergy(player), (player, amount) -> DataAttachments.setMaxSoulEnergy(player, (int)Math.round(amount)));
        private final Function<ServerPlayer, Double> getter;
        private final BiConsumer<ServerPlayer, Double> setter;
        Type(Function<ServerPlayer, Double> getter, BiConsumer<ServerPlayer, Double> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        public void apply(ServerPlayer player, Operation operation, double amount) {
            this.setter.accept(player, operation.function.apply(this.getter.apply(player), amount));
        }
    }
}
