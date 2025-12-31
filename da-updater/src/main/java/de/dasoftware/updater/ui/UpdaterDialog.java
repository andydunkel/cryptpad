package de.dasoftware.updater.ui;

import de.dasoftware.updater.RemoteUpdateData;
import de.dasoftware.updater.UpdateLogic;
import de.dasoftware.updater.UpdaterData;
import de.dasoftware.updater.WebDownloader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;

/**
 * Swing dialog for application updates
 * Shows current version, available version, and handles update download
 */
public class UpdaterDialog extends JDialog {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// Components
    private JLabel labelTitle;
    private JLabel labelInstalledVersionText;
    private JLabel labelInstalledVersion;
    private JLabel labelAvailableVersionText;
    private JLabel labelAvailableVersion;
    private JLabel labelMessage;
    private JLabel labelIcon;
    private JButton buttonStartUpdate;
    private JButton buttonUpdateLater;
    private JProgressBar progressBar;
    private JLabel linkUpdateInformation;
    
    // Data
    private final UpdaterData data;
    private RemoteUpdateData remoteData;
    private final UpdateLogic logic;
    
    // State
    private boolean runningUpdate = false;
    
    // OS detection
    private static final boolean IS_WINDOWS = System.getProperty("os.name")
            .toLowerCase().contains("windows");
    private static final boolean IS_LINUX = System.getProperty("os.name")
            .toLowerCase().contains("linux");
    private static final boolean IS_MAC = System.getProperty("os.name")
            .toLowerCase().contains("mac");
    
    /**
     * Constructor
     * 
     * @param parent Parent frame (can be null)
     * @param data Local update configuration
     */
    public UpdaterDialog(Frame parent, UpdaterData data) {
        super(parent, true);
        
        this.data = data;
        this.remoteData = new RemoteUpdateData();
        this.logic = new UpdateLogic();
        
        initComponents();
        setupLayout();
        setupListeners();
        
        updateUI();
        
        pack();
        setLocationRelativeTo(parent);
        
        // Start update check after 300ms (like WPF version)
        Timer startTimer = new Timer(300, e -> startUpdateCheck());
        startTimer.setRepeats(false);
        startTimer.start();
    }
    
    /**
     * Initializes all components
     */
    private void initComponents() {
        setTitle(data.getUpdaterTitle());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);
        setPreferredSize(new Dimension(450, 225));
        
        // Title
        labelTitle = new JLabel(data.getAppTitle());
        labelTitle.setFont(new Font("Arial", Font.BOLD, 14));
        
        // Installed version
        labelInstalledVersionText = new JLabel("Installed version:");
        labelInstalledVersion = new JLabel(data.getVersionString());
        
        // Available version
        labelAvailableVersionText = new JLabel("Available version:");
        labelAvailableVersionText.setVisible(false);
        labelAvailableVersion = new JLabel("0.0.0");
        labelAvailableVersion.setVisible(false);
        
        // Message label
        labelMessage = new JLabel("");
        labelMessage.setVisible(false);
        
        // Update information link
        linkUpdateInformation = new JLabel("<html><a href='#'>Show version information</a></html>");
        linkUpdateInformation.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkUpdateInformation.setForeground(Color.BLUE);
        linkUpdateInformation.setVisible(false);
        
        // Icon - Load from resources
        labelIcon = new JLabel();
        labelIcon.setHorizontalAlignment(SwingConstants.CENTER);
        labelIcon.setVerticalAlignment(SwingConstants.CENTER);
        labelIcon.setPreferredSize(new Dimension(80, 80));
        
     // Icon - Draw programmatically
        labelIcon = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                   RenderingHints.VALUE_ANTIALIAS_ON);
                
                int size = 80;
                int centerX = (getWidth() - size) / 2;
                int centerY = (getHeight() - size) / 2;
                
                // Draw circle background
                g2d.setColor(new Color(70, 130, 180));
                g2d.fillOval(centerX, centerY, size, size);
                
                // Draw download arrow (white)
                g2d.setColor(Color.WHITE);
                
                // Arrow shaft
                int shaftWidth = 12;
                int shaftHeight = 35;
                int shaftX = centerX + (size - shaftWidth) / 2;
                int shaftY = centerY + 15;
                g2d.fillRect(shaftX, shaftY, shaftWidth, shaftHeight);
                
                // Arrow head (triangle)
                int[] xPoints = {
                    centerX + size / 2,           // Tip (center)
                    centerX + size / 4,           // Left
                    centerX + size * 3 / 4        // Right
                };
                int[] yPoints = {
                    centerY + size - 12,          // Bottom (tip)
                    centerY + size - 30,          // Top
                    centerY + size - 30           // Top
                };
                g2d.fillPolygon(xPoints, yPoints, 3);
                
                g2d.dispose();
            }
        };
        labelIcon.setHorizontalAlignment(SwingConstants.CENTER);
        labelIcon.setVerticalAlignment(SwingConstants.CENTER);
        labelIcon.setPreferredSize(new Dimension(80, 80));
        
        // Buttons - Text depends on OS
        if (IS_WINDOWS) {
            buttonStartUpdate = new JButton("Start update");
        } else {
            buttonStartUpdate = new JButton("Download update");
        }
        buttonStartUpdate.setPreferredSize(new Dimension(140, 28));
        buttonStartUpdate.setVisible(false);
        
        buttonUpdateLater = new JButton("Update later");
        buttonUpdateLater.setPreferredSize(new Dimension(140, 28));
        buttonUpdateLater.setVisible(false);
        
        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(450, 20));
    }
    
    /**
     * Sets up the layout
     */
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Center panel with content
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        
        // Left side - Version info
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        
        labelTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(labelTitle);
        leftPanel.add(Box.createVerticalStrut(10));
        
        // Version grid
        JPanel versionPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        versionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        versionPanel.add(labelInstalledVersionText);
        versionPanel.add(labelInstalledVersion);
        versionPanel.add(labelAvailableVersionText);
        versionPanel.add(labelAvailableVersion);
        leftPanel.add(versionPanel);
        
        leftPanel.add(Box.createVerticalStrut(10));
        
        labelMessage.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(labelMessage);
        
        leftPanel.add(Box.createVerticalStrut(5));
        
        linkUpdateInformation.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftPanel.add(linkUpdateInformation);
        
        // Right side - Icon
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.add(labelIcon);
        
        centerPanel.add(leftPanel, BorderLayout.CENTER);
        centerPanel.add(rightPanel, BorderLayout.EAST);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.add(buttonUpdateLater);
        buttonPanel.add(buttonStartUpdate);
        
        // Add to main panel
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Content pane layout
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);
    }
    
    /**
     * Sets up event listeners
     */
    private void setupListeners() {
        buttonStartUpdate.addActionListener(e -> {
            if (IS_WINDOWS) {
                runUpdate();
            } else {
                openDownloadPage();
            }
        });
        
        buttonUpdateLater.addActionListener(e -> dispose());
        
        linkUpdateInformation.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openUpdateInformation();
            }
        });
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onWindowClosing();
            }
        });
    }
    
    /**
     * Starts the update check
     */
    private void startUpdateCheck() {
        SwingWorker<RemoteUpdateData, Void> worker = new SwingWorker<RemoteUpdateData, Void>() {
            @Override
            protected RemoteUpdateData doInBackground() throws Exception {
                return logic.loadRemoteData(data.getUpdateUrl());
            }
            
            @Override
            protected void done() {
                try {
                    remoteData = get();
                    
                    if (remoteData.isValid()) {
                        updateUI();
                        
                        if (logic.updateNeeded(data, remoteData)) {
                            if (data.isAutoUpdate() && IS_WINDOWS) {
                                // Auto-update only on Windows
                                runUpdate();
                            }
                        } else {
                            if (data.isAutoClose()) {
                                dispose();
                            }
                        }
                    } else {
                        if (data.isAutoClose()) {
                            dispose();
                        }
                    }
                } catch (Exception ex) {
                    if (data.isAutoClose()) {
                        dispose();
                    } else {
                        showError("Failed to check for updates: " + ex.getMessage());
                    }
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Runs the update download (Windows only)
     */
    private void runUpdate() {
        if (!IS_WINDOWS) {
            openDownloadPage();
            return;
        }
        
        buttonStartUpdate.setEnabled(false);
        buttonUpdateLater.setEnabled(false);
        runningUpdate = true;
        
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
        
        // Create downloader
        WebDownloader downloader = new WebDownloader(remoteData.getDownloadUrl());
        
        // Start download in background
        SwingWorker<String, Integer> worker = new SwingWorker<String, Integer>() {
            @Override
            protected String doInBackground() throws Exception {
                // Simple progress simulation (real implementation would need progress tracking)
                for (int i = 0; i <= 100; i += 10) {
                    Thread.sleep(200);
                    publish(i);
                    
                    if (isCancelled()) {
                        break;
                    }
                }
                
                return downloader.downloadFile();
            }
            
            @Override
            protected void process(java.util.List<Integer> chunks) {
                int latest = chunks.get(chunks.size() - 1);
                progressBar.setValue(latest);
            }
            
            @Override
            protected void done() {
                runningUpdate = false;
                
                try {                    
                    // Start update setup
                    logic.startUpdateSetup();
                    
                    if (data.isTerminateApplicationOnUpdate()) {
                        System.exit(0);
                    } else {
                        dispose();
                    }
                    
                } catch (Exception ex) {
                    if (data.isAutoClose()) {
                        dispose();
                    } else {
                        showError("Download failed: " + ex.getMessage());
                        buttonStartUpdate.setEnabled(true);
                        buttonUpdateLater.setEnabled(true);
                    }
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Opens the download page in browser (Linux/macOS)
     */
    private void openDownloadPage() {
        try {
            String url = remoteData.getInformationUrl();
            if (url == null || url.isEmpty()) {
                url = remoteData.getSoftwareUrl();
            }
            
            if (url != null && !url.isEmpty()) {
                Desktop.getDesktop().browse(new URI(url));
                dispose();
            } else {
                showError("No download URL available.");
            }
        } catch (Exception ex) {
            showError("Could not open browser: " + ex.getMessage());
        }
    }
    
    /**
     * Updates the UI with current state
     */
    private void updateUI() {
        labelTitle.setText(data.getAppTitle());
        labelInstalledVersion.setText(data.getVersionString());
        
        if (!data.getUpdaterTitle().isEmpty()) {
            setTitle(data.getUpdaterTitle());
        }
        
        if (remoteData.isValid()) {
            labelAvailableVersionText.setVisible(true);
            labelAvailableVersion.setVisible(true);
            labelAvailableVersion.setText(remoteData.getVersionString());
            
            if (logic.updateNeeded(data, remoteData)) {
                labelAvailableVersion.setForeground(Color.RED);
                
                if (!remoteData.getInformationUrl().isEmpty()) {
                    linkUpdateInformation.setVisible(true);
                }
                
                if (!data.isAutoUpdate()) {
                    buttonStartUpdate.setVisible(true);
                    buttonUpdateLater.setVisible(true);
                    
                    // Different message based on OS
                    if (IS_WINDOWS) {
                        showMessage("A new version is available!");
                    } else {
                        showMessage("A new version is available! Click 'Download update' to visit the download page.");
                    }
                }
            } else {
                showMessage("Latest version is installed.");
            }
        } else {
            labelAvailableVersionText.setVisible(false);
            labelAvailableVersion.setVisible(false);
        }
    }
    
    /**
     * Shows an error message
     */
    private void showError(String error) {
        labelMessage.setVisible(true);
        labelMessage.setText(error);
        labelMessage.setForeground(Color.RED);
    }
    
    /**
     * Shows a normal message
     */
    private void showMessage(String message) {
        labelMessage.setVisible(true);
        labelMessage.setText(message);
        labelMessage.setForeground(Color.BLACK);
    }
    
    /**
     * Opens the update information URL in browser
     */
    private void openUpdateInformation() {
        try {
            Desktop.getDesktop().browse(new URI(remoteData.getInformationUrl()));
        } catch (Exception ex) {
            showError("Could not open browser: " + ex.getMessage());
        }
    }
    
    /**
     * Handles window closing
     */
    private void onWindowClosing() {
        if (runningUpdate) {
            int result = JOptionPane.showConfirmDialog(
                this,
                "Update is in progress. Cancel update?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (result == JOptionPane.YES_OPTION) {
                // Cancel download if possible
                dispose();
            }
        } else {
            dispose();
        }
    }
    
    /**
     * Gets the operating system name
     * 
     * @return OS name string
     */
    public static String getOperatingSystem() {
        if (IS_WINDOWS) return "Windows";
        if (IS_LINUX) return "Linux";
        if (IS_MAC) return "macOS";
        return "Unknown";
    }
}