package library.media;

import java.time.LocalDate;

public final class Magazine extends Media {
    private final String frequency;

    public Magazine(String id, String title, String publisher, int year, String genre, String frequency) {
        super(id, title, publisher, year, genre);
        if (frequency == null || frequency.isBlank()) throw new IllegalArgumentException("Frequency must not be blank");
        this.frequency = frequency;
    }

    public String getFrequency() { return frequency; }
    @Override public String getType() { return "magazine"; }
    @Override public boolean isBorrowingAllowed() {
        int age = LocalDate.now().getYear() - getPublicationYear();
        return super.isBorrowingAllowed() && age <= 2 && "weekly".equalsIgnoreCase(frequency);
    }
}
