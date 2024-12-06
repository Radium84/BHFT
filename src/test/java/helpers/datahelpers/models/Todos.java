package datahelpers.models;



public class Todos {
    private Long id;
    private String text;
    private Boolean completed;

    public Todos(Long id, String text, Boolean completed) {
        this.id = id;
        this.text = text;
        this.completed = completed;
    }
    public Todos(){

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
