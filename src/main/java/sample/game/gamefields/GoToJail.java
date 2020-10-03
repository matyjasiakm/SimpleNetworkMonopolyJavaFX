package sample.game.gamefields;

import sample.game.Player;

public class GoToJail extends Field {

    public GoToJail() {
        name="Do Więzienia!";
    }

    @Override
    public boolean performAction(Player player) {
        player.setInJail(true);
        return player.hasDebet();
    }



}
