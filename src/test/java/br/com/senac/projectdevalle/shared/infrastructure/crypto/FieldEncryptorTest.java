package br.com.senac.projectdevalle.shared.infrastructure.crypto;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FieldEncryptorTest {

    private static final String KEY = Base64.getEncoder().encodeToString(new byte[32]);

    private final FieldEncryptor encryptor = new FieldEncryptor(KEY);

    @Test
    void encryptsAndDecryptsBackToThePlaintext() {
        String encrypted = encryptor.encrypt("12345678909");

        assertThat(encrypted).startsWith("enc:v1:").doesNotContain("12345678909");
        assertThat(encryptor.decrypt(encrypted)).isEqualTo("12345678909");
    }

    // IV aleatório: o mesmo valor nunca gera o mesmo texto cifrado (não dá para comparar/inferir documentos).
    @Test
    void usesARandomIvPerEncryption() {
        assertThat(encryptor.encrypt("same")).isNotEqualTo(encryptor.encrypt("same"));
    }

    @Test
    void readsLegacyPlaintextAsIsAndDoesNotDoubleEncrypt() {
        assertThat(encryptor.decrypt("legado")).isEqualTo("legado");
        String encrypted = encryptor.encrypt("x");
        assertThat(encryptor.encrypt(encrypted)).isEqualTo(encrypted);
        assertThat(encryptor.encrypt(null)).isNull();
    }

    @Test
    void failsToDecryptWithAnotherKey() {
        byte[] otherKey = new byte[32];
        otherKey[0] = 1;
        String encrypted = encryptor.encrypt("segredo");

        assertThatThrownBy(() -> new FieldEncryptor(Base64.getEncoder().encodeToString(otherKey)).decrypt(encrypted))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void refusesToStartWithoutAValidKey() {
        assertThatThrownBy(() -> new FieldEncryptor("")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new FieldEncryptor(Base64.getEncoder().encodeToString(new byte[16])))
                .isInstanceOf(IllegalStateException.class);
    }
}
