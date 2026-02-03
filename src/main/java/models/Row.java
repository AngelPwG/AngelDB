package models;

public record Row(
        long id,
        String name,
        int age
) {
    @Override
    public String toString(){
        return "id=" + id + "| name='" + name + "'| age=" + age + "}";
    }
}
