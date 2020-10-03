package sample.game.gamefields;

import sample.game.Player;

import java.util.Random;

public class Chance extends Field implements ActionWithPlayer {

    public Chance(){

        name="Szansa";
    }

    @Override
    public boolean performAction(Player player) {

        Random rand=new Random();
        Integer ChanceValue=rand.nextInt(4);

        switch (ChanceValue)
        {
            case 0:{
                player.updateBalance(150);
                break;
            }
            case 1: {
                player.updateBalance(-50);
                break;

            }
            case 2:{
                player.updateBalance(-150);
                break;
            }
            case 3:{
                player.updateBalance(50);
                break;
            }
            default:{

                break;
            }

        }
        return player.hasDebet();
    }
}
