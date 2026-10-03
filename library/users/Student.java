package library.users;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class Student extends AbstractUser {
    public Student(String id, String name, int age, String password) {
        super(id, name, "student", age, password);
    }

    @Override public int getMaximumBorrowDays() {
        long months = ChronoUnit.MONTHS.between(getRegistrationDate(), LocalDate.now());
        return 7 + (months < 6 ? 5 : months <= 12 ? 7 : 9);
    }

    @Override public int getMaximumBorrowedAndReservedItems() {
        int previous = getPreviousBorrowings();
        return 2 + (previous < 10 ? 2 : previous <= 20 ? 3 : 4);
    }
}
