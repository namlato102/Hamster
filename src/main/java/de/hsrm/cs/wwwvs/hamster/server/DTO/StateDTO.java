package de.hsrm.cs.wwwvs.hamster.server.DTO;

public class StateDTO {
    private String name;
    private String owner;
    private Integer treats;
    private Integer turns;
    private Integer cost;

    public StateDTO() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Integer getTreats() {
        return treats;
    }

    public void setTreats(Integer treats) {
        this.treats = treats;
    }

    public Integer getTurns() {
        return turns;
    }

    public void setTurns(Integer turns) {
        this.turns = turns;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }
}
