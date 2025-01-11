import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ActivitiesCreate extends JFrame {
    private JTextField activityIDField, typeField, modeOfActivityField, locationField, durationField, contactPersonField, quotaField, createdAtField;
    private JComboBox<String> timeslotComboBox, unitComboBox;
    private Connection connection;

    public ActivitiesCreate(Connection connection) {
        this.connection = connection;

        setTitle("Activity Form");
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(10, 2, 10, 10));

        // Text fields for attributes
        formPanel.add(new JLabel("Activity ID:"));
        activityIDField = new JTextField();
        formPanel.add(activityIDField);

        formPanel.add(new JLabel("Type:"));
        typeField = new JTextField();
        formPanel.add(typeField);

        formPanel.add(new JLabel("Mode of Activity:"));
        modeOfActivityField = new JTextField();
        formPanel.add(modeOfActivityField);

        formPanel.add(new JLabel("Location:"));
        locationField = new JTextField();
        formPanel.add(locationField);

        formPanel.add(new JLabel("Duration (in minutes):"));
        durationField = new JTextField();
        formPanel.add(durationField);

        formPanel.add(new JLabel("Contact Person:"));
        contactPersonField = new JTextField();
        formPanel.add(contactPersonField);

        formPanel.add(new JLabel("Quota:"));
        quotaField = new JTextField();
        formPanel.add(quotaField);

        formPanel.add(new JLabel("Created At (YYYY-MM-DD):"));
        createdAtField = new JTextField();
        formPanel.add(createdAtField);

        // Dropdown for timeslot
        formPanel.add(new JLabel("Time Slot:"));
        timeslotComboBox = new JComboBox<>();
        populateComboBox(timeslotComboBox, "SELECT * FROM timeslot");
        formPanel.add(timeslotComboBox);

        // Dropdown for unit
        formPanel.add(new JLabel("Unit:"));
        unitComboBox = new JComboBox<>();
        populateComboBox(unitComboBox, "SELECT unitid, name FROM units");
        formPanel.add(unitComboBox);

        add(formPanel, BorderLayout.CENTER);

        // Submit button
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> handleSubmit());
        add(submitButton, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // Center the window on the screen
        setVisible(true);
    }

    private void populateComboBox(JComboBox<String> comboBox, String query) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                if (comboBox == timeslotComboBox) {
                    String id = rs.getString("timeslotID");
                    String start = rs.getString("starttime");
                    String end = rs.getString("endtime");
                    String day = rs.getString("dayofweek");
                    comboBox.addItem(id + " - " + start + " to " + end + " on " + day);
                } else if (comboBox == unitComboBox) {
                    comboBox.addItem(rs.getString("unitid") + " - " + rs.getString("name"));
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error fetching data: " + ex.getMessage());
        }
    }

    private void handleSubmit() {
        try {
            connection.setAutoCommit(false);

            // Insert activity data
            String activitySql = "INSERT INTO activities (activityID, unitID, type, modeOfActivity, location, timeSlot, duration, contactPerson, quota, createdAt) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(activitySql)) {
                pstmt.setString(1, activityIDField.getText());
                pstmt.setString(2, unitComboBox.getSelectedItem().toString().split(" - ")[0]); // Extract unitid
                pstmt.setString(3, typeField.getText());
                pstmt.setString(4, modeOfActivityField.getText());
                pstmt.setString(5, locationField.getText());
                pstmt.setString(6, timeslotComboBox.getSelectedItem().toString()); // Use timeslotID
                pstmt.setInt(7, Integer.parseInt(durationField.getText()));
                pstmt.setString(8, contactPersonField.getText());
                pstmt.setInt(9, Integer.parseInt(quotaField.getText()));
                pstmt.setDate(10, java.sql.Date.valueOf(createdAtField.getText()));

                pstmt.executeUpdate();
            }

            connection.commit();
            JOptionPane.showMessageDialog(this, "Activity added successfully!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error inserting data: " + ex.getMessage());
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                JOptionPane.showMessageDialog(this, "Rollback failed: " + rollbackEx.getMessage());
            }
        }
    }
}
