package com.natamus.worldborder.forge.visibleborder;

import com.natamus.worldborder.config.ConfigHandler;

import java.util.Objects;

public class VisibleBorderSnapshot {
	public static final String STYLE_FORCEFIELD = "forcefield";
	public static final String STYLE_FOG = "fog";

	public final boolean showVisibleBorder;
	public final String visibleBorderStyle;
	public final DimensionBounds overworld;
	public final DimensionBounds nether;
	public final DimensionBounds end;

	public VisibleBorderSnapshot(boolean showVisibleBorder, String visibleBorderStyle, DimensionBounds overworld, DimensionBounds nether, DimensionBounds end) {
		this.showVisibleBorder = showVisibleBorder;
		this.visibleBorderStyle = normalizeStyle(visibleBorderStyle);
		this.overworld = overworld;
		this.nether = nether;
		this.end = end;
	}

	public static VisibleBorderSnapshot fromConfig() {
		return new VisibleBorderSnapshot(
			ConfigHandler.showVisibleBorder,
			ConfigHandler.visibleBorderStyle,
			new DimensionBounds(
				ConfigHandler.enableCustomOverworldBorder,
				ConfigHandler.overworldBorderPositiveX,
				ConfigHandler.overworldBorderNegativeX,
				ConfigHandler.overworldBorderPositiveZ,
				ConfigHandler.overworldBorderNegativeZ
			),
			new DimensionBounds(
				ConfigHandler.enableCustomNetherBorder,
				ConfigHandler.netherBorderPositiveX,
				ConfigHandler.netherBorderNegativeX,
				ConfigHandler.netherBorderPositiveZ,
				ConfigHandler.netherBorderNegativeZ
			),
			new DimensionBounds(
				ConfigHandler.enableCustomEndBorder,
				ConfigHandler.endBorderPositiveX,
				ConfigHandler.endBorderNegativeX,
				ConfigHandler.endBorderPositiveZ,
				ConfigHandler.endBorderNegativeZ
			)
		);
	}

	public boolean isFogStyle() {
		return STYLE_FOG.equals(visibleBorderStyle);
	}

	public static String normalizeStyle(String style) {
		if (style != null && STYLE_FOG.equalsIgnoreCase(style.trim())) {
			return STYLE_FOG;
		}

		return STYLE_FORCEFIELD;
	}

	public DimensionBounds getDimensionBounds(String dimensionName) {
		if ("minecraft:overworld".equals(dimensionName)) {
			return overworld;
		}
		if ("minecraft:the_nether".equals(dimensionName)) {
			return nether;
		}
		if ("minecraft:the_end".equals(dimensionName)) {
			return end;
		}

		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof VisibleBorderSnapshot)) {
			return false;
		}

		VisibleBorderSnapshot other = (VisibleBorderSnapshot)obj;
		return showVisibleBorder == other.showVisibleBorder
			&& Objects.equals(visibleBorderStyle, other.visibleBorderStyle)
			&& Objects.equals(overworld, other.overworld)
			&& Objects.equals(nether, other.nether)
			&& Objects.equals(end, other.end);
	}

	@Override
	public int hashCode() {
		return Objects.hash(showVisibleBorder, visibleBorderStyle, overworld, nether, end);
	}

	public static class DimensionBounds {
		public final boolean enabled;
		public final int positiveX;
		public final int negativeX;
		public final int positiveZ;
		public final int negativeZ;

		public DimensionBounds(boolean enabled, int positiveX, int negativeX, int positiveZ, int negativeZ) {
			this.enabled = enabled;
			this.positiveX = positiveX;
			this.negativeX = negativeX;
			this.positiveZ = positiveZ;
			this.negativeZ = negativeZ;
		}

		public boolean hasValidBounds() {
			return positiveX > negativeX && positiveZ > negativeZ;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof DimensionBounds)) {
				return false;
			}

			DimensionBounds other = (DimensionBounds)obj;
			return enabled == other.enabled
				&& positiveX == other.positiveX
				&& negativeX == other.negativeX
				&& positiveZ == other.positiveZ
				&& negativeZ == other.negativeZ;
		}

		@Override
		public int hashCode() {
			return Objects.hash(enabled, positiveX, negativeX, positiveZ, negativeZ);
		}
	}
}
