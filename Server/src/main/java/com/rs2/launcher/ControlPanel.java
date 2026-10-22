package com.rs2.launcher;

import com.rs2.Server;
import com.rs2.ServerSettings;
import com.rs2.launcher.ConsolePrintStream;
import com.rs2.launcher.ControlPanelWindowCloseListener;
import com.rs2.launcher.MapHorizontalScrollListener;
import com.rs2.launcher.MapVerticalScrollListener;
import com.rs2.launcher.WorldMapPanel;
import com.rs2.model.World;
import com.rs2.util.FileUtil;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ControlPanel
extends JFrame
implements ActionListener {
    private JTabbedPane tabbedPane;
    private JPanel mainPanel;
    private JPanel serverSettingsPanel;
    private JPanel connectionSettingsPanel;
    private JPanel skillSettingsPanel;
    private static BotPopulationPanel botPopulationPanel;
    private static ConfigEditorPanel configEditorPanel;
    private static JButton startServerButton;
    private static JButton restartServerButton;
    static JButton shutdownServerButton;
    private static JButton sendServerMessageButton;
    private static JButton defaultsButton;
    private static JLabel serverStatusLabel;
    private static JLabel usersOnlineLabel;
    private static JLabel serverNameStatusLabel;
    private static JLabel runtimeLabel;
    private static JTextField serverMessageField;
    private static JTextField serverNameField;
    private static JTextField maxPlayersField;
    private static JTextField xpRateField;
    private static JTextField startPositionField;
    private static JTextField respawnPositionField;
    private static JCheckBox duelingEnabledCheckbox;
    private static JCheckBox adminInteractionsCheckbox;
    private static JCheckBox freeToPlayOnlyContentCheckbox;
    private static JCheckBox itemSpawningCheckbox;
    private static JCheckBox funPkCheckbox;
    private static JCheckBox pkWorldCheckbox;
    private static JRadioButton noLoginRestrictionButton;
    private static JRadioButton p2pLoginRestrictionButton;
    private static JRadioButton modLoginRestrictionButton;
    private static JRadioButton adminLoginRestrictionButton;
    private static JButton setSettingsButton;
    private static JTextField serverPortField;
    private static JTextField clientVersionField;
    private static JCheckBox sqliteEnabledCheckbox;
    private static JTextField dbDriverField;
    private static JTextField dbUrlField;
    private static JTextField dbUserField;
    private static JTextField dbPasswordField;
    private static JCheckBox rsaEnabledCheckbox;
    private static JTextField rsaModulusField;
    private static JTextField rsaPrivateExponentField;
    private static JCheckBox debugModeCheckbox;
    private static JCheckBox hiscoresEnabledCheckbox;
    private static JCheckBox developModeCheckbox;
    private static JCheckBox woodcuttingEnabledCheckbox;
    private static JCheckBox thievingEnabledCheckbox;
    private static JCheckBox smithingEnabledCheckbox;
    private static JCheckBox slayerEnabledCheckbox;
    private static JCheckBox runecraftingEnabledCheckbox;
    private static JCheckBox prayerEnabledCheckbox;
    private static JCheckBox miningEnabledCheckbox;
    private static JCheckBox herbloreEnabledCheckbox;
    private static JCheckBox fletchingEnabledCheckbox;
    private static JCheckBox fishingEnabledCheckbox;
    private static JCheckBox firemakingEnabledCheckbox;
    private static JCheckBox farmingEnabledCheckbox;
    private static JCheckBox craftingEnabledCheckbox;
    private static JCheckBox cookingEnabledCheckbox;
    private static JCheckBox agilityEnabledCheckbox;
    private JCheckBox showPlayerNamesCheckbox;
    private JButton worldMapZoomResetButton;
    private Font controlFont = new Font("Calibri", 0, 12);
    static WorldMapPanel worldMapPanel;
    double mapScale;
    private JFrame worldMapFrame;
    private JPanel worldMapContainerPanel;
    private JScrollPane worldMapScrollPane;
    private boolean worldMapDragging;
    private Point worldMapDragStartScreen;
    private Point worldMapDragStartView;
    int mapScrollX;
    int mapScrollY;
    private static String serverStatusHtml;
    private static int displayedMaxPlayers;
    private static int displayedOnlinePlayers;
    private static int displayedModeratorCount;
    private static int displayedAdminCount;
    private static String displayedServerName;
    private static int displayedRuntimeMinutes;
    private static int selectedLoginRestrictionMode;

    static {
        worldMapPanel = new WorldMapPanel();
        serverStatusHtml = "Offline";
        displayedMaxPlayers = 2000;
        displayedOnlinePlayers = 0;
        displayedModeratorCount = 0;
        displayedAdminCount = 0;
        displayedServerName = "Orion";
        displayedRuntimeMinutes = 0;
        selectedLoginRestrictionMode = 0;
    }

    public ControlPanel() {
        Serializable serializable;
        this.mapScale = ControlPanel.worldMapPanel.getZoom();
        this.worldMapFrame = new JFrame();
        try {
            Server.loadConfig();
        }
        catch (Exception iOException) {
            serializable = iOException;
            iOException.printStackTrace();
        }
        this.setTitle("ProgressiveSP Control panel Build #" + ServerSettings.cacheVersion + " [" + ServerSettings.serverVersion + "]");
        ServerSettings.serverEmulatorName = "Progressive";
        this.setSize(440, 370);
        this.setResizable(false);
        this.worldMapFrame.setTitle("LIVE World Map");
        this.worldMapFrame.setBounds(100, 100, 415, 400);
        this.worldMapFrame.setResizable(true);
        this.worldMapContainerPanel = new JPanel();
        this.worldMapContainerPanel.setLayout(new BorderLayout());
        this.worldMapContainerPanel.setDoubleBuffered(false);
        this.worldMapContainerPanel.add(worldMapPanel);
        worldMapPanel.setOpaque(true);
        this.worldMapContainerPanel.setBackground(Color.black);
        this.worldMapContainerPanel.setPreferredSize(new Dimension(ControlPanel.worldMapPanel.mapWidthPixels, ControlPanel.worldMapPanel.mapHeightPixels));
        this.worldMapScrollPane = new JScrollPane(this.worldMapContainerPanel, 22, 32);
        this.worldMapScrollPane.setDoubleBuffered(false);
        this.worldMapScrollPane.getViewport().setDoubleBuffered(false);
        this.worldMapScrollPane.getViewport().setScrollMode(javax.swing.JViewport.SIMPLE_SCROLL_MODE);
        this.worldMapScrollPane.setPreferredSize(new Dimension(512, 512));
        serializable = new JMenuBar();
        this.worldMapScrollPane.getHorizontalScrollBar().addAdjustmentListener(new MapHorizontalScrollListener(this));
        this.worldMapScrollPane.getVerticalScrollBar().addAdjustmentListener(new MapVerticalScrollListener(this));
        MouseAdapter worldMapDragListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() != MouseEvent.BUTTON1) {
                    return;
                }
                ControlPanel.this.worldMapDragging = true;
                ControlPanel.this.worldMapDragStartScreen = mouseEvent.getLocationOnScreen();
                ControlPanel.this.worldMapDragStartView = ControlPanel.this.worldMapScrollPane.getViewport().getViewPosition();
                ControlPanel.worldMapPanel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            }

            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                if (!ControlPanel.this.worldMapDragging
                    || ControlPanel.this.worldMapDragStartScreen == null
                    || ControlPanel.this.worldMapDragStartView == null) {
                    return;
                }

                Point currentScreen = mouseEvent.getLocationOnScreen();
                int dragX = currentScreen.x - ControlPanel.this.worldMapDragStartScreen.x;
                int dragY = currentScreen.y - ControlPanel.this.worldMapDragStartScreen.y;
                Dimension viewportSize = ControlPanel.this.worldMapScrollPane.getViewport().getExtentSize();
                int maxViewX = Math.max(0, ControlPanel.worldMapPanel.mapWidthPixels - viewportSize.width);
                int maxViewY = Math.max(0, ControlPanel.worldMapPanel.mapHeightPixels - viewportSize.height);
                int viewX = Math.max(0, Math.min(ControlPanel.this.worldMapDragStartView.x - dragX, maxViewX));
                int viewY = Math.max(0, Math.min(ControlPanel.this.worldMapDragStartView.y - dragY, maxViewY));
                ControlPanel.this.worldMapScrollPane.getViewport().setViewPosition(new Point(viewX, viewY));
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
                    ControlPanel.this.worldMapDragging = false;
                    ControlPanel.this.worldMapDragStartScreen = null;
                    ControlPanel.this.worldMapDragStartView = null;
                    ControlPanel.worldMapPanel.setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        ControlPanel.worldMapPanel.addMouseListener(worldMapDragListener);
        ControlPanel.worldMapPanel.addMouseMotionListener(worldMapDragListener);
        this.worldMapFrame.getContentPane().add((Component)this.worldMapScrollPane, "Center");
        this.worldMapFrame.getContentPane().add((Component)serializable, "North");
        this.showPlayerNamesCheckbox = new JCheckBox("Show names");
        this.showPlayerNamesCheckbox.setFont(this.controlFont);
        this.showPlayerNamesCheckbox.addActionListener(this);
        ((Container)serializable).add(this.showPlayerNamesCheckbox);
        JButton zoomOutButton = new JButton("-");
        zoomOutButton.setFont(this.controlFont);
        zoomOutButton.setToolTipText("Zoom out");
        zoomOutButton.setActionCommand("Map Zoom Out");
        zoomOutButton.addActionListener(this);
        ((Container)serializable).add(zoomOutButton);
        this.worldMapZoomResetButton = new JButton("100%");
        this.worldMapZoomResetButton.setFont(this.controlFont);
        this.worldMapZoomResetButton.setToolTipText("Reset map zoom to 100%");
        this.worldMapZoomResetButton.setActionCommand("Map Zoom Reset");
        this.worldMapZoomResetButton.addActionListener(this);
        ((Container)serializable).add(this.worldMapZoomResetButton);
        JButton zoomInButton = new JButton("+");
        zoomInButton.setFont(this.controlFont);
        zoomInButton.setToolTipText("Zoom in");
        zoomInButton.setActionCommand("Map Zoom In");
        zoomInButton.addActionListener(this);
        ((Container)serializable).add(zoomInButton);
        this.mapScrollX = 3217;
        this.mapScrollY = 3218;
        Point initialMapPoint = ControlPanel.worldMapPanel.worldToMapPoint(this.mapScrollX, this.mapScrollY);
        this.worldMapScrollPane.getViewport().setViewPosition(new Point(Math.max(0, initialMapPoint.x - 190), Math.max(0, initialMapPoint.y - 160)));
        this.showPlayerNamesCheckbox.setSelected(true);
        ControlPanel.worldMapPanel.showPlayerNames = this.showPlayerNamesCheckbox.isSelected();
        worldMapPanel.repaint();
        this.setBackground(Color.gray);
        serializable = new JPanel();
        ((Container)serializable).setLayout(new BorderLayout());
        this.getContentPane().add((Component)serializable);
        Object value = this;
        this.mainPanel = new JPanel();
        ((ControlPanel)value).mainPanel.setLayout(null);
        Object value2 = serverStatusHtml.equals("Online") ? "<font color=green>" : "<font color=red>";
        serverStatusLabel = new JLabel("<html>Server Status: <b>" + (String)value2 + serverStatusHtml);
        serverStatusLabel.setFont(((ControlPanel)value).controlFont);
        serverStatusLabel.setBounds(10, 15, 250, 20);
        ((ControlPanel)value).mainPanel.add(serverStatusLabel);
        usersOnlineLabel = new JLabel("<html>Users Online: " + displayedOnlinePlayers + "/" + displayedMaxPlayers + " <font color=blue>(" + displayedModeratorCount + " Mods, <font color=orange>" + displayedAdminCount + " Admins)");
        usersOnlineLabel.setFont(((ControlPanel)value).controlFont);
        usersOnlineLabel.setBounds(10, 40, 250, 20);
        ((ControlPanel)value).mainPanel.add(usersOnlineLabel);
        value2 = new JLabel("<html>Server Emulator: <font color=blue>ProgressiveSP");
        ((Component)value2).setFont(((ControlPanel)value).controlFont);
        ((Component)value2).setBounds(10, 65, 250, 20);
        ((ControlPanel)value).mainPanel.add((Component)value2);
        serverNameStatusLabel = new JLabel("<html>Server Name: <font color=#1589FF>" + displayedServerName);
        serverNameStatusLabel.setFont(((ControlPanel)value).controlFont);
        serverNameStatusLabel.setBounds(10, 90, 250, 20);
        ((ControlPanel)value).mainPanel.add(serverNameStatusLabel);
        value2 = new JLabel("Server Message:");
        ((Component)value2).setFont(((ControlPanel)value).controlFont);
        ((Component)value2).setBounds(10, 115, 250, 20);
        ((ControlPanel)value).mainPanel.add((Component)value2);
        serverMessageField = new JTextField();
        serverMessageField.setBounds(10, 140, 150, 20);
        ((ControlPanel)value).mainPanel.add(serverMessageField);
        startServerButton = new JButton("Start Server");
        startServerButton.setFont(((ControlPanel)value).controlFont);
        startServerButton.setBounds(280, 15, 125, 21);
        ((ControlPanel)value).mainPanel.add(startServerButton);
        startServerButton.addActionListener((ActionListener)value);
        restartServerButton = new JButton("Restart Server");
        restartServerButton.setFont(((ControlPanel)value).controlFont);
        restartServerButton.setBounds(280, 40, 125, 21);
        ((ControlPanel)value).mainPanel.add(restartServerButton);
        restartServerButton.addActionListener((ActionListener)value);
        restartServerButton.setEnabled(false);
        value2 = displayedRuntimeMinutes / 60 > 0 ? String.valueOf(displayedRuntimeMinutes / 60) + " hours " + displayedRuntimeMinutes % 60 + " mins" : String.valueOf(displayedRuntimeMinutes) + " mins";
        runtimeLabel = new JLabel("<html>Runtime: <font color=#1589FF>" + (String)value2);
        runtimeLabel.setFont(((ControlPanel)value).controlFont);
        runtimeLabel.setBounds(280, 65, 250, 20);
        ((ControlPanel)value).mainPanel.add(runtimeLabel);
        value2 = new JCheckBox("Restart every");
        ((Component)value2).setBounds(259, 90, 95, 21);
        ((Component)value2).setFont(((ControlPanel)value).controlFont);
        ((JCheckBox)value2).addActionListener((ActionListener)value);
        ((ControlPanel)value).mainPanel.add((Component)value2);
        ((Component)value2).setEnabled(false);
        value2 = new JTextField();
        ((Component)value2).setBounds(355, 90, 30, 20);
        ((ControlPanel)value).mainPanel.add((Component)value2);
        value2 = new JLabel("min");
        ((Component)value2).setFont(((ControlPanel)value).controlFont);
        ((Component)value2).setBounds(386, 90, 30, 20);
        ((ControlPanel)value).mainPanel.add((Component)value2);
        shutdownServerButton = new JButton("Shutdown Server");
        shutdownServerButton.setFont(((ControlPanel)value).controlFont);
        shutdownServerButton.setBounds(280, 140, 125, 21);
        ((ControlPanel)value).mainPanel.add(shutdownServerButton);
        shutdownServerButton.addActionListener((ActionListener)value);
        shutdownServerButton.setEnabled(false);
        sendServerMessageButton = new JButton("Send");
        sendServerMessageButton.setFont(((ControlPanel)value).controlFont);
        sendServerMessageButton.setBounds(170, 140, 65, 21);
        ((ControlPanel)value).mainPanel.add(sendServerMessageButton);
        sendServerMessageButton.addActionListener((ActionListener)value);
        sendServerMessageButton.setEnabled(false);
        value2 = new JLabel("Console:");
        ((Component)value2).setFont(((ControlPanel)value).controlFont);
        ((Component)value2).setBounds(10, 165, 250, 20);
        ((ControlPanel)value).mainPanel.add((Component)value2);
        value2 = new JTextArea(20, 30);
        JScrollPane jScrollPane = new JScrollPane((Component)value2, 22, 31);
        ((Component)value2).setForeground(new Color(0, 0, 0));
        ((Component)value2).setBackground(new Color(255, 255, 255));
        System.setOut(new ConsolePrintStream((JTextArea)value2, jScrollPane));
        jScrollPane.setBounds(10, 190, 400, 110);
        ((Component)value2).setEnabled(false);
        ((ControlPanel)value).mainPanel.add(jScrollPane);
        value = this;
        this.serverSettingsPanel = new JPanel();
        ((ControlPanel)value).serverSettingsPanel.setLayout(null);
        value2 = new JLabel("Server Name:");
        ((Component)value2).setFont(((ControlPanel)value).controlFont);
        ((Component)value2).setBounds(10, 15, 80, 20);
        ((ControlPanel)value).serverSettingsPanel.add((Component)value2);
        serverNameField = new JTextField();
        serverNameField.setBounds(95, 15, 155, 20);
        ((ControlPanel)value).serverSettingsPanel.add(serverNameField);
        serverNameField.setEnabled(false);
        value2 = new JLabel("Max Players:");
        ((Component)value2).setFont(((ControlPanel)value).controlFont);
        ((Component)value2).setBounds(10, 40, 80, 20);
        ((ControlPanel)value).serverSettingsPanel.add((Component)value2);
        maxPlayersField = new JTextField();
        maxPlayersField.setBounds(95, 40, 155, 20);
        ((ControlPanel)value).serverSettingsPanel.add(maxPlayersField);
        maxPlayersField.setEnabled(false);
        value2 = new JLabel("XP Rate:");
        ((Component)value2).setFont(((ControlPanel)value).controlFont);
        ((Component)value2).setBounds(10, 65, 80, 20);
        ((ControlPanel)value).serverSettingsPanel.add((Component)value2);
        xpRateField = new JTextField();
        xpRateField.setBounds(95, 65, 155, 20);
        ((ControlPanel)value).serverSettingsPanel.add(xpRateField);
        value2 = new JLabel("Starting Pos:");
        ((Component)value2).setFont(((ControlPanel)value).controlFont);
        ((Component)value2).setBounds(10, 90, 80, 20);
        ((ControlPanel)value).serverSettingsPanel.add((Component)value2);
        startPositionField = new JTextField();
        startPositionField.setBounds(95, 90, 155, 20);
        ((ControlPanel)value).serverSettingsPanel.add(startPositionField);
        startPositionField.setEnabled(false);
        value2 = new JLabel("Respawn Pos:");
        ((Component)value2).setFont(((ControlPanel)value).controlFont);
        ((Component)value2).setBounds(10, 115, 80, 20);
        ((ControlPanel)value).serverSettingsPanel.add((Component)value2);
        respawnPositionField = new JTextField();
        respawnPositionField.setBounds(95, 115, 155, 20);
        ((ControlPanel)value).serverSettingsPanel.add(respawnPositionField);
        respawnPositionField.setEnabled(false);
        freeToPlayOnlyContentCheckbox = new JCheckBox("F2P only content");
        freeToPlayOnlyContentCheckbox.setBounds(250, 15, 170, 21);
        freeToPlayOnlyContentCheckbox.setFont(((ControlPanel)value).controlFont);
        freeToPlayOnlyContentCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).serverSettingsPanel.add(freeToPlayOnlyContentCheckbox);
        freeToPlayOnlyContentCheckbox.setEnabled(false);
        duelingEnabledCheckbox = new JCheckBox("Enable Dueling");
        duelingEnabledCheckbox.setBounds(250, 40, 170, 21);
        duelingEnabledCheckbox.setFont(((ControlPanel)value).controlFont);
        duelingEnabledCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).serverSettingsPanel.add(duelingEnabledCheckbox);
        duelingEnabledCheckbox.setEnabled(false);
        adminInteractionsCheckbox = new JCheckBox("Allow admin interactions");
        adminInteractionsCheckbox.setBounds(250, 65, 170, 21);
        adminInteractionsCheckbox.setFont(((ControlPanel)value).controlFont);
        adminInteractionsCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).serverSettingsPanel.add(adminInteractionsCheckbox);
        adminInteractionsCheckbox.setEnabled(false);
        itemSpawningCheckbox = new JCheckBox("Item spawning");
        itemSpawningCheckbox.setBounds(250, 90, 170, 21);
        itemSpawningCheckbox.setFont(((ControlPanel)value).controlFont);
        itemSpawningCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).serverSettingsPanel.add(itemSpawningCheckbox);
        itemSpawningCheckbox.setEnabled(false);
        funPkCheckbox = new JCheckBox("Fun PK");
        funPkCheckbox.setBounds(250, 115, 170, 21);
        funPkCheckbox.setFont(((ControlPanel)value).controlFont);
        funPkCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).serverSettingsPanel.add(funPkCheckbox);
        funPkCheckbox.setEnabled(false);
        pkWorldCheckbox = new JCheckBox("PK World");
        pkWorldCheckbox.setBounds(250, 140, 170, 21);
        pkWorldCheckbox.setFont(((ControlPanel)value).controlFont);
        pkWorldCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).serverSettingsPanel.add(pkWorldCheckbox);
        pkWorldCheckbox.setEnabled(false);
        value2 = new JLabel("<html><b>Login Restrictions");
        ((Component)value2).setFont(((ControlPanel)value).controlFont);
        ((Component)value2).setBounds(80, 140, 150, 20);
        ((ControlPanel)value).serverSettingsPanel.add((Component)value2);
        noLoginRestrictionButton = new JRadioButton("None");
        noLoginRestrictionButton.setFont(((ControlPanel)value).controlFont);
        noLoginRestrictionButton.setSelected(true);
        noLoginRestrictionButton.setBounds(10, 165, 60, 20);
        ((ControlPanel)value).serverSettingsPanel.add(noLoginRestrictionButton);
        noLoginRestrictionButton.addActionListener((ActionListener)value);
        noLoginRestrictionButton.setEnabled(false);
        p2pLoginRestrictionButton = new JRadioButton("P2P");
        p2pLoginRestrictionButton.setFont(((ControlPanel)value).controlFont);
        p2pLoginRestrictionButton.setBounds(70, 165, 50, 20);
        ((ControlPanel)value).serverSettingsPanel.add(p2pLoginRestrictionButton);
        p2pLoginRestrictionButton.addActionListener((ActionListener)value);
        p2pLoginRestrictionButton.setEnabled(false);
        modLoginRestrictionButton = new JRadioButton("Mod");
        modLoginRestrictionButton.setFont(((ControlPanel)value).controlFont);
        modLoginRestrictionButton.setBounds(120, 165, 50, 20);
        ((ControlPanel)value).serverSettingsPanel.add(modLoginRestrictionButton);
        modLoginRestrictionButton.addActionListener((ActionListener)value);
        modLoginRestrictionButton.setEnabled(false);
        adminLoginRestrictionButton = new JRadioButton("Admin");
        adminLoginRestrictionButton.setFont(((ControlPanel)value).controlFont);
        adminLoginRestrictionButton.setBounds(175, 165, 60, 20);
        ((ControlPanel)value).serverSettingsPanel.add(adminLoginRestrictionButton);
        adminLoginRestrictionButton.addActionListener((ActionListener)value);
        adminLoginRestrictionButton.setEnabled(false);
        value2 = new ButtonGroup();
        ((ButtonGroup)value2).add(noLoginRestrictionButton);
        ((ButtonGroup)value2).add(p2pLoginRestrictionButton);
        ((ButtonGroup)value2).add(modLoginRestrictionButton);
        ((ButtonGroup)value2).add(adminLoginRestrictionButton);
        setSettingsButton = new JButton("Set");
        setSettingsButton.setBounds(145, 280, 60, 20);
        ((ControlPanel)value).serverSettingsPanel.add(setSettingsButton);
        setSettingsButton.addActionListener((ActionListener)value);
        defaultsButton = new JButton("Defaults");
        defaultsButton.setBounds(210, 280, 85, 20);
        ((ControlPanel)value).serverSettingsPanel.add(defaultsButton);
        defaultsButton.addActionListener((ActionListener)value);
        value = this;
        this.connectionSettingsPanel = new JPanel();
        ((ControlPanel)value).connectionSettingsPanel.setLayout(null);
        value2 = new JLabel("Port Number:");
        ((Component)value2).setFont(((ControlPanel)value).controlFont);
        ((Component)value2).setBounds(10, 15, 80, 20);
        ((ControlPanel)value).connectionSettingsPanel.add((Component)value2);
        serverPortField = new JTextField();
        serverPortField.setBounds(95, 15, 155, 20);
        ((ControlPanel)value).connectionSettingsPanel.add(serverPortField);
        value2 = new JLabel("Client Version:");
        ((Component)value2).setFont(((ControlPanel)value).controlFont);
        ((Component)value2).setBounds(10, 40, 80, 20);
        ((ControlPanel)value).connectionSettingsPanel.add((Component)value2);
        clientVersionField = new JTextField();
        clientVersionField.setBounds(95, 40, 155, 20);
        ((ControlPanel)value).connectionSettingsPanel.add(clientVersionField);
        sqliteEnabledCheckbox = new JCheckBox("SQLite player saves");
        sqliteEnabledCheckbox.setBounds(10, 65, 155, 21);
        sqliteEnabledCheckbox.setFont(((ControlPanel)value).controlFont);
        sqliteEnabledCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).connectionSettingsPanel.add(sqliteEnabledCheckbox);
        value2 = new JLabel("DB Driver:");
        ((Component)value2).setFont(((ControlPanel)value).controlFont);
        ((Component)value2).setBounds(10, 90, 80, 20);
        ((ControlPanel)value).connectionSettingsPanel.add((Component)value2);
        dbDriverField = new JTextField();
        dbDriverField.setBounds(95, 90, 155, 20);
        ((ControlPanel)value).connectionSettingsPanel.add(dbDriverField);
        value2 = new JLabel("DB URL:");
        ((Component)value2).setFont(((ControlPanel)value).controlFont);
        ((Component)value2).setBounds(10, 115, 80, 20);
        ((ControlPanel)value).connectionSettingsPanel.add((Component)value2);
        dbUrlField = new JTextField();
        dbUrlField.setBounds(95, 115, 155, 20);
        ((ControlPanel)value).connectionSettingsPanel.add(dbUrlField);
        value2 = new JLabel("DB User:");
        ((Component)value2).setFont(((ControlPanel)value).controlFont);
        ((Component)value2).setBounds(10, 140, 80, 20);
        ((ControlPanel)value).connectionSettingsPanel.add((Component)value2);
        dbUserField = new JTextField();
        dbUserField.setBounds(95, 140, 155, 20);
        ((ControlPanel)value).connectionSettingsPanel.add(dbUserField);
        value2 = new JLabel("DB Pass:");
        ((Component)value2).setFont(((ControlPanel)value).controlFont);
        ((Component)value2).setBounds(10, 165, 80, 20);
        ((ControlPanel)value).connectionSettingsPanel.add((Component)value2);
        dbPasswordField = new JTextField();
        dbPasswordField.setBounds(95, 165, 155, 20);
        ((ControlPanel)value).connectionSettingsPanel.add(dbPasswordField);
        hiscoresEnabledCheckbox = new JCheckBox("Hiscores Enabled");
        hiscoresEnabledCheckbox.setBounds(260, 15, 155, 21);
        hiscoresEnabledCheckbox.setFont(((ControlPanel)value).controlFont);
        hiscoresEnabledCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).connectionSettingsPanel.add(hiscoresEnabledCheckbox);
        rsaEnabledCheckbox = new JCheckBox("RSA Enabled");
        rsaEnabledCheckbox.setBounds(10, 190, 155, 21);
        rsaEnabledCheckbox.setFont(((ControlPanel)value).controlFont);
        rsaEnabledCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).connectionSettingsPanel.add(rsaEnabledCheckbox);
        value2 = new JLabel("RSA Key1:");
        ((Component)value2).setFont(((ControlPanel)value).controlFont);
        ((Component)value2).setBounds(10, 215, 80, 20);
        ((ControlPanel)value).connectionSettingsPanel.add((Component)value2);
        rsaModulusField = new JTextField();
        rsaModulusField.setBounds(95, 215, 155, 20);
        ((ControlPanel)value).connectionSettingsPanel.add(rsaModulusField);
        value2 = new JLabel("RSA Key2:");
        ((Component)value2).setFont(((ControlPanel)value).controlFont);
        ((Component)value2).setBounds(10, 240, 80, 20);
        ((ControlPanel)value).connectionSettingsPanel.add((Component)value2);
        rsaPrivateExponentField = new JTextField();
        rsaPrivateExponentField.setBounds(95, 240, 155, 20);
        ((ControlPanel)value).connectionSettingsPanel.add(rsaPrivateExponentField);
        debugModeCheckbox = new JCheckBox("Debug Mode");
        debugModeCheckbox.setBounds(260, 40, 155, 21);
        debugModeCheckbox.setFont(((ControlPanel)value).controlFont);
        debugModeCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).connectionSettingsPanel.add(debugModeCheckbox);
        developModeCheckbox = new JCheckBox("Develop Mode");
        developModeCheckbox.setBounds(260, 65, 155, 21);
        developModeCheckbox.setFont(((ControlPanel)value).controlFont);
        developModeCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).connectionSettingsPanel.add(developModeCheckbox);
        value = this;
        this.skillSettingsPanel = new JPanel();
        ((ControlPanel)value).skillSettingsPanel.setLayout(null);
        value2 = new JLabel("<html><b>Enabled");
        ((Component)value2).setFont(((ControlPanel)value).controlFont);
        ((Component)value2).setBounds(10, 15, 80, 20);
        ((ControlPanel)value).skillSettingsPanel.add((Component)value2);
        woodcuttingEnabledCheckbox = new JCheckBox("Woodcutting");
        woodcuttingEnabledCheckbox.setBounds(20, 40, 95, 21);
        woodcuttingEnabledCheckbox.setFont(((ControlPanel)value).controlFont);
        woodcuttingEnabledCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).skillSettingsPanel.add(woodcuttingEnabledCheckbox);
        thievingEnabledCheckbox = new JCheckBox("Thieving");
        thievingEnabledCheckbox.setBounds(20, 65, 95, 21);
        thievingEnabledCheckbox.setFont(((ControlPanel)value).controlFont);
        thievingEnabledCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).skillSettingsPanel.add(thievingEnabledCheckbox);
        smithingEnabledCheckbox = new JCheckBox("Smithing");
        smithingEnabledCheckbox.setBounds(20, 90, 95, 21);
        smithingEnabledCheckbox.setFont(((ControlPanel)value).controlFont);
        smithingEnabledCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).skillSettingsPanel.add(smithingEnabledCheckbox);
        slayerEnabledCheckbox = new JCheckBox("Slayer");
        slayerEnabledCheckbox.setBounds(20, 115, 95, 21);
        slayerEnabledCheckbox.setFont(((ControlPanel)value).controlFont);
        slayerEnabledCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).skillSettingsPanel.add(slayerEnabledCheckbox);
        runecraftingEnabledCheckbox = new JCheckBox("Runecrafting");
        runecraftingEnabledCheckbox.setBounds(20, 140, 95, 21);
        runecraftingEnabledCheckbox.setFont(((ControlPanel)value).controlFont);
        runecraftingEnabledCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).skillSettingsPanel.add(runecraftingEnabledCheckbox);
        prayerEnabledCheckbox = new JCheckBox("Prayer");
        prayerEnabledCheckbox.setBounds(20, 165, 95, 21);
        prayerEnabledCheckbox.setFont(((ControlPanel)value).controlFont);
        prayerEnabledCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).skillSettingsPanel.add(prayerEnabledCheckbox);
        miningEnabledCheckbox = new JCheckBox("Mining");
        miningEnabledCheckbox.setBounds(20, 190, 95, 21);
        miningEnabledCheckbox.setFont(((ControlPanel)value).controlFont);
        miningEnabledCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).skillSettingsPanel.add(miningEnabledCheckbox);
        herbloreEnabledCheckbox = new JCheckBox("Herblore");
        herbloreEnabledCheckbox.setBounds(20, 215, 95, 21);
        herbloreEnabledCheckbox.setFont(((ControlPanel)value).controlFont);
        herbloreEnabledCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).skillSettingsPanel.add(herbloreEnabledCheckbox);
        fletchingEnabledCheckbox = new JCheckBox("Fletching");
        fletchingEnabledCheckbox.setBounds(20, 240, 95, 21);
        fletchingEnabledCheckbox.setFont(((ControlPanel)value).controlFont);
        fletchingEnabledCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).skillSettingsPanel.add(fletchingEnabledCheckbox);
        fishingEnabledCheckbox = new JCheckBox("Fishing");
        fishingEnabledCheckbox.setBounds(20, 265, 95, 21);
        fishingEnabledCheckbox.setFont(((ControlPanel)value).controlFont);
        fishingEnabledCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).skillSettingsPanel.add(fishingEnabledCheckbox);
        value2 = new JLabel("<html><b>Enabled");
        ((Component)value2).setFont(((ControlPanel)value).controlFont);
        ((Component)value2).setBounds(110, 15, 80, 20);
        ((ControlPanel)value).skillSettingsPanel.add((Component)value2);
        firemakingEnabledCheckbox = new JCheckBox("Firemaking");
        firemakingEnabledCheckbox.setBounds(120, 40, 95, 21);
        firemakingEnabledCheckbox.setFont(((ControlPanel)value).controlFont);
        firemakingEnabledCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).skillSettingsPanel.add(firemakingEnabledCheckbox);
        farmingEnabledCheckbox = new JCheckBox("Farming");
        farmingEnabledCheckbox.setBounds(120, 65, 95, 21);
        farmingEnabledCheckbox.setFont(((ControlPanel)value).controlFont);
        farmingEnabledCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).skillSettingsPanel.add(farmingEnabledCheckbox);
        craftingEnabledCheckbox = new JCheckBox("Crafting");
        craftingEnabledCheckbox.setBounds(120, 90, 95, 21);
        craftingEnabledCheckbox.setFont(((ControlPanel)value).controlFont);
        craftingEnabledCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).skillSettingsPanel.add(craftingEnabledCheckbox);
        cookingEnabledCheckbox = new JCheckBox("Cooking");
        cookingEnabledCheckbox.setBounds(120, 115, 95, 21);
        cookingEnabledCheckbox.setFont(((ControlPanel)value).controlFont);
        cookingEnabledCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).skillSettingsPanel.add(cookingEnabledCheckbox);
        agilityEnabledCheckbox = new JCheckBox("Agility");
        agilityEnabledCheckbox.setBounds(120, 140, 95, 21);
        agilityEnabledCheckbox.setFont(((ControlPanel)value).controlFont);
        agilityEnabledCheckbox.addActionListener((ActionListener)value);
        ((ControlPanel)value).skillSettingsPanel.add(agilityEnabledCheckbox);
        this.tabbedPane = new JTabbedPane();
        this.tabbedPane.addTab("Main", this.mainPanel);
        botPopulationPanel = new BotPopulationPanel(this.controlFont);
        this.tabbedPane.addTab("Bots", botPopulationPanel);
        configEditorPanel = new ConfigEditorPanel(this.controlFont);
        this.tabbedPane.addTab("Config", configEditorPanel);
        ((Container)serializable).add(this.tabbedPane, "Center");
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(0);
        this.addWindowListener(new ControlPanelWindowCloseListener(this));
        this.worldMapFrame.setLocation(this.getX() + this.getWidth() + 5, this.getY());
        if (FileUtil.exists("data/settings.dat")) {
            try {
                value = new DataInputStream(new BufferedInputStream(new FileInputStream("data/settings.dat")));
                displayedServerName = ((DataInputStream)value).readUTF();
                displayedMaxPlayers = ((DataInputStream)value).readUnsignedShort();
                ServerSettings.serverName = displayedServerName;
                ServerSettings.maxPlayers = displayedMaxPlayers;
                serverNameField.setText(displayedServerName);
                maxPlayersField.setText("" + displayedMaxPlayers);
                xpRateField.setText("" + ((DataInputStream)value).readDouble());
                startPositionField.setText(((DataInputStream)value).readUTF());
                respawnPositionField.setText(((DataInputStream)value).readUTF());
                freeToPlayOnlyContentCheckbox.setSelected(((DataInputStream)value).readBoolean());
                duelingEnabledCheckbox.setSelected(((DataInputStream)value).readBoolean());
                adminInteractionsCheckbox.setSelected(((DataInputStream)value).readBoolean());
                itemSpawningCheckbox.setSelected(((DataInputStream)value).readBoolean());
                funPkCheckbox.setSelected(((DataInputStream)value).readBoolean());
                pkWorldCheckbox.setSelected(((DataInputStream)value).readBoolean());
                byte value3 = ((DataInputStream)value).readByte();
                selectedLoginRestrictionMode = value3;
                if (value3 == 0) {
                    noLoginRestrictionButton.setSelected(true);
                }
                if (selectedLoginRestrictionMode == 1) {
                    p2pLoginRestrictionButton.setSelected(true);
                }
                if (selectedLoginRestrictionMode == 2) {
                    modLoginRestrictionButton.setSelected(true);
                }
                if (selectedLoginRestrictionMode == 3) {
                    adminLoginRestrictionButton.setSelected(true);
                }
                serverPortField.setText("" + ((DataInputStream)value).readInt());
                clientVersionField.setText("" + ((DataInputStream)value).readShort());
                sqliteEnabledCheckbox.setSelected(((DataInputStream)value).readBoolean());
                dbDriverField.setText(((DataInputStream)value).readUTF());
                dbUrlField.setText(((DataInputStream)value).readUTF());
                dbUserField.setText(((DataInputStream)value).readUTF());
                dbPasswordField.setText(((DataInputStream)value).readUTF());
                rsaEnabledCheckbox.setSelected(((DataInputStream)value).readBoolean());
                rsaModulusField.setText(((DataInputStream)value).readUTF());
                rsaPrivateExponentField.setText(((DataInputStream)value).readUTF());
                hiscoresEnabledCheckbox.setSelected(((DataInputStream)value).readBoolean());
                debugModeCheckbox.setSelected(((DataInputStream)value).readBoolean());
                developModeCheckbox.setSelected(((DataInputStream)value).readBoolean());
                woodcuttingEnabledCheckbox.setSelected(((DataInputStream)value).readBoolean());
                thievingEnabledCheckbox.setSelected(((DataInputStream)value).readBoolean());
                smithingEnabledCheckbox.setSelected(((DataInputStream)value).readBoolean());
                slayerEnabledCheckbox.setSelected(((DataInputStream)value).readBoolean());
                runecraftingEnabledCheckbox.setSelected(((DataInputStream)value).readBoolean());
                prayerEnabledCheckbox.setSelected(((DataInputStream)value).readBoolean());
                miningEnabledCheckbox.setSelected(((DataInputStream)value).readBoolean());
                herbloreEnabledCheckbox.setSelected(((DataInputStream)value).readBoolean());
                fletchingEnabledCheckbox.setSelected(((DataInputStream)value).readBoolean());
                fishingEnabledCheckbox.setSelected(((DataInputStream)value).readBoolean());
                firemakingEnabledCheckbox.setSelected(((DataInputStream)value).readBoolean());
                farmingEnabledCheckbox.setSelected(((DataInputStream)value).readBoolean());
                craftingEnabledCheckbox.setSelected(((DataInputStream)value).readBoolean());
                cookingEnabledCheckbox.setSelected(((DataInputStream)value).readBoolean());
                agilityEnabledCheckbox.setSelected(((DataInputStream)value).readBoolean());
                ((FilterInputStream)value).close();
                value2 = serverNameField.getText();
                ServerSettings.serverName = (String)value2;
                value2 = maxPlayersField.getText();
                ServerSettings.maxPlayers = Integer.parseInt((String)value2);
                xpRateField.getText();
                String[] startPositionParts = startPositionField.getText().split(",");
                ServerSettings.startX = Integer.parseInt(startPositionParts[0]);
                ServerSettings.startY = Integer.parseInt(startPositionParts[1]);
                ServerSettings.startPlane = Integer.parseInt(startPositionParts[2]);
                String[] respawnPositionParts = respawnPositionField.getText().split(",");
                ServerSettings.respawnX = Integer.parseInt(respawnPositionParts[0]);
                ServerSettings.respawnY = Integer.parseInt(respawnPositionParts[1]);
                ServerSettings.respawnPlane = Integer.parseInt(respawnPositionParts[2]);
                ServerSettings.duelingDisabled = !duelingEnabledCheckbox.isSelected();
                ServerSettings.adminInteractionsAllowed = adminInteractionsCheckbox.isSelected();
                ServerSettings.itemSpawningEnabled = itemSpawningCheckbox.isSelected();
                ServerSettings.funPkEnabled = funPkCheckbox.isSelected();
                ServerSettings.pkWorldEnabled = pkWorldCheckbox.isSelected();
                ServerSettings.loginRestrictionMode = selectedLoginRestrictionMode;
                ControlPanel.refreshStatusDisplay();
                value2 = serverPortField.getText();
                ServerSettings.serverPort = Integer.parseInt((String)value2);
                value2 = clientVersionField.getText();
                ServerSettings.clientBuild = Integer.parseInt((String)value2);
                ServerSettings.sqlitePlayerSaveEnabled = sqliteEnabledCheckbox.isSelected();
                if (sqliteEnabledCheckbox.isSelected()) {
                    value2 = dbDriverField.getText();
                    ServerSettings.databaseDriverClass = (String)value2;
                    value2 = dbUrlField.getText();
                    ServerSettings.databaseJdbcUrl = (String)value2;
                    value2 = dbUserField.getText();
                    ServerSettings.databaseUsername = (String)value2;
                    value2 = dbPasswordField.getText();
                    ServerSettings.databasePassword = (String)value2;
                }
                ServerSettings.rsaEnabled = rsaEnabledCheckbox.isSelected();
                if (rsaEnabledCheckbox.isSelected()) {
                    value2 = rsaModulusField.getText();
                    ServerSettings.rsaModulusString = (String)value2;
                    value2 = rsaPrivateExponentField.getText();
                    ServerSettings.rsaPrivateExponentString = (String)value2;
                    ServerSettings.rsaModulus = new BigInteger(ServerSettings.rsaModulusString);
                    ServerSettings.rsaPrivateExponent = new BigInteger(ServerSettings.rsaPrivateExponentString);
                }
                ServerSettings.controlPanelHiscoresEnabled = hiscoresEnabledCheckbox.isSelected();
                ServerSettings.debugModeEnabled = debugModeCheckbox.isSelected();
                ServerSettings.developModeEnabled = developModeCheckbox.isSelected();
                ControlPanel.refreshStatusDisplay();
                ServerSettings.woodcuttingEnabled = woodcuttingEnabledCheckbox.isSelected();
                ServerSettings.thievingEnabled = thievingEnabledCheckbox.isSelected();
                ServerSettings.smithingEnabled = smithingEnabledCheckbox.isSelected();
                ServerSettings.slayerEnabled = slayerEnabledCheckbox.isSelected();
                ServerSettings.runecraftingEnabled = runecraftingEnabledCheckbox.isSelected();
                ServerSettings.prayerEnabled = prayerEnabledCheckbox.isSelected();
                ServerSettings.miningEnabled = miningEnabledCheckbox.isSelected();
                ServerSettings.herbloreEnabled = herbloreEnabledCheckbox.isSelected();
                ServerSettings.fletchingEnabled = fletchingEnabledCheckbox.isSelected();
                ServerSettings.fishingEnabled = fishingEnabledCheckbox.isSelected();
                ServerSettings.firemakingEnabled = firemakingEnabledCheckbox.isSelected();
                ServerSettings.farmingEnabled = farmingEnabledCheckbox.isSelected();
                ServerSettings.craftingEnabled = craftingEnabledCheckbox.isSelected();
                ServerSettings.cookingEnabled = cookingEnabledCheckbox.isSelected();
                ServerSettings.agilityEnabled = agilityEnabledCheckbox.isSelected();
                ControlPanel.refreshStatusDisplay();
                ControlPanel.refreshStatusDisplay();
            }
            catch (Exception exception) {}
        }
    }

    public static void main(String[] args) {
        ControlPanel controlPanel = new ControlPanel();
        controlPanel.setVisible(true);
        if (Boolean.getBoolean("prs.dev.autoStart") && startServerButton != null) {
            startServerButton.doClick();
        }
        if (Boolean.getBoolean("prs.dev.hotReload")) {
            ControlPanel.startDevHotReloadRestartWatcher();
        }
        String classResourceName = controlPanel.getClass().getName().replace('.', '/');
        String classResourcePath = controlPanel.getClass().getResource("/" + classResourceName + ".class").toString();
        String launcherJarPath = null;
        if (classResourcePath.startsWith("jar:")) {
            String[] pathParts = classResourcePath.split("/");
            int pathPartCount = pathParts.length;
            int index = 0;
            while (index < pathPartCount) {
                String pathPart = pathParts[index];
                if (pathPart.contains("!")) {
                    launcherJarPath = pathPart.substring(0, pathPart.length() - 1);
                    break;
                }
                ++index;
            }
        }
        ServerSettings.launcherJarPath = launcherJarPath;
    }

    private static void startDevHotReloadRestartWatcher() {
        final java.io.File restartFlag = new java.io.File(
                System.getProperty("prs.dev.restartFlag", "build/hotswap-restart.flag"));
        Thread watcher = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (restartFlag.exists()) {
                        System.out.println("[Dev] Structural HotSwap edit detected; performing graceful server restart...");
                        if (Server.serverStatus == 0) {
                            System.exit(0);
                            return;
                        }
                        if (Server.serverStatus != 3) {
                            Server.scheduleShutdown(true);
                        }
                        return;
                    }
                    try {
                        Thread.sleep(250L);
                    }
                    catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }, "server-dev-restart-watcher");
        watcher.setDaemon(true);
        watcher.start();
    }

    public static void refreshStatusDisplay() {
        if (startServerButton == null || restartServerButton == null || shutdownServerButton == null || sendServerMessageButton == null || serverStatusLabel == null || usersOnlineLabel == null || serverNameStatusLabel == null || runtimeLabel == null) {
            return;
        }
        if (Server.serverStatus == 2) {
            serverStatusHtml = "<font color=green>Online";
            startServerButton.setEnabled(false);
            shutdownServerButton.setEnabled(true);
        }
        if (Server.serverStatus == 1) {
            serverStatusHtml = "<font color=black>Starting up...";
            startServerButton.setEnabled(false);
            shutdownServerButton.setEnabled(true);
            sendServerMessageButton.setEnabled(false);
        }
        if (Server.serverStatus == 3) {
            serverStatusHtml = "<font color=black>Shutting down...";
            startServerButton.setEnabled(false);
            restartServerButton.setEnabled(false);
            shutdownServerButton.setEnabled(false);
        }
        if (Server.serverStatus == 0) {
            serverStatusHtml = "<font color=red>Offline";
            startServerButton.setEnabled(true);
            restartServerButton.setEnabled(false);
            shutdownServerButton.setEnabled(false);
            sendServerMessageButton.setEnabled(false);
        }
        serverStatusLabel.setText("<html>Server Status: <b>" + serverStatusHtml);
        displayedRuntimeMinutes = Server.runtimeMinutes;
        displayedServerName = ServerSettings.serverName;
        serverNameStatusLabel.setText("<html>Server Name: <font color=#1589FF>" + displayedServerName);
        String text = displayedRuntimeMinutes / 60 > 0 ? String.valueOf(displayedRuntimeMinutes / 60) + " hours " + displayedRuntimeMinutes % 60 + " mins" : String.valueOf(displayedRuntimeMinutes) + " mins";
        runtimeLabel.setText("<html>Runtime: <font color=#1589FF>" + text);
        displayedOnlinePlayers = Server.onlinePlayerCount;
        displayedMaxPlayers = ServerSettings.maxPlayers;
        displayedModeratorCount = Server.moderatorPlayerCount;
        displayedAdminCount = Server.adminPlayerCount;
        usersOnlineLabel.setText("<html>Users Online: " + displayedOnlinePlayers + "/" + displayedMaxPlayers + " <font color=blue>(" + displayedModeratorCount + " Mods, <font color=orange>" + displayedAdminCount + " Admins)");
        worldMapPanel.repaint();
        if (botPopulationPanel != null) {
            botPopulationPanel.refreshLockState();
        }
    }

    private void changeWorldMapZoom(double requestedZoom) {
        double oldZoom = ControlPanel.worldMapPanel.getZoom();
        if (requestedZoom < 0.5) {
            requestedZoom = 0.5;
        }
        if (requestedZoom > 3.0) {
            requestedZoom = 3.0;
        }
        if (Math.abs(requestedZoom - oldZoom) < 0.0001) {
            return;
        }

        Point oldViewPosition = this.worldMapScrollPane.getViewport().getViewPosition();
        Dimension viewportSize = this.worldMapScrollPane.getViewport().getExtentSize();
        double centerMapX = (oldViewPosition.x + viewportSize.width / 2.0) / oldZoom;
        double centerMapY = (oldViewPosition.y + viewportSize.height / 2.0) / oldZoom;

        ControlPanel.worldMapPanel.setZoom(requestedZoom);
        this.mapScale = requestedZoom;
        this.worldMapContainerPanel.setPreferredSize(
            new Dimension(ControlPanel.worldMapPanel.mapWidthPixels, ControlPanel.worldMapPanel.mapHeightPixels)
        );
        this.worldMapContainerPanel.revalidate();

        int newViewX = (int)Math.round(centerMapX * requestedZoom - viewportSize.width / 2.0);
        int newViewY = (int)Math.round(centerMapY * requestedZoom - viewportSize.height / 2.0);
        int maxViewX = Math.max(0, ControlPanel.worldMapPanel.mapWidthPixels - viewportSize.width);
        int maxViewY = Math.max(0, ControlPanel.worldMapPanel.mapHeightPixels - viewportSize.height);
        newViewX = Math.max(0, Math.min(newViewX, maxViewX));
        newViewY = Math.max(0, Math.min(newViewY, maxViewY));
        this.worldMapScrollPane.getViewport().setViewPosition(new Point(newViewX, newViewY));

        if (this.worldMapZoomResetButton != null) {
            this.worldMapZoomResetButton.setText((int)Math.round(requestedZoom * 100.0) + "%");
        }
        ControlPanel.worldMapPanel.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getActionCommand() == "Start Server") {
            if (configEditorPanel != null && !configEditorPanel.applyPendingChanges(false)) {
                return;
            }
            if (botPopulationPanel != null && !botPopulationPanel.applyPendingChanges(false)) {
                return;
            }
            Server.main(new String[]{""});
            ControlPanel.refreshStatusDisplay();
        }
        if (actionEvent.getActionCommand() == "Shutdown Server") {
            if (World.getPlayerCount() > 0 && World.getNonBotPlayerCount() == 0) {
                World.logoutBotsAndScheduleShutdown(true);
            } else {
                // Single-player world: save and close right away instead of the 5 minute countdown.
                Server.scheduleShutdown(true);
            }
            ControlPanel.refreshStatusDisplay();
        }
        if (actionEvent.getActionCommand() == "Send") {
            String serverMessage = serverMessageField.getText();
            Server.broadcastServerMessage(serverMessage);
        }
        if (actionEvent.getActionCommand() == "Map") {
            this.worldMapFrame.setVisible(true);
        }
        if (actionEvent.getActionCommand() == "Show names") {
            ControlPanel.worldMapPanel.showPlayerNames = this.showPlayerNamesCheckbox.isSelected();
            worldMapPanel.repaint();
        }
        if ("Map Zoom In".equals(actionEvent.getActionCommand())) {
            this.changeWorldMapZoom(ControlPanel.worldMapPanel.getZoom() + 0.25);
        }
        if ("Map Zoom Out".equals(actionEvent.getActionCommand())) {
            this.changeWorldMapZoom(ControlPanel.worldMapPanel.getZoom() - 0.25);
        }
        if ("Map Zoom Reset".equals(actionEvent.getActionCommand())) {
            this.changeWorldMapZoom(1.0);
        }
    }

    static JTabbedPane getTabbedPane(ControlPanel controlPanel) {
        return controlPanel.tabbedPane;
    }
}
