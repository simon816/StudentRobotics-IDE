package com.simon816.rtext.srplugin.tree;

import com.simon816.rtext.srplugin.Plugin;

public abstract class DelayedTreeNode extends AbstractTreeNode {

    private boolean hasLoaded;

    public DelayedTreeNode(Plugin plugin) {
        super(plugin);
        hasLoaded = false;
    }

    public boolean isLoaded() {
        return hasLoaded;
    }

    public abstract void loadTree();

    protected void loadSuccess() {
        hasLoaded = true;
    }

    protected void unload() {
        hasLoaded = false;
    }

}
