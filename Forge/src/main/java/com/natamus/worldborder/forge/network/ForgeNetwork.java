package com.natamus.worldborder.forge.network;

import com.natamus.worldborder.forge.network.packet.VisibleBorderSyncPacket;
import com.natamus.worldborder.util.Reference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ForgeNetwork {
	private static final String PROTOCOL_VERSION = "1";
	private static boolean initialized = false;

	private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
		new ResourceLocation(Reference.MOD_ID, "main"),
		() -> PROTOCOL_VERSION,
		PROTOCOL_VERSION::equals,
		PROTOCOL_VERSION::equals
	);

	public static void init() {
		if (initialized) {
			return;
		}

		initialized = true;
		CHANNEL.messageBuilder(VisibleBorderSyncPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
			.encoder(VisibleBorderSyncPacket::encode)
			.decoder(VisibleBorderSyncPacket::decode)
			.consumerMainThread(VisibleBorderSyncPacket::handle)
			.add();
	}

	public static void sendToPlayer(ServerPlayer player, Object packet) {
		CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
	}

	public static void sendToAll(Object packet) {
		CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
	}
}
