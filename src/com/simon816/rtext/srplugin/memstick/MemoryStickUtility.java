package com.simon816.rtext.srplugin.memstick;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.fife.rtext.RText;
import org.fife.ui.OptionsDialog;
import org.fife.ui.OptionsDialogPanel;

public class MemoryStickUtility extends OptionsDialog {

    public MemoryStickUtility(RText rtext, File root) {
        super(rtext);
        setTitle("Robot Memory Stick Utility");
        setLocationRelativeTo(rtext);
        List<OptionsDialogPanel> panels = new ArrayList<OptionsDialogPanel>();
        panels.add(new LogViewerPanel(root, rtext));
        setOptionsPanels(panels.toArray(new OptionsDialogPanel[panels.size()]));
    }

}
