package com.simon816.rtext.srplugin;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.rsta.ui.EscapableDialog;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.UIUtil;

public class CommitDialog extends EscapableDialog {

    private JTextField message;
    private JButton okButton;
    private JButton cancelButton;

    public CommitDialog() {
        super();
        Listener listener = new Listener();

        ComponentOrientation orientation = ComponentOrientation.getOrientation(getLocale());
        JPanel cp = new ResizableFrameContentPane(new BorderLayout());
        cp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(cp);

        JLabel nameLabel = new JLabel("Commit Message:");
        message = new JTextField(20);
        message.getDocument().addDocumentListener(listener);
        nameLabel.setLabelFor(message);
        JPanel topPanel = new JPanel(new BorderLayout());

        topPanel.add(nameLabel, BorderLayout.LINE_START);
        topPanel.add(message, BorderLayout.LINE_START);
        // Make a panel containing the OK and Cancel buttons.
        okButton = new JButton("OK");
        okButton.addActionListener(listener);
        cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("Cancel");
        cancelButton.addActionListener(listener);

        message.setText("Commit Message");

        // Put everything into a neat little package.
        cp.add(topPanel, BorderLayout.NORTH);
        Container buttons = UIUtil.createButtonFooter(okButton, cancelButton);
        cp.add(buttons, BorderLayout.SOUTH);
        JRootPane rootPane = getRootPane();
        rootPane.setDefaultButton(okButton);
        setTitle("Add a message to commit");
        setModal(true);
        applyComponentOrientation(orientation);
        pack();
        setLocationRelativeTo(null);
        listener.insertUpdate(null);
    }

    public String getMessage() {
        return message.getText();
    }

    protected void escapePressed() {
        super.escapePressed();
        message.setText(null);
    }

    private class Listener implements ActionListener, DocumentListener {

        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("OK")) {
                setVisible(false);
            } else if (command.equals("Cancel")) {
                escapePressed();
            }
        }

        public void changedUpdate(DocumentEvent e) {
        }

        private void handleDocumentEvent(DocumentEvent e) {
            if (message.getDocument().getLength() == 0) {
                okButton.setEnabled(false);
                return;
            }
            okButton.setEnabled(true);

        }

        public void insertUpdate(DocumentEvent e) {
            handleDocumentEvent(e);
        }

        public void removeUpdate(DocumentEvent e) {
            handleDocumentEvent(e);
        }

    }
}
