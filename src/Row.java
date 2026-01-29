public record Row(
        long id,
        String name,
        int age
) {
    @Override
    public String toString(){
        return "Row{id=" + id + ", name='" + name + "', age=" + age + "}";
    }
}
