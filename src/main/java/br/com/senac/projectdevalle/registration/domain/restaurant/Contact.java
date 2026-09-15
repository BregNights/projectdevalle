package br.com.senac.projectdevalle.registration.domain.restaurant;

public record Contact(String name, String role, String phone, String email) {

    public Contact {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }
}
</content>
