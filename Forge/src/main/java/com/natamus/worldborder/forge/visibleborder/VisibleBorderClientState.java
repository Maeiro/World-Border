package com.natamus.worldborder.forge.visibleborder;

public class VisibleBorderClientState {
	private static VisibleBorderSnapshot snapshot = null;

	public static void setSnapshot(VisibleBorderSnapshot syncedSnapshot) {
		snapshot = syncedSnapshot;
	}

	public static VisibleBorderSnapshot getSnapshot() {
		return snapshot;
	}

	public static void clear() {
		snapshot = null;
	}
}
