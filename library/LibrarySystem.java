package library;

import library.persistence.CsvManager;
import library.users.Administrator;
import library.users.LibraryUser;
import library.users.RetiredUser;
import library.users.StandardUser;
import library.users.Student;
import library.users.VipUser;
import library.persistence.LoanRecord;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Self-contained English refactoring of the recoverable library domain model.
 * The original text contained several concatenated and duplicated source files,
 * so the related classes are kept as nested types in this recovery version.
 */
public final class LibrarySystem {
    private LibrarySystem() {
    }

    public static void main(String[] args) {
        Library library = new Library();
        try {
            for (Media item : CsvManager.loadMedia("media.csv")) {
                library.addMedia(item);
            }
            for (LibraryUser user : CsvManager.loadUsers("users.csv")) {
                library.addUser(user);
            }
            library.restoreUserMediaCounts();
        } catch (Exception exception) {
            System.err.println("Could not load CSV data: " + exception.getMessage());
            return;
        }

        System.out.println("Loaded media: " + library.getMedia().size());
        System.out.println("Loaded users: " + library.getUsers().size());
        System.out.println(library.getStatistics());
    }

    public static final class Library {
        private final Map<String, Media> media = new HashMap<>();
        private final Map<String, LibraryUser> users = new HashMap<>();
        private final List<LoanRecord> loanHistory = new ArrayList<>();

        public void addMedia(Media item) {
            Objects.requireNonNull(item, "Media must not be null");
            media.put(item.getId(), item);
        }

        public void addUser(LibraryUser user) {
            Objects.requireNonNull(user, "User must not be null");
            users.put(user.getId(), user);
        }

        public LibraryUser authenticate(String userId, String password) {
            LibraryUser user = users.get(userId);
            if (user == null || !user.authenticate(password)) {
                throw new IllegalArgumentException("Invalid user ID or password");
            }
            return user;
        }

        public Map<String, Media> getMedia() {
            return Collections.unmodifiableMap(media);
        }

        public Map<String, LibraryUser> getUsers() {
            return Collections.unmodifiableMap(users);
        }

        public void restoreUserMediaCounts() {
            for (Media item : media.values()) {
                String userId = item.isBorrowed() ? item.getBorrowedBy() : item.getReservedBy();
                LibraryUser user = users.get(userId);
                if (user != null) {
                    user.incrementBorrowedMediaCount();
                }
            }
        }

        public List<Media> search(String query) {
            if (query == null || query.isBlank()) {
                return Collections.emptyList();
            }
            String normalizedQuery = query.toLowerCase(Locale.ROOT).trim();
            List<Media> results = new ArrayList<>();
            for (Media item : media.values()) {
                if (item.matches(normalizedQuery)) {
                    results.add(item);
                }
            }
            return results;
        }

        public void borrow(String mediaId, String userId) {
            Media item = getMedia(mediaId);
            LibraryUser user = getUser(userId);
            if (item.isBorrowed()) {
                throw new IllegalStateException("Media is already borrowed: " + item.getTitle());
            }
            if (!item.isBorrowingAllowed()) {
                throw new IllegalStateException("Media cannot be borrowed: " + item.getTitle());
            }
            if (user.getBorrowedMediaCount() >= user.getMaximumBorrowedAndReservedItems()) {
                throw new IllegalStateException("User has reached the borrowing limit");
            }
            item.borrowedBy = userId;
            item.borrowedAt = LocalDate.now();
            item.dueDate = LocalDate.now().plusDays(user.getMaximumBorrowDays());
            user.incrementBorrowedMediaCount();
            user.recordBorrowing();
            loanHistory.add(new LoanRecord(mediaId, userId, item.getBorrowedAt(), item.getDueDate(), null, 0));
        }

        public double returnMedia(String mediaId, String userId) {
            Media item = getMedia(mediaId);
            if (item.borrowedBy == null) {
                throw new IllegalStateException("Media is not borrowed");
            }
            if (userId == null || !userId.equals(item.borrowedBy)) {
                throw new IllegalStateException("Only the borrowing user can return this media");
            }
            double fine = item.getFine();
            if (item.borrowedBy != null) {
                LibraryUser borrower = users.get(item.borrowedBy);
                if (borrower != null) {
                    borrower.decrementBorrowedMediaCount();
                }
            }
            item.borrowedBy = null;
            item.borrowedAt = null;
            item.dueDate = null;
            for (int index = loanHistory.size() - 1; index >= 0; index--) {
                LoanRecord record = loanHistory.get(index);
                if (record.mediaId().equals(mediaId) && !record.isReturned()) {
                    loanHistory.set(index, new LoanRecord(record.mediaId(), record.userId(),
                            record.borrowedAt(), record.dueDate(), LocalDate.now(), fine));
                    break;
                }
            }
            return fine;
        }

        public List<LoanRecord> getLoanHistory() {
            return Collections.unmodifiableList(loanHistory);
        }

        public void restoreLoanHistory(List<LoanRecord> records) {
            loanHistory.clear();
            loanHistory.addAll(records);
        }

        public double returnMedia(String mediaId) {
            Media item = getMedia(mediaId);
            return returnMedia(mediaId, item.borrowedBy);
        }

        public void cancelReservation(String mediaId, String userId) {
            Media item = getMedia(mediaId);
            if (!item.isReserved()) {
                throw new IllegalStateException("Media is not reserved: " + item.getTitle());
            }
            if (!userId.equals(item.getReservedBy())) {
                throw new IllegalStateException("Only the reserving user can cancel this reservation");
            }
            LibraryUser user = users.get(item.getReservedBy());
            if (user != null) {
                user.decrementBorrowedMediaCount();
            }
            item.setReservedBy(null);
        }

        public void reserve(String mediaId, String userId) {
            Media item = getMedia(mediaId);
            LibraryUser user = getUser(userId);
            if (item.isBorrowed() || item.isReserved()) {
                throw new IllegalStateException("Media is not available: " + item.getTitle());
            }
            if (user.getBorrowedMediaCount() >= user.getMaximumBorrowedAndReservedItems()) {
                throw new IllegalStateException("User has reached the borrowing limit");
            }
            item.reservedBy = userId;
            item.setReserved(true);
            user.incrementBorrowedMediaCount();
        }

        public String getStatistics() {
            int borrowed = 0;
            int reserved = 0;
            for (Media item : media.values()) {
                if (item.isBorrowed()) {
                    borrowed++;
                } else if (item.isReserved()) {
                    reserved++;
                }
            }
            return "Library statistics: total=" + media.size()
                    + ", borrowed=" + borrowed
                    + ", reserved=" + reserved
                    + ", available=" + (media.size() - borrowed - reserved);
        }

        private Media getMedia(String id) {
            Media item = media.get(id);
            if (item == null) {
                throw new IllegalArgumentException("Unknown media id: " + id);
            }
            return item;
        }

        private LibraryUser getUser(String id) {
            LibraryUser user = users.get(id);
            if (user == null) {
                throw new IllegalArgumentException("Unknown user id: " + id);
            }
            return user;
        }
    }

    public abstract static class Media {
        private final String id;
        private final String title;
        private final String creator;
        private final int publicationYear;
        private final String genre;
        private String borrowedBy;
        private String reservedBy;
        private LocalDate borrowedAt;
        private LocalDate dueDate;

            public String getBorrowedBy() { return borrowedBy; }
            public String getReservedBy() { return reservedBy; }
            public void setBorrowedAt(LocalDate borrowedAt) { this.borrowedAt = borrowedAt; }
            public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
            public LocalDate getBorrowedAt() { return borrowedAt; }
            public LocalDate getDueDate() { return dueDate; }
            public long getDaysOverdue() {
                return dueDate == null ? 0 : Math.max(0, ChronoUnit.DAYS.between(dueDate, LocalDate.now()));
            }
            public double getFine() { return getDaysOverdue() * 1.50; }
            public void setBorrowedBy(String borrowedBy) { this.borrowedBy = borrowedBy; }
            public void setReservedBy(String reservedBy) {
                this.reservedBy = reservedBy;
                this.reserved = reservedBy != null;
            }
            public String getDetailedDescription() {
                return getType() + ": " + title + ", creator: " + creator
                    + ", genre: " + genre + ", year: " + publicationYear;
            }

                public boolean isBorrowingAllowed() {
                    return !isBorrowed() && !isReserved();
                }
        private boolean reserved;

        protected Media(String id, String title, String creator, int publicationYear, String genre) {
            this.id = requireText(id, "id");
            this.title = requireText(title, "title");
            this.creator = creator == null ? "" : creator;
            this.publicationYear = publicationYear;
            this.genre = genre == null ? "" : genre;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getCreator() {
            return creator;
        }

        public int getPublicationYear() {
            return publicationYear;
        }

        public String getGenre() {
            return genre;
        }

        public boolean isBorrowed() {
            return borrowedBy != null;
        }

        public boolean isReserved() {
            return reserved;
        }

        public void setReserved(boolean reserved) {
            this.reserved = reserved;
        }

        public boolean matches(String query) {
            return title.toLowerCase(Locale.ROOT).contains(query)
                    || creator.toLowerCase(Locale.ROOT).contains(query)
                    || genre.toLowerCase(Locale.ROOT).contains(query);
        }

        public abstract String getType();

        @Override
        public String toString() {
            return getType() + "{id='" + id + "', title='" + title + "', creator='" + creator + "'}";
        }
    }

    public static final class Book extends Media {
        private final String publisher;

        public Book(String id, String title, String author, int year, String genre) {
            this(id, title, author, year, genre, "");
        }

        public Book(String id, String title, String author, int year, String genre, String publisher) {
            super(id, title, author, year, genre);
            this.publisher = publisher == null ? "" : publisher;
        }

        public String getPublisher() {
            return publisher;
        }

        @Override
        public String getType() {
            return "book";
        }

        @Override
        public boolean isBorrowingAllowed() {
            return super.isBorrowingAllowed() && !"rarity".equalsIgnoreCase(getGenre());
        }
    }

    public static final class Film extends Media {
        public Film(String id, String title, String director, int year, String genre) {
            super(id, title, director, year, genre);
        }

        @Override
        public String getType() {
            return "film";
        }

        @Override
        public boolean isBorrowingAllowed() {
            return super.isBorrowingAllowed() && !"horror".equalsIgnoreCase(getGenre());
        }
    }

    public static final class Audiobook extends Media {
        private final int durationMinutes;

        public Audiobook(String id, String title, String author, int year, String genre, int durationMinutes) {
            super(id, title, author, year, genre);
            if (durationMinutes < 0) {
                throw new IllegalArgumentException("Duration cannot be negative");
            }
            this.durationMinutes = durationMinutes;
        }

        public int getDurationMinutes() {
            return durationMinutes;
        }

        public int getDuration() {
            return durationMinutes;
        }

        public int getDurationMinutesLegacy() {
            return durationMinutes;
        }

        @Override
        public String getType() {
            return "audiobook";
        }
    }

    public static final class EBook extends Media {
        private final String format;

        public EBook(String id, String title, String author, int year, String genre, String format) {
            super(id, title, author, year, genre);
            this.format = requireText(format, "format");
        }

        public String getFormat() {
            return format;
        }

        @Override
        public String getType() {
            return "ebook";
        }
    }

    public static final class ReferenceLiterature extends Media {
        private final String category;

        public ReferenceLiterature(String id, String title, String author, int year, String genre, String category) {
            super(id, title, author, year, genre);
            this.category = category == null ? "" : category;
        }

        public String getCategory() {
            return category;
        }

        @Override
        public String getType() {
            return "reference literature";
        }

        @Override
        public boolean isBorrowingAllowed() {
            int age = LocalDate.now().getYear() - getPublicationYear();
            return super.isBorrowingAllowed() && age <= 30 && !"archive".equalsIgnoreCase(category);
        }
    }

    public static final class Magazine extends Media {
        private final String frequency;

        public Magazine(String id, String title, String publisher, int year, String genre, String frequency) {
            super(id, title, publisher, year, genre);
            this.frequency = requireText(frequency, "frequency");
        }

        public String getFrequency() {
            return frequency;
        }

        @Override
        public String getType() {
            return "magazine";
        }

        @Override
        public boolean isBorrowingAllowed() {
            int age = LocalDate.now().getYear() - getPublicationYear();
            return super.isBorrowingAllowed() && age <= 2 && "weekly".equalsIgnoreCase(frequency);
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
