package emptylune.fuelabstraction.network;

import emptylune.fuelabstraction.screen.fuelabstractor.FuelAbstractorMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FuelAbstractionSolidificationItemChangePacket implements FuelAbstractionPacket {

    private final byte solidificationItemIndex;

    public static FuelAbstractionSolidificationItemChangePacket decoder(FriendlyByteBuf buffer) {
        byte solidificationItemIndex = buffer.readByte();
        return new FuelAbstractionSolidificationItemChangePacket(solidificationItemIndex);
    }

    public FuelAbstractionSolidificationItemChangePacket(byte solidificationItemIndex) {
        this.solidificationItemIndex = solidificationItemIndex;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeByte(solidificationItemIndex);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if (sender.containerMenu instanceof FuelAbstractorMenu menu) {
                menu.setSolidificationItemFromIndex(solidificationItemIndex);
            }
        });
        context.get().setPacketHandled(true);
    }
}
