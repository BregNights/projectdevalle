package br.com.senac.projectdevalle.shared.domain.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

class CoordinatesTest {

    @Test
    void acceptsValidRange() {
        assertThatNoException().isThrownBy(() -> new Coordinates(-26.9194, -48.6647));
    }

    @Test
    void rejectsLatitudeOutOfRange() {
        assertThatThrownBy(() -> new Coordinates(91, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsLongitudeOutOfRange() {
        assertThatThrownBy(() -> new Coordinates(0, 181))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
