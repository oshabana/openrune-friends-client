package dev.scapemar.login;

import java.awt.Canvas;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

/**
 * ScapeMar creates an account on first sign-in, so the client's "New User" button only sends
 * players to Jagex's sign-up site. This presses "Existing User" on the welcome screen and "Login" on
 * the Jagex Account upgrade screen that follows, so players land on the username/password form.
 * It polls instead of using @Subscribe because RuneLite cannot bind event handlers of sideloaded
 * plugins on this client build.
 */
@PluginDescriptor(
    name = "ScapeMar Login",
    description = "Skips the New User / Existing User screen and opens the sign-in form",
    enabledByDefault = true
)
public class ScapeMarLoginPlugin extends Plugin {
    private static final int WELCOME_SCREEN = 0;
    private static final int JAGEX_ACCOUNT_SCREEN = 38;
    private static final int LOGIN_SCREEN_WIDTH = 765;
    private static final int BUTTON_X = 462;
    private static final int EXISTING_USER_Y = 291;
    private static final int LEGACY_LOGIN_Y = 321;

    @Inject private Client client;
    @Inject private ClientThread clientThread;

    private ScheduledExecutorService poller;

    @Override
    protected void startUp() {
        poller = Executors.newSingleThreadScheduledExecutor();
        poller.scheduleWithFixedDelay(() -> clientThread.invoke(this::skipWelcomeScreen), 1, 1, TimeUnit.SECONDS);
    }

    @Override
    protected void shutDown() {
        poller.shutdownNow();
    }

    private void skipWelcomeScreen() {
        if (client.getGameState() != GameState.LOGIN_SCREEN) {
            return;
        }
        switch (client.getLoginIndex()) {
            case WELCOME_SCREEN:
                press(EXISTING_USER_Y);
                break;
            case JAGEX_ACCOUNT_SCREEN:
                press(LEGACY_LOGIN_Y);
                break;
            default:
                break;
        }
    }

    private void press(int y) {
        Canvas canvas = client.getCanvas();
        int x = Math.max(0, (client.getCanvasWidth() - LOGIN_SCREEN_WIDTH) / 2) + BUTTON_X;
        long now = System.currentTimeMillis();
        canvas.dispatchEvent(new MouseEvent(canvas, MouseEvent.MOUSE_PRESSED, now, InputEvent.BUTTON1_DOWN_MASK, x, y, 1, false, MouseEvent.BUTTON1));
        canvas.dispatchEvent(new MouseEvent(canvas, MouseEvent.MOUSE_RELEASED, now + 1, 0, x, y, 1, false, MouseEvent.BUTTON1));
    }
}
