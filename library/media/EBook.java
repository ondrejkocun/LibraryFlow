package library.media;

public final class EBook extends Media {
    private final String format;

    public EBook(String id, String title, String author, int year, String genre, String format) {
        super(id, title, author, year, genre);
        if (format == null || format.isBlank()) throw new IllegalArgumentException("Format must not be blank");
        this.format = format;
    }

    public String getFormat() { return format; }
    @Override public String getType() { return "ebook"; }
}
