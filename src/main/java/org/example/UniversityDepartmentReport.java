package org.example; // Sesuaikan dengan package Anda

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UniversityDepartmentReport{

    // Metode untuk mendapatkan koneksi ke SQL Server (tetap sama)
    private Connection getConnection() throws SQLException {
        String jdbcUrl = "jdbc:sqlserver://localhost:1433;databaseName=University;integratedSecurity=true;encrypt=true;trustServerCertificate=true;";
        // Pastikan DLL untuk integratedSecurity sudah terkonfigurasi jika menggunakan Windows Authentication
        return DriverManager.getConnection(jdbcUrl);
    }

    // Metode untuk menghasilkan laporan menggunakan template JRXML
    public void generateReport() {
        String outputPdfFile = "University_Department_Report_From_Template.pdf";
        String jrxmlTemplatePath = "/Report/Coffee_Landscape.jrxml";
        long startTime = System.currentTimeMillis();

        try (Connection connection = getConnection()) {
            System.out.println("Berhasil terhubung ke database University.");

            // 1. Memuat stream dari file .jrxml
            InputStream reportStream = getClass().getResourceAsStream(jrxmlTemplatePath);
            if (reportStream == null) {
                System.err.println("Template laporan tidak ditemukan di: " + jrxmlTemplatePath);
                System.err.println("Pastikan file .jrxml ada di classpath (misalnya, src/main/resources" + jrxmlTemplatePath + ")");
                return;
            }
            System.out.println("Template laporan " + jrxmlTemplatePath + " berhasil dimuat.");

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
            System.out.println("Template laporan berhasil dikompilasi.");

            Map<String, Object> parameters = new HashMap<>();
            System.out.println("Mengisi laporan dengan data...");
            // 4. Mengisi laporan dengan data dari koneksi database
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
            System.out.println("Laporan berhasil diisi dengan data.");

            // 5. Mengekspor laporan ke format PDF
            JasperExportManager.exportReportToPdfFile(jasperPrint, outputPdfFile);
            System.out.println("Laporan berhasil diekspor ke: " + outputPdfFile);

            // 6. Menampilkan laporan di JasperViewer (opsional)
            JasperViewer.viewReport(jasperPrint, false);
            System.out.println("Menampilkan laporan di JasperViewer...");

            System.out.println("Pembuatan laporan selesai dalam " + (System.currentTimeMillis() - startTime) + " ms.");

        } catch (SQLException e) {
            System.err.println("Koneksi database atau eksekusi SQL gagal: " + e.getMessage());
            e.printStackTrace();
        } catch (JRException e) {
            System.err.println("Terjadi error pada JasperReports: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Terjadi error umum: " + e.getMessage());
            e.printStackTrace();
        }
    }
}