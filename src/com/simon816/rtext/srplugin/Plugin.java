package com.simon816.rtext.srplugin;

import java.awt.ComponentOrientation;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultTreeModel;

import org.fife.rtext.RText;
import org.fife.rtext.RTextEditorPane;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.app.AbstractPluggableGUIApplication;
import org.fife.ui.app.GUIPlugin;
import org.fife.ui.app.PluginOptionsDialogPanel;
import org.fife.ui.app.StandardAction;
import org.fife.ui.rsyntaxtextarea.CodeTemplateManager;
import org.srobo.ide.api.SRAPI;
import org.srobo.ide.api.SRException;
import org.srobo.ide.api.SRFile;
import org.srobo.ide.api.SRTeam;
import org.srobo.ide.api.SRUser;

import com.simon816.rtext.srplugin.SRProjectWindow.StagedNode;
import com.simon816.rtext.srplugin.memstick.MemoryStickUtility;
import com.simon816.rtext.srplugin.tree.FileNode;
import com.simon816.rtext.srplugin.tree.TeamTree;

public class Plugin extends GUIPlugin {

    private static final String DOCKABLE_WINDOW_PROJECTS = "SRprojectsDockableWindow";
    private RText rtext;
    private SRAPI sr;
    private SRUser user;
    public IconManager iconman;
    private SRProjectWindow window;
    public boolean isMemStickInserted;
    private MemoryStickUtility msu;
    private static boolean atCollege;

    public Plugin(AbstractPluggableGUIApplication app) {
        rtext = (RText) app;
        atCollege = rtext.getWorkingDirectory().startsWith("\\\\brock.internal") || new File("N:\\").exists();
        if (atCollege) {
            File home = new File("N:\\Robotics\\");
            if (!home.exists())
                home.mkdirs();
            System.setProperty("user.home", home.getAbsolutePath());
        }

        // Enable templates in text areas.
        if (RTextUtilities.enableTemplates(rtext, true)) {
            // If there are no templates, assume this is the user's first
            // time in RText and add some "standard" templates.
            CodeTemplateManager ctm = RTextEditorPane.getCodeTemplateManager();
            if (ctm.getTemplateCount() == 0) {
                RTextUtilities.addDefaultCodeTemplates();
            }
        }

        SRPluginPrefs prefs = loadPrefs();

        iconman = new IconManager(this);

        StandardAction a = new ViewProjectsAction((RText) app, this);
        a.setAccelerator(prefs.windowVisibilityAccelerator);

        loadInitialProject(prefs);

        // Window MUST always be created for preference saving on shutdown
        window = new SRProjectWindow(rtext, this, prefs);
        ComponentOrientation o = ComponentOrientation.getOrientation(Locale.getDefault());
        window.applyComponentOrientation(o);
        putDockableWindow(DOCKABLE_WINDOW_PROJECTS, window);
        new Thread(new DriveListenerThread(this)).start();
    }

    public boolean atCollege() {
        return atCollege;
    }

    private void loadInitialProject(SRPluginPrefs prefs) {
        sr = new SRAPI();
        // memoryStickInserted(new File("C:\\Users\\Simon\\Documents\\Robotics\\MemStick"));
       // if (sr == null) {
            try {
                if (prefs.username == null || prefs.username.equals("") || prefs.token == null || prefs.token.equals(""))
                    throw new SRException("No username or token");
                user = sr.Tlogin(prefs.username, prefs.token);
            } catch (SRException e1) {
                login(prefs.username);
            }
       // }
    }

    public void login(String username) {
        LoginDialog dlg = new LoginDialog(rtext, username);
        dlg.setVisible(true);
        while (dlg.wasCanceled() != true) {
            try {
                user = sr.login(dlg.getUsername(), dlg.getPassword());
                break;
            } catch (SRException e) {
                e.printStackTrace();
                dlg.reset(dlg.getUsername());
                dlg.showError(e.getMessage());
                dlg.setVisible(true);
            } catch (Exception e) {
                sr.logout();
                displayException(e);
                break;
            }
        }
    }

    public void displayException(Throwable t) {
        rtext.displayException(t);
    }

    private SRPluginPrefs loadPrefs() {
        SRPluginPrefs prefs = new SRPluginPrefs();
        File prefsFile = getPrefsFile();
        if (prefsFile.isFile()) {
            try {
                prefs.load(prefsFile);
            } catch (IOException ioe) {
                displayException(ioe);
                // (Some) defaults will be used
            }
        }
        return prefs;
    }

    @Override
    public PluginOptionsDialogPanel getOptionsDialogPanel() {
        return null;
    }

    @Override
    public String getPluginAuthor() {
        return "Simon816";
    }

    @Override
    public Icon getPluginIcon() {
        return null;
    }

    @Override
    public String getPluginName() {
        return "Student Robotics Plugin";
    }

    @Override
    public String getPluginVersion() {
        return "0.0.1";
    }

    private static final File getPrefsFile() {
        return new File(RTextUtilities.getPreferencesDirectory(), "sr.properties");
    }

    @Override
    public void install(AbstractPluggableGUIApplication app) {
        window.load();
    }

    @Override
    public void savePreferences() {

        SRProjectWindow window = getDockableWindow();

        SRPluginPrefs prefs = new SRPluginPrefs();
        prefs.windowPosition = window.getPosition();
        // StandardAction a = (StandardAction) rtext.getAction(VIEW_CONSOLE_ACTION);
        // prefs.windowVisibilityAccelerator = a.getAccelerator();
        prefs.windowVisible = window.isActive();
        prefs.openProjectName = (String) (user == null ? null : user.getSetting("project.last"));
        prefs.username = isLoggedIn() ? user.getName() : null;
        prefs.token = isLoggedIn() ? sr.getToken() : null;
        // prefs.treeRootVisible = getTree().isRootVisible();

        File prefsFile = getPrefsFile();
        try {
            prefs.save(prefsFile);
        } catch (IOException ioe) {
            rtext.displayException(ioe);
        }
    }

    public TeamTree getTree() {
        return getDockableWindow().getTree();
    }

    public JList<StagedNode> getStagedList() {
        return getDockableWindow().getStagedList();
    }

    @Override
    public boolean uninstall() {
        savePreferences();
        return true;
    }

    protected boolean isProjectWindowVisible() {
        return getDockableWindow().isActive();
    }

    protected void setProjectWindowVisible(boolean visible) {
        if (visible != isProjectWindowVisible()) {
            getDockableWindow().setActive(visible);
        }
    }

    public SRProjectWindow getDockableWindow() {
        return (SRProjectWindow) getDockableWindow(DOCKABLE_WINDOW_PROJECTS);
    }

    public SRTeam getTeam() {
        return user.getTeam();
    }

    public SRTeam[] getTeams() {
        return user.getTeams();
    }

    public void openFile(SRFile file, FileNode fileNode) {
        String content = file.getContents();
        if (content == null) {
            rtext.displayException(new Exception("Could not retrieve file contents"));
            return;
        }
        rtext.getMainView().openFile(new VirtualFileLocation(this, file, fileNode), "UTF-8", true);
    }

    public void handleFileSave(SRFile file, FileNode fileNode, String newContent) {
        rtext.getStatusBar().setStatusMessage("Uploading file...");
        rtext.getMainView().getCurrentTextArea().setDirty(true);
        file.putContents(newContent);
        rtext.getStatusBar().setStatusMessage("Uploading success");
        rtext.getMainView().getCurrentTextArea().setDirty(false);
        ((DefaultTreeModel) getTree().getModel()).reload(fileNode);
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public String getUserName() {
        return user.getDisplayName();
    }

    public SRTeam setTeam(int teamindex) {
        return user.switchTeam(teamindex);
    }

    public void logout() {
        if (sr.logout())
            user = null;
        else
            JOptionPane.showMessageDialog(rtext, "Failed to logout", "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void memoryStickInserted(File memPath) {
        isMemStickInserted = true;
        getDockableWindow().updateMemStick();
        msu = new MemoryStickUtility(rtext, memPath);
        if (JOptionPane.showConfirmDialog(rtext, "Memory Stick Inserted, open USB utility?", "Memory Stick Inserted", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            openMemStickDlg();
        }
    }

    public void memoryStickRemoved() {
        isMemStickInserted = false;
        getDockableWindow().updateMemStick();
    }

    public void openMemStickDlg() {
        msu.initialize();
        msu.setVisible(true);
    }
}
