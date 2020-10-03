package sample.game.gamefields;

public abstract class Field implements ActionWithPlayer {
    String name;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
