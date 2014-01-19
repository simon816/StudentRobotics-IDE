package com.simon816.rtext.srplugin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.srobo.ide.api.SRFile;

import com.simon816.rtext.srplugin.tree.FileNode;

public class TriggeredOutputStream extends ByteArrayOutputStream {
    private Plugin plugin;
    private SRFile file;
    private FileNode fileNode;

    public TriggeredOutputStream(Plugin plugin, SRFile file, FileNode fileNode) {
        super();
        this.plugin = plugin;
        this.file = file;
        this.fileNode=fileNode;
    }

    public void close() throws IOException {
        plugin.handleFileSave(file, fileNode, toString("UTF-8"));
        reset();

    }
}
