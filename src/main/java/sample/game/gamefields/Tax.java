package sample.game.gamefields;

import sample.game.Player;

public class Tax extends Field{

    private Integer Value;

    public Tax(Integer _TaxValue){

        Value=-_TaxValue;
        name="Podatek";
    }

    public Integer getValue() {
        return -Value;
    }

    public Tax() {
    }

    @Override
    public boolean performAction(Player player) {

        player.updateBalance(Value);
        return player.hasDebet();

    }
}
