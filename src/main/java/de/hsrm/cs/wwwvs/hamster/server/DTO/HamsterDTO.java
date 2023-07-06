package de.hsrm.cs.wwwvs.hamster.server.DTO;

public class HamsterDTO {
    private String name;
    private String owner;
    private Integer treats;
    private Integer price;

    public HamsterDTO() {

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

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}
