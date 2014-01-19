package com.simon816.rtext.srplugin;

import javax.swing.AbstractAction;
import javax.swing.Action;

public abstract class SimpleAction extends AbstractAction {

    public Object getValue(String key) {
        if (key == Action.MNEMONIC_KEY)
            return getMnemonic();
        if (key == Action.NAME)
            return getText();
        return super.getValue(key);
    }

    private Integer getMnemonic() {
        return null;
    }

    public abstract String getText();
}
