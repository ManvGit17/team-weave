package io.teamweave.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Keeps docs/diagrams/domain-model.md in step with the code: the committed
 * diagram must equal what the compiled domain generates, so it cannot drift.
 */
class DomainDiagramTest {

    private static final Path DIAGRAM = Path.of("docs", "diagrams", "domain-model.md");

    @Test
    void committedDiagramMatchesTheCode() throws IOException {
        String generated = DomainDiagram.generate();

        if (Boolean.getBoolean("diagram.write")) {
            Files.createDirectories(DIAGRAM.getParent());
            Files.writeString(DIAGRAM, generated);
        }

        assertThat(DIAGRAM)
            .as("diagram missing; run ./mvnw test -Dtest=DomainDiagramTest -Ddiagram.write=true")
            .exists();
        assertThat(Files.readString(DIAGRAM).replace("\r\n", "\n"))
            .as("diagram is stale; regenerate with -Ddiagram.write=true")
            .isEqualTo(generated);
    }
}
