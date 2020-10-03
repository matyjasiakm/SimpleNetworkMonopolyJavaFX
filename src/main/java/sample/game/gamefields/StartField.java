package sample.game.gamefields;

import sample.game.Player;

public class StartField extends Field {
    public StartField() {
        name="Start";
    }

    final Integer prize=200;
    @Override
    public boolean performAction(Player player) {
        player.updateBalance(prize);
        return player.hasDebet();
    }
}
