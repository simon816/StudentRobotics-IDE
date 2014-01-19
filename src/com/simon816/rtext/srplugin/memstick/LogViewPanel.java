package com.simon816.rtext.srplugin.memstick;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.fife.rtext.RText;
import org.fife.ui.OptionsDialogPanel;

public class LogViewPanel extends OptionsDialogPanel {

    Pattern lineRe = Pattern.compile("^(\\d{4}-\\d\\d-\\d\\d \\d\\d:\\d\\d:\\d\\d,\\d{3})@(\\S+) \\[([A-Z]+)\\] \\[([\\w\\.]+),([\\w<>-]+):(\\d+)\\] (.*)$");
    private ArrayList<Hashtable<String, Object>> log;
    private FontMetrics fm;

    public LogViewPanel(File file, String time, RText rtext) {
        super(time);
        setLayout(new BorderLayout());
        log = readLogFile(file);
        fm = rtext.getGraphics().getFontMetrics();
    }

    @Override
    protected void doApplyImpl(Frame owner) {
        // TODO Auto-generated method stub

    }

    @Override
    protected OptionsPanelCheckResult ensureValidInputsImpl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JComponent getTopJComponent() {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void setValuesImpl(Frame owner) {
        final String[] columnNames = new String[] { "Time", "Level", "Thread", "Name", "Function", "Line", "Message" };
        int[] widths = new int[columnNames.length];
        for (int i = 0; i < columnNames.length; i++) {
            widths[i] = fm.stringWidth(columnNames[i]) + 5;
        }
        final Object[][] rowData = new Object[log.size()][7];
        int w;
        for (int i = 0; i < rowData.length; i++) {
            Hashtable<String, Object> d = log.get(i);
            rowData[i][0] = d.get("time");
            w = fm.stringWidth((String) rowData[i][0]);
            if (w > widths[0])
                widths[0] = w;
            rowData[i][1] = d.get("level");
            w = fm.stringWidth((String) rowData[i][1]);
            if (w > widths[1])
                widths[1] = w;
            rowData[i][2] = d.get("thread");
            w = fm.stringWidth((String) rowData[i][2]);
            if (w > widths[2])
                widths[2] = w;
            rowData[i][3] = d.get("name");
            w = fm.stringWidth((String) rowData[i][3]);
            if (w > widths[3])
                widths[3] = w;
            rowData[i][4] = d.get("func");
            w = fm.stringWidth((String) rowData[i][4]);
            if (w > widths[4])
                widths[4] = w;
            rowData[i][5] = d.get("line");
            w = fm.stringWidth((String) rowData[i][5]);
            if (w > widths[5])
                widths[5] = w;
            rowData[i][6] = d.get("message");
            if (d.containsKey("trace")) {
                for (String s : (ArrayList<String>) d.get("trace")) {
                    String rdata = (String) rowData[i][6];
                    rowData[i][6] = rdata + s + "\n";
                }
            }
            w = fm.stringWidth((String) rowData[i][6]);
            if (w > widths[6])
                widths[6] = w;
        }

        JTable table = new JTable(new AbstractTableModel() {
            public String getColumnName(int column) {
                return columnNames[column].toString();
            }

            public int getRowCount() {
                return rowData.length;
            }

            public int getColumnCount() {
                return columnNames.length;
            }

            public Object getValueAt(int row, int col) {
                return rowData[row][col];
            }
        });
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
        TableCellRenderer cellRenderer = new CustomCellRenderer(table.getRowCount());
        table.setDefaultRenderer(Object.class, cellRenderer);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < 7; i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setResizable(false);
            column.setPreferredWidth(widths[i]);
            // column.setCellRenderer(cellRenderer);
            if (column.getHeaderValue().equals("Message")) {
                column.setResizable(true);
            }
        }
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    @SuppressWarnings("unchecked")
    private ArrayList<Hashtable<String, Object>> readLogFile(File log) {
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(log));
            ArrayList<Hashtable<String, Object>> events = new ArrayList<Hashtable<String, Object>>();
            String line;
            while ((line = br.readLine()) != null) {
                Hashtable<String, Object> data = new Hashtable<String, Object>();
                Matcher m = lineRe.matcher(line);
                if (!m.matches()) {
                    Hashtable<String, Object> d = events.get(events.size() - 1);
                    if (!d.containsKey("trace")) {
                        d.put("trace", new ArrayList<String>());
                    }
                    ((ArrayList<String>) d.get("trace")).add(line.trim());
                } else {
                    data.put("time", m.group(1));
                    data.put("thread", m.group(2));
                    data.put("level", m.group(3));
                    data.put("name", m.group(4));
                    data.put("func", m.group(5));
                    data.put("line", m.group(6));
                    data.put("message", m.group(7));
                    events.add(data);
                }
            }
            br.close();
            return events;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private class CustomCellRenderer extends JTextArea implements TableCellRenderer {
        private Color[] colors;
        private List<List<Integer>> rowColHeight = new ArrayList<List<Integer>>();

        private CustomCellRenderer(int rowCount) {
            super();
            colors = new Color[rowCount];
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
            setEditable(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground(null);
            // super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (column == 1) {
                if (value.equals("INFO"))
                    setColorForRow(row, Color.GREEN);
                else if (value.equals("DEBUG"))
                    setColorForRow(row, Color.CYAN);
                else if (value.equals("ERROR"))
                    setColorForRow(row, Color.RED);
                else if (value.equals("WARNING"))
                    setColorForRow(row, Color.ORANGE);
                else if (value.equals("CRITICAL")) {
                    setColorForRow(row, Color.BLACK);
                }
            }
            setBackground(colors[row]);
            setForeground(colors[row] == Color.BLACK ? Color.WHITE : Color.BLACK);

            // System.out.println(value);
            setFont(table.getFont());
            if (hasFocus) {
                setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
            } else {
                setBorder(new EmptyBorder(1, 2, 1, 2));
            }
            setText(value.toString());
            setWrapStyleWord(true);
            setLineWrap(true);
            adjustRowHeight(table, row, column);
            return this;
        }

        private void setColorForRow(int row, Color color) {
            colors[row] = color;
        }

        private void adjustRowHeight(JTable table, int row, int column) {
            // The trick to get this to work properly is to set the width of the column to the
            // textarea. The reason for this is that getPreferredSize(), without a width tries
            // to place all the text in one line. By setting the size with the with of the column,
            // getPreferredSize() returnes the proper height which the row should have in
            // order to make room for the text.
            int cWidth = table.getTableHeader().getColumnModel().getColumn(column).getWidth();
            if (cWidth > 200)
                cWidth = 200;
            setSize(new Dimension(cWidth + 20, 20));
            int prefH = getPreferredSize().height;
            while (rowColHeight.size() <= row) {
                rowColHeight.add(new ArrayList<Integer>(column));
            }
            List<Integer> colHeights = rowColHeight.get(row);
            while (colHeights.size() <= column) {
                colHeights.add(0);
            }
            colHeights.set(column, prefH);
            int maxH = prefH;
            for (Integer colHeight : colHeights) {
                if (colHeight > maxH) {
                    maxH = colHeight;
                }
            }
            if (table.getRowHeight(row) != maxH) {
                table.setRowHeight(row, maxH);
            }
        }
    }
}
