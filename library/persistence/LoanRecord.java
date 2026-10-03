package library.persistence;

import java.time.LocalDate;

public record LoanRecord(String mediaId, String userId, LocalDate borrowedAt,
                         LocalDate dueDate, LocalDate returnedAt, double fine) {
    public boolean isReturned() {
        return returnedAt != null;
    }
}