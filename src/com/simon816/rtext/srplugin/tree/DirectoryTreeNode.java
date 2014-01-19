package com.simon816.rtext.srplugin.tree;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JMenu;

import com.simon816.rtext.srplugin.Plugin;
import com.simon816.rtext.srplugin.SimpleAction;

public abstract class DirectoryTreeNode extends DelayedTreeNode {

    class DeleteAction extends SimpleAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            handleDelete();
        }

        @Override
        public String getText() {
            return "Delete";
        }

    }

    private class NewFolderAction extends SimpleAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            handleNewFolder();
        }

        @Override
        public String getText() {
            return "Folder";
        }

    }

    private class NewFileAction extends SimpleAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            handleNewFile();
        }

        @Override
        public String getText() {
            return "File";
        }

    }

    private class RefreshAction extends SimpleAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            handleRefresh();
        }

        @Override
        public String getText() {
            return "Refresh";
        }

    }

    public DirectoryTreeNode(Plugin plugin) {
        super(plugin);
    }

    public abstract void handleDelete();

    public abstract void handleRefresh();

    public abstract void handleNewFile();

    @Override
    public ArrayList<Object> getPopupItems() {
        ArrayList<Object> items = new ArrayList<Object>();
        items.add(new RefreshAction());
        JMenu newmenu = new JMenu("New");
        newmenu.add(new NewFileAction());
        newmenu.add(new NewFolderAction());
        items.add(newmenu);
        items.add(new DeleteAction());
        return items;
    }

    public abstract void handleNewFolder();
}
