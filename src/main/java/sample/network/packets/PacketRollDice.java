package sample.network.packets;

public class PacketRollDice {
    Integer number;
    String message;
    public PacketRollDice() {
    }

    public PacketRollDice(Integer number, String message) {
        this.number = number;
        this.message = message;
    }
}
