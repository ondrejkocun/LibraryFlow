package library.media;

public abstract class Media extends library.LibrarySystem.Media {
    protected Media(String id, String title, String creator, int year, String genre) {
        super(id, title, creator, year, genre);
    }
}
