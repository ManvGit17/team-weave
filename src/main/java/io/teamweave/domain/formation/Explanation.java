package io.teamweave.domain.formation;

/** Why the engine placed a student on a team. */
public record Explanation(String studentName, String teamName, String reason) {
}
