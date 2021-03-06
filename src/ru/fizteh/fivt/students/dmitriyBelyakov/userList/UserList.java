package ru.fizteh.fivt.students.dmitriyBelyakov.userList;

import ru.fizteh.fivt.bind.test.Permissions;
import ru.fizteh.fivt.bind.test.User;
import ru.fizteh.fivt.bind.test.UserName;
import ru.fizteh.fivt.bind.test.UserType;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;

class UserListTableModel extends AbstractTableModel {
    Vector<String> names;
    Vector<Vector<Object>> users;
    final DefaultCellEditor editor;

    UserListTableModel(Vector<String> names, Vector<Vector<Object>> users) {
        this.names = names;
        this.users = users;
        UserType[] types = UserType.values();
        JComboBox typeCombo = new JComboBox(types);
        editor = new DefaultCellEditor(typeCombo);
    }

    @Override
    public int getColumnCount() {
        return names.size();
    }

    @Override
    public int getRowCount() {
        return users.size();
    }

    @Override
    public String getColumnName(int col) {
        return names.get(col);
    }

    @Override
    public Object getValueAt(int row, int col) {
        return users.get(row).get(col);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        users.get(row).set(col, value);
        fireTableDataChanged();
    }

    public void removeRow(int row) {
        users.remove(row);
        fireTableDataChanged();
    }

    public void addRow(Vector<Object> row) {
        users.add(row);
        fireTableDataChanged();
    }

    public Vector<Vector<Object>> getData() {
        return users;
    }

    public void clear() {
        users.clear();
        fireTableDataChanged();
    }
}

public class UserList extends JFrame {
    private JMenuBar menu;
    private JFrame frame = this;
    private JTable table;
    private XmlUserList xmlUserList;
    private File xmlFile;

    UserList() {
        super("UserList");
        xmlUserList = new XmlUserList();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(500, 700);
        createMenu();
        createTable();
        setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UserList userList = new UserList();
        } catch (Throwable t) {
            System.exit(1);
        }
    }

    public class Listener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            String actionCommand = event.getActionCommand();
            if (actionCommand.equals("OPEN")) {
                JFileChooser fileOpen = new JFileChooser();
                int ret = fileOpen.showDialog(frame, "Open");
                if (ret == JFileChooser.APPROVE_OPTION) {
                    xmlFile = fileOpen.getSelectedFile();
                    if (!xmlFile.exists()) {
                        JOptionPane.showMessageDialog(frame, "Cannot find file '" + xmlFile.getName() + "'");
                        xmlFile = null;
                    } else {
                        updateTable(xmlUserList.loadUsers(xmlFile));
                    }
                }
            } else if (actionCommand.equals("SAVE")) {
                try {
                    save();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(frame, "Incorrect list: " + e.getMessage());
                }
            } else if (actionCommand.equals("SAVE_AS")) {
                File last = xmlFile;
                JFileChooser fileSave = new JFileChooser();
                int ret = fileSave.showDialog(frame, "Save as");
                if (ret == JFileChooser.APPROVE_OPTION) {
                    xmlFile = fileSave.getSelectedFile();
                    try {
                        save();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(frame, "Incorrect list: " + e.getMessage());
                        xmlFile = last;
                    }
                }
            } else if (actionCommand.equals("NEW_USER")) {
                Vector<Object> vector = new Vector<>();
                vector.add(0);
                vector.add(UserType.USER);
                vector.add(new String());
                vector.add(new String());
                vector.add(false);
                vector.add(0);
                ((UserListTableModel) table.getModel()).addRow(vector);
                table.updateUI();
            } else if (actionCommand.equals("DELETE_USER")) {
                int num = table.getSelectedRow();
                if (num == -1) {
                    JOptionPane.showMessageDialog(frame, "No row has been selected.");
                }
                ((UserListTableModel) table.getModel()).removeRow(num);
                table.updateUI();
            }
        }

        public void save() {
            if (xmlFile == null) {
                JOptionPane.showMessageDialog(frame, "File for save don't open.");
                return;
            }
            ArrayList<User> usersList = new ArrayList<>();
            Vector<Vector<Object>> users = ((UserListTableModel) table.getModel()).getData();
            for (Vector<Object> vector : users) {
                int id = (Integer) vector.get(0);
                UserType userType = (UserType) vector.get(1);
                UserName name = new UserName((String) vector.get(2), (String) vector.get(3));
                Permissions permissions = new Permissions();
                permissions.setRoot((Boolean) vector.get(4));
                permissions.setQuota((Integer) vector.get(5));
                User user = new User(id, userType, name, permissions);
                usersList.add(user);
            }
            xmlUserList.saveUsers(usersList, xmlFile);
        }

        public void updateTable(ArrayList<User> list) {
            ((UserListTableModel) table.getModel()).clear();
            for (User user : list) {
                if (user == null) {
                    continue;
                }
                Vector row = new Vector();
                row.add(user.getId());
                row.add(user.getUserType() == null ? UserType.USER : user.getUserType());
                UserName name = user.getName();
                if (name == null) {
                    row.add(new String());
                    row.add(new String());
                } else {
                    row.add(name == null ? new String() : name.getFirstName());
                    row.add(name == null ? new String() : name.getLastName());
                }
                Permissions permissions = user.getPermissions();
                if (permissions == null) {
                    permissions = new Permissions();
                }
                row.add(permissions.isRoot());
                row.add(permissions.getQuota());
                ((UserListTableModel) table.getModel()).addRow(row);
            }
            table.updateUI();
        }
    }

    private void createMenu() {
        Listener listener = new Listener();
        menu = new JMenuBar();
        JMenu file = new JMenu("File");
        menu.add(file);
        JMenuItem fileOpen = new JMenuItem("Open");
        fileOpen.setActionCommand("OPEN");
        fileOpen.addActionListener(listener);
        file.add(fileOpen);
        JMenuItem fileSave = new JMenuItem("Save");
        fileSave.setActionCommand("SAVE");
        fileSave.addActionListener(listener);
        file.add(fileSave);
        JMenuItem fileSaveAs = new JMenuItem("Save as");
        fileSaveAs.setActionCommand("SAVE_AS");
        fileSaveAs.addActionListener(listener);
        file.add(fileSaveAs);
        JMenu edit = new JMenu("Edit");
        JMenuItem editNewUser = new JMenuItem("New user");
        editNewUser.setActionCommand("NEW_USER");
        editNewUser.addActionListener(listener);
        edit.add(editNewUser);
        JMenuItem editDeleteUser = new JMenuItem("Delete user");
        editDeleteUser.setActionCommand("DELETE_USER");
        editDeleteUser.addActionListener(listener);
        edit.add(editDeleteUser);
        menu.add(edit);
        setJMenuBar(menu);
    }

    private void createTable() {
        Vector<String> names = new Vector<>();
        names.add("ID");
        names.add("Type");
        names.add("First name");
        names.add("Last name");
        names.add("Root");
        names.add("Quota");
        final int columnCount = names.size();
        UserType[] types = UserType.values();
        JComboBox typeCombo = new JComboBox(types);
        final DefaultCellEditor editor = new DefaultCellEditor(typeCombo);
        table = new JTable(new UserListTableModel(names, new Vector<Vector<Object>>())) {
            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                int modelColumn = convertColumnIndexToModel(column);

                if (modelColumn == 1)
                    return editor;
                else
                    return super.getCellEditor(row, column);
            }

            @Override
            public Class getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return Integer.class;
                    case 1:
                        return UserType.class;
                    case 2:
                        return String.class;
                    case 3:
                        return String.class;
                    case 4:
                        return Boolean.class;
                    case 5:
                        return Integer.class;
                    default:
                        return String.class;
                }
            }
        };
        TableRowSorter<TableModel> sorter
                = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);
        add(new JScrollPane(table));
    }
}