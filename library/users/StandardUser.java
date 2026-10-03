package library.users;

public final class StandardUser extends AbstractUser {
    public StandardUser(String id, String name, int age, String password) {
        super(id, name, "standard", age, password);
    }

    @Override public int getMaximumBorrowDays() { return 14; }
    @Override public int getMaximumBorrowedAndReservedItems() { return 3; }
}
