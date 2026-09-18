package br.com.senac.projectdevalle.catalog.domain.offer;

import java.util.EnumSet;
import java.util.Set;

public enum ProductCategory {
    VEGETABLES,
    FRUITS,
    FISH,
    MEAT_POULTRY,
    DAIRY,
    GRAINS_CEREALS,
    PROCESSED,
    OTHER;

    private static final Set<ProductCategory> PERISHABLE =
            EnumSet.of(VEGETABLES, FRUITS, FISH, MEAT_POULTRY, DAIRY);

    // RN06 — pescado e hortifruti fresco (entre outros perecíveis) exigem validade obrigatória.
    public boolean isPerishable() {
        return PERISHABLE.contains(this);
    }
}
