package br.com.senac.projectdevalle.shared.infrastructure.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

// RNF06 — aplicado via @Convert nos campos sensíveis das entidades JPA. É um bean Spring (o Hibernate usa o
// container do Spring para instanciar conversores), por isso recebe o FieldEncryptor por injeção.
@Component
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private final FieldEncryptor fieldEncryptor;

    public EncryptedStringConverter(FieldEncryptor fieldEncryptor) {
        this.fieldEncryptor = fieldEncryptor;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return fieldEncryptor.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return fieldEncryptor.decrypt(dbData);
    }
}
