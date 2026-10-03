package library.users;

public final class VipUser extends AbstractUser {
    public VipUser(String id, String name, int age, String password) {
        super(id, name, "vip", age, password);
    }

    @Override public int getMaximumBorrowDays() {
        int previous = getPreviousBorrowings();
        return 14 + (previous < 10 ? 10 : previous <= 30 ? 15 : 20);
    }

    @Override public int getMaximumBorrowedAndReservedItems() {
        long years = java.time.temporal.ChronoUnit.YEARS.between(getRegistrationDate(), java.time.LocalDate.now());
        return 4 + (years < 1 ? 2 : years <= 3 ? 3 : 4);
    }
}
