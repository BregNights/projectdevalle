package br.com.senac.projectdevalle.registration.infrastructure.persistence.mapper;

import br.com.senac.projectdevalle.registration.domain.user.User;
import br.com.senac.projectdevalle.registration.infrastructure.persistence.UserJpaEntity;
import br.com.senac.projectdevalle.shared.domain.vo.Email;
import org.springframework.stereotype.Component;

// Mapeamento manual (não MapStruct): User é um agregado com construtor privado e invariantes,
// não um bean simples de getters/setters — reconstituí-lo exige chamar sua factory dedicada.
@Component
public class UserEntityMapper {

    public UserJpaEntity toEntity(User user) {
        return UserJpaEntity.builder()
                .id(user.id())
                .email(user.email().value())
                .passwordHash(user.passwordHash())
                .role(user.role())
                .active(user.active())
                .build();
    }

    public User toDomain(UserJpaEntity entity) {
        return User.reconstitute(entity.getId(), new Email(entity.getEmail()), entity.getPasswordHash(),
                entity.getRole(), entity.isActive());
    }
}
</content>
