package library.users;

public final class Administrator extends AbstractUser {
    public Administrator(String id, String name, int age, String password) {
        super(id, name, "administrator", age, password);
    }

    @Override public int getMaximumBorrowDays() { return 0; }
    @Override public int getMaximumBorrowedAndReservedItems() { return 0; }
}
