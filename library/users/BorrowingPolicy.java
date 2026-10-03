package library.users;

public interface BorrowingPolicy {
    int getMaximumBorrowDays();
    int getMaximumBorrowedAndReservedItems();
}
