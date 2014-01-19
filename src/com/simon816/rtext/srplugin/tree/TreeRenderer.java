package com.simon816.rtext.srplugin.tree;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class TreeRenderer extends DefaultTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean focused) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, focused);
        if (value instanceof AbstractTreeNode) {
            AbstractTreeNode node = (AbstractTreeNode) value;
            setIcon(node.getIcon());
            node.handleLabel((JLabel) this);
        }
        return this;
    }
}
