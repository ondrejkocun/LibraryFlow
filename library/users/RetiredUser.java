package library.users;

public final class RetiredUser extends AbstractUser {
    public RetiredUser(String id, String name, int age, String password) {
        super(id, name, "retired", age, password);
    }

    @Override public int getMaximumBorrowDays() {
        return 7 + (getAge() < 65 ? 5 : getAge() <= 75 ? 7 : 9);
    }

    @Override public int getMaximumBorrowedAndReservedItems() {
        return 2 + (getAge() < 70 ? 1 : 2);
    }
}
