package sample.network.packets;

public class PacketBuyBuilding {
    String message;
    Boolean playerBought;

    public PacketBuyBuilding(String message) {
        this.message = message;
        this.playerBought=false;
    }

    public PacketBuyBuilding(Boolean playerBought) {
        this.playerBought = playerBought;
        message="";
    }

    public PacketBuyBuilding() {
    }

    public Boolean getPlayerBought() {
        return playerBought;
    }
}
