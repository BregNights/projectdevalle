package br.com.senac.projectdevalle.shared.infrastructure.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

// RNF06 — dados gravados antes da criptografia (texto puro) são criptografados uma única vez no start.
// Idempotente: só toca linhas cujo valor ainda não tem o prefixo "enc:v1:".
@Component
class LegacySensitiveDataEncryptionRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LegacySensitiveDataEncryptionRunner.class);

    // Nomes fixos no código (nunca vindos de entrada externa), então é seguro montá-los no SQL.
    private static final Map<String, List<String>> SENSITIVE_COLUMNS = Map.of(
            "producers", List.of("document_number", "bank_name", "bank_agency", "bank_account",
                    "bank_account_holder"),
            "producer_supporting_documents", List.of("document_number"),
            "restaurants", List.of("cnpj"));

    private final JdbcTemplate jdbcTemplate;
    private final FieldEncryptor fieldEncryptor;

    LegacySensitiveDataEncryptionRunner(JdbcTemplate jdbcTemplate, FieldEncryptor fieldEncryptor) {
        this.jdbcTemplate = jdbcTemplate;
        this.fieldEncryptor = fieldEncryptor;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        SENSITIVE_COLUMNS.forEach((table, columns) -> columns.forEach(column -> encryptColumn(table, column)));
    }

    private void encryptColumn(String table, String column) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, " + column + " AS value FROM " + table
                        + " WHERE " + column + " IS NOT NULL AND " + column + " NOT LIKE ?",
                FieldEncryptor.PREFIX + "%");
        for (Map<String, Object> row : rows) {
            jdbcTemplate.update("UPDATE " + table + " SET " + column + " = ? WHERE id = ?",
                    fieldEncryptor.encrypt((String) row.get("value")), row.get("id"));
        }
        if (!rows.isEmpty()) {
            log.info("RNF06: {} valor(es) legado(s) criptografado(s) em {}.{}", rows.size(), table, column);
        }
    }
}
