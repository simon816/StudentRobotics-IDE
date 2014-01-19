package com.simon816.rtext.srplugin;

import java.awt.event.ActionEvent;
import org.fife.rtext.RText;
import org.fife.ui.app.StandardAction;

public class ViewProjectsAction extends StandardAction {

    private Plugin plugin;

    public ViewProjectsAction(RText owner, Plugin plugin) {
        super(owner, Messages.getBundle(), "Action.ViewSRProjects");
        this.plugin = plugin;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        plugin.setProjectWindowVisible(!plugin.isProjectWindowVisible());
    }

}
