package sample.network;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import sample.game.Player;
import sample.game.gamefields.*;
import sample.network.packets.*;

import java.util.Random;

public class MonopolyServerListener extends Listener {

    Integer playerNumber;
    Player[] players;
    Field[] board;
    boolean gameStarted;
    Integer actualConnectedPlayer;
    Server server;
    Integer readyPlayer;
    Integer actualPlayerNumber;
    Player actualPlayer;

    public MonopolyServerListener(Integer playerNumber,Server server){

        this.playerNumber=playerNumber;
        gameStarted=false;
        actualConnectedPlayer=0;

        players =new Player[playerNumber];
        board =new Field[24];
        setBoard();
        this.server=server;
        readyPlayer=0;
        actualPlayerNumber=0;
        actualPlayer=null;
    }
    public void connected(Connection connection) {


        if(actualConnectedPlayer<playerNumber){

            players[actualConnectedPlayer]=new Player(connection.getID());
            setPlayerColor(players[actualConnectedPlayer],actualConnectedPlayer);
            actualConnectedPlayer++;
            connection.sendTCP(new PacketConnectedToGame());


        }
        else{
            connection.sendTCP(new PacketRefuseToConnect());
            connection.close();
        }




    }

    private void initializeGame(){

        for (Player player : players) {
            player.setMoney(2000);
            player.setPositionOnBoard(0);
        }
    }

    public void received(Connection connection, Object o) {

        if(!gameStarted) {
            if (o instanceof PacketSetName) {
                for (Player player : players) {
                    if (player == null) continue;
                    if (player.getId() == connection.getID()) {
                        player.setName(((PacketSetName) o).getName());
                        if(player.getName()=="")player.setName(player.getId().toString());
                        server.sendToAllExceptTCP(connection.getID(), new PacketGameStatus(board, players, "Gracz " + player.getName() + " dołączył do gry!"));
                        server.sendToTCP(player.getId(), new PacketGameStatus(board, players, player.getName()+" dołaczyłeś do gry!"));

                        readyPlayer++;
                        if (readyPlayer == playerNumber) {
                            server.sendToAllTCP(new PacketGameStatus(board, players, "Wszyscy na pokładzie!\nGra się rozpoczyna"));
                            initializeGame();
                            actualPlayer= players[actualPlayerNumber];
                            server.sendToTCP(actualPlayer.getId(),new PacketRoundStart());
                            server.sendToAllExceptTCP(actualPlayer.getId(),new PacketGameStatus(board, players, "Gracz "+actualPlayer.getName()+" rozpoczyna runde!"));
                            server.sendToTCP(actualPlayer.getId(),new PacketGameStatus(board, players, "Twój ruch!"));
                            gameStarted=true;

                        }
                    }

                }
            }
        }
        else{

            if(o instanceof PacketRollDice){

            Integer diceNumber=rollTheDice();
            server.sendToTCP(actualPlayer.getId(),new  PacketGameStatus(board, players,"Wyrzuciłeś "+ diceNumber.toString()));
            server.sendToAllExceptTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Gracz "+actualPlayer.getName()+" wyrzucił "+diceNumber.toString()));

            Integer position=actualPlayer.getPositionOnBoard();
            position+=diceNumber;

            if(position>=24){
                actualPlayer.updateBalance(200);
                position=position%24;

                server.sendToTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Przechodzisz przez start +200"));
                server.sendToAllExceptTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Gracz "+actualPlayer.getName()+" przeszedł przez start!"));
            }
                actualPlayer.setPositionOnBoard(position);

            if(board[actualPlayer.getPositionOnBoard()] instanceof Chance){
                Integer beforeChance=actualPlayer.getMoney();
                Chance chance=(Chance) board[actualPlayer.getPositionOnBoard()];
                chance.performAction(actualPlayer);
                beforeChance=actualPlayer.getMoney()-beforeChance;

                server.sendToTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Dobierasz kartę szansa.\nOtrzymujesz "+beforeChance));
                server.sendToAllExceptTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Gracz "+actualPlayer.getName()+" dobiera karte szansa\n otrzymuje "+beforeChance));
                server.sendToTCP(actualPlayer.getId(),new PacketNothingToDo());
            }else if(board[actualPlayer.getPositionOnBoard()] instanceof Parking) {

                server.sendToTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Jesteś na parkingu.\nNic sie nie dzieje."));
                server.sendToAllExceptTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Gracz "+actualPlayer.getName()+" jest na parkingu."));
                server.sendToTCP(actualPlayer.getId(),new PacketNothingToDo());

            }else if(board[actualPlayer.getPositionOnBoard()] instanceof Tax){
                Integer beforeTax=actualPlayer.getMoney();
                Tax tax=(Tax) board[actualPlayer.getPositionOnBoard()];
                tax.performAction(actualPlayer);
                beforeTax=actualPlayer.getMoney()-beforeTax;

                server.sendToTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Płacisz podatek.\nTracisz "+-beforeTax));
                server.sendToAllExceptTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Gracz "+actualPlayer.getName()+" płaci podatek w wysokosci "+beforeTax));
                server.sendToTCP(actualPlayer.getId(),new PacketNothingToDo());
            }
            else if(board[actualPlayer.getPositionOnBoard()] instanceof GoToJail){
                GoToJail goToJail=(GoToJail) board[actualPlayer.getPositionOnBoard()];
                goToJail.performAction(actualPlayer);
                actualPlayer.setPositionOnBoard(6);

                server.sendToTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Idziesz do więźienia!!!"));
                server.sendToAllExceptTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Gracz "+actualPlayer.getName()+" idzie do więzienia!"));
                server.sendToTCP(actualPlayer.getId(),new PacketNothingToDo());
            }
            else if (board[actualPlayer.getPositionOnBoard()] instanceof Jail){
                server.sendToTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Stoisz obok więzienia\nale do niego nie wchodzisz!"));
                server.sendToAllExceptTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Gracz "+actualPlayer.getName()+" wykonał ruch!"));
                server.sendToTCP(actualPlayer.getId(),new PacketNothingToDo());

            }
            else if (board[actualPlayer.getPositionOnBoard()] instanceof Building){
                Building building=(Building) board[actualPlayer.getPositionOnBoard()];

                if(building.isThisOwner(actualPlayer)){
                    server.sendToTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Stoisz na swojej własności"));
                    server.sendToAllExceptTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Gracz "+actualPlayer.getName()+" stoi na swojej własności!"));
                    server.sendToTCP(actualPlayer.getId(),new PacketNothingToDo());
                }
                else{
                    if(building.hasOwner()){

                        building.performAction(actualPlayer);

                        server.sendToTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Płacisz graczowi " + building.getOwner().getName()+" "+building.payValue()));
                        server.sendToAllExceptTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Gracz "+actualPlayer.getName()+" płaci \ngraczowi "+building.getOwner().getName()+" "+building.payValue()));
                        server.sendToTCP(actualPlayer.getId(),new PacketNothingToDo());
                    }
                    else{

                        if(actualPlayer.getMoney()>=building.getValue()) {
                            server.sendToTCP(actualPlayer.getId(), new PacketGameStatus(board, players, "Możesz kupić " + building.getName() + " za cene " + building.getValue()));
                            server.sendToAllExceptTCP(actualPlayer.getId(), new PacketGameStatus(board, players, "Gracz " + actualPlayer.getName() + " może kupic " + building.getName()));
                            server.sendToTCP(actualPlayer.getId(), new PacketBuyBuilding());
                        }
                        else{
                            server.sendToTCP(actualPlayer.getId(),new PacketNothingToDo());
                        }

                    }
                }
            }else if (board[actualPlayer.getPositionOnBoard()] instanceof StartField){

                server.sendToTCP(actualPlayer.getId(),new PacketNothingToDo());
                server.sendToTCP(actualPlayer.getId(), new PacketGameStatus(board, players, "Jesteś na starcie!"));
                server.sendToAllExceptTCP(actualPlayer.getId(), new PacketGameStatus(board, players, "Gracz " + actualPlayer.getName() + "jest na starcie."));
            }




            


        }else if(o instanceof PacketBuyBuilding){

            PacketBuyBuilding packetBuyBuilding=(PacketBuyBuilding)o;
            if(packetBuyBuilding.getPlayerBought()){
                Building building=(Building) board[actualPlayer.getPositionOnBoard()];
                building.setOwner(actualPlayer);
                actualPlayer.updateBalance(-building.getValue());

                server.sendToTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Kupiłeś "+building.getName()+ "za cene "+building.getValue()));
                server.sendToAllExceptTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Gracz "+actualPlayer.getName()+"kupił"+ building.getName()));
                server.sendToTCP(actualPlayer.getId(),new PacketNothingToDo());
            }


        }else if(o instanceof PacketRoundEnd){


            server.sendToTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Zakończyłeś runde"));
            server.sendToAllExceptTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Gracz "+actualPlayer.getName()+" zakończył runde!"));

            if(isGameEnded()){
                Player player=new Player();
                for (Player p:players) {
                    if(!p.hasDebet()&&p.getId()>0)player=p;

                }

                server.sendToTCP(player.getId(),new PacketGameStatus(board, players,"Wygrałes!"));
                server.sendToAllExceptTCP(player.getId(),new PacketGameStatus(board, players,"Gracz "+actualPlayer.getName()+" wygrał rozgrywke!"));
                server.sendToTCP(player.getId(),new PacketEndGame());
                server.sendToAllExceptTCP(player.getId(),new PacketEndGame());
            }
            actualPlayerNumber++;
            actualPlayerNumber%=playerNumber;
            actualPlayer= players[actualPlayerNumber];



            while(actualPlayer.getInJail() || actualPlayer.hasDebet()|| actualPlayer.getId()<0){

                if(actualPlayer.getInJail()){
                    server.sendToTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Tracisz runde na wyjscie z więzienia!"));
                    server.sendToAllExceptTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Gracz "+actualPlayer.getName()+" wychodzi z więzienia!"));
                    actualPlayer.setInJail(false);
                }
                else if(actualPlayer.hasDebet()){
                    server.sendToTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Masz debet tylko obserwuj!"));
                    server.sendToAllExceptTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Gracz "+actualPlayer.getName()+" ma debet i moze tylko obserwowac!"));
                    }


                actualPlayerNumber++;
                actualPlayerNumber%=playerNumber;
                actualPlayer= players[actualPlayerNumber];

            }

            server.sendToTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Twoja runda!"));
            server.sendToAllExceptTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Gracz "+actualPlayer.getName()+" rozpoczyna runde!"));
            server.sendToTCP(actualPlayer.getId(),new PacketRoundStart());
        }


        }

    }

    private boolean isGameEnded(){
        int winner=0;
        for (Player p:players) {
            if(!p.hasDebet())winner++;
        }
        if(winner!=1)return false;
        return true;
    }

    private Integer rollTheDice(){
        Random rand=new Random(Double.doubleToLongBits(Math.random()));
        return rand.nextInt(12)+1;
    }

    private void setPlayerColor(Player player,int num){

        switch(num){
            case 0: {player.setColor("PINK");
                break;
            }

            case 1:{player.setColor("lightGray");
                break;

            }

            case 2:{player.setColor("magenta");
                break;
            }

            case 3:{player.setColor("CYAN");
                break;
            }
        }

    }


    public void disconnected(Connection connection) {


            if(actualPlayer==null)return;


        for (Player pl:players) {
            if(pl==null)continue;
            if(pl.getId()==connection.getID()){
                if(actualPlayer.getId()==connection.getID()) {

                    server.sendToAllExceptTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Gracz "+actualPlayer.getName()+" sie rozłaczył!"));
                    pl.setId(-1);

                    if(isGameEnded()){
                        Player player=new Player();
                        for (Player p:players) {
                            if(!p.hasDebet()&&p.getId()>0)player=p;

                        }

                        server.sendToTCP(player.getId(),new PacketGameStatus(board, players,"Wygrałes!"));
                        server.sendToAllExceptTCP(player.getId(),new PacketGameStatus(board, players,"Gracz "+actualPlayer.getName()+" wygrał rozgrywke!"));
                        server.sendToTCP(player.getId(),new PacketEndGame());
                        server.sendToAllExceptTCP(player.getId(),new PacketEndGame());
                    }
                    actualPlayerNumber++;
                    actualPlayerNumber%=playerNumber;
                    actualPlayer= players[actualPlayerNumber];


                    int i=0;
                    while(actualPlayer.getInJail() || actualPlayer.hasDebet()|| actualPlayer.getId()<0){
                        if(i==players.length+1)break;
                        if(actualPlayer.getInJail()){
                            server.sendToTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Tracisz runde na wyjscie z więzienia!"));
                            server.sendToAllExceptTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Gracz "+actualPlayer.getName()+" wychodzi z więzienia!"));
                            actualPlayer.setInJail(false);
                            i=0;
                        }
                        else if(actualPlayer.hasDebet()){
                            server.sendToTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Masz debet tylko obserwuj!"));
                            server.sendToAllExceptTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Gracz "+actualPlayer.getName()+" ma debet i moze tylko obserwowac!"));
                        }

                        i++;
                        actualPlayerNumber++;
                        actualPlayerNumber%=playerNumber;
                        actualPlayer= players[actualPlayerNumber];

                    }

                    server.sendToTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Twoja runda!"));
                    server.sendToAllExceptTCP(actualPlayer.getId(),new PacketGameStatus(board, players,"Gracz "+actualPlayer.getName()+" rozpoczyna runde!"));
                    server.sendToTCP(actualPlayer.getId(),new PacketRoundStart());


                }
                else
                {
                    server.sendToAllTCP(new PacketGameStatus(board, players,"Gracz "+actualPlayer.getName()+" sie rozłaczył!"));
                    pl.setId(-1);

                    if(isGameEnded()){
                        Player player=new Player();
                        for (Player p:players) {
                            if(!p.hasDebet()&&p.getId()>0)player=p;

                        }

                        server.sendToTCP(player.getId(),new PacketGameStatus(board, players,"Wygrałes!"));
                        server.sendToAllExceptTCP(player.getId(),new PacketGameStatus(board, players,"Gracz "+actualPlayer.getName()+" wygrał rozgrywke!"));
                        server.sendToTCP(player.getId(),new PacketEndGame());
                        server.sendToAllExceptTCP(player.getId(),new PacketEndGame());
                    }

                }



            }

        }
        int i=0;
        Player winner=new Player();
        for (Player p:players) {
            if(p==null)continue;
            if(p.getId()>0){
                i++;
                winner=p;
            }

        }
        if(i==1){

            server.sendToTCP(winner.getId(),new PacketGameStatus(board, players,"Wszyscy gracze uciekli wygrywasz przez K.O.!"));
            server.sendToTCP(winner.getId(),new PacketEndGame());
        }

    }

    private void setBoard()
    {
        board[0]=new StartField();
        board[1]=new Building("Zamek Ujazdowski",60);
        board[2]=new Building("Katedra Jana Chrzciciela",60);
        board[3]=new Tax(50);
        board[4]=new Chance();
        board[5]=new Building("Pałac Prezydencki",100);
        board[6]=new Jail();
        board[7]=new Building("Belweder",140);
        board[8]=new Building("Pałac w Wilanowie",160);
        board[9]=new Tax(100);
        board[10]=new Building("Muzeum Narodowe",180);
        board[11]=new Chance();
        board[12]=new Parking();
        board[13]=new Tax(150);
        board[14]=new Building("Centrum Nauki Kopernik",200);
        board[15]=new Building("Złote Tarasy",240);
        board[16]=new Chance();
        board[17]=new Building("Pałac Kultury i Nauki",280);
        board[18]=new GoToJail();
        board[19]=new Building("Teatr Narodowy",300);
        board[20]=new Chance();
        board[21]=new Tax(200);
        board[22]=new Building("Stadion Narodowy",320);
        board[23]=new Building("Zamek Królewski",400);

    }
}
