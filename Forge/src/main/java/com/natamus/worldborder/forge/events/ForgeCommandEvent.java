package com.natamus.worldborder.forge.events;

import com.mojang.brigadier.CommandDispatcher;
import com.natamus.collective.config.DuskConfig;
import com.natamus.collective.functions.DataFunctions;
import com.natamus.worldborder.util.Reference;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.nio.file.Path;

public class ForgeCommandEvent {
	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		dispatcher.register(
			Commands.literal("worldborder")
				.then(
					Commands.literal("reload")
						.requires(source -> source.hasPermission(2))
						.executes(context -> reloadConfig(context.getSource()))
				)
		);
	}

	private static int reloadConfig(CommandSourceStack source) {
		try {
			Path configPath = DataFunctions.getConfigDirectoryPath().resolve(Reference.MOD_ID + ".json5");
			DuskConfig.loadJson(configPath, Reference.MOD_ID);
			DuskConfig.write(Reference.MOD_ID);
			ForgeVisibleBorderSyncEvent.syncNowToAll();
			source.sendSuccess(() -> Component.literal("WorldBorder config reloaded."), true);
			return 1;
		}
		catch (Exception e) {
			source.sendFailure(Component.literal("Failed to reload WorldBorder config. Check server log for details."));
			e.printStackTrace();
			return 0;
		}
	}
}
