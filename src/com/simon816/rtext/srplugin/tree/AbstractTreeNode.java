package com.simon816.rtext.srplugin.tree;

import java.net.URL;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.tree.DefaultMutableTreeNode;

import com.simon816.rtext.srplugin.Plugin;

public abstract class AbstractTreeNode extends DefaultMutableTreeNode {
    protected Plugin plugin;

    public AbstractTreeNode(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public abstract String toString();

    public abstract Icon getIcon();

    protected Icon getIconByName(String name) {
        URL url = AbstractTreeNode.class.getResource(name + ".png");
        if (url == null)
            url = AbstractTreeNode.class.getResource(name + ".gif");
        if (url == null)
            return null;
        return new ImageIcon(url);
    }

    public void handleLabel(JLabel jLabel) {
    }

    public abstract ArrayList<Object> getPopupItems();
}
