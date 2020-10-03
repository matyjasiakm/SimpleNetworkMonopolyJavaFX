package sample;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Server;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.game.Player;
import sample.game.gamefields.*;
import sample.network.MonopolyClientListener;
import sample.network.MonopolyServerListener;
import sample.network.PortValue;
import sample.network.packets.*;


import java.io.IOException;
import java.net.BindException;


public class Monopoly extends Application {

    Player[] players;
    Field[] board;
    Controller controller;
    Server server;
    Client client;
    Stage stage;
    boolean isClosing;

    public Monopoly() {
        board = new Field[6 * 4];
        players = new Player[4];
        isClosing = false;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        FXMLLoader loader = new FXMLLoader(Controller.class.getResource("sample.fxml"));
        System.out.println(Monopoly.class.getResource("sample.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Monopoly");
        primaryStage.setScene(new Scene(root, 1200, 600));
        primaryStage.setMaxHeight(850);
        primaryStage.setMaxWidth(1200);
        primaryStage.setMaximized(true);

        setBoard();

        controller = loader.getController();
        controller.initialize(this);
        controller.setGameInstance(this);

        primaryStage.setOnHiding(event -> {
            isClosing = true;
            closeServer();
            closeClient();
        });

        primaryStage.show();
    }

    public boolean isClosing() {
        return isClosing;
    }

    public Stage getStage() {
        return stage;
    }

    private void setBoard() {
        board[0] = new StartField();
        board[1] = new Building("Zamek Ujazdowski", 60);
        board[2] = new Building("Katedra Jana Chrzciciela", 60);
        board[3] = new Tax(50);
        board[4] = new Chance();
        board[5] = new Building("Pałac Prezydencki", 100);
        board[6] = new Jail();
        board[7] = new Building("Belweder", 140);
        board[8] = new Building("Pałac w Wilanowie", 160);
        board[9] = new Tax(100);
        board[10] = new Building("Muzeum Narodowe", 180);
        board[11] = new Chance();
        board[12] = new Parking();
        board[13] = new Tax(150);
        board[14] = new Building("Centrum Nauki Kopernik", 200);
        board[15] = new Building("Złote Tarasy", 240);
        board[16] = new Chance();
        board[17] = new Building("Pałac Kultury i Nauki", 280);
        board[18] = new GoToJail();
        board[19] = new Building("Teatr Narodowy", 300);
        board[20] = new Chance();
        board[21] = new Tax(200);
        board[22] = new Building("Stadion Narodowy", 320);
        board[23] = new Building("Zamek Królewski", 400);
    }

    public Field[] getBoard() {
        return board;
    }

    public static void main(String[] args) {
        launch(args);
    }


    public void startServer(Integer number, PortValue value) throws IOException {
        server = new Server();
        Kryo kryo = server.getKryo();
        registerClasses(kryo);
        boolean notBinded = true;
        int bind = 20000;

        while (notBinded) {
            try {
                server.bind(bind, bind + 1);
                value.setValue(bind);
                notBinded = false;
            } catch (BindException e) {
                bind += 2;
            }
        }

        server.start();
        server.addListener(new MonopolyServerListener(number, server));
        controller.addLogMessage("Server hosted!");
    }

    public void closeServer() {
        if (server != null)
            server.close();
    }

    public void closeClient() {
        if (client != null)
            client.close();
    }

    public boolean startClient(String ip, PortValue value) {

        client = new Client();
        Kryo kryo = client.getKryo();
        registerClasses(kryo);
        client.start();
        try {
            client.connect(50000, ip, value.getValue(), value.getValue() + 1);
        } catch (IOException e) {
            client = null;
            return false;
        }
        client.addListener(new MonopolyClientListener(this, controller));
        return true;
    }

    public void sendToServer(Object o) {
        client.sendTCP(o);
    }

    public void updateGame(Field[] board, Player[] players) {
        this.board = board.clone();
        this.players = players.clone();
        controller.refreshBoard();
    }

    public Player[] getPlayers() {
        return players;
    }

    public boolean isServerActive() {
        return server != null;
    }

    private void registerClasses(Kryo kryo) {
        kryo.register(Field[].class);
        kryo.register(Player.class);
        kryo.register(Player[].class);
        kryo.register(StartField.class);
        kryo.register(Field.class);
        kryo.register(Building.class);
        kryo.register(GoToJail.class);
        kryo.register(Jail.class);
        kryo.register(StartField.class);
        kryo.register(Chance.class);
        kryo.register(Parking.class);
        kryo.register(Tax.class);
        kryo.register(PacketSetName.class);
        kryo.register(PacketGameStatus.class);
        kryo.register(PacketRoundStart.class);
        kryo.register(PacketRoundEnd.class);
        kryo.register(float[].class);
        kryo.register(PacketNothingToDo.class);
        kryo.register(PacketRollDice.class);
        kryo.register(PacketBuyBuilding.class);
        kryo.register(PacketEndGame.class);
        kryo.register(PacketRefuseToConnect.class);
        kryo.register(PacketConnectedToGame.class);
    }
}
