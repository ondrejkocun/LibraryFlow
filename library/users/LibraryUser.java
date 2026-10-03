package library.users;

import java.time.LocalDate;

public interface LibraryUser extends BorrowingPolicy {
    String getId();
    String getName();
    String getType();
    int getAge();
    LocalDate getRegistrationDate();
    int getPreviousBorrowings();
    int getBorrowedMediaCount();
    boolean authenticate(String password);
    String getPasswordHash();
    void recordBorrowing();
    void incrementBorrowedMediaCount();
    void decrementBorrowedMediaCount();
}
