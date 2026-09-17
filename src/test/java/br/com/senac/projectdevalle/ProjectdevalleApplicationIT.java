package br.com.senac.projectdevalle;

import br.com.senac.projectdevalle.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

// Smoke test do contexto completo (Postgres real via Testcontainers) — por isso é *IT, não *Test,
// e só roda em `mvn verify`, não em `mvn test` (que não deve depender de Docker).
class ProjectdevalleApplicationIT extends AbstractIntegrationTest {

	@Test
	void contextLoads() {
	}

}
