package JavaProject;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

class Main extends JFrame implements ActionListener{
    JTextField id;
    JPasswordField passwd;
    JButton b1,b2,b3,b4,b5;

    Login(String title){
        setTitle(title);
        Container ct = getContentPane();
        ct.setLayout(null);
        JLabel l1 = new JLabel("LoginID : ");
        id = new JTextField(8);
        l1.setBounds(80, 60, 70, 30);
        id.setBounds(170, 60, 120, 30);
        ct.add(l1);
        ct.add(id);

        JLabel l2 = new JLabel("PASSWD");
        passwd = new JPasswordField(8);
        l2.setBounds(80, 100, 70, 30);
        passwd.setBounds(170, 100, 120, 30);
        ct.add(l2);
        ct.add(passwd);

        b1 = new JButton("로그인");
        b2 = new JButton("취소");
        b3 = new JButton("회원가입");
        b4 = new JButton("아이디 찾기");
        b5 = new JButton("비밀번호 찾기");
        passwd.addActionListener(this);
        b1.addActionListener(this);
        b2.addActionListener(this);
        b3.addActionListener(this);
        b4.addActionListener(this);
        b5.addActionListener(this);
        b1.setBounds(30, 170, 80, 30);
        b2.setBounds(120, 170, 80, 30);
        b3.setBounds(210, 170, 100, 30);
        b4.setBounds(30, 220, 120, 30);
        b5.setBounds(160, 220, 140, 30);
        ct.add(b1);
        ct.add(b2);
        ct.add(b3);
        ct.add(b4);
        ct.add(b5);
    }

    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();
        if(s.equals("로그인")||e.getSource()==passwd) {
            String userId = id.getText().trim();
            String password = new String(passwd.getPassword());

            if (checklogin(userId, password)) {
                FitnessApp app = new FitnessApp(userId,password);
                app.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                app.setSize(1500, 1024);
                app.setLocationRelativeTo(null);
                app.setVisible(true);
                this.dispose();
            }
        }
        else if(s.equals("취소")) {
            id.setText("");
            passwd.setText("");
        }
        else if(s.equals("회원가입")) {
            NewMember my = new NewMember("회원가입");
            my.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            my.setSize(520,500);
            my.setLocation(400,300);
            my.show();
        }
        else {}
    }
    private boolean checklogin(String userid, String password) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://fitnessapp.chqw04eu8yfk.ap-southeast-2.rds.amazonaws.com:3306/fitnessapp", "mih", "ansxoddl123")) {

            String sql = "SELECT * FROM User WHERE Userid = ? AND Password = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userid);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            boolean result = rs.next();
            return result;

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "데이터베이스 연결 오류가 발생했습니다.",
                    "오류",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}

class NewMember extends JFrame implements ActionListener{
    JTextField id;
    JTextField name;
    JTextField weight;
    JTextField height;
    JTextField age;
    JButton check;
    JPasswordField passwd;
    JButton b1;
    JButton b2;
    JTextField answer;
    private JRadioButton maleButton;
    private JRadioButton femaleButton;
    private ButtonGroup genderGroup;
    private JRadioButton dietButton;
    private JRadioButton BulkButton;
    private JRadioButton StrengthButton;
    private ButtonGroup goalGroup;
    private JComboBox<String> comboBox;
    private boolean idChecked = false;
    NewMember(String title){
        setTitle(title);
        Container ct = getContentPane();
        ct.setLayout(new BorderLayout(0, 20));
        JPanel top = new JPanel();
        top.setLayout(new GridLayout(10,1));
        JPanel p1 = new JPanel();
        p1.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l1 = new JLabel("ID	        :");
        id = new JTextField(10);
        check = new JButton("중복 체크");
        check.addActionListener(this);
        p1.add(l1);
        p1.add(id);
        p1.add(check);
        Panel p2 = new Panel();
        p2.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l2 = new JLabel("PASSWORD :");
        passwd = new JPasswordField(10);
        p2.add(l2);
        p2.add(passwd);

        JPanel p3 = new JPanel();
        p3.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l3 = new JLabel("이름		:");
        name = new JTextField(10);
        p3.add(l3);
        p3.add(name);

        JPanel p4 = new JPanel();
        p4.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l4 = new JLabel("몸무게	:");
        weight = new JTextField(10);
        p4.add(l4);
        p4.add(weight);

        JPanel p5 = new JPanel();
        p5.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l5 = new JLabel("키		:");
        height = new JTextField(10);
        p5.add(l5);
        p5.add(height);

        JPanel p6 = new JPanel();
        p6.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l6 = new JLabel("나이		:");
        age = new JTextField(10);
        p6.add(l6);
        p6.add(age);

        JPanel p7 = new JPanel();
        p7.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l7 = new JLabel("성별		:");
        maleButton = new JRadioButton("남");
        femaleButton = new JRadioButton("여");
        genderGroup = new ButtonGroup();
        genderGroup.add(maleButton);
        genderGroup.add(femaleButton);

        p7.add(l7);
        p7.add(maleButton);
        p7.add(femaleButton);
        JPanel p8 = new JPanel();
        p8.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l8 = new JLabel("목표		:");
        dietButton = new JRadioButton("다이어트");
        BulkButton = new JRadioButton("벌크업");
        StrengthButton = new JRadioButton("근력 강화");
        goalGroup = new ButtonGroup();
        goalGroup.add(dietButton);
        goalGroup.add(BulkButton);
        goalGroup.add(StrengthButton);

        p8.add(l8);
        p8.add(dietButton);
        p8.add(BulkButton);
        p8.add(StrengthButton);
        JPanel p9 = new JPanel();
        p9.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l9 = new JLabel("힌트	:");
        comboBox = new JComboBox<>(loadKeyQuestionsFromDB());

        p9.add(l9);
        p9.add(comboBox);

        JPanel p10 = new JPanel();
        p10.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel l10 = new JLabel("힌트 답 :");
        answer = new JTextField(20);
        p10.add(l10);
        p10.add(answer);

        top.add(p1);
        top.add(p2);
        top.add(p3);
        top.add(p4);
        top.add(p5);
        top.add(p6);
        top.add(p7);
        top.add(p8);
        top.add(p9);
        top.add(p10);
        ct.add(top, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        b1 = new JButton("확인");
        b2 = new JButton("취소");
        b1.setEnabled(false);
        b1.addActionListener(this);
        b2.addActionListener(this);
        bottom.add(b1);
        bottom.add(b2);
        ct.add(bottom, BorderLayout.SOUTH);

        addValidationListeners();
    }

    public void actionPerformed(ActionEvent e) {
        String s = e.getActionCommand();

        if(s.equals("취소")) {
            id.setText("");
            passwd.setText("");
            weight.setText("");
            height.setText("");
            name.setText("");
            age.setText("");
            genderGroup.clearSelection();
            goalGroup.clearSelection();
            comboBox.setSelectedIndex(0);
            b1.setEnabled(false);
        }
        else if(s.equals("중복 체크")) {
            Duplicate();
        }
        else if(s.equals("확인")) {
            saveUserToDatabase();
        }
    }
    private void saveUserToDatabase() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://fitnessapp.chqw04eu8yfk.ap-southeast-2.rds.amazonaws.com:3306/fitnessapp", "mih", "ansxoddl123")) {
            String sql = "INSERT INTO User (Userid, Password, Username, Weight, Height, Age, Gender, GoalType, Keyqusid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id.getText().trim());
            pstmt.setString(2, new String(passwd.getPassword()));
            pstmt.setString(3, name.getText().trim());
            pstmt.setString(4, weight.getText().trim());
            pstmt.setString(5, height.getText().trim());
            pstmt.setString(6, age.getText().trim());
            pstmt.setInt(7, maleButton.isSelected() ? 1 : 0);
            pstmt.setString(8, getSelectedGoal());
            pstmt.setInt(9, comboBox.getSelectedIndex());
            pstmt.executeUpdate();

            String ukeySql = "INSERT INTO Ukey (Userid, Keyqusid, Keyanswer) VALUES (?, ?, ?)";
            PreparedStatement pstmtUKey = conn.prepareStatement(ukeySql);
            pstmtUKey.setString(1, id.getText().trim()); // Userid
            pstmtUKey.setInt(2, comboBox.getSelectedIndex()); // Keyqusid
            pstmtUKey.setString(3, answer.getText().trim()); // Keyanswer
            pstmtUKey.executeUpdate();

            JOptionPane.showMessageDialog(this, "회원가입을 완료하였습니다.!", "회원가입 성공", JOptionPane.INFORMATION_MESSAGE);
            this.dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "회원가입에 실패했습니다.", "회원가입 실패", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getSelectedGoal() {
        if (dietButton.isSelected()) return "다이어트";
        if (BulkButton.isSelected()) return "벌크업";
        if (StrengthButton.isSelected()) return "근력 강화";
        return null;
    }

    private void addValidationListeners() {
        DocumentListener docListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                validateAllFields();
            }

            public void removeUpdate(DocumentEvent e) {
                validateAllFields();
            }

            public void changedUpdate(DocumentEvent e) {
                validateAllFields();
            }
        };

        id.getDocument().addDocumentListener(docListener);
        passwd.getDocument().addDocumentListener(docListener);
        name.getDocument().addDocumentListener(docListener);
        weight.getDocument().addDocumentListener(docListener);
        height.getDocument().addDocumentListener(docListener);
        age.getDocument().addDocumentListener(docListener);
        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                validateAllFields();
            }
        });
    }

    private void validateAllFields() {
        boolean allValid = idChecked && !id.getText().trim().isEmpty()
                && !new String(passwd.getPassword()).trim().isEmpty()
                && !name.getText().trim().isEmpty()
                && !weight.getText().trim().isEmpty()
                && !height.getText().trim().isEmpty()
                && !age.getText().trim().isEmpty()
                && (maleButton.isSelected() || femaleButton.isSelected())
                && (dietButton.isSelected() || BulkButton.isSelected() || StrengthButton.isSelected())
                && comboBox.getSelectedIndex() > 0;
        b1.setEnabled(allValid);
    }

    private String[] loadKeyQuestionsFromDB() {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://fitnessapp.chqw04eu8yfk.ap-southeast-2.rds.amazonaws.com:3306/fitnessapp",
                "mih", "ansxoddl123")) {

            String sql = "SELECT KeyQus FROM KeyQus";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            // 결과 저장용 리스트
            ArrayList<String> questions = new ArrayList<>();
            questions.add("힌트를 선택하세요");

            // ResultSet 데이터 읽기
            while (rs.next()) {
                questions.add(rs.getString("KeyQus"));
            }

            rs.close();
            pstmt.close();
            conn.close();

            // 리스트를 배열로 변환하여 반환
            return questions.toArray(new String[0]);

        } catch (Exception ex) {
            ex.printStackTrace();
            return new String[] {"Error loading hints"};
        }
    }

    private void Duplicate() {
        try {
            // 데이터베이스 연결
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://fitnessapp.chqw04eu8yfk.ap-southeast-2.rds.amazonaws.com:3306/fitnessapp",
                    "mih",
                    "ansxoddl123");

            // SQL 쿼리 준비
            String sql = "SELECT COUNT(*) FROM User WHERE Userid = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id.getText().trim()); // TextField의 값 가져오기

            // SQL 실행
            ResultSet rs = pstmt.executeQuery();
            rs.next(); // 결과 집합의 첫 번째 행으로 이동

            int count = rs.getInt(1); // COUNT(*) 결과 가져오기

            // 중복 여부 확인
            if (count > 0) {
                // 중복된 ID
                MessageDialog md = new MessageDialog(this, "ID 중복체크", true, "중복된 ID입니다.");
                md.setLocation(500,300);
                md.setVisible(true);
            } else {
                // 사용 가능한 ID
                MessageDialog md = new MessageDialog(this, "ID 중복체크", true, "사용 가능한 ID입니다.");
                md.setLocation(500,300);
                md.setVisible(true);
                idChecked = true;
                validateAllFields();
            }

            // 자원 닫기
            rs.close();
            pstmt.close();
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "데이터베이스 연결 실패", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class MessageDialog extends JDialog implements ActionListener{
    JButton ok;
    MessageDialog(JFrame parent, String title, boolean mode, String msg)
    { // 생성자를 이용하여 다이어로그 창의 UI 구성
        super(parent, title, mode); // JDialog(parent, title, mode); 생성자 호출
        JPanel pc = new JPanel();
        JLabel label = new JLabel(msg);
        pc.add(label); // JPanel객체 pc 위에 레이블을 추가
        add(pc, BorderLayout.CENTER); // JPanel객체 pc를 다이어로그 창에 추가
        JPanel ps = new JPanel();
        ok = new JButton("OK");
        ok.addActionListener(this);
        ps.add(ok);
        add(ps, BorderLayout.SOUTH); // JPanel객체 ps를 다이어로그 창에 추가
        pack(); // 컴포넌트를 배치하고 다이어로그 박스의 초기 크기를 설정
    }
    public void actionPerformed(ActionEvent ae) { // ID중복체크 창의 AE
        dispose(); // ok 버튼 클릭하면 창닫기
    }

}
public class Main {

    public static void main(String[] args) {
        Login win = new Login("로그인");
        win.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        win.setSize(350, 300);
        win.setLocation(100, 200);
        win.show();
    }

}