package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;

import javafx.util.Pair;
import sample.game.Player;
import sample.game.gamefields.*;
import sample.network.PortValue;
import sample.network.packets.PacketBuyBuilding;
import sample.network.packets.PacketRollDice;
import sample.network.packets.PacketRoundEnd;
import sample.network.packets.PacketSetName;

import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Controller {
    @FXML
    GridPane gameBoardGridPane;
    @FXML
    Button hostGameButton;
    @FXML
    TextArea infoTextArea;
    @FXML
    Button connectButton;
    @FXML
    TableView<Player> playerTableView;
    @FXML
    TableColumn<Player, String> nameTableColumn;
    @FXML
    TableColumn<Player, String> moneyTableColumn;
    @FXML
    TableColumn<Player, String> idTableColumn;
    @FXML
    Button rollDiceButton;
    @FXML
    Button endRoundButton;
    @FXML
    Button buyBuildingButton;
    @FXML
    TextField serverLabel;

    Monopoly game;
    HBox[] placeForPawnHBox;

    public Controller() {
    }

    public void setGameInstance(Monopoly monopoly) {
        game = monopoly;
    }

    private void disableGameButtons() {
        endRoundButton.setDisable(true);
        buyBuildingButton.setDisable(true);
        rollDiceButton.setDisable(true);
    }

    @FXML
    void initialize(Monopoly monopoly) {
        placeForPawnHBox = new HBox[24];
        game = monopoly;
        refreshBoard();
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        moneyTableColumn.setCellValueFactory(new PropertyValueFactory<>("money"));
        idTableColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        playerTableView.setPlaceholder(new Label(""));
        disableGameButtons();
    }

    @FXML
    public void rollTheDiceButtonEvent(ActionEvent e) {
        game.sendToServer(new PacketRollDice());
        setDisabledAfterClick((Button) e.getSource());
    }

    @FXML
    public void buyBuildingsButtonEvent(ActionEvent e) {
        game.sendToServer(new PacketBuyBuilding(true));
        setDisabledAfterClick((Button) e.getSource());
    }

    @FXML
    public void endRoundButtonEvent() {
        game.sendToServer(new PacketRoundEnd());
        setDisableGameButtons();
    }

    public void setDisabledAfterClick(Button button) {
        button.setDisable(true);
    }

    @FXML
    public void connectToServerOnAction() {
        if (!showWindowAndConnectToServer()) return;
        makeButtonToConnectDisable();
    }

    public boolean showWindowAndConnectToServer() {
        TextInputDialog dialog = new TextInputDialog("127.0.0.1:20000");
        dialog.setTitle("Dołączanie do gry.");
        dialog.setHeaderText("Dołącz do gry!");
        dialog.setContentText("Podaj adress IP oraz port [ip:port]:");
        dialog.setGraphic(null);

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            String[] ipPort = result.get().split(":");
            if (!(ipPort.length == 2)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("UWAGA!");
                alert.setHeaderText(null);
                alert.setContentText("Zły addres ip:port!");
                alert.showAndWait();

                return false;
            } else {
                final String IPADDRESS_PATTERN =
                        "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

                Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
                Matcher matcher = pattern.matcher(ipPort[0]);

                if (!matcher.matches() || !matcher.matches()) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("UWAGA!");
                    alert.setHeaderText(null);
                    alert.setContentText("Zły addres ip:port!");
                    alert.showAndWait();

                    return false;
                }
            }

            if (!game.startClient(ipPort[0], new PortValue(Integer.parseInt(ipPort[1])))) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("UWAGA!");
                alert.setHeaderText(null);
                alert.setContentText("Nie można połączyc z serwerem!");
                alert.showAndWait();

                return false;
            }
            return true;
        }
        return false;
    }

    public void showWindowAndSetPlayerName() {
        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Nazwa Gracza");
            dialog.setHeaderText("Wpisz nazwe gracza.");
            dialog.setContentText("Nazwa: ");
            dialog.setGraphic(null);
            dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setDisable(true);
            dialog.getDialogPane().getButtonTypes().remove(1);
            dialog.initModality(Modality.APPLICATION_MODAL);

            Optional<String> result = dialog.showAndWait();

            if (result.isPresent()) {
                game.sendToServer(new PacketSetName(result.get()));
            } else {
                game.sendToServer(new PacketSetName(""));
            }
        });
    }

    /**
     * Show window and create server for picked player number
     */
    @FXML
    public void hostServerButtonOnAction() throws IOException {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tworzenie Gry");
        alert.setHeaderText("Tworzenie serwera na określona liczbe graczy!");
        alert.setContentText("Wybierz ilość graczy.");

        ButtonType buttonTwoPlayers = new ButtonType("Dwóch");
        ButtonType buttonThreePlayers = new ButtonType("Troje");
        ButtonType buttonFourPlayers = new ButtonType("Czworo");
        ButtonType buttonTypeCancel = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTwoPlayers, buttonThreePlayers, buttonFourPlayers, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        if (!result.isPresent()) return;

        PortValue port = new PortValue(-1);
        if (result.get() == buttonTwoPlayers) {
            game.startServer(2, port);
        } else if (result.get() == buttonThreePlayers) {
            game.startServer(3, port);
        } else if (result.get() == buttonFourPlayers) {
            game.startServer(4, port);
        } else {
            return;
        }

        if (port.getValue() != -1) {
            game.startClient("127.0.0.1", port);
            InetAddress inetAddress = InetAddress.getLocalHost();
            serverLabel.setText("Adres serwera: " + inetAddress.getHostAddress() + ":" + port.getValue());
            makeButtonToConnectDisable();
        }
    }

    /**
     * Convert from pawn position on board to hbox position
     */
    private Pair<Integer, Integer> positionOnGrid(int x) {

        if (x >= 0 && x < 6) {
            for (int i = 6; i > 0; i--)
                if ((6 - i) == x)
                    return new Pair<>(0, i);//7 * i;
        } else if (x >= 6 && x < 12) {
            for (int i = 0; i < 6; i++) {
                if (6 + i == x)
                    return new Pair<>(i, 0);//i;
            }
        } else if (x >= 12 && x < 18) {
            for (int i = 0; i < 6; i++) {
                if (12 + i == x)
                    return new Pair<>(6, i);//7*i+6;
            }
        } else {
            for (int i = 6; i > 0; i--) {
                if (24 - i == x)
                    return new Pair<>(i, 6);//7*6+i;
            }

        }
        return null;
    }

    private void makeButtonToConnectDisable() {
        hostGameButton.setDisable(true);
        connectButton.setDisable(true);
    }

    public void makeButtonToConnectEnable() {
        Platform.runLater(() -> {
            hostGameButton.setDisable(false);
            connectButton.setDisable(false);
        });
    }

    public Button getRollDiceButton() {
        return rollDiceButton;
    }

    public Button getEndRoundButton() {
        return endRoundButton;
    }

    public Button getBuyBuildingButton() {
        return buyBuildingButton;
    }

    public void refreshBoard() {
        Platform.runLater(() -> {
            gameBoardGridPane.getChildren();
            Field[] board = game.getBoard();
            Player[] players = game.getPlayers();

            for (int boardPawnFieldIndex = 0; boardPawnFieldIndex < board.length; boardPawnFieldIndex++) {

                Pair<Integer, Integer> positionOnHBox = positionOnGrid(boardPawnFieldIndex);

                VBox field = new VBox(10);
                field.setAlignment(Pos.CENTER);
                field.setStyle("-fx-background-color: white;" + "-fx-border-color: black;" + "-fx-font-weight: bold;");
                Label fieldName = new Label(board[boardPawnFieldIndex].getName());
                fieldName.setWrapText(true);
                fieldName.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                field.getChildren().addAll(fieldName);

                if (board[boardPawnFieldIndex] instanceof Building) {
                    Building building = (Building) board[boardPawnFieldIndex];
                    field.getChildren().add(new Label("Koszt: " + building.getValue().toString()));
                    if (building.hasOwner())
                        field.setStyle("-fx-background-color: " + building.getOwner().getColor() + ";" + "-fx-border-color: black;" + "-fx-font-weight: bold;");
                } else if (board[boardPawnFieldIndex] instanceof Tax) {
                    Tax tax = (Tax) board[boardPawnFieldIndex];
                    field.getChildren().add(new Label("Podatek: " + tax.getValue().toString()));
                    field.setStyle("-fx-background-color: orangered;" + "-fx-border-color: black;" + "-fx-font-weight: bold;");
                } else if (board[boardPawnFieldIndex] instanceof Chance) {
                    field.setStyle("-fx-background-color: yellow;" + "-fx-border-color: black;" + "-fx-font-weight: bold;");
                } else if (board[boardPawnFieldIndex] instanceof Jail || board[boardPawnFieldIndex] instanceof GoToJail) {
                    field.setStyle("-fx-background-color: saddlebrown;" + "-fx-border-color: black;" + "-fx-font-weight: bold;");
                } else if (board[boardPawnFieldIndex] instanceof StartField) {
                    field.setStyle("-fx-background-color: skyblue;" + "-fx-border-color: black;" + "-fx-font-weight: bold;");
                } else if (board[boardPawnFieldIndex] instanceof Parking) {
                    field.setStyle("-fx-background-color: lightsteelblue;" + "-fx-border-color: black;" + "-fx-font-weight: bold;");
                }

                HBox pawnPlace = new HBox(4);
                pawnPlace.setAlignment(Pos.CENTER);
                placeForPawnHBox[boardPawnFieldIndex] = pawnPlace;
                field.getChildren().add(pawnPlace);
                assert positionOnHBox != null;
                gameBoardGridPane.add(field, positionOnHBox.getKey(), positionOnHBox.getValue());
            }

            for (HBox place : placeForPawnHBox) {
                place.getChildren().clear();
            }

            playerTableView.getItems().clear();
            for (Player player : players) {
                if (player != null) {
                    playerTableView.getItems().add(player);
                    if (player.getId() < 0) continue;
                    Label l = new Label("G" + player.getId());
                    l.setPadding(new Insets(2, 2, 2, 2));
                    l.setStyle("-fx-background-color:" + player.getColor() + ";-fx-border-color: black;");
                    placeForPawnHBox[player.getPositionOnBoard()].getChildren().add(l);
                }
            }
        });
    }

    public void setDisableGameButtons() {
        Platform.runLater(() -> {
            getEndRoundButton().setDisable(true);
            getBuyBuildingButton().setDisable(true);
            getRollDiceButton().setDisable(true);
        });
    }

    public void displayAlert(String string) {
        if (game.isServerActive()) return;
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("UWAGA!");
            alert.setHeaderText(null);
            alert.initOwner(game.getStage());
            alert.setContentText(string);
            alert.showAndWait();
        });
    }

    public void addLogMessage(String message) {
        Platform.runLater(() -> {
            String timeStamp = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
            infoTextArea.appendText(timeStamp + " " + message + "\n");
        });
    }

}
