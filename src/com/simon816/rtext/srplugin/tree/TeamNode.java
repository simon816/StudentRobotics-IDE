package com.simon816.rtext.srplugin.tree;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.tree.DefaultTreeModel;

import org.srobo.ide.api.SRException;
import org.srobo.ide.api.SRProject;
import org.srobo.ide.api.SRTeam;

import com.simon816.rtext.srplugin.NameDialog;
import com.simon816.rtext.srplugin.Plugin;
import com.simon816.rtext.srplugin.SimpleAction;

public class TeamNode extends AbstractTreeNode {

    private class NewProjectAction extends SimpleAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            NameDialog dlg = new NameDialog("Project Name:", null);
            dlg.setTitle("New Project");
            dlg.setVisible(true);
            String name = dlg.getName();
            if (name == null || name.equals(""))
                return;
            try {
                addProject(new ProjectNode(plugin, team.newProject(name)));
                ((DefaultTreeModel) plugin.getTree().getModel()).reload();
            } catch (SRException e1) {
                e1.printStackTrace();
                plugin.displayException(e1);
            }
        }

        @Override
        public String getText() {
            return "New Project";
        }

    }

    private class RefreshAction extends SimpleAction {
        private TeamNode teamNode;

        private RefreshAction(TeamNode teamNode) {
            this.teamNode = teamNode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            teamNode.removeAllChildren();
            try {
                for (SRProject project : team.listProjects()) {
                    addProject(new ProjectNode(plugin, project));
                }
                ((DefaultTreeModel) plugin.getTree().getModel()).reload();
            } catch (SRException e1) {
                e1.printStackTrace();
                plugin.displayException(e1);
            }
        }

        @Override
        public String getText() {
            return "Refresh";
        }

    }

    private class ListMembersAction extends SimpleAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub

        }

        @Override
        public String getText() {
            return "List Members";
        }

    }

    private SRTeam team;

    public TeamNode(Plugin plugin, SRTeam team) {
        super(plugin);
        this.team = team;
    }

    public void addProject(ProjectNode projectNode) {
        add(projectNode);
    }

    @Override
    public String toString() {
        return team.getName();
    }

    @Override
    public Icon getIcon() {
        return getIconByName("teamstrm_rep");
    }

    @Override
    public ArrayList<Object> getPopupItems() {
        ArrayList<Object> items = new ArrayList<Object>();
        items.add(new NewProjectAction());
        items.add(new RefreshAction(this));
        items.add(new ListMembersAction());
        return items;
    }

}
