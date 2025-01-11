import java.sql.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;

public class UpdateWindow {
    private Connection connection;
    private String selectedTable;
    private JTable table;
    private DefaultTableModel tableModel;

    public UpdateWindow(Connection connection, String selectedTable) {
        this.connection = connection;
        this.selectedTable = selectedTable;
        initializeUI();
    }

    private void initializeUI() {
        JFrame frame = new JFrame("Update Entry");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        loadTableData();

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = table.rowAtPoint(evt.getPoint());
                if (row >= 0) {
                    openEditWindow(row);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        frame.add(scrollPane, BorderLayout.CENTER);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void loadTableData() {
        tableModel.setRowCount(0); // Clear existing rows
        tableModel.setColumnCount(0); // Clear existing columns
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM " + selectedTable)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
    
            // Add column names to the table model
            for (int i = 1; i <= columnCount; i++) {
                tableModel.addColumn(metaData.getColumnName(i));
            }
    
            // Add rows to the table model
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void openEditWindow(int row) {
        JFrame editFrame = new JFrame("Edit Entry");
        editFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        editFrame.setSize(400, 300);
        editFrame.setLayout(new GridLayout(0, 2));

        int columnCount = tableModel.getColumnCount();
        JTextField[] textFields = new JTextField[columnCount];
        Object[] originalValues = new Object[columnCount];
        for (int i = 0; i < columnCount; i++) {
            String columnName = tableModel.getColumnName(i);
            Object value = tableModel.getValueAt(row, i);
            originalValues[i] = value;
            editFrame.add(new JLabel(columnName));
            textFields[i] = new JTextField(value != null ? value.toString() : "");
            if (i == 0) {
                textFields[i].setEditable(false);
            }
            editFrame.add(textFields[i]);
        }

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                String primaryKeyColumn = tableModel.getColumnName(0);
                Object primaryKeyValue = tableModel.getValueAt(row, 0);

                StringBuilder updateQuery = new StringBuilder("UPDATE " + selectedTable + " SET ");
                boolean first = true;
                for (int i = 1; i < columnCount; i++) {
                    if (!textFields[i].getText().equals(originalValues[i] != null ? originalValues[i].toString() : "")) {
                        if (!first) {
                            updateQuery.append(", ");
                        }
                        updateQuery.append(tableModel.getColumnName(i)).append(" = ?");
                        first = false;
                    }
                }
                updateQuery.append(" WHERE ").append(primaryKeyColumn).append(" = ?");

                try (PreparedStatement pstmt = connection.prepareStatement(updateQuery.toString())) {
                    int paramIndex = 1;
                    for (int i = 1; i < columnCount; i++) {
                        if (!textFields[i].getText().equals(originalValues[i] != null ? originalValues[i].toString() : "")) {
                            pstmt.setObject(paramIndex++, textFields[i].getText());
                        }
                    }
                    pstmt.setObject(paramIndex, primaryKeyValue);

                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(null, "Update successful!");
                        loadTableData();
                        editFrame.dispose();
                    } else {
                        JOptionPane.showMessageDialog(null, "Update failed: No matching rows.");
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Update failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        editFrame.add(saveButton);
        editFrame.setLocationRelativeTo(null);
        editFrame.setVisible(true);
    }
}
