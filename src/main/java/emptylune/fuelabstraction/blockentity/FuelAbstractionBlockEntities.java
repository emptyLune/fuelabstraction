package emptylune.fuelabstraction.blockentity;

import emptylune.fuelabstraction.FuelAbstractionMain;
import emptylune.fuelabstraction.block.FuelAbstractionBlocks;
import emptylune.fuelabstraction.blockentity.FuelAbstractorBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class FuelAbstractionBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, FuelAbstractionMain.MOD_ID);

    public static final RegistryObject<BlockEntityType<FuelAbstractorBlockEntity>> FUEL_ABSTRACTOR = TILE_ENTITY_TYPES.register("fuel_abstractor", () -> BlockEntityType.Builder.of(FuelAbstractorBlockEntity::new, FuelAbstractionBlocks.FUEL_ABSTRACTOR.get()).build(null));

    private FuelAbstractionBlockEntities() {
    }
}
