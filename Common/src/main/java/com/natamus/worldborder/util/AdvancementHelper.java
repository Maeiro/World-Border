package com.natamus.worldborder.util;

import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class AdvancementHelper {
	private static final ResourceLocation LOOP_BORDER_ADVANCEMENT = new ResourceLocation(Reference.MOD_ID, "loop_border");
	private static final String LOOP_BORDER_CRITERION = "looped_once";

	public static void grantLoopBorderAdvancement(ServerPlayer player) {
		if (player.getServer() == null) {
			return;
		}

		Advancement advancement = player.getServer().getAdvancements().getAdvancement(LOOP_BORDER_ADVANCEMENT);
		if (advancement == null) {
			return;
		}

		player.getAdvancements().award(advancement, LOOP_BORDER_CRITERION);
	}
}
