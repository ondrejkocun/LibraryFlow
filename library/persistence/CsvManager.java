package library.persistence;

import library.LibrarySystem;
import library.media.Audiobook;
import library.media.Book;
import library.media.EBook;
import library.media.Film;
import library.media.Magazine;
import library.media.ReferenceLiterature;
import library.users.Administrator;
import library.users.LibraryUser;
import library.users.RetiredUser;
import library.users.StandardUser;
import library.users.Student;
import library.users.VipUser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CsvManager {
    private CsvManager() {
    }

    public static void saveMedia(HashMap<String, LibrarySystem.Media> media, String fileName) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(fileName))) {
            writer.write("id;title;creator;year;genre;type;status;extra;borrowedBy;reservedBy;borrowedAt;dueDate");
            writer.newLine();
            for (LibrarySystem.Media item : media.values()) {
                writer.write(joinEscaped(';', item.getId(), item.getTitle(), item.getCreator(),
                    String.valueOf(item.getPublicationYear()), item.getGenre(), item.getType(),
                    item.isBorrowed() ? "borrowed" : item.isReserved() ? "reserved" : "available",
                    getExtra(item), valueOrEmpty(item.getBorrowedBy()), valueOrEmpty(item.getReservedBy()),
                    valueOrEmpty(item.getBorrowedAt()), valueOrEmpty(item.getDueDate())));
                writer.newLine();
            }
        }
    }

    public static List<LibraryUser> loadUsers(String fileName) throws IOException {
        List<LibraryUser> users = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Path.of(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String[] columns = splitLine(line, ',');
                    if (columns.length < 2 || columns[0].equalsIgnoreCase("id")) {
                        continue;
                    }
                    String id = columns[0].isBlank() ? "U-" + (users.size() + 1) : columns[0].trim();
                    String name = columns[1].trim();
                    String type = columns.length > 2 ? columns[2].trim().toLowerCase() : "standard";
                    String password = columns.length > 3 && !columns[3].isBlank() ? columns[3].trim() : name.toLowerCase();
                    int age = columns.length > 4 && !columns[4].isBlank() ? Integer.parseInt(columns[4].trim()) : 18;
                    if (!name.isBlank()) {
                        users.add(createUser(id, name, type, age, password));
                    }
                } catch (RuntimeException exception) {
                    Logger.log("Skipping invalid user row: " + line + " (" + exception.getMessage() + ")");
                }
            }
        }
        return users;
    }

    public static void saveUsers(Map<String, LibraryUser> users, String fileName) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(fileName))) {
            writer.write("id,name,type,password,age");
            writer.newLine();
            for (LibraryUser user : users.values()) {
                writer.write(joinEscaped(',', user.getId(), user.getName(), user.getType(),
                    "sha256$" + user.getPasswordHash(), String.valueOf(user.getAge())));
                writer.newLine();
            }
        }
    }

    public static void saveLoanHistory(List<LoanRecord> records, String fileName) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(fileName))) {
            writer.write("mediaId,userId,borrowedAt,dueDate,returnedAt,fine");
            writer.newLine();
            for (LoanRecord record : records) {
                writer.write(joinEscaped(',', record.mediaId(), record.userId(),
                        valueOrEmpty(record.borrowedAt()), valueOrEmpty(record.dueDate()),
                        valueOrEmpty(record.returnedAt()), String.valueOf(record.fine())));
                writer.newLine();
            }
        }
    }

    public static List<LoanRecord> loadLoanHistory(String fileName) throws IOException {
        List<LoanRecord> records = new ArrayList<>();
        Path path = Path.of(fileName);
        if (!Files.exists(path)) return records;
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String[] columns = splitLine(line, ',');
                    if (columns.length < 6 || columns[0].equalsIgnoreCase("mediaId")) continue;
                    records.add(new LoanRecord(columns[0], columns[1], LocalDate.parse(columns[2]),
                            LocalDate.parse(columns[3]), columns[4].isBlank() ? null : LocalDate.parse(columns[4]),
                            Double.parseDouble(columns[5])));
                } catch (RuntimeException exception) {
                    Logger.log("Skipping invalid loan history row: " + line);
                }
            }
        }
        return records;
    }

    public static List<LibrarySystem.Media> loadMedia(String fileName) throws IOException {
        List<LibrarySystem.Media> media = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Path.of(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String[] columns = splitLine(line, ';');
                    if (columns.length < 6 || columns[0].equalsIgnoreCase("id")) {
                        continue;
                    }
                    String id = columns[0].trim();
                    String title = columns[1].trim();
                    String creator = columns[2].trim();
                    int year = Integer.parseInt(columns[3].trim());
                    String genre = columns[4].trim();
                    String type = columns[5].trim().toLowerCase();
                    String status = columns.length > 6 ? columns[6].trim().toLowerCase() : "available";
                    String extra = columns.length > 7 ? columns[7].trim() : "";
                    String borrowedBy = columns.length > 8 ? columns[8].trim() : "";
                    String reservedBy = columns.length > 9 ? columns[9].trim() : "";
                    String borrowedAt = columns.length > 10 ? columns[10].trim() : "";
                    String dueDate = columns.length > 11 ? columns[11].trim() : "";
                    LibrarySystem.Media item = createMedia(id, title, creator, year, genre, type, extra);
                    if ("borrowed".equals(status)) {
                        item.setBorrowedBy(borrowedBy.isBlank() ? "imported" : borrowedBy);
                        if (!borrowedAt.isBlank()) item.setBorrowedAt(LocalDate.parse(borrowedAt));
                        if (!dueDate.isBlank()) item.setDueDate(LocalDate.parse(dueDate));
                    } else if ("reserved".equals(status)) {
                        item.setReservedBy(reservedBy.isBlank() ? "imported" : reservedBy);
                    }
                    media.add(item);
                } catch (RuntimeException exception) {
                    Logger.log("Skipping invalid media row: " + line + " (" + exception.getMessage() + ")");
                }
            }
        }
        return media;
    }

    private static LibraryUser createUser(String id, String name, String type, int age, String password) {
        return switch (type) {
            case "administrator", "admin" -> new Administrator(id, name, age, password);
            case "student" -> new Student(id, name, age, password);
            case "vip" -> new VipUser(id, name, age, password);
            case "retired" -> new RetiredUser(id, name, age, password);
            default -> new StandardUser(id, name, age, password);
        };
    }

    private static LibrarySystem.Media createMedia(String id, String title, String creator, int year,
                                                    String genre, String type, String extra) {
        return switch (type) {
                case "book" -> new Book(id, title, creator, year, genre, extra);
                case "film" -> new Film(id, title, creator, year, genre);
                case "audiobook" -> new Audiobook(id, title, creator, year, genre,
                    Integer.parseInt(extra.isBlank() ? "0" : extra));
                case "ebook" -> new EBook(id, title, creator, year, genre,
                    extra.isBlank() ? "PDF" : extra);
                case "reference literature" -> new ReferenceLiterature(id, title, creator,
                    year, genre, extra);
                case "magazine" -> new Magazine(id, title, creator, year, genre,
                    extra.isBlank() ? "weekly" : extra);
            default -> throw new IllegalArgumentException("Unknown media type: " + type);
        };
    }

    private static String getExtra(LibrarySystem.Media item) {
        if (item instanceof Book book) {
            return book.getPublisher();
        }
        if (item instanceof Audiobook audiobook) {
            return String.valueOf(audiobook.getDurationMinutes());
        }
        if (item instanceof EBook ebook) {
            return ebook.getFormat();
        }
        if (item instanceof ReferenceLiterature reference) {
            return reference.getCategory();
        }
        if (item instanceof Magazine magazine) {
            return magazine.getFrequency();
        }
        return "";
    }

    private static String valueOrEmpty(Object value) {
        return value == null ? "" : value.toString();
    }

    private static String[] splitLine(String line, char separator) {
        List<String> columns = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    value.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == separator && !quoted) {
                columns.add(value.toString());
                value.setLength(0);
            } else {
                value.append(character);
            }
        }
        columns.add(value.toString());
        return columns.toArray(String[]::new);
    }

    private static String joinEscaped(char separator, String... values) {
        String separatorText = String.valueOf(separator);
        return java.util.Arrays.stream(values)
                .map(value -> value == null ? "" : value)
                .map(value -> value.contains(separatorText) || value.contains("\"")
                        ? "\"" + value.replace("\"", "\"\"") + "\"" : value)
                .collect(java.util.stream.Collectors.joining(separatorText));
    }
}
