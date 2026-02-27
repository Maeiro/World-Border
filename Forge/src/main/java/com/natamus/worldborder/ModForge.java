package com.natamus.worldborder;

import com.natamus.collective_common_forge.check.RegisterMod;
import com.natamus.collective_common_forge.check.ShouldLoadCheck;
import com.natamus.worldborder.forge.config.IntegrateForgeConfig;
import com.natamus.worldborder.forge.events.ForgeBorderEvent;
import com.natamus.worldborder.forge.events.ForgeCommandEvent;
import com.natamus.worldborder.forge.events.ForgeVisibleBorderSyncEvent;
import com.natamus.worldborder.forge.network.ForgeNetwork;
import com.natamus.worldborder.util.Reference;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Reference.MOD_ID)
public class ModForge {
	
	public ModForge() {
		if (!ShouldLoadCheck.shouldLoad(Reference.MOD_ID)) {
			return;
		}

		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(this::loadComplete);

		setGlobalConstants();
		ModCommon.init();
		ForgeNetwork.init();

		IntegrateForgeConfig.registerScreen(ModLoadingContext.get());

		RegisterMod.register(Reference.NAME, Reference.MOD_ID, Reference.VERSION, Reference.ACCEPTED_VERSIONS);
	}

	private void loadComplete(final FMLLoadCompleteEvent event) {
    	MinecraftForge.EVENT_BUS.register(ForgeBorderEvent.class);
		MinecraftForge.EVENT_BUS.register(ForgeCommandEvent.class);
		MinecraftForge.EVENT_BUS.register(ForgeVisibleBorderSyncEvent.class);
	}

	private static void setGlobalConstants() {

	}
}
