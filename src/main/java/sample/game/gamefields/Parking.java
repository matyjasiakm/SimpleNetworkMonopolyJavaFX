package sample.game.gamefields;

import sample.game.Player;

public class Parking extends Field {
    public Parking() {
        name="Parking";
    }

    @Override
    public boolean performAction(Player player) {
        return true;
    }
}
