package emptylune.fuelabstraction.block;

import emptylune.fuelabstraction.item.BurnableBlockItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class BurnableBlock extends Block implements IHasRegisterBlockItem {

    private final int burnTime;

    public BurnableBlock(Properties properties, int burnTime) {
        super(properties);
        this.burnTime = burnTime;
    }

    @Override
    public BlockItem registerationBlockItem(Item.Properties properties) {
        return new BurnableBlockItem(this, properties, burnTime);
    }
}
