package br.com.senac.projectdevalle.shared.domain.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CnpjTest {

    @Test
    void acceptsValidCnpj() {
        Cnpj cnpj = new Cnpj("12.345.678/0001-95");

        assertThat(cnpj.digits()).isEqualTo("12345678000195");
    }

    @Test
    void rejectsInvalidCheckDigits() {
        assertThatThrownBy(() -> new Cnpj("12345678000100"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsWrongLength() {
        assertThatThrownBy(() -> new Cnpj("123456780001"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
