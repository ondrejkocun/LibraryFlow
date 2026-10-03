package library.media;

public final class Audiobook extends Media {
    private final int durationMinutes;

    public Audiobook(String id, String title, String author, int year, String genre, int durationMinutes) {
        super(id, title, author, year, genre);
        if (durationMinutes < 0) throw new IllegalArgumentException("Duration cannot be negative");
        this.durationMinutes = durationMinutes;
    }

    public int getDurationMinutes() { return durationMinutes; }
    @Override public String getType() { return "audiobook"; }
}
