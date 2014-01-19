package com.simon816.rtext.srplugin;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

public class IconManager {
    private Hashtable<String, Icon> extTable;
    private Icon folderIcon;
    private Plugin plugin;

    public IconManager(Plugin plugin) {
        extTable = new Hashtable<String, Icon>();
        folderIcon = null;
        this.plugin = plugin;
    }

    public Icon getIconForExt(String ext) {
        ext = ext.substring(ext.lastIndexOf('.') + 1);
        if (extTable.containsKey(ext)) {
            return extTable.get(ext);
        }
        if (plugin.atCollege()) {
            ImageIcon icon = null;
            if (ext.equals("py"))
                icon = new ImageIcon(IconManager.class.getResource(ext + ".png"));
            if (icon != null) {
                extTable.put(ext, icon);
                return icon;
            }
        }
        try {
            File f = File.createTempFile("temp", "." + ext);
            Icon icon = FileSystemView.getFileSystemView().getSystemIcon(f);
            f.delete();
            extTable.put(ext, icon);
            return icon;
        } catch (IOException e) {
            e.printStackTrace();
            return UIManager.getIcon("FileView.fileIcon");
        }
    }

    public Icon getFolderIcon() {
        if (folderIcon == null)
            folderIcon = FileSystemView.getFileSystemView().getSystemIcon(new File(System.getProperty("user.home")));
        return folderIcon;
    }
}
