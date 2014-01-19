package com.simon816.rtext.srplugin;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.KeyStroke;

import org.fife.ui.app.Prefs;
import org.fife.ui.dockablewindows.DockableWindow;

public class SRPluginPrefs extends Prefs {

    public int windowPosition;
    public boolean windowVisible;
    public KeyStroke windowVisibilityAccelerator;
    public String openProjectName;
    public String username;
    public String token;

    @Override
    public void load(InputStream in) throws IOException {
        super.load(in);
        // Ensure window position is valid.
        if (!DockableWindow.isValidPosition(windowPosition)) {
            windowPosition = DockableWindow.BOTTOM;
        }
    }

    @Override
    public void setDefaults() {
        windowVisible = true;
        windowPosition = DockableWindow.LEFT;
        windowVisibilityAccelerator = null;
        openProjectName = null;
        username = null;
        token = null;
    }

}
