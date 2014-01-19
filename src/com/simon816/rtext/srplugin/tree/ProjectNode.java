package com.simon816.rtext.srplugin.tree;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.fife.rtext.RTextUtilities;
import org.json.JSONObject;
import org.srobo.ide.api.AbstractFile;
import org.srobo.ide.api.SRException;
import org.srobo.ide.api.SRFile;
import org.srobo.ide.api.SRProject;
import org.srobo.ide.api.SRTree;
import org.srobo.ide.api.SRFile.State;

import com.simon816.rtext.srplugin.CommitDialog;
import com.simon816.rtext.srplugin.NameDialog;
import com.simon816.rtext.srplugin.Plugin;
import com.simon816.rtext.srplugin.SRProjectWindow.StagedListModel;
import com.simon816.rtext.srplugin.SRProjectWindow.StagedNode;
import com.simon816.rtext.srplugin.SimpleAction;

public class ProjectNode extends DirectoryTreeNode implements ExpandableNode {

    private class ExportAction extends SimpleAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            export();
        }

        @Override
        public String getText() {
            return "Export";
        }

    }

    private SRProject project;
    private Icon openedIcon;
    private Icon closedIcon;
    private Icon icon;
    private boolean invalid;
    private SRTree tree;
    private ArrayList<String> pyenvExec = new ArrayList<String>();

    public ProjectNode(Plugin plugin, SRProject project) {
        super(plugin);
        this.project = project;
        openedIcon = getIconByName("ref-10");
        closedIcon = getIconByName("ref-11");
        icon = closedIcon;
        for (String e : new String[] { "bin/flashb", "bin/fwsplash", "bin/imgshow", "bin/pyenv_start", "bin/python", "bin/squidge", "bin/sricd", "bin/srinput", "bin/sr-ts", "firmware/power-bottom", "firmware/power-top", "firmware/servo-bottom",
                "firmware/servo-top", "lib/libelf.so.0", "lib/libkoki.so", "lib/libopencv_contrib.so.2.1", "lib/libopencv_features2d.so.2.1", "lib/libopencv_legacy.so.2.1", "lib/libopencv_video.so.2.1", "lib/libsric.so", "pylib/sr/enumerate_tty.py",
                "fw.py", "run.py" }) {
            pyenvExec.add(e);
        }
    }

    private ArrayList<SRFile> getFiles(SRTree root) {
        ArrayList<SRFile> list = new ArrayList<SRFile>();
        for (AbstractFile file : root.getEntries()) {
            if (file instanceof SRTree) {
                list.addAll(getFiles((SRTree) file));
            } else if (file instanceof SRFile) {
                list.add((SRFile) file);
            }
        }
        return list;
    }

    private class ExDlg extends JFrame {
        private JLabel text;

        public ExDlg() {
            super("Exporting");
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            JPanel cp = new JPanel(new BorderLayout());
            setContentPane(cp);
            text = new JLabel("Begin Export");
            cp.add(text);
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
        }

        public void setText(String text) {
            this.text.setText(text);
        }
    }

    public void export() {
        SRTree root;
        Deflater def = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        try {
            root = project.listAllFiles();
        } catch (Exception e) {
            e.printStackTrace();
            plugin.displayException(e);
            return;
        }
        ZipOutputStream zip;
        File zipfile = new File(RTextUtilities.getPreferencesDirectory(), "robot.zip");
        try {
            zip = new ZipOutputStream(new FileOutputStream(zipfile), StandardCharsets.US_ASCII);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            plugin.displayException(e);
            return;
        }
        zip.setMethod(8);
        ExDlg dlg = new ExDlg();
        for (SRFile file : getFiles(root)) {
            try {
                dlg.setText(file.getPath());
                byte[] bytes = file.getContents().getBytes("UTF-8");
                ZipEntry entry = new ZipEntry("user/" + file.getPath());
                entry.setMethod(8);
                entry.setSize(bytes.length);
                CRC32 crc = new CRC32();
                crc.update(bytes);
                entry.setCrc(crc.getValue());
                def.setInput(bytes);
                byte[] buf = new byte[1024 * 64];
                while (!def.needsInput()) {
                    def.deflate(buf, 0, buf.length);
                }
                def.finish();
                while (!def.finished()) {
                    def.deflate(buf);
                }
                entry.setCompressedSize(def.getBytesWritten());
                def.reset();
                zip.putNextEntry(entry);
                zip.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
                plugin.displayException(e);
            }
        }
        // try {
        // zip.putNextEntry(new ZipEntry("user/.user-rev"));
        // zip.write((project.getName() + " @ HEAD").getBytes());
        // } catch (IOException e2) {
        // e2.printStackTrace();
        // plugin.displayException(e2);
        // }
        try {
            File f = new File(RTextUtilities.getPreferencesDirectory(), "pyenv.zip");
            ZipInputStream pyenv = new ZipInputStream(new FileInputStream(f));
            ZipEntry entry;
            while ((entry = pyenv.getNextEntry()) != null) {
                // entry = new ZipEntry(entry);
                dlg.setText(entry.getName());
                // if (pyenvExec.contains(entry.getName()))
                // ((ZipEntry) entry).setUnixMode(0655);
                // else
                // ((ZipEntry) entry).setUnixMode(0644);
                ZipEntry nEntry = new ZipEntry(entry.getName());
                nEntry.setMethod(0);
                nEntry.setSize(entry.getSize());
                nEntry.setCrc(entry.getCrc());
                nEntry.setCompressedSize(entry.getSize());
                zip.putNextEntry(nEntry);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = pyenv.read(buffer)) > 0) {
                    zip.write(buffer, 0, len);
                }
                // try {
                // zip.closeEntry();
                // } catch (java.util.zip.ZipException e) {

                // }
            }
            pyenv.close();
        } catch (IOException e1) {
            e1.printStackTrace();
            plugin.displayException(e1);
            return;
        }
        try {
            zip.close();
        } catch (IOException e) {
            e.printStackTrace();
            plugin.displayException(e);
        }
        dlg.setVisible(false);
        dlg.dispose();
        if (zipfile.exists()) {
            JOptionPane.showMessageDialog(null, "Exporting finished and saved to " + zipfile.getAbsolutePath());
        }
    }

    @Override
    public String toString() {
        return project.getName();
    }

    public void loadTree() {
        try {
            tree = project.listAllFiles(true);
        } catch (Exception e) {
            invalid = true;
            plugin.displayException(e);
            return;
        }
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
    public boolean isLeaf() {
        return false;
    }

    @Override
    public Icon getIcon() {
        if (invalid)
            return getIconByName("icon_error_white");
        return icon;
    }

    @Override
    public void onExpand() {
        icon = openedIcon;
        updateStagedList();
    }

    @Override
    public void onCollapse() {
        icon = closedIcon;
    }

    @Override
    public void handleRefresh() {
        unload();
        removeAllChildren();
        loadTree();
        ((DefaultTreeModel) plugin.getTree().getModel()).nodeStructureChanged(this);
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
            project.deleteProject();
            ((DefaultTreeModel) plugin.getTree().getModel()).removeNodeFromParent(this);
            ((DefaultTreeModel) plugin.getTree().getModel()).reload(getParent());
        } catch (SRException e) {
            e.printStackTrace();
            plugin.displayException(e);
        }
    }

    @Override
    public ArrayList<Object> getPopupItems() {
        ArrayList<Object> items;
        if (!isLoaded()) {
            items = new ArrayList<Object>();
            items.add(new DeleteAction());
        } else {
            items = super.getPopupItems();
            items.add(null);
        }
        items.add(new ExportAction());
        return items;
    }

    public void updateStagedList() {
        StagedListModel m = (StagedListModel) plugin.getStagedList().getModel();
        m.removeAll(this);
        try {
            JSONObject staged = project.getStagedFiles();
            if (staged.length() > 0) {
                for (String path : JSONObject.getNames(staged)) {
                    m.addElement(new StagedNode(path, SRFile.getStateForId(staged.getString(path))));
                }
            }
        } catch (SRException e1) {
            e1.printStackTrace();
            plugin.displayException(e1);
        }
    }

    public boolean isExpanded() {
        return icon == openedIcon;
    }

    public void commit() {
        CommitDialog d = new CommitDialog();
        d.setVisible(true);
        String message = d.getMessage();
        if (message != null && !message.equals("")) {
            try {
                project.commit(message);
                TreePath path = plugin.getTree().getSelectionPath();
                ((StagedListModel) plugin.getStagedList().getModel()).removeAll(this);
                ((DefaultTreeModel) plugin.getTree().getModel()).reload(this);
                plugin.getTree().setSelectionPath(path);
            } catch (SRException e) {
                e.printStackTrace();
                plugin.displayException(e);
            }
        }
    }

    public void unStage(String path) {
        try {
            JSONObject files = project.getStagedFiles();
            if (files.has(path)) {
                files.remove(path);
                project.putStagedFiles(files);
                updateStagedList();
                handleRefresh();
            }
        } catch (SRException e) {
            e.printStackTrace();
        }

    }
}
