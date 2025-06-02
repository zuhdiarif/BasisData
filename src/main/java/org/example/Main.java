package org.example;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        System.out.println("Memulai aplikasi laporan universitas...");
        System.out.println("java.library.path saat ini: " + System.getProperty("java.library.path"));
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new IFrame().setVisible(true);
            }
        });
        System.out.println("InstructorSalaryReportFrame dijadwalkan untuk tampil.");
    }
}