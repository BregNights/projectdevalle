package br.com.senac.projectdevalle.shared.domain.vo;

// Documento fiscal usado para identificar produtores (CPF ou CNPJ) e restaurantes (CNPJ).
public sealed interface TaxDocument permits Cpf, Cnpj {

    String digits();
}
