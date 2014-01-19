package com.simon816.rtext.srplugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import org.fife.ui.rsyntaxtextarea.FileLocation;
import org.srobo.ide.api.SRFile;

import com.simon816.rtext.srplugin.tree.FileNode;

public class VirtualFileLocation extends FileLocation {

    private String name;
    private String path;
    private ByteArrayInputStream instream;
    private TriggeredOutputStream outstream;

    public VirtualFileLocation(Plugin plugin, SRFile file, FileNode fileNode) {
        this.name = file.getName();
        this.path = file.getHostProject() + "/" + file.getPath();
        try {
            instream = new ByteArrayInputStream(file.getContents().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            instream = new ByteArrayInputStream(file.getContents().getBytes());
        }
        outstream = new TriggeredOutputStream(plugin, file, fileNode);
    }

    @Override
    protected long getActualLastModified() {
        return 0;
    }

    @Override
    public String getFileFullPath() {
        return path;
    }

    @Override
    public String getFileName() {
        return name;
    }

    @Override
    protected InputStream getInputStream() throws IOException {
        return instream;
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
        return outstream;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public boolean isLocalAndExists() {
        return false;
    }

}
