package com.example.evokerdrop.command;

import com.example.evokerdrop.EvokerDropsMod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ReloadCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("evokerdrops")
                        .then(CommandManager.literal("reload")
                                .requires(source -> source.hasPermissionLevel(2))
                                .executes(ReloadCommand::executeReload)
                        )
        );
    }

    private static int executeReload(CommandContext<ServerCommandSource> context) {
        try {
            int mobCount = EvokerDropsMod.getConfig().mobConfigs.size();
            EvokerDropsMod.reloadConfig();
            int newMobCount = EvokerDropsMod.getConfig().mobConfigs.size();

            context.getSource().sendFeedback(
                    () -> Text.literal("§a[Evoker Drops] Configuration reloaded successfully!"),
                    true
            );
            context.getSource().sendFeedback(
                    () -> Text.literal("§7Loaded " + newMobCount + " mob configuration(s)"),
                    false
            );

            if (newMobCount != mobCount) {
                context.getSource().sendFeedback(
                        () -> Text.literal("§eNote: Mob count changed from " + mobCount + " to " + newMobCount),
                        false
                );
            }

            return 1;
        } catch (Exception e) {
            context.getSource().sendError(
                    Text.literal("§c[Evoker Drops] Failed to reload configuration: " + e.getMessage())
            );
            EvokerDropsMod.LOGGER.error("Failed to reload config via command", e);
            return 0;
        }
    }
}