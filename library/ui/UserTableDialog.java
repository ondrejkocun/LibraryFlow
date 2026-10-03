package library.ui;

import library.LibrarySystem;
import library.users.LibraryUser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

public final class UserTableDialog extends JDialog {
    public UserTableDialog(JFrame parent, List<LibraryUser> users) {
        super(parent, "User list", true);
        String[] columns = {"ID", "Name", "Type", "Maximum borrow days", "Age", "Borrowed"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (LibraryUser user : users) {
            model.addRow(new Object[]{user.getId(), user.getName(), user.getType(),
                    user.getMaximumBorrowDays(), user.getAge(), user.getBorrowedMediaCount()});
        }
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table), BorderLayout.CENTER);
        setPreferredSize(new Dimension(650, 400));
        pack();
        setLocationRelativeTo(parent);
    }
}
