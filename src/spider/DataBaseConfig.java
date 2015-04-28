package spider;

import javax.swing.*;
import java.awt.event.*;

public class DataBaseConfig extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField tf_database_username;
    private JTextField tf_database_passwd;
    private Context context = null;
    public DataBaseConfig(Context context) {
        setContentPane(contentPane);
        setModal(true);
        this.context = context;
        if(context.getDataBaseUsername() != "" && context.getDataBaseUsername() != null){
            tf_database_username.setText(context.getDataBaseUsername());
        }

        if(context.getDataBasePasswd() != "" && context.getDataBasePasswd() != null){
            tf_database_passwd.setText(context.getDataBasePasswd());
        }

        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        pack();
    }

    private void onOK() {
        if(!tf_database_username.getText().trim().equals("")
                && tf_database_username.getText().trim() != null){
            if(!tf_database_passwd.getText().trim().equals("")
                    && tf_database_passwd.getText().trim() != null) {

                context.setDataBaseUsername(tf_database_username.getText().trim());
                context.setDataBasePasswd(tf_database_passwd.getText().trim());

                java.sql.Connection connection = null;
                try {

                    connection = context.getDataBase().getConnection();
                } catch (Exception e) {

                    context.setDataBasePass("false");
                   System.out.println(context.getDataBase().getUsername() + "--" + context.getDataBase().getPassword());
                }
                if (null != connection) {
                    context.setDataBaseUsername(tf_database_username.getText().trim());
                    context.setDataBasePasswd(tf_database_passwd.getText().trim());
                    context.setDataBasePass("true");
                    System.out.println("true");
                }
            }else{
                new ErrorDialog("密码不能为空").setVisible(true);
                return;
            }
        }else{
            new ErrorDialog("用户名不能为空").setVisible(true);
            return;
        }


        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }



}
