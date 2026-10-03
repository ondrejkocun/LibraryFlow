package library.media;

import java.time.LocalDate;

public final class ReferenceLiterature extends Media {
    private final String category;

    public ReferenceLiterature(String id, String title, String author, int year, String genre, String category) {
        super(id, title, author, year, genre);
        this.category = category == null ? "" : category;
    }

    public String getCategory() { return category; }
    @Override public String getType() { return "reference literature"; }
    @Override public boolean isBorrowingAllowed() {
        int age = LocalDate.now().getYear() - getPublicationYear();
        return super.isBorrowingAllowed() && age <= 30 && !"archive".equalsIgnoreCase(category);
    }
}
