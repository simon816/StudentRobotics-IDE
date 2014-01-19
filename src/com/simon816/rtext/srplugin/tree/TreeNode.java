package com.simon816.rtext.srplugin.tree;

import javax.swing.Icon;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.srobo.ide.api.AbstractFile;
import org.srobo.ide.api.SRException;
import org.srobo.ide.api.SRFile;
import org.srobo.ide.api.SRTree;
import org.srobo.ide.api.SRFile.State;

import com.simon816.rtext.srplugin.NameDialog;
import com.simon816.rtext.srplugin.Plugin;

public class TreeNode extends DirectoryTreeNode {

    private SRTree tree;

    public TreeNode(Plugin plugin, SRTree tree) {
        super(plugin);
        this.tree = tree;
    }

    @Override
    public String toString() {
        return tree.getName();
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public void loadTree() {
        for (AbstractFile file : tree.getEntries()) {
            if (file instanceof SRFile) {
                if (((SRFile) file).getState() != State.Staged_del)
                    add(new FileNode(plugin, (SRFile) file));
            } else if (file instanceof SRTree) {
                add(new TreeNode(plugin, (SRTree) file));
            }
        }
        loadSuccess();
    }

    @Override
    public Icon getIcon() {
        return plugin.iconman.getFolderIcon();
    }

    @Override
    public void handleRefresh() {
        getProjectNode().handleRefresh();
        loadTree();
    }

    public ProjectNode getProjectNode() {
        javax.swing.tree.TreeNode node = this;
        while (!(node instanceof ProjectNode)) {
            System.out.println(node);
            node = node.getParent();
        }
        return (ProjectNode) node;
    }

    @Override
    public void handleNewFile() {
        NameDialog dlg = new NameDialog("File Name:", null);
        dlg.setTitle("New File");
        dlg.setVisible(true);
        String name = dlg.getName();
        if (name == null || name.equals(""))
            return;
        try {
            add(new FileNode(plugin, tree.makeNewFile(name)));
            ((DefaultTreeModel) plugin.getTree().getModel()).reload(this);
            plugin.getTree().expandPath(new TreePath(getPath()));
        } catch (SRException e) {
            e.printStackTrace();
            plugin.displayException(e);
        }
    }

    @Override
    public void handleNewFolder() {
        NameDialog dlg = new NameDialog("Folder Name:", null);
        dlg.setTitle("New Folder");
        dlg.setVisible(true);
        String name = dlg.getName();
        if (name == null || name.equals(""))
            return;
        try {
            add(new TreeNode(plugin, tree.makeNewTree(name)));
            ((DefaultTreeModel) plugin.getTree().getModel()).reload(this);
            plugin.getTree().expandPath(new TreePath(getPath()));
        } catch (SRException e) {
            e.printStackTrace();
            plugin.displayException(e);
        }
    }

    @Override
    public void handleDelete() {
        try {
            tree.delete();
            ((DirectoryTreeNode) getParent()).remove(this);
            ((DefaultTreeModel) plugin.getTree().getModel()).reload(this);
        } catch (SRException e) {
            e.printStackTrace();
            plugin.displayException(e);
        }
    }
}
