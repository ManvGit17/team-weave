package io.teamweave.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import io.teamweave.domain.command.Command;
import io.teamweave.domain.constraint.Constraint;
import io.teamweave.domain.matching.MatchingStrategy;
import io.teamweave.domain.team.TeamChangeListener;
import io.teamweave.domain.team.TeamState;

/**
 * The architecture rules, enforced as tests: a broken layer or a pattern in
 * the wrong place fails the build.
 */
@AnalyzeClasses(packages = "io.teamweave", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule layers = layeredArchitecture()
        .consideringOnlyDependenciesInLayers()
        .layer("Web").definedBy("io.teamweave.web..")
        .layer("Application").definedBy("io.teamweave.application..")
        .layer("Domain").definedBy("io.teamweave.domain..")
        .layer("Infrastructure").definedBy("io.teamweave.infrastructure..")
        .layer("Config").definedBy("io.teamweave.config..")
        .whereLayer("Web").mayNotBeAccessedByAnyLayer()
        .whereLayer("Application").mayOnlyBeAccessedByLayers("Web", "Config")
        .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Application", "Config")
        .whereLayer("Config").mayNotBeAccessedByAnyLayer();

    @ArchTest
    static final ArchRule domainIsFrameworkFree = noClasses()
        .that().resideInAPackage("io.teamweave.domain..")
        .should().dependOnClassesThat().resideInAnyPackage(
            "org.springframework..", "io.teamweave.web..",
            "io.teamweave.application..", "io.teamweave.infrastructure..")
        .because("the domain holds the design; it must not know about frameworks or outer layers");

    @ArchTest
    static final ArchRule strategiesLiveInMatching = classes()
        .that().implement(MatchingStrategy.class)
        .should().resideInAPackage("io.teamweave.domain.matching");

    @ArchTest
    static final ArchRule constraintsLiveInConstraint = classes()
        .that().implement(Constraint.class)
        .should().resideInAPackage("io.teamweave.domain.constraint");

    @ArchTest
    static final ArchRule commandsLiveInCommand = classes()
        .that().implement(Command.class)
        .should().resideInAPackage("io.teamweave.domain.command");

    @ArchTest
    static final ArchRule statesLiveInTeam = classes()
        .that().implement(TeamState.class)
        .should().resideInAPackage("io.teamweave.domain.team");

    @ArchTest
    static final ArchRule observersAreInDomainOrApplication = classes()
        .that().implement(TeamChangeListener.class)
        .should().resideInAnyPackage("io.teamweave.domain..", "io.teamweave.application..");
}
