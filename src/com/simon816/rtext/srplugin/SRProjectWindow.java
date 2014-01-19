package com.simon816.rtext.srplugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.border.EtchedBorder;

import org.fife.rtext.BottomLineBorder;
import org.fife.rtext.RText;
import org.fife.rtext.RTextUtilities;
import org.fife.ui.EscapableDialog;
import org.fife.ui.MenuButton;
import org.fife.ui.dockablewindows.DockableWindow;
import org.fife.ui.dockablewindows.DockableWindowScrollPane;
import org.srobo.ide.api.SRFile.State;
import org.srobo.ide.api.SRTeam;

import com.simon816.rtext.srplugin.tree.ProjectNode;
import com.simon816.rtext.srplugin.tree.TeamTree;

public class SRProjectWindow extends DockableWindow {

    public static class StagedNode {

        private class UnstageAction extends SimpleAction {
            @Override
            public void actionPerformed(ActionEvent e) {
                project.unStage(path);
            }

            @Override
            public String getText() {
                return "Unstage";
            }
        }

        private static final Icon modIcon = new ImageIcon(SRProjectWindow.class.getResource("icon_check_modified.gif"));
        private static final Icon delIcon = new ImageIcon(SRProjectWindow.class.getResource("delete.gif"));
        private static final Icon newIcon = new ImageIcon(SRProjectWindow.class.getResource("new.gif"));
        private State state;
        private String path;
        private ProjectNode project;

        public StagedNode(String path, State state) {
            this.state = state;
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public Icon getIcon() {
            if (state == State.Staged_mod)
                return modIcon;
            if (state == State.Staged_del)
                return delIcon;
            if (state == State.Staged_new)
                return newIcon;
            return null;
        }

        @Override
        public String toString() {
            return path;
        }

        public void onRightClick(JList<StagedNode> jList, Point point) {
            JPopupMenu popup = new JPopupMenu();
            popup.add(new JMenuItem(new UnstageAction()));
            popup.show(jList, point.x, point.y);
        }

        public void setProject(ProjectNode project) {
            this.project = project;
        }

    }

    private class ListRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            StagedNode node = (StagedNode) value;
            super.getListCellRendererComponent(list, node, index, isSelected, cellHasFocus);
            setIcon(node.getIcon());
            return this;
        }

    }

    public class StagedListModel extends AbstractListModel<StagedNode> {

        private ArrayList<StagedNode> list;
        private ArrayList<String> names;
        private ProjectNode project;

        public StagedListModel() {
            super();
            list = new ArrayList<StagedNode>();
            names = new ArrayList<String>();
        }

        @Override
        public int getSize() {
            return list.size();
        }

        @Override
        public StagedNode getElementAt(int index) {
            return list.get(index);
        }

        public int addElement(StagedNode stagedNode) {
            int index = names.size();
            stagedNode.setProject(project);
            if (names.contains(stagedNode.getPath())) {
                remove(stagedNode);
            }
            list.add(stagedNode);
            names.add(stagedNode.getPath());
            fireIntervalAdded(this, index, index);
            commitButton.setEnabled(true);
            return index;
        }

        public void removeAll(ProjectNode project) {
            fireIntervalRemoved(this, 0, names.size());
            list.clear();
            names.clear();
            commitButton.setEnabled(false);
            this.project = project;
        }

        public void remove(StagedNode stagedNode) {
            int index = names.indexOf(stagedNode.getPath());
            if (index == -1)
                return;
            names.remove(index);
            list.remove(index);
            fireIntervalRemoved(this, index, index);
            if (getSize() == 0)
                commitButton.setEnabled(false);
        }
    }

    private class TeamSelection extends EscapableDialog {
        private JComboBox<String> combo;
        private int index;

        public TeamSelection() {
            super();
            SRTeam[] teams = plugin.getTeams();
            String[] names = new String[teams.length];
            for (int i = 0; i < teams.length; i++)
                names[i] = teams[i].getName();
            combo = new JComboBox<String>(names);
            combo.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    @SuppressWarnings("unchecked")
                    JComboBox<String> cb = (JComboBox<String>) e.getSource();
                    index = cb.getSelectedIndex();
                }
            });
            add(combo);
            setModal(true);
            pack();
        }

        public int getIndex() {
            return index;
        }

    }

    private class SwitchTeamAction extends SimpleAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            TeamSelection sel = new TeamSelection();
            sel.setVisible(true);
            plugin.setTeam(sel.getIndex());
            refreshComponents();
        }

        @Override
        public String getText() {
            return "Switch Team";
        }

    }

    private class LogoutAction extends SimpleAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            plugin.logout();
            refreshComponents();

        }

        @Override
        public String getText() {
            return "Logout";
        }

    }

    private class ActionLogin extends SimpleAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            plugin.login(null);
            refreshComponents();
        }

        @Override
        public String getText() {
            return null;
        }

    }

    private class CommitAction implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            ProjectNode p = tree.getProject();
            if (p == null)
                return;
            p.commit();
        }
    }

    private Plugin plugin;
    private TeamTree tree;
    private JList<StagedNode> changeList;
    private MenuButton userButton;
    private ActionListener _acLoginLner;
    private JLabel loginLabel;
    private JSplitPane pane;
    private Action _acPopupLner;
    public JButton commitButton;
    private JButton memstickButton;

    public SRProjectWindow(RText rtext, Plugin plugin, SRPluginPrefs prefs) {

        this.plugin = plugin;
        setDockableWindowName(Messages.getString("Project.DockableWindow.Title"));
        setIcon(plugin.getPluginIcon());
        setPosition(DockableWindow.LEFT);
        setLayout(new BorderLayout());

        _acLoginLner = new ActionLogin();
        add(createToolBar(prefs), BorderLayout.NORTH);
        pane = null;
        loginLabel = null;
        setPosition(prefs.windowPosition);
        setActive(prefs.windowVisible);
    }

    private void addLoginMessage() {
        loginLabel = new JLabel("You need to login to use Student Robotics");
        add(loginLabel);
    }

    private void addTree() {
        tree = new TeamTree(plugin, plugin.getTeam());
        RTextUtilities.removeTabbedPaneFocusTraversalKeyBindings(tree);
        setPrimaryComponent(tree);
        DockableWindowScrollPane sp = new DockableWindowScrollPane(tree);
        RTextUtilities.removeTabbedPaneFocusTraversalKeyBindings(sp);
        JPanel panel = new JPanel(new BorderLayout());
        changeList = new JList<StagedNode>(new StagedListModel()) {
            @Override
            protected void processMouseEvent(MouseEvent e) {
                super.processMouseEvent(e);
                if (e.isPopupTrigger()) {
                    Point point = e.getPoint();
                    int row = this.locationToIndex(point);
                    if (row != -1) {
                        setSelectedIndex(row);
                        getModel().getElementAt(row).onRightClick(this, point);
                    } else {
                        clearSelection();
                    }
                }
            }
        };
        changeList.setCellRenderer(new ListRenderer());
        changeList.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        commitButton = new JButton("Commit");
        commitButton.addActionListener(new CommitAction());
        commitButton.setEnabled(false);
        panel.add(new JLabel("Staging Area"), BorderLayout.NORTH);
        panel.add(changeList, BorderLayout.CENTER);
        panel.add(commitButton, BorderLayout.PAGE_END);
        pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, sp, panel);
        add(pane);
        pane.setDividerLocation(0.8);
        tree.setRootVisible(true);
    }

    private JToolBar createToolBar(SRPluginPrefs prefs) {

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        userButton = new MenuButton(null);
        _acPopupLner = userButton.getAction();
        toolbar.add(userButton);

        toolbar.add(Box.createHorizontalGlue());

        toolbar.add((memstickButton = new JButton("Mem Stick")));
        updateMemStick();
        memstickButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                plugin.openMemStickDlg();
            }
        });

        toolbar.setMinimumSize(new Dimension(8, 8)); // Allow small resize
        toolbar.setBorder(new BottomLineBorder(3));
        return toolbar;

    }

    private void refreshComponents() {
        userButton.removeActionListener(_acLoginLner);
        userButton.removeActionListener(_acPopupLner);
        if (loginLabel != null)
            this.remove(loginLabel);
        if (pane != null)
            this.remove(pane);
        for (int i = 0; i < userButton.getItemCount(); i++) {
            userButton.removeItem(i);
        }
        if (plugin.isLoggedIn()) {
            userButton.addActionListener(_acPopupLner);
            userButton.setText(plugin.getUserName());
            userButton.addMenuItem(new SwitchTeamAction());
            userButton.addMenuItem(new LogoutAction());
            addTree();
        } else {
            userButton.setText("Login");
            userButton.addActionListener(_acLoginLner);
            addLoginMessage();
        }
    }

    public TeamTree getTree() {
        return tree;
    }

    public JList<StagedNode> getStagedList() {
        return changeList;
    }

    public void load() {
        refreshComponents();
    }

    public void updateMemStick() {
        memstickButton.setVisible(plugin.isMemStickInserted);
    }

}
