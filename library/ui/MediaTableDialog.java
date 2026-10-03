package library.ui;

import library.LibrarySystem;
import library.media.Audiobook;
import library.media.Book;
import library.media.EBook;
import library.media.Magazine;
import library.media.ReferenceLiterature;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

public final class MediaTableDialog extends JDialog {
    public MediaTableDialog(JFrame parent, List<LibrarySystem.Media> media) {
        super(parent, "Media list", true);
        String[] columns = {"ID", "Title", "Creator", "Year", "Genre", "Type", "Status", "Due", "Fine", "Extra"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (LibrarySystem.Media item : media) {
            String status = item.isBorrowed() ? "Borrowed" : item.isReserved() ? "Reserved" : "Available";
            model.addRow(new Object[]{item.getId(), item.getTitle(), item.getCreator(),
                    item.getPublicationYear(), item.getGenre(), item.getType(), status,
                    item.getDueDate() == null ? "-" : item.getDueDate(),
                    String.format("%.2f", item.getFine()), getExtra(item)});
        }
        JTable table = new JTable(model);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        JTextField filter = new JTextField(25);
        filter.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                String text = filter.getText().trim();
                sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent event) { update(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent event) { update(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent event) { update(); }
        });
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filter);
        add(filterPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        setPreferredSize(new Dimension(1050, 450));
        pack();
        setLocationRelativeTo(parent);
    }

    private static String getExtra(LibrarySystem.Media item) {
        if (item instanceof Book book) return book.getPublisher();
        if (item instanceof Audiobook audiobook) return audiobook.getDurationMinutes() + " min";
        if (item instanceof EBook ebook) return ebook.getFormat();
        if (item instanceof ReferenceLiterature reference) return reference.getCategory();
        if (item instanceof Magazine magazine) return magazine.getFrequency();
        return "";
    }
}
