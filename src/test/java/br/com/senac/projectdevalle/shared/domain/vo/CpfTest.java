package br.com.senac.projectdevalle.shared.domain.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfTest {

    @Test
    void acceptsValidCpf() {
        Cpf cpf = new Cpf("123.456.789-09");

        assertThat(cpf.digits()).isEqualTo("12345678909");
    }

    @Test
    void rejectsInvalidCheckDigits() {
        assertThatThrownBy(() -> new Cpf("12345678900"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsAllDigitsEqual() {
        assertThatThrownBy(() -> new Cpf("11111111111"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsWrongLength() {
        assertThatThrownBy(() -> new Cpf("123456789"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
</content>
