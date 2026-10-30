package com.rs2.launcher;

import com.rs2.Server;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public final class ConfigEditorPanel extends JPanel {
    private static final Set<String> SERVER_EXCLUDED_KEYS = new HashSet<String>(Arrays.asList(
        "CACHE_VERSION",
        "CLUE_MERCHANT",
        "WILDY_BOTS",
        "HOTZONES_FOR_WILDYBOTS",
        "SKILLING_BOTS",
        "PROGRESSIVE_BOTS",
        "TRADE_BOTS",
        "OTHER_BOTS",
        "OTHER_BOT_COUNT",
        "CLANWAR_BOTS",
        "DROP_PARTY_CHANCE",
        "SCAMMER_CHANCE",
        "WALKING_BOTS",
        "BOT_ESCAPE_HIGH_LVLS",
        "BOT_XP_RATE",
        "TRADE_BOT_COMMON_ITEM_CHANCE",
        "REMOVED_BOT_NAMES",
        "FORCE_RESET_BOTS",
        "BOT_LOGIN",
        "RELOG_FROZEN_BOTS"
    ));

    private static final ConfigEntry[] ADDITIONAL_SERVER_ENTRIES = new ConfigEntry[]{
        new ConfigEntry("2007_CONTENT", "0", "1 = enable 2007 content, 0 = disable."),
        new ConfigEntry("WILDERNESS_SLAYER", "0", "1 = enable Wilderness Slayer, 0 = disable.")
    };

    private final Font controlFont;
    private final ConfigSourcePanel serverPanel;
    private final ConfigSourcePanel clientPanel;

    public ConfigEditorPanel(Font font) {
        super(new BorderLayout());
        this.controlFont = font;

        JTabbedPane sourceTabs = new JTabbedPane();

        this.serverPanel = new ConfigSourcePanel(
            "Server",
            new File("config/server.cfg"),
            SERVER_EXCLUDED_KEYS,
            true,
            "Server config changes take effect on the next server start."
        );
        sourceTabs.addTab("Server", this.serverPanel);

        File clientConfigFile = ConfigEditorPanel.findClientConfigFile();
        this.clientPanel = new ConfigSourcePanel(
            "Client",
            clientConfigFile,
            new HashSet<String>(),
            false,
            "Client config changes take effect after Reload Userconfig or the next client launch."
        );
        sourceTabs.addTab("Client", this.clientPanel);

        this.add(sourceTabs, BorderLayout.CENTER);
    }

    public boolean applyPendingChanges(boolean showSuccessMessage) {
        if (!this.serverPanel.saveSettings(false)) {
            return false;
        }
        if (!this.clientPanel.saveSettings(false)) {
            return false;
        }
        if (showSuccessMessage) {
            JOptionPane.showMessageDialog(
                this,
                "Configuration saved. Restart the server/client where required for changes to take effect.",
                "Config",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
        return true;
    }

    private static File findClientConfigFile() {
        String configuredPath = System.getProperty("prs.clientConfig");
        if (configuredPath != null && configuredPath.trim().length() > 0) {
            File configuredFile = new File(configuredPath.trim());
            if (configuredFile.exists()) {
                return configuredFile;
            }
        }

        File workingDirectory = new File(System.getProperty("user.dir", ".")).getAbsoluteFile();
        File parentDirectory = workingDirectory.getParentFile();
        File siblingRuntimeConfig = parentDirectory == null
            ? new File("../Client/runtime/userConfig.cfg")
            : new File(parentDirectory, "Client/runtime/userConfig.cfg");

        File[] candidates = new File[]{
            siblingRuntimeConfig,
            new File("../Client/runtime/userConfig.cfg"),
            new File("../Client/userConfig.cfg"),
            new File("runtime/userConfig.cfg"),
            new File("userConfig.cfg")
        };
        for (int index = 0; index < candidates.length; ++index) {
            if (candidates[index].exists()) {
                return candidates[index];
            }
        }
        return candidates[0];
    }

    private final class ConfigSourcePanel extends JPanel {
        private final String sourceName;
        private final File configFile;
        private final Set<String> excludedKeys;
        private final boolean serverConfig;
        private final String applyNote;
        private final Map<String, JTextField> valueFields = new LinkedHashMap<String, JTextField>();
        private final JPanel settingsContainer = new JPanel();
        private final JLabel filePathLabel = new JLabel();
        private final JLabel statusLabel = new JLabel(" ");

        private ConfigSourcePanel(
            String sourceName,
            File configFile,
            Set<String> excludedKeys,
            boolean serverConfig,
            String applyNote
        ) {
            super(new BorderLayout(6, 6));
            this.sourceName = sourceName;
            this.configFile = configFile;
            this.excludedKeys = excludedKeys;
            this.serverConfig = serverConfig;
            this.applyNote = applyNote;

            this.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            JPanel headerPanel = new JPanel(new BorderLayout(6, 4));
            JPanel buttonPanel = new JPanel();

            JButton saveButton = new JButton("Save");
            saveButton.setFont(ConfigEditorPanel.this.controlFont);
            saveButton.addActionListener(actionEvent -> this.saveSettings(true));
            buttonPanel.add(saveButton);

            JButton reloadButton = new JButton("Reload");
            reloadButton.setFont(ConfigEditorPanel.this.controlFont);
            reloadButton.addActionListener(actionEvent -> this.reloadSettings());
            buttonPanel.add(reloadButton);

            this.filePathLabel.setFont(ConfigEditorPanel.this.controlFont);
            this.filePathLabel.setText(this.configFile.getPath());
            headerPanel.add(this.filePathLabel, BorderLayout.NORTH);

            JLabel noteLabel = new JLabel(this.applyNote);
            noteLabel.setFont(ConfigEditorPanel.this.controlFont);
            headerPanel.add(noteLabel, BorderLayout.CENTER);
            headerPanel.add(buttonPanel, BorderLayout.EAST);

            this.add(headerPanel, BorderLayout.NORTH);

            this.settingsContainer.setLayout(new BoxLayout(this.settingsContainer, BoxLayout.Y_AXIS));
            JScrollPane scrollPane = new JScrollPane(this.settingsContainer);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            this.add(scrollPane, BorderLayout.CENTER);

            this.statusLabel.setFont(ConfigEditorPanel.this.controlFont);
            this.add(this.statusLabel, BorderLayout.SOUTH);

            this.reloadSettings();
        }

        private void reloadSettings() {
            this.valueFields.clear();
            this.settingsContainer.removeAll();

            if (!this.configFile.exists()) {
                JLabel missingLabel = new JLabel(
                    "<html><b>" + this.sourceName + " config not found:</b><br>" + this.configFile.getPath() + "</html>"
                );
                missingLabel.setFont(ConfigEditorPanel.this.controlFont);
                missingLabel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
                this.settingsContainer.add(missingLabel);
                this.statusLabel.setText("Config file not found.");
                this.revalidate();
                this.repaint();
                return;
            }

            try {
                List<ConfigEntry> entries = this.readEntries();
                for (int index = 0; index < entries.size(); ++index) {
                    ConfigEntry entry = entries.get(index);
                    this.settingsContainer.add(this.createSettingRow(entry));
                    this.settingsContainer.add(Box.createVerticalStrut(4));
                }
                this.settingsContainer.add(Box.createVerticalGlue());
                this.statusLabel.setText(entries.size() + " settings loaded.");
            }
            catch (IOException ioException) {
                this.statusLabel.setText("Unable to read config: " + ioException.getMessage());
                ioException.printStackTrace();
            }

            this.revalidate();
            this.repaint();
        }

        private Component createSettingRow(ConfigEntry entry) {
            JPanel row = new JPanel(new BorderLayout(8, 3));
            row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 210)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
            ));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

            JLabel keyLabel = new JLabel(ConfigEditorPanel.prettyName(entry.key));
            keyLabel.setFont(ConfigEditorPanel.this.controlFont.deriveFont(Font.BOLD));
            keyLabel.setPreferredSize(new Dimension(175, 24));
            keyLabel.setVerticalAlignment(SwingConstants.TOP);

            JTextField valueField;
            if ("PASSWORD".equals(entry.key)) {
                valueField = new JPasswordField(entry.value);
            } else {
                valueField = new JTextField(entry.value);
            }
            valueField.setFont(ConfigEditorPanel.this.controlFont);
            valueField.setToolTipText("Raw value(s) written after [" + entry.key + "];");
            this.valueFields.put(entry.key, valueField);

            JPanel valuePanel = new JPanel(new BorderLayout(6, 0));
            valuePanel.add(keyLabel, BorderLayout.WEST);
            valuePanel.add(valueField, BorderLayout.CENTER);
            if ("UI_SCALE_PERCENT".equals(entry.key)) {
                JLabel percentLabel = new JLabel("%");
                percentLabel.setFont(ConfigEditorPanel.this.controlFont);
                percentLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
                valuePanel.add(percentLabel, BorderLayout.EAST);
                valueField.setToolTipText("Resizable/fullscreen UI size, from 50% to 200%.");
            } else if ("CAMERA_REFRESH_RATE".equals(entry.key)) {
                JLabel fpsLabel = new JLabel("FPS");
                fpsLabel.setFont(ConfigEditorPanel.this.controlFont);
                fpsLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
                valuePanel.add(fpsLabel, BorderLayout.EAST);
                valueField.setToolTipText("Camera/render refresh rate, from 50 to 240 FPS. Game simulation remains at 50 Hz.");
            }
            row.add(valuePanel, BorderLayout.NORTH);

            if (entry.description.length() > 0) {
                JTextArea descriptionArea = new JTextArea(entry.description);
                descriptionArea.setFont(ConfigEditorPanel.this.controlFont.deriveFont(10.0f));
                descriptionArea.setEditable(false);
                descriptionArea.setOpaque(false);
                descriptionArea.setLineWrap(true);
                descriptionArea.setWrapStyleWord(true);
                descriptionArea.setFocusable(false);
                descriptionArea.setRows(Math.min(3, Math.max(1, entry.descriptionLineCount())));
                row.add(descriptionArea, BorderLayout.CENTER);
            }

            return row;
        }

        private List<ConfigEntry> readEntries() throws IOException {
            Map<String, ConfigEntry> entriesByKey = new LinkedHashMap<String, ConfigEntry>();
            BufferedReader reader = new BufferedReader(new FileReader(this.configFile));
            StringBuilder pendingDescription = new StringBuilder();
            boolean cameraRefreshRatePresent = false;
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("//")) {
                        String comment = trimmed.substring(2).trim();
                        if (!comment.startsWith("[")) {
                            if (pendingDescription.length() > 0) {
                                pendingDescription.append('\n');
                            }
                            pendingDescription.append(comment);
                        }
                        continue;
                    }

                    if (trimmed.startsWith("[") && trimmed.contains("]")) {
                        int closingBracket = trimmed.indexOf(']');
                        String key = trimmed.substring(1, closingBracket);
                        String value = "";
                        if ("CAMERA_REFRESH_RATE".equals(key)) {
                            cameraRefreshRatePresent = true;
                        }
                        if (closingBracket + 1 < trimmed.length() && trimmed.charAt(closingBracket + 1) == ';') {
                            value = trimmed.substring(closingBracket + 2);
                        }

                        if (!this.excludedKeys.contains(key)) {
                            String description = ConfigEditorPanel.cleanDescription(key, pendingDescription.toString());
                            // Config files may contain the same key more than once
                            // after settings are migrated between client versions.
                            // The client applies them in file order, so keep the last
                            // occurrence while showing a single control-panel row.
                            entriesByKey.put(key, new ConfigEntry(key, value, description));
                        }
                        pendingDescription.setLength(0);
                        continue;
                    }

                    if (trimmed.length() > 0) {
                        pendingDescription.setLength(0);
                    }
                }
            }
            finally {
                reader.close();
            }

            // Older client configs predate this option. Still expose it in the
            // control panel immediately; Save will append the setting to the
            // client's cfg if it is not already present.
            if (!this.serverConfig && !cameraRefreshRatePresent) {
                entriesByKey.put("CAMERA_REFRESH_RATE", new ConfigEntry(
                    "CAMERA_REFRESH_RATE",
                    "120",
                    "50-240 = camera/render refresh rate in frames per second.\n"
                        + "Game simulation remains at 50 Hz; this only makes camera motion/redrawing smoother."
                ));
            }
            if (this.serverConfig) {
                for (ConfigEntry entry : ADDITIONAL_SERVER_ENTRIES) {
                    if (!entriesByKey.containsKey(entry.key)) {
                        entriesByKey.put(entry.key, entry);
                    }
                }
            }
            return new ArrayList<ConfigEntry>(entriesByKey.values());
        }

        private boolean saveSettings(boolean showSuccessMessage) {
            if (!this.configFile.exists()) {
                if (showSuccessMessage) {
                    JOptionPane.showMessageDialog(
                        ConfigEditorPanel.this,
                        this.sourceName + " config file was not found:\n" + this.configFile.getPath(),
                        "Config",
                        JOptionPane.WARNING_MESSAGE
                    );
                }
                return !this.serverConfig;
            }

            for (Map.Entry<String, JTextField> fieldEntry : this.valueFields.entrySet()) {
                String value = fieldEntry.getValue().getText().trim();
                if (value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
                    JOptionPane.showMessageDialog(
                        ConfigEditorPanel.this,
                        fieldEntry.getKey() + " contains an invalid line break.",
                        "Config",
                        JOptionPane.WARNING_MESSAGE
                    );
                    return false;
                }
                if (value.length() == 0) {
                    JOptionPane.showMessageDialog(
                        ConfigEditorPanel.this,
                        fieldEntry.getKey() + " cannot be blank.",
                        "Config",
                        JOptionPane.WARNING_MESSAGE
                    );
                    return false;
                }
                if (!this.serverConfig && "UI_SCALE_PERCENT".equals(fieldEntry.getKey())) {
                    try {
                        int uiScalePercent = Integer.parseInt(value);
                        if (uiScalePercent < 50 || uiScalePercent > 200) {
                            JOptionPane.showMessageDialog(
                                ConfigEditorPanel.this,
                                "UI SCALE PERCENT must be between 50 and 200.",
                                "Config",
                                JOptionPane.WARNING_MESSAGE
                            );
                            return false;
                        }
                    }
                    catch (NumberFormatException numberFormatException) {
                        JOptionPane.showMessageDialog(
                            ConfigEditorPanel.this,
                            "UI SCALE PERCENT must be a whole-number percentage between 50 and 200.",
                            "Config",
                            JOptionPane.WARNING_MESSAGE
                        );
                        return false;
                    }
                }
                if (!this.serverConfig && "CAMERA_REFRESH_RATE".equals(fieldEntry.getKey())) {
                    try {
                        int cameraRefreshRate = Integer.parseInt(value);
                        if (cameraRefreshRate < 50 || cameraRefreshRate > 240) {
                            JOptionPane.showMessageDialog(
                                ConfigEditorPanel.this,
                                "CAMERA REFRESH RATE must be between 50 and 240 FPS.",
                                "Config",
                                JOptionPane.WARNING_MESSAGE
                            );
                            return false;
                        }
                    }
                    catch (NumberFormatException numberFormatException) {
                        JOptionPane.showMessageDialog(
                            ConfigEditorPanel.this,
                            "CAMERA REFRESH RATE must be a whole-number FPS value between 50 and 240.",
                            "Config",
                            JOptionPane.WARNING_MESSAGE
                        );
                        return false;
                    }
                }
            }

            try {
                List<String> lines = new ArrayList<String>();
                BufferedReader reader = new BufferedReader(new FileReader(this.configFile));
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        lines.add(line);
                    }
                }
                finally {
                    reader.close();
                }

                Set<String> writtenKeys = new HashSet<String>();
                boolean cameraRefreshRateWritten = false;
                for (int lineIndex = 0; lineIndex < lines.size(); ++lineIndex) {
                    String trimmed = lines.get(lineIndex).trim();
                    if (!trimmed.startsWith("[") || !trimmed.contains("]")) {
                        continue;
                    }
                    int closingBracket = trimmed.indexOf(']');
                    String key = trimmed.substring(1, closingBracket);
                    writtenKeys.add(key);
                    JTextField valueField = this.valueFields.get(key);
                    if (valueField != null) {
                        lines.set(lineIndex, "[" + key + "];" + valueField.getText().trim());
                    }
                    if ("CAMERA_REFRESH_RATE".equals(key)) {
                        cameraRefreshRateWritten = true;
                    }
                }

                if (!this.serverConfig && !cameraRefreshRateWritten) {
                    JTextField cameraRefreshRateField = this.valueFields.get("CAMERA_REFRESH_RATE");
                    if (cameraRefreshRateField != null) {
                        lines.add("");
                        lines.add("//CAMERA_REFRESH_RATE - Parameters for customization:");
                        lines.add("//50-240 = camera/render refresh rate in frames per second.");
                        lines.add("//Game simulation remains at 50 Hz; this only makes camera motion/redrawing smoother.");
                        lines.add("");
                        lines.add("[CAMERA_REFRESH_RATE];" + cameraRefreshRateField.getText().trim());
                    }
                }

                if (this.serverConfig) {
                    for (ConfigEntry entry : ADDITIONAL_SERVER_ENTRIES) {
                        JTextField valueField = this.valueFields.get(entry.key);
                        if (!writtenKeys.contains(entry.key) && valueField != null) {
                            lines.add("");
                            lines.add("//" + entry.key + " - Parameters for customization:");
                            lines.add("//" + entry.description);
                            lines.add("");
                            lines.add("[" + entry.key + "];" + valueField.getText().trim());
                        }
                    }
                }

                File backupFile = new File(this.configFile.getPath() + ".bak");
                Files.copy(
                    this.configFile.toPath(),
                    backupFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                );

                File parentFile = this.configFile.getAbsoluteFile().getParentFile();
                File temporaryFile = new File(parentFile, this.configFile.getName() + ".tmp");
                BufferedWriter writer = new BufferedWriter(new FileWriter(temporaryFile));
                try {
                    for (int lineIndex = 0; lineIndex < lines.size(); ++lineIndex) {
                        writer.write(lines.get(lineIndex));
                        writer.newLine();
                    }
                }
                finally {
                    writer.close();
                }

                Files.move(
                    temporaryFile.toPath(),
                    this.configFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                );

                if (this.serverConfig && Server.serverStatus == 0) {
                    Server.loadConfig();
                }

                String status = "Saved. Backup: " + backupFile.getName();
                if (this.serverConfig && Server.serverStatus != 0) {
                    status += " (server restart required)";
                } else if (!this.serverConfig) {
                    status += " (client restart may be required)";
                }
                this.statusLabel.setText(status);

                if (showSuccessMessage) {
                    JOptionPane.showMessageDialog(
                        ConfigEditorPanel.this,
                        this.sourceName + " configuration saved.",
                        "Config",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }
                return true;
            }
            catch (IOException ioException) {
                this.statusLabel.setText("Unable to save config: " + ioException.getMessage());
                if (showSuccessMessage) {
                    JOptionPane.showMessageDialog(
                        ConfigEditorPanel.this,
                        "Unable to save " + this.sourceName.toLowerCase() + " configuration:\n" + ioException.getMessage(),
                        "Config",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
                ioException.printStackTrace();
                return false;
            }
        }
    }

    private static String prettyName(String key) {
        return key.replace('_', ' ');
    }

    private static String cleanDescription(String key, String description) {
        String[] lines = description.split("\\n");
        StringBuilder cleaned = new StringBuilder();
        for (int index = 0; index < lines.length; ++index) {
            String line = lines[index].trim();
            if (line.length() == 0) {
                continue;
            }
            if (line.startsWith(key) && line.contains("Parameters for customization")) {
                continue;
            }
            if (line.startsWith(key + " (") && line.contains("Parameters for customization")) {
                continue;
            }
            if (cleaned.length() > 0) {
                cleaned.append('\n');
            }
            cleaned.append(line);
        }
        return cleaned.toString();
    }

    private static final class ConfigEntry {
        private final String key;
        private final String value;
        private final String description;

        private ConfigEntry(String key, String value, String description) {
            this.key = key;
            this.value = value;
            this.description = description;
        }

        private int descriptionLineCount() {
            if (this.description.length() == 0) {
                return 0;
            }
            int count = 1;
            for (int index = 0; index < this.description.length(); ++index) {
                if (this.description.charAt(index) == '\n') {
                    ++count;
                }
            }
            return count;
        }
    }
}
