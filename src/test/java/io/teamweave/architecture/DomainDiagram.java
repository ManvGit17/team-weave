package io.teamweave.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.JavaParameterizedType;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Generates a Mermaid class diagram of the domain from the compiled classes,
 * so the diagram is derived from the code rather than drawn by hand.
 */
final class DomainDiagram {

    private DomainDiagram() {
    }

    static String generate() {
        JavaClasses imported = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("io.teamweave.domain");

        List<JavaClass> domain = imported.stream()
            .filter(c -> !c.isAnonymousClass() && !c.getSimpleName().isEmpty())
            .filter(c -> !c.isAssignableTo(Throwable.class))
            .sorted(Comparator.comparing(JavaClass::getSimpleName))
            .toList();
        Set<String> names = domain.stream().map(JavaClass::getSimpleName)
            .collect(Collectors.toSet());

        StringBuilder out = new StringBuilder();
        out.append("# Domain model\n\n")
           .append("> Generated from the compiled code by `DomainDiagramTest`; do not edit by hand.\n")
           .append("> Refresh with `./mvnw test -Dtest=DomainDiagramTest -Ddiagram.write=true`.\n\n")
           .append("```mermaid\nclassDiagram\n");

        Set<String> relations = new TreeSet<>();
        for (JavaClass c : domain) {
            String name = c.getSimpleName();
            List<String> body = new ArrayList<>();
            stereotype(c).ifPresent(s -> body.add("<<" + s + ">>"));

            if (c.isEnum()) {
                c.getEnumConstants().stream().map(e -> e.name()).sorted().forEach(body::add);
            } else {
                c.getFields().stream()
                    .filter(f -> !f.getModifiers().contains(JavaModifier.STATIC))
                    .filter(f -> !f.getModifiers().contains(JavaModifier.SYNTHETIC))
                    .sorted(Comparator.comparing(JavaField::getName))
                    .forEach(f -> body.add("+" + typeName(f) + " " + f.getName()));
            }
            if (c.isInterface()) {
                c.getMethods().stream()
                    .filter(m -> m.getModifiers().contains(JavaModifier.PUBLIC))
                    .filter(m -> !m.getModifiers().contains(JavaModifier.STATIC))
                    .map(m -> m.getName()).distinct().sorted()
                    .forEach(m -> body.add("+" + m + "()"));
            }

            // Mermaid rejects an empty "{ }" body, so a member-less class is
            // declared bare.
            if (body.isEmpty()) {
                out.append("  class ").append(name).append("\n");
            } else {
                out.append("  class ").append(name).append(" {\n");
                body.forEach(line -> out.append("    ").append(line).append("\n"));
                out.append("  }\n");
            }

            for (JavaClass iface : c.getRawInterfaces()) {
                if (names.contains(iface.getSimpleName())) {
                    relations.add(iface.getSimpleName() + " <|.. " + name);
                }
            }
            c.getRawSuperclass().ifPresent(sup -> {
                if (names.contains(sup.getSimpleName())) {
                    relations.add(sup.getSimpleName() + " <|-- " + name);
                }
            });
            for (JavaField f : c.getFields()) {
                if (f.getModifiers().contains(JavaModifier.STATIC)) {
                    continue;
                }
                for (String target : referencedTypes(f)) {
                    if (names.contains(target) && !target.equals(name)) {
                        relations.add(name + " --> " + target);
                    }
                }
            }
        }

        relations.forEach(r -> out.append("  ").append(r).append("\n"));
        out.append("```\n");
        return out.toString();
    }

    private static java.util.Optional<String> stereotype(JavaClass c) {
        if (c.isInterface()) {
            return java.util.Optional.of("interface");
        }
        if (c.isEnum()) {
            return java.util.Optional.of("enumeration");
        }
        if (c.getRawSuperclass().map(s -> s.getName().equals("java.lang.Record")).orElse(false)) {
            return java.util.Optional.of("record");
        }
        if (c.getModifiers().contains(JavaModifier.ABSTRACT)) {
            return java.util.Optional.of("abstract");
        }
        return java.util.Optional.empty();
    }

    private static String typeName(JavaField f) {
        JavaType type = f.getType();
        if (type instanceof JavaParameterizedType p) {
            List<JavaType> args = p.getActualTypeArguments();
            String raw = p.toErasure().getSimpleName();
            if (args.size() == 1) {
                return raw + "~" + args.get(0).toErasure().getSimpleName() + "~";
            }
            return raw;
        }
        return f.getRawType().getSimpleName();
    }

    private static List<String> referencedTypes(JavaField f) {
        List<String> types = new ArrayList<>();
        types.add(f.getRawType().getSimpleName());
        if (f.getType() instanceof JavaParameterizedType p) {
            p.getActualTypeArguments().forEach(a -> types.add(a.toErasure().getSimpleName()));
        }
        return types;
    }
}
