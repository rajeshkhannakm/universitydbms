/**
 * @author Rajesh Khanna
 */
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                JFrame frame = new JFrame("University Management");
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

                JTabbedPane tabbedPane = new JTabbedPane();
                tabbedPane.addTab("Labors", new Tab("Labors", new String[]{"Id", "Name", "Salary", "Works"}));
                tabbedPane.addTab("Professors", new Tab("Professors", new String[]{"Id", "Name", "Salary", "Subjects"}));
                tabbedPane.addTab("Students", new Tab("Students", new String[]{"Id", "Name", "Course", "Branch"}));
                frame.add(tabbedPane);

                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }

        });

        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static class Tab extends JPanel {

        private final String tableName;
        private final String[] tableColumns;

        private JTable table;

        public Tab(String tableName, String[] tableColumns) {
            this.tableName = tableName;
            this.tableColumns = tableColumns;

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            add(Box.createVerticalStrut(10));

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
            add(panel);

            panel.add(Box.createHorizontalStrut(10));

            JTextField[] textFields = new JTextField[tableColumns.length];
            for (int i = 1; i < tableColumns.length; i++) {
                panel.add(new JLabel(tableColumns[i]));

                panel.add(Box.createHorizontalStrut(10));

                JTextField textField = new JTextField();
                panel.add(textField);

                panel.add(Box.createHorizontalStrut(10));

                textFields[i] = textField;
            }

            JButton addButton = new JButton("Add");
            addButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Connection connection = DriverManager.getConnection("jdbc:ucanaccess://database.accdb");

                        String sql = "INSERT INTO " + tableName + " VALUES (0";
                        for (int i = 1; i < textFields.length; i++) {
                            sql += ", '" + textFields[i].getText() + "'";
                        }
                        sql += ")";

                        connection.createStatement().executeUpdate(sql);

                        connection.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }

                    update();
                }

            });
            panel.add(addButton);

            panel.add(Box.createHorizontalStrut(10));

            add(Box.createVerticalStrut(10));

            table = new JTable();
            table.setModel(new DefaultTableModel(tableColumns, 0));
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(800, 500));
            add(scrollPane);

            add(Box.createVerticalStrut(10));

            JButton deleteButton = new JButton("Delete");
            deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            deleteButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    int rowIndex = table.getSelectedRow();
                    if (rowIndex == -1) {
                        return;
                    }

                    try {
                        Connection connection = DriverManager.getConnection("jdbc:ucanaccess://database.accdb");

                        connection.createStatement().executeUpdate("DELETE FROM " + tableName + " WHERE Id = " +
                                table.getValueAt(rowIndex, 0));

                        connection.close();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }

                    update();
                }

            });
            add(deleteButton);

            add(Box.createVerticalStrut(10));

            update();
        }

        private void update() {
            DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
            tableModel.setRowCount(0);

            try {
                Connection connection = DriverManager.getConnection("jdbc:ucanaccess://database.accdb");

                ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM " + tableName);
                while (resultSet.next()) {
                    String[] row = new String[tableColumns.length];
                    for (int i = 0; i < row.length; i++) {
                        row[i] = resultSet.getString(i + 1);
                    }

                    tableModel.addRow(row);
                }

                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

