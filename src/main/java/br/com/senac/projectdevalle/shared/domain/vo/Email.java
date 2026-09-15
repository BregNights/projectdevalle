package br.com.senac.projectdevalle.shared.domain.vo;

import java.util.regex.Pattern;

public record Email(String value) {

    private static final Pattern PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public Email {
        if (value == null || !PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email: " + value);
        }
        value = value.trim().toLowerCase();
    }
}
</content>
