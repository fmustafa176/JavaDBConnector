import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;

public class Main {
    private static String selectedTable = "StudyAreas"; // Default to the first table
    private static Connection connection; // Static connection variable

    public static void main(String[] args) {
        OracleDBConnection odbc = new OracleDBConnection();
        connection = odbc.getConnection(); // Initialize the connection

        // Create the main frame
        JFrame frame = new JFrame("Database GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 250);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null); // Center the frame

        // Create a panel for the dropdown
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JLabel selectTableLabel = new JLabel("Select Table:");
        String[] tables = {"StudyAreas", "Course_", "CourseOfferings", "Units", "Activities"};
        JComboBox<String> tableDropdown = new JComboBox<>(tables);
        tableDropdown.setPreferredSize(new Dimension(200, 25));

        // Add a listener to update the selected table
        tableDropdown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedTable = (String) tableDropdown.getSelectedItem();
            }
        });

        topPanel.add(selectTableLabel);
        topPanel.add(tableDropdown);

        // Create a panel for CRUD buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        JButton createButton = new JButton("Create");
        JButton readButton = new JButton("Read");
        JButton updateButton = new JButton("Update");
        JButton deleteButton = new JButton("Delete");

        buttonPanel.add(createButton);
        buttonPanel.add(readButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCreateWindow(); // Call method to open create window
            }
        });

        readButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ReadWindow(connection, selectedTable);
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new UpdateWindow(connection, selectedTable);
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new DeleteWindow(connection, selectedTable);
            }
        });

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.CENTER);

        // Make the frame visible
        frame.setVisible(true);
    }

    // Method to open the appropriate create window
    private static void openCreateWindow() {
        switch (selectedTable) {
            case "StudyAreas":
                new StudyAreasCreate(connection); // Pass the connection
                break;
            case "Course_":
                new CourseCreate(connection);
                break;
            case "CourseOfferings":
                new CourseOfferingsCreate(connection);
                break;
            case "Units":
                new UnitCreate(connection);
                break;
            case "Activities":
                new ActivitiesCreate(connection);
                break;
            default:
                JOptionPane.showMessageDialog(null, "Unknown table selected.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void openDeleteWindow() {

    }
}
