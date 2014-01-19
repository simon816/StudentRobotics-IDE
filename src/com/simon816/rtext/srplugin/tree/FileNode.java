package com.simon816.rtext.srplugin.tree;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.srobo.ide.api.SRException;
import org.srobo.ide.api.SRFile;
import org.srobo.ide.api.SRFile.State;

import com.simon816.rtext.srplugin.DiffViewer;
import com.simon816.rtext.srplugin.Plugin;
import com.simon816.rtext.srplugin.NameDialog;
import com.simon816.rtext.srplugin.SRProjectWindow.StagedListModel;
import com.simon816.rtext.srplugin.SRProjectWindow.StagedNode;
import com.simon816.rtext.srplugin.SimpleAction;

public class FileNode extends AbstractTreeNode {

    private class CheckoutAction extends SimpleAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                file.revertChanges();
            } catch (SRException e1) {
                e1.printStackTrace();
                plugin.displayException(e1);
            }
        }

        @Override
        public String getText() {
            return "Revert Changes";
        }

    }

    private class StageAction extends SimpleAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (!file.isStaged())
                    file.stageFile("M");
                ((StagedListModel) plugin.getStagedList().getModel()).addElement(new StagedNode(file.getPath(), file.getState()));
            } catch (SRException e1) {
                e1.printStackTrace();
                plugin.displayException(e1);
            }
        }

        @Override
        public String getText() {
            return "Stage Changes";
        }

    }

    private class UnstageAction extends SimpleAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                file.unstageFile();
                ((StagedListModel) plugin.getStagedList().getModel()).remove(new StagedNode(file.getPath(), file.getState()));
            } catch (SRException e1) {
                e1.printStackTrace();
                plugin.displayException(e1);
            }
        }

        @Override
        public String getText() {
            return "Unstage Changes";
        }

    }

    private class ViewDiffAction extends SimpleAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String diff = file.getDiff(file.getContents());
                new DiffViewer(diff).setVisible(true);
            } catch (SRException e1) {
                e1.printStackTrace();
                plugin.displayException(e1);
            }
        }

        @Override
        public String getText() {
            return "View Diff";
        }

    }

    private class DeleteAction extends SimpleAction {

        private FileNode fileNode;

        private DeleteAction(FileNode fileNode) {
            this.fileNode = fileNode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                file.delete();
                AbstractTreeNode parent = (AbstractTreeNode) fileNode.getParent();
                ((DirectoryTreeNode) getParent()).remove(fileNode);
                ((DefaultTreeModel) plugin.getTree().getModel()).reload(parent);
                ((StagedListModel) plugin.getStagedList().getModel()).addElement(new StagedNode(file.getPath(), file.getState()));
            } catch (SRException e1) {
                e1.printStackTrace();
                plugin.displayException(e1);
            }
        }

        @Override
        public String getText() {
            return "Delete";
        }

    }

    private class RenameAction extends SimpleAction {

        private TreeNode fileNode;

        private RenameAction(FileNode fileNode) {
            this.fileNode = fileNode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            NameDialog dlg = new NameDialog("Rename:", file.getName());
            dlg.setTitle("Rename File");
            dlg.setVisible(true);
            String name = dlg.getName();
            if (name == null || name.equals(""))
                return;
            try {
                file.rename(name);
                ((DefaultTreeModel) plugin.getTree().getModel()).nodeChanged(fileNode);
            } catch (SRException e1) {
                e1.printStackTrace();
                plugin.displayException(e1);
            }
        }

        @Override
        public String getText() {
            return "Rename";
        }

    }

    private SRFile file;
    private boolean listRender;

    public FileNode(Plugin plugin, SRFile file) {
        super(plugin);
        this.file = file;
    }

    @Override
    public String toString() {
        if (listRender)
            return file.getPath();
        return file.getName();
    }

    public void openFile() {
        plugin.openFile(file, this);
    }

    @Override
    public Icon getIcon() {
        if (listRender)
            return getIconByName("icon_check_modified");
        return plugin.iconman.getIconForExt(file.getName());
    }

    @Override
    public void handleLabel(JLabel label) {
        switch (file.getState()) {
        case Unstaged:
            label.setForeground(Color.red);
            break;
        default:
            if (file.isStaged())
                label.setForeground(Color.green);
            break;
        }
    }

    @Override
    public ArrayList<Object> getPopupItems() {
        ArrayList<Object> items = new ArrayList<Object>();
        items.add(new RenameAction(this));
        items.add(new DeleteAction(this));
        if (file.isStaged()) {
            items.add(null);
            items.add(new ViewDiffAction());
            items.add(new UnstageAction());
        } else if (file.getState() == State.Unstaged) {
            items.add(null);
            items.add(new ViewDiffAction());
            items.add(new StageAction());
            items.add(new CheckoutAction());
        }
        return items;
    }

    public FileNode getListRenderNode() {
        FileNode node = (FileNode) clone();
        node.listRender = true;
        return node;
    }

    public boolean isStaged() {
        return file.isStaged();
    }
}
