package br.com.senac.projectdevalle.shared.domain.vo;

public record Address(
        String street,
        String number,
        String neighborhood,
        String city,
        String state,
        String zipCode,
        String complement
) {

    public Address {
        requireNonBlank(street, "street");
        requireNonBlank(number, "number");
        requireNonBlank(neighborhood, "neighborhood");
        requireNonBlank(city, "city");
        requireNonBlank(state, "state");
        requireNonBlank(zipCode, "zipCode");
        if (state != null) {
            state = state.trim().toUpperCase();
            if (state.length() != 2) {
                throw new IllegalArgumentException("Invalid state: " + state);
            }
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    public String toFullAddressString() {
        StringBuilder builder = new StringBuilder();
        builder.append(street).append(", ").append(number);
        if (complement != null && !complement.isBlank()) {
            builder.append(" - ").append(complement);
        }
        builder.append(", ").append(neighborhood)
                .append(", ").append(city)
                .append(" - ").append(state)
                .append(", ").append(zipCode);
        return builder.toString();
    }
}
</content>
