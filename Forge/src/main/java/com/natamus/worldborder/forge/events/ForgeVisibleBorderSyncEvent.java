package com.natamus.worldborder.forge.events;

import com.natamus.worldborder.forge.network.ForgeNetwork;
import com.natamus.worldborder.forge.network.packet.VisibleBorderSyncPacket;
import com.natamus.worldborder.forge.visibleborder.VisibleBorderSnapshot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeVisibleBorderSyncEvent {
	private static int tickCounter = 0;
	private static VisibleBorderSnapshot lastBroadcastSnapshot = null;

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer)) {
			return;
		}

		ServerPlayer player = (ServerPlayer)event.getEntity();
		ForgeNetwork.sendToPlayer(player, new VisibleBorderSyncPacket(VisibleBorderSnapshot.fromConfig()));
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (!event.phase.equals(TickEvent.Phase.END)) {
			return;
		}

		tickCounter += 1;
		if (tickCounter < 20) {
			return;
		}
		tickCounter = 0;

		VisibleBorderSnapshot snapshot = VisibleBorderSnapshot.fromConfig();
		if (snapshot.equals(lastBroadcastSnapshot)) {
			return;
		}

		lastBroadcastSnapshot = snapshot;
		ForgeNetwork.sendToAll(new VisibleBorderSyncPacket(snapshot));
	}
}
