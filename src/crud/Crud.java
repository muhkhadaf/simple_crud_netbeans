package crud;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Element;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.PageSize;



public class Crud extends JFrame {
    // UI Components
    private JTextField txtNIM, txtNama, txtAlamat, txtNoTlp;
    private JButton btnSimpan, btnHapus, btnEdit, btnView, btnCetakPDF;
    private JTable table;
    private DefaultTableModel tableModel;

    // Database connection components
    private Connection connection;
    private PreparedStatement pst;
    private ResultSet rs;

    public Crud() {
        super("Formulir Mahasiswa");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Initialize components
        initComponents();

        // Connect to DB and load data immediately
        connectToDatabase();
        viewData();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblNIM = new JLabel("NIM");
        JLabel lblNama = new JLabel("NAMA");
        JLabel lblAlamat = new JLabel("ALAMAT");
        JLabel lblNoTlp = new JLabel("NO_TLP");

        txtNIM = new JTextField(15);
        txtNama = new JTextField(15);
        txtAlamat = new JTextField(15);
        txtNoTlp = new JTextField(15);

        btnSimpan = new JButton("SIMPAN");
        btnHapus = new JButton("HAPUS");
        btnEdit = new JButton("EDIT");
        btnView = new JButton("VIEW");
        btnCetakPDF = new JButton("CETAK PDF");

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(lblNIM, gbc);
        gbc.gridy = 1;
        formPanel.add(lblNama, gbc);
        gbc.gridy = 2;
        formPanel.add(lblAlamat, gbc);
        gbc.gridy = 3;
        formPanel.add(lblNoTlp, gbc);

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
        gbc.gridy = 4;
        formPanel.add(btnCetakPDF, gbc);

        String[] columns = {"NIM", "NAMA", "ALAMAT", "NO_TLP"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        add(formPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        btnSimpan.addActionListener(e -> saveData());
        btnHapus.addActionListener(e -> deleteData());
        btnEdit.addActionListener(e -> editData());
        btnView.addActionListener(e -> viewData());
        btnCetakPDF.addActionListener(e -> cetakPDF());

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
            Class.forName("org.mariadb.jdbc.Driver");
            String url = "jdbc:mariadb://localhost:3306/db_latihan6";
            String user = "root";
            String password = "";
            connection = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException | SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database connection error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
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
            pst = connection.prepareStatement("SELECT nim FROM tabel_mhs WHERE nim = ?");
            pst.setString(1, nim);
            rs = pst.executeQuery();

            if (rs.next()) {
                pst = connection.prepareStatement("UPDATE tabel_mhs SET nama = ?, alamat = ?, no_tlp = ? WHERE nim = ?");
                pst.setString(1, nama);
                pst.setString(2, alamat);
                pst.setString(3, noTlp);
                pst.setString(4, nim);
                pst.executeUpdate();
                JOptionPane.showMessageDialog(this, "Record updated successfully!");
            } else {
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
            tableModel.setRowCount(0);
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

    private void cetakPDF() {
    Document document = new Document(PageSize.A4, 50, 50, 50, 50); // Margin diperbesar

    try {
        String outputPath = "Laporan_Mahasiswa.pdf";
        PdfWriter.getInstance(document, new FileOutputStream(outputPath));
        document.open();

        // Tambahkan judul
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Paragraph title = new Paragraph("Laporan Data Mahasiswa", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20f);
        document.add(title);

        // Membuat tabel
        PdfPTable pdfTable = new PdfPTable(4); // 4 kolom
        pdfTable.setWidthPercentage(100); // Lebar tabel penuh
        pdfTable.setSpacingBefore(10f);
        pdfTable.setSpacingAfter(10f);

        // Mengatur lebar kolom
        float[] columnWidths = {2f, 3f, 4f, 3f};
        pdfTable.setWidths(columnWidths);

        // Header tabel
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        addTableHeader(pdfTable, "NIM", headerFont);
        addTableHeader(pdfTable, "Nama", headerFont);
        addTableHeader(pdfTable, "Alamat", headerFont);
        addTableHeader(pdfTable, "No_Tlp", headerFont);

        // Data tabel
        Font cellFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL);
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                PdfPCell cell = new PdfPCell(new Phrase(tableModel.getValueAt(i, j).toString(), cellFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(8);
                pdfTable.addCell(cell);
            }
        }

        document.add(pdfTable);
        JOptionPane.showMessageDialog(this, "Laporan PDF berhasil dibuat: " + outputPath);

    } catch (DocumentException | IOException ex) {
        JOptionPane.showMessageDialog(this, "Error membuat PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    } finally {
        document.close();
    }

    // Langsung buka file PDF setelah selesai
    try {
        Desktop.getDesktop().open(new java.io.File("Laporan_Mahasiswa.pdf"));
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, "Gagal membuka file PDF: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}

// Fungsi tambahan untuk membuat header tabel dengan style
private void addTableHeader(PdfPTable table, String headerTitle, Font font) {
    PdfPCell header = new PdfPCell();
    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
    header.setHorizontalAlignment(Element.ALIGN_CENTER);
    header.setPadding(10);
    header.setPhrase(new Phrase(headerTitle, font));
    table.addCell(header);
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
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(Crud::new);
    }
}
