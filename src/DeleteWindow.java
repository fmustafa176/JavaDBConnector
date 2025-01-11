import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.DefaultTableModel;

public class DeleteWindow {
    private Connection connection;
    private String selectedTable;
    private JTable table;
    private DefaultTableModel tableModel;

    public DeleteWindow(Connection connection, String selectedTable) {
        this.connection = connection;
        this.selectedTable = selectedTable;
        initializeUI();
    }

    private void initializeUI() {
        JFrame frame = new JFrame("Delete Entry");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        loadTableData();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        frame.add(scrollPane, BorderLayout.CENTER);

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedEntry();
            }
        });
        frame.add(deleteButton, BorderLayout.SOUTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void loadTableData() {
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

    private void deleteSelectedEntry() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            try {
                String primaryKeyColumn = tableModel.getColumnName(0);
                Object primaryKeyValue = tableModel.getValueAt(selectedRow, 0);
    
                String warningMessage = "Deleting this entry might affect related tables. Do you want to proceed?";
                int response = JOptionPane.showConfirmDialog(null, warningMessage, "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    
                if (response == JOptionPane.YES_OPTION) {
                    String deleteQuery = "DELETE FROM " + selectedTable + " WHERE " + primaryKeyColumn + " = ?";
                    try (PreparedStatement pstmt = connection.prepareStatement(deleteQuery)) {
                        pstmt.setObject(1, primaryKeyValue);
                        pstmt.executeUpdate();
                    }
    
                    tableModel.removeRow(selectedRow);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(null, "Please select a row to delete.");
        }
    }
}