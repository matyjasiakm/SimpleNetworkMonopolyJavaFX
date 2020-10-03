package sample.network.packets;

import sample.game.gamefields.Field;
import sample.game.Player;

public class PacketGameStatus {
    Field[] board;
    Player[] players;
    String message;
    public PacketGameStatus(Field[] board, Player[] players, String message) {
        this.board = board;
        this.players = players;
        this.message=message;
    }


    public PacketGameStatus() {
    }

    public Field[] getBoard() {
        return board;
    }

    public void setBoard(Field[] board) {
        this.board = board;
    }

    public Player[] getPlayers() {
        return players;
    }

    public void setPlayers(Player[] players) {
        this.players = players;
    }

    public String getMessage() {
        return message;
    }
}
