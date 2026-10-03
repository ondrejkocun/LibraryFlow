package library.media;

public final class Film extends Media {
    public Film(String id, String title, String director, int year, String genre) {
        super(id, title, director, year, genre);
    }

    @Override public String getType() { return "film"; }
    @Override public boolean isBorrowingAllowed() {
        return super.isBorrowingAllowed() && !"horror".equalsIgnoreCase(getGenre());
    }
}
