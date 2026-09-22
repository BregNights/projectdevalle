package br.com.senac.projectdevalle.catalog.domain.offer.exception;

import br.com.senac.projectdevalle.shared.domain.BusinessRuleViolationException;

public class ProductCategoryNotEnabledException extends BusinessRuleViolationException {

    public ProductCategoryNotEnabledException(String category) {
        super("Product category is not enabled on the platform: " + category);
    }
}
