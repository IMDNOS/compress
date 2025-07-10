package GUI;

import logic.Huffman;


import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class CompressGUI extends JFrame {

    public CompressGUI() {
        applyFrameSettings();
        showMainMenu();
        this.setVisible(true);
    }

    public CompressGUI(File fileToDecode) {
        applyFrameSettings();
        showExtractPage(fileToDecode);
        this.setVisible(true);
    }

    public CompressGUI(List<File> selectedFiles) {
        applyFrameSettings();
        showCompressSettingsPage(selectedFiles);
        this.setVisible(true);
    }

    private void applyFrameSettings() {
        this.setTitle("Compress and Decompress");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBounds(100, 100, 450, 400);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
    }

    private void showMainMenu() {
        this.getContentPane().removeAll();
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        JButton compressButton = new JButton("Compress");
        JButton decompressButton = new JButton("Decompress");

        compressButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        decompressButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        compressButton.setPreferredSize(new Dimension(150, 40));
        decompressButton.setPreferredSize(new Dimension(150, 40));

        compressButton.addActionListener(e -> this.showSelectFilesPage());
        decompressButton.addActionListener(e -> this.showSelectCompressedFilePage());

        panel.add(compressButton);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(decompressButton);

        this.setContentPane(panel);
        this.revalidate();
        this.repaint();
    }

    private void showSelectFilesPage() {
        this.setTitle("Compress");
        ArrayList<File> selectedFiles = new ArrayList<>();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JButton backButton = new JButton("← Back");
        backButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        backButton.addActionListener(e -> showMainMenu());
        panel.add(backButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton selectFilesButton = new JButton("Select Files");
        selectFilesButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        selectFilesButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);
            int option = fileChooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File[] files = fileChooser.getSelectedFiles();
                selectedFiles.clear();
                selectedFiles.addAll(Arrays.asList(files));

                if (!selectedFiles.isEmpty()) {
                    showCompressSettingsPage(selectedFiles);
                } else {
                    JOptionPane.showMessageDialog(this, "No files selected.");
                }
            }
        });

        panel.add(selectFilesButton);

        this.setContentPane(panel);
        this.revalidate();
        this.repaint();
    }

    private void showCompressSettingsPage(List<File> selectedFiles) {
        this.setTitle("Compress");

        Huffman huffman = new Huffman();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JButton backButton = new JButton("← Back");
        backButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        backButton.addActionListener(e -> showSelectFilesPage());
        panel.add(backButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel passwordLabel = new JLabel("Password:");
        JTextField passwordField = new JTextField(15);
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);
        panel.add(passwordPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton compressButton = new JButton("Compress");
        compressButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        compressButton.addActionListener(e -> {
            String pw = passwordField.getText();
            final String finalPassword = pw.isEmpty() ? null : pw;

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Compressed File As");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("IAK Compressed Files (*.iak)", "iak"));

            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection != JFileChooser.APPROVE_OPTION) return;

            File selected = fileChooser.getSelectedFile();
            String filePath = selected.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".iak")) {
                filePath += ".iak";
                selected = new File(filePath);
            }
            final File finalCompressedFile = selected;

            // Progress dialog
            JDialog progressDialog = new JDialog(this, "Compressing...", true);
            JLabel progressLabel = new JLabel("Compressing, please wait...");
            progressLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            progressDialog.add(progressLabel);
            progressDialog.pack();
            progressDialog.setLocationRelativeTo(this);

            SwingWorker<Integer, Void> worker = new SwingWorker<>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    return huffman.compress(selectedFiles, finalCompressedFile, finalPassword);
                }

                @Override
                protected void done() {
                    progressDialog.dispose();
                    try {
                        int ratio = get();
                        JOptionPane.showMessageDialog(CompressGUI.this,
                                "Compression completed successfully!\n" +
                                        "Saved to: " + finalCompressedFile.getAbsolutePath() + "\n" +
                                        "Compression ratio: " + ratio + "%");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(CompressGUI.this,
                                "Compression failed: " + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            worker.execute();
            progressDialog.setVisible(true);
        });

        panel.add(compressButton);

        this.setContentPane(panel);
        this.revalidate();
        this.repaint();
    }

    private void showSelectCompressedFilePage() {
        this.setTitle("Decompress");
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JButton backButton = new JButton("← Back");
        backButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        backButton.addActionListener(e -> showMainMenu());
        panel.add(backButton);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton selectFileButton = new JButton("Select .iak File");
        selectFileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        selectFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Compressed File");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("IAK Compressed Files (*.iak)", "iak"));

            int option = fileChooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                showExtractPage(selectedFile);
            }
        });

        panel.add(selectFileButton);
        this.setContentPane(panel);
        this.revalidate();
        this.repaint();
    }

    private void showExtractPage(File compressedFile) {
        this.setTitle("Decompress");

        Huffman huffman = new Huffman();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JButton backButton = new JButton("← Back");
        backButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        backButton.addActionListener(e -> showSelectCompressedFilePage()); // goes back to select page
        panel.add(backButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        DefaultListModel<String> fileListModel = new DefaultListModel<>();
        JList<String> fileList = new JList<>(fileListModel);
        fileList.setVisibleRowCount(5);
        fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(fileList);
        scrollPane.setPreferredSize(new Dimension(300, 100));
        panel.add(scrollPane);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel passwordLabel = new JLabel("Password:");
        JTextField passwordField = new JTextField(15);
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);
        panel.add(passwordPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        try {
            Map<String, Integer> files = huffman.getAllCompressedFiles(compressedFile);
            for (String fileName : files.keySet()) {
                fileListModel.addElement(fileName);
            }

            JOptionPane.showMessageDialog(this,
                    "Found " + files.size() + " file(s) in archive.",
                    "Files Loaded",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to read archive: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return; // don’t show page if failed
        }

        JButton decodeSelectedButton = new JButton("Decode Selected");
        decodeSelectedButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        decodeSelectedButton.addActionListener(e -> {
            String password = passwordField.getText();
            if (password.isEmpty()) password = null;

            JFileChooser dirChooser = new JFileChooser();
            dirChooser.setDialogTitle("Select Output Folder");
            dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int option = dirChooser.showSaveDialog(this);
            if (option != JFileChooser.APPROVE_OPTION) return;

            File outputDir = dirChooser.getSelectedFile();
            String outputPath = outputDir.getAbsolutePath() + File.separator;

            try {
                Map<String, Integer> files = huffman.getAllCompressedFiles(compressedFile);
                List<String> selected = fileList.getSelectedValuesList();
                if (selected.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please select at least one file to decode.");
                    return;
                }

                boolean allSuccess = true;
                for (String name : selected) {
                    int start = files.get(name);
                    boolean success = huffman.decodeFile(compressedFile, start, outputPath, password);
                    if (!success) {
                        allSuccess = false;
                        break;
                    }
                }

                if (allSuccess) {
                    JOptionPane.showMessageDialog(this, "Selected files are being decoded, please wait", null, JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Decoding failed: Wrong password or corrupted archive.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error decoding: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton decodeAllButton = new JButton("Decode All");
        decodeAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        decodeAllButton.addActionListener(e -> {
            String password = passwordField.getText();
            if (password.isEmpty()) password = null;

            JFileChooser dirChooser = new JFileChooser();
            dirChooser.setDialogTitle("Select Output Folder");
            dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int option = dirChooser.showSaveDialog(this);
            if (option != JFileChooser.APPROVE_OPTION) return;

            File outputDir = dirChooser.getSelectedFile();
            String outputPath = outputDir.getAbsolutePath() + File.separator;

            try {
                boolean success = huffman.decodeAll(compressedFile, outputPath, password);
                if (success) {
                    JOptionPane.showMessageDialog(this, "All files are being decoded, please wait", null, JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Decoding failed: Wrong password or corrupted archive.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error decoding: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(decodeSelectedButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(decodeAllButton);

        this.setContentPane(panel);
        this.revalidate();
        this.repaint();
    }

}
