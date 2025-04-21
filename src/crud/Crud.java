package crud;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Crud extends JFrame {
    // UI Components
    private JTextField txtNIM, txtNama, txtAlamat, txtNoTlp;
    private JButton btnSimpan, btnHapus, btnEdit, btnView, btnKoneksi;
    private JTable table;
    private DefaultTableModel tableModel;
    
    // Database connection components
    private Connection connection;
    private PreparedStatement pst;
    private ResultSet rs;
    
    public Crud() {
        // Set up JFrame
        super("Student Management System");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // Initialize components
        initComponents();
        
        // Center the frame on screen
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void initComponents() {
        // Form panel - North
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Labels
        JLabel lblNIM = new JLabel("NIM");
        JLabel lblNama = new JLabel("NAMA");
        JLabel lblAlamat = new JLabel("ALAMAT");
        JLabel lblNoTlp = new JLabel("NO_TLP");
        
        // Text fields
        txtNIM = new JTextField(15);
        txtNama = new JTextField(15);
        txtAlamat = new JTextField(15);
        txtNoTlp = new JTextField(15);
        
        // Buttons
        btnSimpan = new JButton("SIMPAN");
        btnHapus = new JButton("HAPUS");
        btnEdit = new JButton("EDIT");
        btnView = new JButton("VIEW");
        btnKoneksi = new JButton("KONEKSI");
        
        // Add components to form panel
        // First column - labels
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(lblNIM, gbc);
        
        gbc.gridy = 1;
        formPanel.add(lblNama, gbc);
        
        gbc.gridy = 2;
        formPanel.add(lblAlamat, gbc);
        
        gbc.gridy = 3;
        formPanel.add(lblNoTlp, gbc);
        
        // Second column - text fields
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtNIM, gbc);
        
        gbc.gridy = 1;
        formPanel.add(txtNama, gbc);
        
        gbc.gridy = 2;
        formPanel.add(txtAlamat, gbc);
        
        gbc.gridy = 3;
        formPanel.add(txtNoTlp, gbc);
        
        // Third column - buttons
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(btnSimpan, gbc);
        
        gbc.gridy = 1;
        formPanel.add(btnHapus, gbc);
        
        gbc.gridy = 2;
        formPanel.add(btnEdit, gbc);
        
        gbc.gridy = 3;
        formPanel.add(btnView, gbc);
        
        // Fourth column - connection button
        gbc.gridx = 3;
        gbc.gridy = 3;
        formPanel.add(btnKoneksi, gbc);
        
        // Table panel - Center
        String[] columns = {"NIM", "NAMA", "ALAMAT", "NO_TLP"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        
        // Add panels to frame
        add(formPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // Add action listeners to buttons
        btnSimpan.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveData();
            }
        });
        
        btnHapus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteData();
            }
        });
        
        btnEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editData();
            }
        });
        
        btnView.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewData();
            }
        });
        
        btnKoneksi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToDatabase();
            }
        });
        
        // Add mouse listener to table
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    txtNIM.setText(tableModel.getValueAt(row, 0).toString());
                    txtNama.setText(tableModel.getValueAt(row, 1).toString());
                    txtAlamat.setText(tableModel.getValueAt(row, 2).toString());
                    txtNoTlp.setText(tableModel.getValueAt(row, 3).toString());
                    txtNIM.setEditable(false);
                }
            }
        });
    }
    
    private void connectToDatabase() {
        try {
            // Load the JDBC driver
            Class.forName("org.mariadb.jdbc.Driver");
            
            // Establish connection
            String url = "jdbc:mariadb://localhost:3306/db_latihan6";
            String user = "root";
            String password = "";
            connection = DriverManager.getConnection(url, user, password);
            
            JOptionPane.showMessageDialog(this, "Database connection successful!");
            viewData(); // Load data after successful connection
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "MariaDB JDBC Driver not found: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveData() {
        String nim = txtNIM.getText();
        String nama = txtNama.getText();
        String alamat = txtAlamat.getText();
        String noTlp = txtNoTlp.getText();
        
        if (nim.isEmpty() || nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "NIM and Nama are required fields!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            if (connection == null || connection.isClosed()) {
                JOptionPane.showMessageDialog(this, "Please connect to the database first!", "Database Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if NIM already exists
            pst = connection.prepareStatement("SELECT nim FROM tabel_mhs WHERE nim = ?");
            pst.setString(1, nim);
            rs = pst.executeQuery();
            
            if (rs.next()) {
                // NIM exists, update the record
                pst = connection.prepareStatement("UPDATE tabel_mhs SET nama = ?, alamat = ?, no_tlp = ? WHERE nim = ?");
                pst.setString(1, nama);
                pst.setString(2, alamat);
                pst.setString(3, noTlp);
                pst.setString(4, nim);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Record updated successfully!");
            } else {
                // NIM doesn't exist, insert new record
                pst = connection.prepareStatement("INSERT INTO tabel_mhs (nim, nama, alamat, no_tlp) VALUES (?, ?, ?, ?)");
                pst.setString(1, nim);
                pst.setString(2, nama);
                pst.setString(3, alamat);
                pst.setString(4, noTlp);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Record saved successfully!");
            }
            
            clearFields();
            viewData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error saving data: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteData() {
        String nim = txtNIM.getText();
        
        if (nim.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a record to delete!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            if (connection == null || connection.isClosed()) {
                JOptionPane.showMessageDialog(this, "Please connect to the database first!", "Database Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this record?", "Confirmation", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                pst = connection.prepareStatement("DELETE FROM tabel_mhs WHERE nim = ?");
                pst.setString(1, nim);
                int rowsAffected = pst.executeUpdate();
                
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Record deleted successfully!");
                    clearFields();
                    viewData();
                } else {
                    JOptionPane.showMessageDialog(this, "Record not found!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting data: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void editData() {
        String nim = txtNIM.getText();
        
        if (nim.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a record to edit!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            if (connection == null || connection.isClosed()) {
                JOptionPane.showMessageDialog(this, "Please connect to the database first!", "Database Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            pst = connection.prepareStatement("SELECT * FROM tabel_mhs WHERE nim = ?");
            pst.setString(1, nim);
            rs = pst.executeQuery();
            
            if (rs.next()) {
                txtNIM.setText(rs.getString("nim"));
                txtNama.setText(rs.getString("nama"));
                txtAlamat.setText(rs.getString("alamat"));
                txtNoTlp.setText(rs.getString("no_tlp"));
                txtNIM.setEditable(false);
            } else {
                JOptionPane.showMessageDialog(this, "Record not found!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error editing data: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void viewData() {
        try {
            if (connection == null || connection.isClosed()) {
                JOptionPane.showMessageDialog(this, "Please connect to the database first!", "Database Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Clear existing table data
            tableModel.setRowCount(0);
            
            // Query and populate table
            pst = connection.prepareStatement("SELECT * FROM tabel_mhs");
            rs = pst.executeQuery();
            
            while (rs.next()) {
                String nim = rs.getString("nim");
                String nama = rs.getString("nama");
                String alamat = rs.getString("alamat");
                String noTlp = rs.getString("no_tlp");
                
                Object[] row = {nim, nama, alamat, noTlp};
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error viewing data: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearFields() {
        txtNIM.setText("");
        txtNama.setText("");
        txtAlamat.setText("");
        txtNoTlp.setText("");
        txtNIM.setEditable(true);
        txtNIM.requestFocus();
    }
    
    public static void main(String[] args) {
        // Set the look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Launch the application
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Crud();
            }
        });
    }
}