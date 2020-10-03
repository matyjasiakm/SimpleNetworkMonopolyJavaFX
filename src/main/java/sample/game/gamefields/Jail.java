package sample.game.gamefields;

import sample.game.Player;

public class Jail extends Field{
    public Jail() {
        name="Więzienie";
    }

    @Override
    public boolean performAction(Player player) {
        return player.hasDebet();
    }
}
