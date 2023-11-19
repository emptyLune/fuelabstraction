package emptylune.fuelabstraction.network;

import emptylune.fuelabstraction.FuelAbstractionMain;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Function;

public class FuelAbstractorChannels {
    private static final String PROTOCOL_VERSION = "0.1.0";

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(FuelAbstractionMain.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    private FuelAbstractorChannels() {
    }

    public static class Initializer {
        private int id;

        public Initializer() {
            id = 0;
        }

        public void init() {
            register(FuelAbstractorSolidificationTogglePacket.class, FuelAbstractorSolidificationTogglePacket::decoder);
            register(FuelAbstractionSolidificationItemChangePacket.class, FuelAbstractionSolidificationItemChangePacket::decoder);
        }

        public <MSG extends FuelAbstractionPacket> void register(Class<MSG> type, Function<FriendlyByteBuf, MSG> decoder) {
            CHANNEL.messageBuilder(type, id++)
                    .encoder(FuelAbstractionPacket::encode)
                    .decoder(decoder)
                    .consumer(FuelAbstractionPacket::handle)
                    .add();
        }
    }
}
