package cz.yorick.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import cz.yorick.NecromancersShadow;
import cz.yorick.data.DataAttachments;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SoulEnergyCommand {
    public static final String MODIFIED_TRANSLATION_KEY = "command." + NecromancersShadow.MOD_ID + ".soul_energy_modified";
    public static final String GET_ENERGY_TRANSLATION_KEY = "command." + NecromancersShadow.MOD_ID + ".soul_energy_get";
    public static final String GET_MAX_ENERGY_TRANSLATION_KEY = "command." + NecromancersShadow.MOD_ID + ".max_soul_energy_get";
    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("soulEnergy").requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("get")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .then(CommandManager.literal("amount").executes(context -> executeGet(context, Type.AMOUNT)))
                                .then(CommandManager.literal("max").executes(context -> executeGet(context, Type.MAX)))
                        )
                ).then(CommandManager.literal("set")
                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                .then(CommandManager.literal("amount").then(amount().executes(context -> execute(context, Type.AMOUNT, Operation.SET))))
                                .then(CommandManager.literal("max").then(amount().executes(context -> execute(context, Type.MAX, Operation.SET))))
                        )
                ).then(CommandManager.literal("add")
                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                .then(CommandManager.literal("amount").then(amount().executes(context -> execute(context, Type.AMOUNT, Operation.ADD))))
                                .then(CommandManager.literal("max").then(amount().executes(context -> execute(context, Type.MAX, Operation.ADD))))
                        )
                ).then(CommandManager.literal("remove")
                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                .then(CommandManager.literal("amount").then(amount().executes(context -> execute(context, Type.AMOUNT, Operation.REMOVE))))
                                .then(CommandManager.literal("max").then(amount().executes(context -> execute(context, Type.MAX, Operation.REMOVE))))
                        )
                )
        );
    }

    private static RequiredArgumentBuilder<ServerCommandSource, ?> amount() {
        return CommandManager.argument("amount", DoubleArgumentType.doubleArg(0, Integer.MAX_VALUE));
    }

    private static int execute(CommandContext<ServerCommandSource> context, Type type, Operation operation) throws CommandSyntaxException {
        return execute(context.getSource(), EntityArgumentType.getPlayers(context, "players"), type, operation, DoubleArgumentType.getDouble(context, "amount"));
    }

    private static int execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Type type, Operation operation, double amount) {
        for (ServerPlayerEntity target : targets) {
            type.apply(target, operation, amount);
        }

        source.sendMessage(Text.translatable(MODIFIED_TRANSLATION_KEY, targets.size()).formatted(Formatting.GREEN));
        return 1;
    }

    private static int executeGet(CommandContext<ServerCommandSource> context, Type type) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        double amount = type.getter.apply(player);
        context.getSource().sendMessage(Text.translatable(type == Type.MAX ? GET_MAX_ENERGY_TRANSLATION_KEY : GET_ENERGY_TRANSLATION_KEY, player.getDisplayName(), amount));
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
        private final Function<ServerPlayerEntity, Double> getter;
        private final BiConsumer<ServerPlayerEntity, Double> setter;
        Type(Function<ServerPlayerEntity, Double> getter, BiConsumer<ServerPlayerEntity, Double> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        public void apply(ServerPlayerEntity player, Operation operation, double amount) {
            this.setter.accept(player, operation.function.apply(this.getter.apply(player), amount));
        }
    }
}
