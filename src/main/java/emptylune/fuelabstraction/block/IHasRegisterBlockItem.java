package emptylune.fuelabstraction.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public interface IHasRegisterBlockItem {
    BlockItem registerationBlockItem(Item.Properties properties);
}
