package br.com.senac.projectdevalle.registration.application.restaurant.command;

import br.com.senac.projectdevalle.registration.domain.restaurant.Contact;
import br.com.senac.projectdevalle.registration.domain.restaurant.EstablishmentCategory;
import br.com.senac.projectdevalle.shared.domain.vo.Address;
import br.com.senac.projectdevalle.shared.domain.vo.Cnpj;
import br.com.senac.projectdevalle.shared.domain.vo.Email;

public record RegisterRestaurantCommand(
        Email email,
        String rawPassword,
        String corporateName,
        Cnpj cnpj,
        EstablishmentCategory category,
        Contact contact,
        String initialAddressLabel,
        Address initialAddress
) {
}
</content>
