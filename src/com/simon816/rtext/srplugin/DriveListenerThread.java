package com.simon816.rtext.srplugin;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DriveListenerThread implements Runnable {

    private Plugin plugin;
    private File stick = null;

    public DriveListenerThread(Plugin plugin) {
        super();
        this.plugin = plugin;
    }

    @Override
    public void run() {
        try {
            List<Path> roots = new ArrayList<Path>();
            for (;;) {
                Thread.sleep(500);

                if (stick != null && !stick.exists()) {
                    plugin.memoryStickRemoved();
                    stick = null;
                }

                List<Path> newRoots = asList(FileSystems.getDefault().getRootDirectories());
                for (Path newRoot : newRoots) {
                    if (!roots.contains(newRoot)) {
                        File f = newRoot.toFile();
                        if (new File(f, ".srobo").exists()) {
                            try {
                                plugin.memoryStickInserted(stick = f);
                            } catch (Exception e) {
                                plugin.displayException(e);
                            }
                        }
                    }
                }
                roots = newRoots;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private List<Path> asList(Iterable<Path> it) {
        ArrayList<Path> list = new ArrayList<Path>();
        Iterator<Path> i = it.iterator();
        while (i.hasNext()) {
            list.add(i.next());
        }
        return list;
    }

}
