package com.natamus.worldborder.forge.network.packet;

import com.natamus.worldborder.forge.visibleborder.VisibleBorderClientState;
import com.natamus.worldborder.forge.visibleborder.VisibleBorderSnapshot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class VisibleBorderSyncPacket {
	private final VisibleBorderSnapshot snapshot;

	public VisibleBorderSyncPacket(VisibleBorderSnapshot snapshot) {
		this.snapshot = snapshot;
	}

	public static void encode(VisibleBorderSyncPacket packet, FriendlyByteBuf buffer) {
		buffer.writeBoolean(packet.snapshot.showVisibleBorder);
		buffer.writeUtf(packet.snapshot.visibleBorderStyle, 16);
		writeBounds(buffer, packet.snapshot.overworld);
		writeBounds(buffer, packet.snapshot.nether);
		writeBounds(buffer, packet.snapshot.end);
	}

	public static VisibleBorderSyncPacket decode(FriendlyByteBuf buffer) {
		boolean showVisibleBorder = buffer.readBoolean();
		String visibleBorderStyle = buffer.readUtf(16);
		VisibleBorderSnapshot.DimensionBounds overworld = readBounds(buffer);
		VisibleBorderSnapshot.DimensionBounds nether = readBounds(buffer);
		VisibleBorderSnapshot.DimensionBounds end = readBounds(buffer);

		return new VisibleBorderSyncPacket(new VisibleBorderSnapshot(showVisibleBorder, visibleBorderStyle, overworld, nether, end));
	}

	public static void handle(VisibleBorderSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
		VisibleBorderClientState.setSnapshot(packet.snapshot);
	}

	private static void writeBounds(FriendlyByteBuf buffer, VisibleBorderSnapshot.DimensionBounds bounds) {
		buffer.writeBoolean(bounds.enabled);
		buffer.writeInt(bounds.positiveX);
		buffer.writeInt(bounds.negativeX);
		buffer.writeInt(bounds.positiveZ);
		buffer.writeInt(bounds.negativeZ);
	}

	private static VisibleBorderSnapshot.DimensionBounds readBounds(FriendlyByteBuf buffer) {
		boolean enabled = buffer.readBoolean();
		int positiveX = buffer.readInt();
		int negativeX = buffer.readInt();
		int positiveZ = buffer.readInt();
		int negativeZ = buffer.readInt();
		return new VisibleBorderSnapshot.DimensionBounds(enabled, positiveX, negativeX, positiveZ, negativeZ);
	}
}
