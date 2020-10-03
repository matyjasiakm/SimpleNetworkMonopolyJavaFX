package sample.network;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import sample.Controller;
import sample.Monopoly;
import sample.network.packets.*;

public class MonopolyClientListener extends Listener {
    Monopoly game;
    Controller controller;
    boolean serverFull;
    public MonopolyClientListener(Monopoly monopoly, Controller controller){
        game=monopoly;
        this.controller=controller;
        serverFull=false;
    }


    public void connected(Connection connection) {




    }


    public void received(Connection connection, Object o) {

        if(o instanceof PacketGameStatus){
            PacketGameStatus packetGameStatus=(PacketGameStatus)o;
            game.updateGame(packetGameStatus.getBoard(),packetGameStatus.getPlayers());
            controller.addLogMessage(packetGameStatus.getMessage());
        }
        else if(o instanceof PacketSetName){
            PacketSetName packetSetName=(PacketSetName)o;
            controller.addLogMessage("Twoja nazwa: "+packetSetName.getName());
        }
        else if(o instanceof PacketRoundStart){


            controller.getRollDiceButton().setDisable(false);

        }
        else if(o instanceof PacketBuyBuilding){
            controller.getBuyBuildingButton().setDisable(false);
            controller.getEndRoundButton().setDisable(false);
        }
        else if(o instanceof PacketNothingToDo){
            controller.getEndRoundButton().setDisable(false);
        }
        else if(o instanceof PacketEndGame){
            controller.getEndRoundButton().setDisable(true);
            controller.getBuyBuildingButton().setDisable(true);
            controller.getRollDiceButton().setDisable(true);
            controller.addLogMessage("KONIEC GRY!");
        }
        else if(o instanceof PacketRefuseToConnect){
            serverFull=true;
        }
        else if(o instanceof PacketConnectedToGame) {
            controller.showWindowAndSetPlayerName();
        }
    }


    public void disconnected(Connection connection) {
        if(game.isClosing())return;
        if(serverFull){
            controller.displayAlert("Gra pełna!");
            controller.makeButtonToConnectEnable();
        }
        else
        {
            controller.setDisableGameButtons();
            controller.displayAlert("Utracono połaczenie z serwerem!");
        }

    }
}
