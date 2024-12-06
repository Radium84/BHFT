package helpers.datahelpers.models;

public class Ws {
    private String type;
    private Todos data;

    public Ws(String type, Todos data) {
        this.type = type;
        this.data = data;
    }

    public Ws() {

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Todos getData() {
        return data;
    }

    public void setData(Todos data) {
        this.data = data;
    }
}
