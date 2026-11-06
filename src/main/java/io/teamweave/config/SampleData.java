package io.teamweave.config;

import io.teamweave.domain.student.Availability;
import io.teamweave.domain.student.Role;
import io.teamweave.domain.student.Student;
import io.teamweave.infrastructure.StudentRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Seeds a cohort of sample students when the {@code dev} profile is active and
 * the database is empty. Idempotent, so restarting doesn't duplicate anyone.
 */
@Component
@Profile("dev")
public class SampleData implements CommandLineRunner {

    private final StudentRepository students;

    public SampleData(StudentRepository students) {
        this.students = students;
    }

    @Override
    public void run(String... args) {
        if (students.count() > 0) {
            return;
        }

        Student ava = new Student("Ava Patel", "ava@example.edu");
        ava.addSkill("java", 4).addSkill("sql", 3);
        ava.getAvailability().addAll(List.of(Availability.MON_AM, Availability.WED_AM));
        ava.getInterests().add("backend");
        ava.getDesiredRoles().add(Role.BACKEND);

        Student ben = new Student("Ben Ortiz", "ben@example.edu");
        ben.addSkill("react", 5).addSkill("design", 4);
        ben.getAvailability().addAll(List.of(Availability.MON_AM, Availability.TUE_PM));
        ben.getInterests().add("frontend");
        ben.getDesiredRoles().add(Role.FRONTEND);

        Student cara = new Student("Cara Lin", "cara@example.edu");
        cara.addSkill("python", 5).addSkill("data", 4);
        cara.getAvailability().addAll(List.of(Availability.WED_AM, Availability.THU_PM));
        cara.getInterests().add("ml");
        cara.getDesiredRoles().add(Role.DATA);

        Student dev = new Student("Dev Shah", "dev@example.edu");
        dev.addSkill("java", 3).addSkill("devops", 4);
        dev.getAvailability().addAll(List.of(Availability.MON_AM, Availability.FRI_AM));
        dev.getInterests().add("infra");
        dev.getDesiredRoles().add(Role.BACKEND);

        Student eli = new Student("Eli Novak", "eli@example.edu");
        eli.addSkill("design", 5).addSkill("product", 3);
        eli.getAvailability().addAll(List.of(Availability.TUE_PM, Availability.THU_PM));
        eli.getInterests().add("ux");
        eli.getDesiredRoles().add(Role.DESIGN);

        Student fay = new Student("Fay Reyes", "fay@example.edu");
        fay.addSkill("product", 4).addSkill("qa", 3);
        fay.getAvailability().addAll(List.of(Availability.MON_AM, Availability.WED_AM));
        fay.getInterests().add("planning");
        fay.getDesiredRoles().add(Role.PRODUCT);

        Student gus = new Student("Gus Meyer", "gus@example.edu");
        gus.addSkill("react", 3).addSkill("java", 3);
        gus.getAvailability().addAll(List.of(Availability.TUE_PM, Availability.FRI_AM));
        gus.getInterests().add("fullstack");
        gus.getDesiredRoles().add(Role.FRONTEND);

        Student hana = new Student("Hana Kim", "hana@example.edu");
        hana.addSkill("sql", 4).addSkill("python", 3);
        hana.getAvailability().addAll(List.of(Availability.WED_AM, Availability.THU_PM));
        hana.getInterests().add("data");
        hana.getDesiredRoles().add(Role.QA);

        List<Student> cohort = List.of(ava, ben, cara, dev, eli, fay, gus, hana);
        students.saveAll(cohort);

        // A couple of peer preferences, now that ids exist.
        ava.getPreferredPeerIds().add(cara.getId());
        ben.getPreferredPeerIds().add(gus.getId());
        students.saveAll(List.of(ava, ben));
    }
}
