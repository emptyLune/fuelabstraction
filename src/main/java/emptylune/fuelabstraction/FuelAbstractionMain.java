package emptylune.fuelabstraction;

import com.mojang.logging.LogUtils;
import emptylune.fuelabstraction.block.FuelAbstractionBlocks;
import emptylune.fuelabstraction.blockentity.FuelAbstractionBlockEntities;
import emptylune.fuelabstraction.item.FuelAbstractionItems;
import emptylune.fuelabstraction.network.FuelAbstractorChannels;
import emptylune.fuelabstraction.screen.FuelAbstractionMenuTypes;
import emptylune.fuelabstraction.screen.fuelabstractor.FuelAbstractorScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod("fuelabstraction")
public class FuelAbstractionMain {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String MOD_ID = "fuelabstraction";

    public FuelAbstractionMain() {
        final IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        FuelAbstractionItems.ITEMS.register(eventBus);
        FuelAbstractionBlocks.BLOCKS.register(eventBus);
        FuelAbstractionBlockEntities.TILE_ENTITY_TYPES.register(eventBus);
        FuelAbstractionMenuTypes.MENUS.register(eventBus);

        eventBus.addListener(this::setup);
        eventBus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        FuelAbstractorChannels.Initializer channelInitializer = new FuelAbstractorChannels.Initializer();
        channelInitializer.init();
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        MenuScreens.register(FuelAbstractionMenuTypes.FUEL_ABSTRACTOR_MENU.get(), FuelAbstractorScreen::new);
    }
}
