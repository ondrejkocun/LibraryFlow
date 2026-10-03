package library.media;

public final class Book extends Media {
    private final String publisher;

    public Book(String id, String title, String author, int year, String genre, String publisher) {
        super(id, title, author, year, genre);
        this.publisher = publisher == null ? "" : publisher;
    }

    public String getPublisher() { return publisher; }
    @Override public String getType() { return "book"; }
    @Override public boolean isBorrowingAllowed() {
        return super.isBorrowingAllowed() && !"rarity".equalsIgnoreCase(getGenre());
    }
}
