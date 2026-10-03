package library.services;

import library.LibrarySystem;
import java.util.HashMap;

public final class StatisticsService {
    private StatisticsService() {
    }

    public static String getStatistics(HashMap<String, LibrarySystem.Media> media) {
        int borrowed = 0;
        int reserved = 0;
        int available = 0;
        int books = 0;
        int films = 0;
        int audiobooks = 0;
        int ebooks = 0;
        int referenceLiterature = 0;
        int magazines = 0;

        for (LibrarySystem.Media item : media.values()) {
            switch (item.getType()) {
                case "book" -> books++;
                case "film" -> films++;
                case "audiobook" -> audiobooks++;
                case "ebook" -> ebooks++;
                case "reference literature" -> referenceLiterature++;
                case "magazine" -> magazines++;
                default -> {
                }
            }
            if (item.isBorrowed()) {
                borrowed++;
            } else if (item.isReserved()) {
                reserved++;
            } else {
                available++;
            }
        }

        return "Library statistics:\n\n"
                + "Total media: " + media.size() + "\n"
                + "Books: " + books + "\n"
                + "Films: " + films + "\n"
                + "Audiobooks: " + audiobooks + "\n"
                + "E-books: " + ebooks + "\n"
                + "Reference literature: " + referenceLiterature + "\n"
                + "Magazines: " + magazines + "\n\n"
                + "Borrowed: " + borrowed + "\n"
                + "Reserved: " + reserved + "\n"
                + "Available: " + available;
    }
}
