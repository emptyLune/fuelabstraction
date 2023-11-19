package emptylune.fuelabstraction.block;

import emptylune.fuelabstraction.FuelAbstractionMain;
import emptylune.fuelabstraction.item.FuelAbstractionItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class FuelAbstractionBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, FuelAbstractionMain.MOD_ID);

    public static final RegistryObject<Block> BLOCK_ABSTRACT_FUEL_INSTANCE = BLOCKS.register("block_abstract_fuel_instance", () -> new BurnableBlock(Block.Properties.copy(Blocks.COAL_BLOCK), 14400));

    public static final RegistryObject<Block> FUEL_ABSTRACTOR = BLOCKS.register("fuel_abstractor", () -> new FuelAbstractorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion()));

    private FuelAbstractionBlocks() {
    }

    @SubscribeEvent
    public static void onRegisterItems(final RegistryEvent.Register<Item> event) {
        final IForgeRegistry<Item> registry = event.getRegistry();

        for (RegistryObject<Block> registryObject : BLOCKS.getEntries()) {
            Block block = registryObject.get();
            final Item.Properties properties = new Item.Properties().tab(FuelAbstractionItems.FuelAbstractionCreativeTab.instance);
            BlockItem blockItem;
            if (block instanceof IHasRegisterBlockItem hasRegisterBlockItem) {
                blockItem = hasRegisterBlockItem.registerationBlockItem(properties);
            } else {
                blockItem = new BlockItem(block, properties);
            }
            blockItem.setRegistryName(block.getRegistryName());
            registry.register(blockItem);
        }
    }

}
