package library.tests;

import library.LibrarySystem;
import library.persistence.CsvManager;
import library.users.Student;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public final class LibrarySystemTest {
    private LibrarySystemTest() {
    }

    public static void main(String[] args) throws Exception {
        LibrarySystem.Library library = new LibrarySystem.Library();
        Student alice = new Student("TEST-USER", "Alice", 18, "secret");
        Student bob = new Student("OTHER-USER", "Bob", 18, "other");
        library.addUser(alice);
        library.addUser(bob);
        library.addMedia(new LibrarySystem.Book("TEST-MEDIA", "Test Book", "Author",
                2024, "test", "Publisher"));

        assert library.authenticate("TEST-USER", "secret") == alice;
        library.borrow("TEST-MEDIA", "TEST-USER");
        assert library.getMedia().get("TEST-MEDIA").getBorrowedBy().equals("TEST-USER");
        try {
            library.returnMedia("TEST-MEDIA", "OTHER-USER");
            throw new AssertionError("A different user returned the media");
        } catch (IllegalStateException expected) {
            // Expected ownership check.
        }
        library.returnMedia("TEST-MEDIA", "TEST-USER");
        assert alice.getBorrowedMediaCount() == 0;

        Path mediaFile = Path.of("test-media.csv");
        CsvManager.saveMedia(new HashMap<>(library.getMedia()), mediaFile.toString());
        assert CsvManager.loadMedia(mediaFile.toString()).size() == 1;
        Files.deleteIfExists(mediaFile);
        System.out.println("All library tests passed.");
    }
}
