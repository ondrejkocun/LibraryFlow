package library.services;

import library.LibrarySystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class MediaSearcher {
    private MediaSearcher() {
    }

    public static List<LibrarySystem.Media> search(HashMap<String, LibrarySystem.Media> media, String query) {
        List<LibrarySystem.Media> results = new ArrayList<>();
        if (media == null || query == null || query.isBlank()) {
            return results;
        }
        String normalizedQuery = query.toLowerCase().trim();
        for (LibrarySystem.Media item : media.values()) {
            if (item.matches(normalizedQuery)) {
                results.add(item);
            }
        }
        return results;
    }
}
