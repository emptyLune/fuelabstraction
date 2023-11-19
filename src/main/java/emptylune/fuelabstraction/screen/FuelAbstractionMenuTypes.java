package emptylune.fuelabstraction.screen;

import emptylune.fuelabstraction.FuelAbstractionMain;
import emptylune.fuelabstraction.screen.fuelabstractor.FuelAbstractorMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class FuelAbstractionMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.CONTAINERS, FuelAbstractionMain.MOD_ID);

    public static final RegistryObject<MenuType<FuelAbstractorMenu>> FUEL_ABSTRACTOR_MENU = registerMenuType(FuelAbstractorMenu::new, "fuel_abstractor");

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(IContainerFactory<T> factory, String name) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }
}
