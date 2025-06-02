package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.*;

public class IFrame extends JFrame {

    private JTable instructorTable;
    private DefaultTableModel tableModel;
    private JButton generateReportButton;

    public IFrame() {
        setTitle("Laporan Gaji Instruktur Universitas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // Menengahkan frame

        initComponents();
        loadInstructorData();
    }

    private void initComponents() {
        // Kolom untuk JTable: ID Instruktur, Nama, Gaji, Departemen
        tableModel = new DefaultTableModel(new String[]{"ID Instruktur", "Nama", "Gaji", "Departemen"}, 0) {
            // Membuat sel gaji tidak bisa diedit langsung di tabel
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        instructorTable = new JTable(tableModel);
        instructorTable.setFillsViewportHeight(true); // Agar tabel mengisi tinggi scrollpane

        JScrollPane scrollPane = new JScrollPane(instructorTable);
        add(scrollPane, BorderLayout.CENTER);

        generateReportButton = new JButton("Buat Laporan Departemen Universitas");
        generateReportButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        generateReportButton.setToolTipText("Klik untuk menghasilkan laporan PDF daftar departemen dan anggarannya.");

        generateReportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Tombol 'Buat Laporan Departemen Universitas' ditekan.");
                new Thread(() -> {
                    try {
                        UniversityDepartmentReport reportGenerator = new UniversityDepartmentReport();
                        reportGenerator.generateReport();
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(IFrame.this,
                                        "Proses pembuatan laporan departemen telah dimulai.\nSilakan periksa konsol dan file PDF yang dihasilkan.",
                                        "Pembuatan Laporan", JOptionPane.INFORMATION_MESSAGE)
                        );
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(IFrame.this,
                                        "Terjadi kesalahan saat membuat laporan departemen: " + ex.getMessage(),
                                        "Error Pembuatan Laporan", JOptionPane.ERROR_MESSAGE)
                        );
                    }
                }).start();
            }
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Memberi padding
        bottomPanel.add(generateReportButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private Connection getConnection() throws SQLException {
        // Menggunakan string koneksi yang sama seperti di UniversityDepartmentReport
        String jdbcUrl = "jdbc:sqlserver://localhost:1433;databaseName=University;integratedSecurity=true;encrypt=true;trustServerCertificate=true;";
        return DriverManager.getConnection(jdbcUrl);
    }

    private void loadInstructorData() {
        tableModel.setRowCount(0);
        String sql = "SELECT i.ID, i.name, i.salary, d.dept_name AS department_name " +
                "FROM instructor i " +
                "LEFT JOIN department d ON i.dept_name = d.dept_name " +
                "ORDER BY d.dept_name, i.name";

        System.out.println("Mencoba memuat data instruktur untuk JTable...");
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("Berhasil terhubung ke database University untuk memuat data instruktur.");
            int rowCount = 0;
            while (rs.next()) {
                String id = rs.getString("ID");
                String name = rs.getString("name");
                BigDecimal salary = rs.getBigDecimal("salary");
                String departmentName = rs.getString("department_name");

                tableModel.addRow(new Object[]{
                        id,
                        name,
                        salary,
                        departmentName == null ? "N/A" : departmentName
                });
                rowCount++;
            }
            System.out.println(rowCount + " baris data instruktur berhasil dimuat ke JTable.");
            if (rowCount == 0) {
                System.out.println("Tidak ada data instruktur yang ditemukan.");
            }

        } catch (SQLException e) {
            System.err.println("Koneksi database atau eksekusi SQL untuk JTable gagal: " + e.getMessage());
            e.printStackTrace();
            if (e.getMessage().contains("Login failed for user") && !e.getMessage().contains("integratedSecurity=true")) {
                System.err.println("--> PERIKSA: Konfigurasi username/password jika tidak menggunakan Windows Authentication.");
            } else if (e.getMessage().contains("integrated authentication")) {
                System.err.println("--> PASTIKAN: DLL otentikasi JDBC SQL Server (mssql-jdbc_auth-<versi>-<arsitektur>.dll) ada di java.library.path.");
                System.err.println("    Biasanya perlu ditambahkan VM option: -Djava.library.path=\"C:/path/to/dll\"");
            }
            JOptionPane.showMessageDialog(this,
                    "Gagal memuat data instruktur: " + e.getMessage() +
                            (e.getMessage().contains("integrated authentication") ? "\nPastikan konfigurasi Integrated Security sudah benar dan DLL terkait ada di path." : ""),
                    "Kesalahan Database", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            System.err.println("Terjadi error umum saat memuat data instruktur: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Terjadi kesalahan umum: " + e.getMessage(),
                    "Kesalahan Aplikasi", JOptionPane.ERROR_MESSAGE);
        }
    }
}