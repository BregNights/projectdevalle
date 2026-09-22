package br.com.senac.projectdevalle.shared.domain;

import java.text.Normalizer;
import java.util.Locale;

// Comparação de nomes de municípios/regiões sem depender de acento, caixa ou espaços extras
// ("Itajai" = "Itajaí", "balneario  camboriu" = "Balneário Camboriú").
public final class PlaceNames {

    private PlaceNames() {
    }

    public static String normalize(String name) {
        if (name == null) {
            return "";
        }
        String withoutAccents = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return withoutAccents.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    public static boolean sameName(String first, String second) {
        return normalize(first).equals(normalize(second));
    }
}
