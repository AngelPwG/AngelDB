package api.dto;

public record InsertRequest(
        long id,
        String name,
        int age
) {
}
