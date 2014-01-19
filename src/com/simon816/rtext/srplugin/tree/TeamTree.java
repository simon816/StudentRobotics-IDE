package com.simon816.rtext.srplugin.tree;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import com.simon816.rtext.srplugin.Plugin;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import org.srobo.ide.api.SRException;
import org.srobo.ide.api.SRProject;
import org.srobo.ide.api.SRTeam;

public class TeamTree extends JTree {

    private Plugin plugin;
    private DefaultTreeModel model;
    private TeamNode root;
    private ProjectNode currentProject;

    public TeamTree(Plugin plugin, SRTeam team) {
        this.plugin = plugin;
        root = new TeamNode(plugin, team);
        model = new DefaultTreeModel(root);
        setModel(model);
        model.setRoot(root);

        for (SRProject project : getProjects(team)) {
            root.addProject(new ProjectNode(plugin, project));
        }
        expandPath(new TreePath(root.getPath()));
        setCellRenderer(new TreeRenderer());
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3, 3, 0, 3), getBorder()));
        ToolTipManager.sharedInstance().registerComponent(this);
        BasicTreeUI ui = (BasicTreeUI) getUI();
        ui.setExpandedIcon(null);
        ui.setCollapsedIcon(null);
        currentProject = null;
    }

    private SRProject[] getProjects(SRTeam team) {
        List<SRProject> prj;
        try {
            prj = team.listProjects();
        } catch (SRException e) {
            e.printStackTrace();
            plugin.displayException(e);
            return new SRProject[0];
        }
        SRProject[] projects = new SRProject[prj.size()];
        for (int i = 0; i < projects.length; i++) {
            projects[i] = prj.get(i);
        }
        return projects;
    }

    public TeamNode getRoot() {
        return root;
    }

    private void handleDoubleClick(AbstractTreeNode node) {
        if (node instanceof FileNode) {
            Cursor orig = getCursor();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            ((FileNode) node).openFile();
            setCursor(orig);
        }
        if (node instanceof ProjectNode) {
            currentProject = (ProjectNode) node;
        }
    }

    private void handleSingleClick(AbstractTreeNode node) {
        if (node instanceof ProjectNode && ((ProjectNode) node).isLoaded() && ((ProjectNode) node).isExpanded()) {
            ((ProjectNode) node).updateStagedList();
            currentProject = (ProjectNode) node;
        }
    }

    @Override
    public void fireTreeWillExpand(TreePath path) throws ExpandVetoException {
        super.fireTreeWillExpand(path);

        AbstractTreeNode node = (AbstractTreeNode) path.getLastPathComponent();

        if (node instanceof DelayedTreeNode) {
            DelayedTreeNode dNode = (DelayedTreeNode) node;
            if (!dNode.isLoaded()) {
                Cursor orig = getCursor();
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                dNode.loadTree();
                setCursor(orig);
            }
        }
        if (node instanceof ExpandableNode) {
            ((ExpandableNode) node).onExpand();
        }
    }

    @Override
    public void fireTreeWillCollapse(TreePath path) throws ExpandVetoException {
        super.fireTreeWillCollapse(path);
        AbstractTreeNode node = (AbstractTreeNode) path.getLastPathComponent();

        if (node instanceof ExpandableNode) {
            ((ExpandableNode) node).onCollapse();
        }
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        super.processMouseEvent(e);
        if (e.isPopupTrigger()) {
            Point point = e.getPoint();
            TreePath path = getPathForLocation(point.x, point.y);
            Object node = null;
            if (path != null) {
                setSelectionPath(path);
                scrollPathToVisible(path);
                node = getSelectionPath().getLastPathComponent();
            } else {
                clearSelection();
                node = model.getRoot();
            }
            if (node != null && node instanceof AbstractTreeNode) {
                ArrayList<Object> popupcontent = ((AbstractTreeNode) node).getPopupItems();
                if (popupcontent == null || popupcontent.isEmpty())
                    return;
                JPopupMenu popup = new JPopupMenu();
                for (Object obj : popupcontent) {
                    if (obj instanceof JMenuItem) {
                        popup.add((JMenuItem) obj);
                    } else if (obj == null) {
                        popup.addSeparator();
                    } else {
                        Action action = (Action) obj;
                        popup.add(new JMenuItem(action));
                    }
                }
                popup.show(this, point.x, point.y);

            }
        } else if (e.getID() == MouseEvent.MOUSE_CLICKED) {
            TreePath path = getSelectionPath();
            if (path != null) {
                Object comp = path.getLastPathComponent();
                if (e.getClickCount() == 2) {
                    if (comp instanceof AbstractTreeNode) {
                        handleDoubleClick((AbstractTreeNode) comp);
                    }
                } else if (e.getClickCount() == 1) {
                    if (comp instanceof AbstractTreeNode) {
                        handleSingleClick((AbstractTreeNode) comp);
                    }
                }

            } else {
                clearSelection();
            }
        }
    }

    public ProjectNode getProject() {
        return currentProject;
    }
}
