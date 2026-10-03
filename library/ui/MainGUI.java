package library.ui;

import library.LibrarySystem;
import library.persistence.CsvManager;
import library.persistence.LoanRecord;
import library.services.MediaSearcher;
import library.services.StatisticsService;
import library.users.Administrator;
import library.users.LibraryUser;
import library.users.RetiredUser;
import library.users.StandardUser;
import library.users.Student;
import library.users.VipUser;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class MainGUI {
    private final LibrarySystem.Library library;
    private final LibraryUser currentUser;

    public MainGUI(LibrarySystem.Library library) {
        this.library = library;
        this.currentUser = login();
        if (currentUser == null) {
            System.exit(0);
        }
        createGui();
    }

    private LibraryUser login() {
        String userId = JOptionPane.showInputDialog(null, "User ID:");
        String password = JOptionPane.showInputDialog(null, "Password:");
        if (userId == null || password == null) {
            return null;
        }
        try {
            return library.authenticate(userId.trim(), password);
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(null, exception.getMessage(), "Login failed",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private void createGui() {
        JFrame frame = new JFrame("Library System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(420, 600);

        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        boolean isAdministrator = currentUser instanceof Administrator;
        panel.add(new javax.swing.JLabel("Signed in: " + currentUser.getName()
            + " (" + currentUser.getType() + ")"));
        addButton(panel, "Show media", () -> showMedia(frame));
        addButton(panel, "Show users", () -> showUsers(frame));
        addButton(panel, "Search media", () -> searchMedia(frame));
        addButton(panel, "Borrow media", () -> borrowMedia(frame));
        addButton(panel, "Reserve media", () -> reserveMedia(frame));
        addButton(panel, "Return media", () -> returnMedia(frame));
        if (isAdministrator) {
            addButton(panel, "Add user", () -> addUser(frame));
            addButton(panel, "Export media to CSV", () -> exportMedia(frame));
            addButton(panel, "Import users from CSV", () -> importUsers(frame));
            addButton(panel, "Dashboard", () -> showDashboard(frame));
        }
        addButton(panel, "Log out", () -> {
            frame.dispose();
            new MainGUI(library);
        });
        addButton(panel, "Exit", () -> System.exit(0));

        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void addButton(JPanel panel, String label, Runnable action) {
        JButton button = new JButton(label);
        button.addActionListener(event -> action.run());
        panel.add(button);
    }

    private void showMedia(JFrame frame) {
        new MediaTableDialog(frame, new ArrayList<>(library.getMedia().values())).setVisible(true);
    }

    private void showUsers(JFrame frame) {
        new UserTableDialog(frame, new ArrayList<>(library.getUsers().values())).setVisible(true);
    }

    private void addUser(JFrame frame) {
        String name = JOptionPane.showInputDialog(frame, "Enter user name:");
        String password = JOptionPane.showInputDialog(frame, "Enter password:");
        String[] types = {"standard", "student", "vip", "retired", "administrator"};
        String type = (String) JOptionPane.showInputDialog(frame, "Choose user type:",
                "User type", JOptionPane.QUESTION_MESSAGE, null, types, types[0]);
        if (name == null || name.isBlank() || password == null || password.isBlank() || type == null) {
            showMessage(frame, "Name, password, and user type are required.");
            return;
        }
        String id = "U-" + (library.getUsers().size() + 1);
        LibraryUser user = switch (type) {
            case "student" -> new Student(id, name.trim(), 18, password);
            case "vip" -> new VipUser(id, name.trim(), 18, password);
            case "retired" -> new RetiredUser(id, name.trim(), 65, password);
            case "administrator" -> new Administrator(id, name.trim(), 18, password);
            default -> new StandardUser(id, name.trim(), 18, password);
        };
        library.addUser(user);
        try {
            CsvManager.saveUsers(library.getUsers(), "users.csv");
            showMessage(frame, "User added with ID: " + id);
        } catch (IOException exception) {
            showMessage(frame, "User added, but saving failed: " + exception.getMessage());
        }
    }

    private void searchMedia(JFrame frame) {
        String query = JOptionPane.showInputDialog(frame, "Enter title, creator, or genre:");
        if (query == null || query.isBlank()) {
            return;
        }
        List<LibrarySystem.Media> results = MediaSearcher.search(
            new HashMap<>(library.getMedia()), query);
        StringBuilder output = new StringBuilder("Search results:\n\n");
        for (LibrarySystem.Media item : results) {
            output.append("- ").append(item.getTitle()).append(" | ")
                    .append(item.getCreator()).append(" | ")
                    .append(item.getGenre()).append('\n');
        }
        if (results.isEmpty()) {
            output.append("Nothing was found.");
        }
        showMessage(frame, output.toString());
    }

    private void borrowMedia(JFrame frame) {
        String userId = currentUser instanceof Administrator
            ? JOptionPane.showInputDialog(frame, "Enter user ID:")
            : currentUser.getId();
        String mediaId = JOptionPane.showInputDialog(frame, "Enter media ID:");
        if (userId == null || mediaId == null || userId.isBlank() || mediaId.isBlank()) {
            showMessage(frame, "User ID and media ID are required.");
            return;
        }
        try {
            library.borrow(mediaId.trim(), userId.trim());
            saveHistory();
            showMessage(frame, "Media borrowed successfully.");
        } catch (RuntimeException exception) {
            showMessage(frame, "Error: " + exception.getMessage());
        }
    }

    private void reserveMedia(JFrame frame) {
        String mediaId = JOptionPane.showInputDialog(frame, "Enter media ID to reserve:");
        if (mediaId == null || mediaId.isBlank()) {
            return;
        }
        try {
            library.reserve(mediaId.trim(), currentUser.getId());
            showMessage(frame, "Media reserved successfully.");
        } catch (RuntimeException exception) {
            showMessage(frame, "Error: " + exception.getMessage());
        }
    }

    private void returnMedia(JFrame frame) {
        String mediaId = JOptionPane.showInputDialog(frame, "Enter media ID to return:");
        if (mediaId == null || mediaId.isBlank()) {
            return;
        }
        try {
            String borrowerId = currentUser instanceof Administrator
                ? JOptionPane.showInputDialog(frame, "Enter borrowing user ID:")
                : currentUser.getId();
            double fine = library.returnMedia(mediaId.trim(), borrowerId);
            saveHistory();
            showMessage(frame, fine > 0
                ? String.format("Media returned. Fine: %.2f", fine)
                : "Media returned successfully.");
        } catch (RuntimeException exception) {
            showMessage(frame, "Error: " + exception.getMessage());
        }
    }

    private void exportMedia(JFrame frame) {
        try {
            CsvManager.saveMedia(new HashMap<>(library.getMedia()), "media.csv");
            showMessage(frame, "Media exported to media.csv.");
        } catch (IOException exception) {
            showMessage(frame, "Export failed: " + exception.getMessage());
        }
    }

    private void importUsers(JFrame frame) {
        try {
            List<LibraryUser> importedUsers = CsvManager.loadUsers("users.csv");
            for (LibraryUser user : importedUsers) {
                library.addUser(user);
            }
            CsvManager.saveUsers(library.getUsers(), "users.csv");
            showMessage(frame, importedUsers.size() + " users imported from users.csv.");
        } catch (IOException exception) {
            showMessage(frame, "Import failed: " + exception.getMessage());
        }
    }

    private void showMessage(JFrame frame, String message) {
        JOptionPane.showMessageDialog(frame, message);
    }

    private void saveHistory() {
        try {
            CsvManager.saveLoanHistory(library.getLoanHistory(), "loan-history.csv");
            CsvManager.saveMedia(new HashMap<>(library.getMedia()), "media.csv");
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save loan history", exception);
        }
    }

    private void showDashboard(JFrame frame) {
        StringBuilder dashboard = new StringBuilder();
        dashboard.append(StatisticsService.getStatistics(new HashMap<>(library.getMedia()))).append("\n\n");
        dashboard.append("Loan history: ").append(library.getLoanHistory().size()).append("\n");
        long openLoans = library.getLoanHistory().stream().filter(record -> !record.isReturned()).count();
        dashboard.append("Open loans: ").append(openLoans).append("\n");
        long overdue = library.getMedia().values().stream()
                .filter(LibrarySystem.Media::isBorrowed)
                .filter(item -> item.getDaysOverdue() > 0)
                .count();
        dashboard.append("Overdue media: ").append(overdue).append("\n\n");
        for (LoanRecord record : library.getLoanHistory()) {
            dashboard.append(record.userId()).append(" -> ").append(record.mediaId())
                    .append(record.isReturned() ? " (returned)" : " (open)").append('\n');
        }
        showMessage(frame, dashboard.toString());
    }

    public static void main(String[] args) {
        LibrarySystem.Library library = new LibrarySystem.Library();
        try {
            for (LibrarySystem.Media item : CsvManager.loadMedia("media.csv")) {
                library.addMedia(item);
            }
            for (LibraryUser user : CsvManager.loadUsers("users.csv")) {
                library.addUser(user);
            }
            library.restoreUserMediaCounts();
            library.restoreLoanHistory(CsvManager.loadLoanHistory("loan-history.csv"));
        } catch (IOException | IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(null,
                    "Could not load CSV data: " + exception.getMessage(),
                    "Startup error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new MainGUI(library);
    }
}