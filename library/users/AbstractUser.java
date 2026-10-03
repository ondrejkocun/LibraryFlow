package library.users;

import java.time.LocalDate;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Objects;

public abstract class AbstractUser implements LibraryUser {
    private final String id;
    private final String name;
    private final String type;
    private final int age;
    private final String password;
    private final LocalDate registrationDate;
    private int previousBorrowings;
    private int borrowedMediaCount;

    protected AbstractUser(String id, String name, String type, int age, String password) {
        this.id = requireText(id, "id");
        this.name = requireText(name, "name");
        this.type = requireText(type, "type");
        this.age = age;
        String validPassword = requireText(password, "password");
        this.password = validPassword.startsWith("sha256$")
            ? validPassword.substring("sha256$".length())
            : hash(validPassword);
        this.registrationDate = LocalDate.now();
    }

    protected AbstractUser(String id, String name, String type, int age) {
        this(id, name, type, age, name.toLowerCase(Locale.ROOT));
    }

    @Override public String getId() { return id; }
    @Override public String getName() { return name; }
    @Override public String getType() { return type; }
    @Override public int getAge() { return age; }
    @Override public LocalDate getRegistrationDate() { return registrationDate; }
    @Override public int getPreviousBorrowings() { return previousBorrowings; }
    @Override public int getBorrowedMediaCount() { return borrowedMediaCount; }
    @Override public boolean authenticate(String candidate) {
        return candidate != null && password.equals(hash(candidate));
    }
    @Override public String getPasswordHash() { return password; }
    @Override public void recordBorrowing() { previousBorrowings++; }
    @Override public void incrementBorrowedMediaCount() { borrowedMediaCount++; }
    @Override public void decrementBorrowedMediaCount() {
        if (borrowedMediaCount > 0) borrowedMediaCount--;
    }

    private static String requireText(String value, String field) {
        return Objects.requireNonNull(value, field + " must not be null").isBlank()
                ? throwBlank(field) : value;
    }

    private static String throwBlank(String field) {
        throw new IllegalArgumentException(field + " must not be blank");
    }

    private static String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte valueByte : digest) {
                result.append(String.format("%02x", valueByte));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
