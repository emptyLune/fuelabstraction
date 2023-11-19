package emptylune.fuelabstraction.item;

import emptylune.fuelabstraction.FuelAbstractionMain;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class FuelAbstractionItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, FuelAbstractionMain.MOD_ID);

    public static final RegistryObject<Item> ABSTRACTION_CATALYST = ITEMS.register("abstraction_catalyst", () -> new Item(new Item.Properties().tab(FuelAbstractionCreativeTab.instance)));
    public static final RegistryObject<Item> TINY_ABSTRACT_FUEL_INSTANCE = ITEMS.register("tiny_abstract_fuel_instance", () -> new BurnableItem(new Item.Properties().tab(FuelAbstractionCreativeTab.instance), 200));
    public static final RegistryObject<Item> ABSTRACT_FUEL_INSTANCE = ITEMS.register("abstract_fuel_instance", () -> new BurnableItem(new Item.Properties().tab(FuelAbstractionCreativeTab.instance), 1600));

    private FuelAbstractionItems() {
    }

    public static class FuelAbstractionCreativeTab extends CreativeModeTab {

        public static final FuelAbstractionCreativeTab instance = new FuelAbstractionCreativeTab(CreativeModeTab.TABS.length, "fuelabstraction");

        private FuelAbstractionCreativeTab(int index, String label) {
            super(index, label);
        }

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ABSTRACT_FUEL_INSTANCE.get());
        }
    }
}
