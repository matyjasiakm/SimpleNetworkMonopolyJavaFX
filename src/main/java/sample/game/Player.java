package sample.game;

public class Player {

    private String name;
    private Integer id;
    private Integer money;
    private Integer positionOnBoard;
    private String color;
    private Boolean inJail;


    public Player() {
    }

    public Player(Integer id) {
        this.id = id;
        money = 0;
        color = "white";
        inJail = false;
        positionOnBoard = 0;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPositionOnBoard() {
        return positionOnBoard;
    }

    public void setPositionOnBoard(Integer positionOnBoard) {
        this.positionOnBoard = positionOnBoard;
    }

    public void updateBalance(Integer value) {
        money += value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getInJail() {
        return inJail;
    }

    public void setInJail(Boolean inJail) {
        this.inJail = inJail;
    }

    public boolean hasDebet() {
        return money < 0;
    }
}
