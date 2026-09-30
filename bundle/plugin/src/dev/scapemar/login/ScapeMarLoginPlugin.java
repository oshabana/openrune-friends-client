package dev.scapemar.login;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.input.MouseAdapter;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

/**
 * ScapeMar creates an account on first sign-in, so "New User" opens a create-account box (name,
 * password, password again) instead of Jagex's sign-up site, and "Existing User" skips the Jagex
 * Account upgrade screen. It polls instead of using @Subscribe because RuneLite cannot bind event
 * handlers of sideloaded plugins on this client build.
 */
@PluginDescriptor(
    name = "ScapeMar Login",
    description = "New User creates an account in the client; Existing User opens the sign-in form",
    enabledByDefault = true
)
public class ScapeMarLoginPlugin extends Plugin {
    private static final int WELCOME_SCREEN = 0;
    private static final int CREDENTIALS_SCREEN = 2;
    private static final int TERMS_SCREEN = 12;
    private static final int JAGEX_ACCOUNT_SCREEN = 38;
    private static final int LOGIN_SCREEN_WIDTH = 765;
    private static final int NEW_USER_X = 302;
    private static final int EXISTING_USER_X = 462;
    private static final int WELCOME_BUTTON_Y = 291;
    private static final int WELCOME_BUTTON_HALF_WIDTH = 74;
    private static final int WELCOME_BUTTON_HALF_HEIGHT = 20;
    private static final int LEGACY_LOGIN_Y = 321;
    private static final int ACCEPT_TERMS_Y = 311;
    private static final int MAX_NAME_LENGTH = 12;

    @Inject private Client client;
    @Inject private ClientThread clientThread;
    @Inject private MouseManager mouseManager;

    private final MouseAdapter newUserClick = new MouseAdapter() {
        @Override
        public MouseEvent mousePressed(MouseEvent event) {
            return interceptNewUser(event);
        }

        @Override
        public MouseEvent mouseReleased(MouseEvent event) {
            return interceptNewUser(event);
        }

        @Override
        public MouseEvent mouseClicked(MouseEvent event) {
            return interceptNewUser(event);
        }
    };

    private ScheduledExecutorService poller;
    private JDialog dialog;
    private volatile String pendingName;
    private volatile String pendingPassword;

    @Override
    protected void startUp() {
        mouseManager.registerMouseListener(0, newUserClick);
        poller = Executors.newSingleThreadScheduledExecutor();
        poller.scheduleWithFixedDelay(() -> clientThread.invoke(this::advance), 300, 300, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void shutDown() {
        mouseManager.unregisterMouseListener(newUserClick);
        poller.shutdownNow();
        SwingUtilities.invokeLater(this::closeDialog);
    }

    private void advance() {
        if (client.getGameState() != GameState.LOGIN_SCREEN) {
            clearPending();
            return;
        }
        switch (client.getLoginIndex()) {
            case TERMS_SCREEN:
                press(NEW_USER_X, ACCEPT_TERMS_Y);
                break;
            case JAGEX_ACCOUNT_SCREEN:
                press(EXISTING_USER_X, LEGACY_LOGIN_Y);
                break;
            case CREDENTIALS_SCREEN:
                submitPending();
                break;
            case WELCOME_SCREEN:
                if (pendingName != null) {
                    press(EXISTING_USER_X, WELCOME_BUTTON_Y);
                }
                break;
            default:
                break;
        }
    }

    private void submitPending() {
        if (pendingName == null) {
            return;
        }
        client.setUsername(pendingName);
        client.setPassword(pendingPassword);
        // Enter on the name field only moves to the password field, so press it on both.
        pressEnter();
        pressEnter();
        clearPending();
    }

    private void clearPending() {
        pendingName = null;
        pendingPassword = null;
    }

    private MouseEvent interceptNewUser(MouseEvent event) {
        if (client.getGameState() != GameState.LOGIN_SCREEN
            || client.getLoginIndex() != WELCOME_SCREEN
            || !onNewUserButton(event.getX(), event.getY())) {
            return event;
        }
        if (event.getID() == MouseEvent.MOUSE_PRESSED) {
            SwingUtilities.invokeLater(this::openDialog);
        }
        event.consume();
        return event;
    }

    private boolean onNewUserButton(int x, int y) {
        int left = loginScreenLeft();
        return Math.abs(x - left - NEW_USER_X) <= WELCOME_BUTTON_HALF_WIDTH
            && Math.abs(y - WELCOME_BUTTON_Y) <= WELCOME_BUTTON_HALF_HEIGHT;
    }

    private void openDialog() {
        if (dialog != null) {
            dialog.toFront();
            return;
        }
        Window owner = SwingUtilities.getWindowAncestor(client.getCanvas());
        dialog = new JDialog(owner, "Create your account");
        JTextField name = new JTextField(16);
        JPasswordField password = new JPasswordField(16);
        JPasswordField repeat = new JPasswordField(16);
        JLabel error = new JLabel(" ");
        error.setForeground(new Color(0xFF, 0x40, 0x40));
        JButton create = new JButton("Create account");
        JButton cancel = new JButton("Cancel");

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 8, 4, 8);
        c.anchor = GridBagConstraints.WEST;
        addRow(form, c, 0, "Username", name);
        addRow(form, c, 1, "Password", password);
        addRow(form, c, 2, "Password again", repeat);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        form.add(error, c);
        JPanel buttons = new JPanel();
        buttons.add(cancel);
        buttons.add(create);
        c.gridy = 4;
        c.anchor = GridBagConstraints.EAST;
        form.add(buttons, c);

        Runnable submit = () -> {
            String username = name.getText().trim();
            char[] first = password.getPassword();
            char[] second = repeat.getPassword();
            String problem = validate(username, first, second);
            if (problem != null) {
                error.setText(problem);
                return;
            }
            pendingPassword = new String(first);
                pendingName = username;
            Arrays.fill(first, '\0');
            Arrays.fill(second, '\0');
            closeDialog();
        };
        create.addActionListener(e -> submit.run());
        name.addActionListener(e -> password.requestFocusInWindow());
        password.addActionListener(e -> repeat.requestFocusInWindow());
        repeat.addActionListener(e -> submit.run());
        cancel.addActionListener(e -> closeDialog());

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                dialog = null;
            }
        });
        dialog.setContentPane(form);
        dialog.getRootPane().setDefaultButton(create);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        name.requestFocusInWindow();
    }

    private static void addRow(JPanel form, GridBagConstraints c, int row, String label, JTextField field) {
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 1;
        form.add(new JLabel(label), c);
        c.gridx = 1;
        form.add(field, c);
    }

    private static String validate(String username, char[] password, char[] repeat) {
        if (username.isEmpty()) {
            return "Choose a username.";
        }
        if (username.length() > MAX_NAME_LENGTH) {
            return "Usernames can be up to " + MAX_NAME_LENGTH + " characters.";
        }
        if (password.length == 0) {
            return "Choose a password.";
        }
        if (!Arrays.equals(password, repeat)) {
            return "The two passwords don't match.";
        }
        return null;
    }

    private void closeDialog() {
        if (dialog != null) {
            dialog.dispose();
            dialog = null;
        }
    }

    private int loginScreenLeft() {
        return Math.max(0, (client.getCanvasWidth() - LOGIN_SCREEN_WIDTH) / 2);
    }

    private void press(int x, int y) {
        Canvas canvas = client.getCanvas();
        int canvasX = loginScreenLeft() + x;
        long now = System.currentTimeMillis();
        canvas.dispatchEvent(new MouseEvent(canvas, MouseEvent.MOUSE_PRESSED, now, InputEvent.BUTTON1_DOWN_MASK, canvasX, y, 1, false, MouseEvent.BUTTON1));
        canvas.dispatchEvent(new MouseEvent(canvas, MouseEvent.MOUSE_RELEASED, now + 1, 0, canvasX, y, 1, false, MouseEvent.BUTTON1));
    }

    private void pressEnter() {
        Canvas canvas = client.getCanvas();
        long now = System.currentTimeMillis();
        canvas.dispatchEvent(new KeyEvent(canvas, KeyEvent.KEY_PRESSED, now, 0, KeyEvent.VK_ENTER, '\n'));
        canvas.dispatchEvent(new KeyEvent(canvas, KeyEvent.KEY_TYPED, now, 0, KeyEvent.VK_UNDEFINED, '\n'));
        canvas.dispatchEvent(new KeyEvent(canvas, KeyEvent.KEY_RELEASED, now + 1, 0, KeyEvent.VK_ENTER, '\n'));
    }
}
