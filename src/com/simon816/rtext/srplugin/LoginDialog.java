package com.simon816.rtext.srplugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.rsta.ui.EscapableDialog;
import org.fife.rtext.RText;
import org.fife.ui.ResizableFrameContentPane;
import org.fife.ui.UIUtil;

public class LoginDialog extends EscapableDialog {

    private JLabel usernameLabel;
    private JTextField usernameField;
    private JPanel topPanel;
    private JButton okButton;
    private JButton cancelButton;
    private JLabel passwordLabel;
    private JPasswordField passwordField;
    private boolean canceled;
    private JLabel errorLabel;

    public LoginDialog(RText owner) {
        this(owner, null);
    }

    public LoginDialog(RText owner, String username) {

        super(owner);
        Listener listener = new Listener();

        ComponentOrientation orientation = ComponentOrientation.getOrientation(getLocale());
        ResourceBundle bundle = owner.getResourceBundle();

        JPanel cp = new ResizableFrameContentPane(new BorderLayout());
        cp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(cp);

        errorLabel = new JLabel();
        errorLabel.setForeground(Color.red);

        usernameLabel = new JLabel(Messages.getString("LoginDialog.Field.Username"));
        usernameLabel.setDisplayedMnemonic(Messages.getString("LoginDialog.Field.Username.Mnemonic").charAt(0));
        usernameField = new JTextField(20);
        if (username != null)
            usernameField.setText(username);
        usernameField.getDocument().addDocumentListener(listener);
        usernameLabel.setLabelFor(usernameField);

        passwordLabel = new JLabel(Messages.getString("LoginDialog.Field.Password"));
        passwordLabel.setDisplayedMnemonic(Messages.getString("LoginDialog.Field.Password.Mnemonic").charAt(0));
        passwordField = new JPasswordField(20);
        passwordField.getDocument().addDocumentListener(listener);
        passwordLabel.setLabelFor(passwordField);

        topPanel = new JPanel(new BorderLayout());

        topPanel.add(errorLabel, BorderLayout.BEFORE_LINE_BEGINS);

        JPanel uPanel = new JPanel(new BorderLayout());
        uPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        uPanel.add(usernameLabel, BorderLayout.LINE_START);
        uPanel.add(usernameField, BorderLayout.LINE_END);
        topPanel.add(uPanel, BorderLayout.NORTH);

        JPanel pPanel = new JPanel(new BorderLayout());
        pPanel.add(passwordLabel, BorderLayout.LINE_START);
        pPanel.add(passwordField, BorderLayout.LINE_END);
        topPanel.add(pPanel, BorderLayout.SOUTH);

        // Make a panel containing the OK and Cancel buttons.
        okButton = UIUtil.newButton(bundle, "OKButtonLabel", "OKButtonMnemonic");
        okButton.setActionCommand("OK");
        okButton.addActionListener(listener);
        cancelButton = UIUtil.newButton(bundle, "Cancel", "CancelMnemonic");
        cancelButton.setActionCommand("Cancel");
        cancelButton.addActionListener(listener);

        // Put everything into a neat little package.
        cp.add(topPanel, BorderLayout.NORTH);
        Container buttons = UIUtil.createButtonFooter(okButton, cancelButton);
        cp.add(buttons, BorderLayout.SOUTH);
        JRootPane rootPane = getRootPane();
        rootPane.setDefaultButton(okButton);
        setTitle(Messages.getString("LoginDialog.Title"));
        setModal(true);
        applyComponentOrientation(orientation);
        pack();
        setLocationRelativeTo(owner);
        listener.insertUpdate(null);
    }

    public void reset(String username) {
        canceled = false;
        if (username != null)
            usernameField.setText(username);
    }

    public void showError(String message) {
        errorLabel.setText(message);
    }

    @Override
    public void escapePressed() {
        usernameField.setText(null);
        passwordField.setText(null);
        canceled = true;
        super.escapePressed();
    }

    public boolean wasCanceled() {
        return canceled;
    }

    protected String getUsername() {
        return usernameField.getText();
    }

    protected String getPassword() {
        char[] p = passwordField.getPassword();
        if (p == null)
            return null;
        return new String(p);
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
            if (usernameField.getDocument().getLength() == 0) {
                okButton.setEnabled(false);
                return;
            }
            if (passwordField.getDocument().getLength() == 0) {
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
