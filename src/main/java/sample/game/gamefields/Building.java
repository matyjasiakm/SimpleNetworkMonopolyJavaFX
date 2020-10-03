package sample.game.gamefields;

import sample.game.Player;

public class Building extends Field {

    private Integer value;
    private Player owner;

    public Integer getValue() {
        return value;
    }

    public Building(String _name, Integer _value) {
        name=_name;
        value=_value;
    }

    public Building() {
    }

    public boolean hasOwner(){

        return owner==null?false:true;
    }

    public void setOwner(Player player){

        owner=player;
    }
    public boolean isThisOwner(Player player){
        return player==owner?true:false;
    }
    @Override
    public boolean performAction(Player player) {


        if(player!=owner&& !hasOwner()) {
            player.updateBalance(-value / 2);
            owner.updateBalance(value / 2);
        }

        return player.hasDebet();
    }
    public Integer payValue(){
        return value/2;
    }

    public Player getOwner() {
        return owner;
    }
}
