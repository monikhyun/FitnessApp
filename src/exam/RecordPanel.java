package exam;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;
import java.sql.*;

public class RecordPanel extends JPanel implements ActionListener {
    private JPanel ExecGrid;
    private JPanel RecordGrid;
    private Calendar currentCalendar;
    private int selectedDay, selectedMonth, selectedYear;
    private String loginedid, loginedpass;
    private JLabel dateLabel;
    private JButton prevDay, nextDay;
    private JButton[] execbtn;
    private JPanel bottompanel = new JPanel();
    private JPanel panel = new JPanel(new BorderLayout());

    private static final String dburl = "jdbc:mysql://fitnessapp.chqw04eu8yfk.ap-southeast-2.rds.amazonaws.com:3306/fitnessapp";
    private static final String dbusr = "mih";
    private static final String dbpass = "ansxoddl123";

    public RecordPanel(String id, String passwd) {
        this.loginedid = id;
        this.loginedpass = passwd;

        setLayout(new GridLayout(1, 2));

        selectedYear = Calendar.getInstance().get(Calendar.YEAR);
        selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        selectedDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        ExecGrid = SearchDailyExec();
        add(ExecGrid);

        RecordGrid = ExecRecordPanel();
        add(RecordGrid);
    }

    private JPanel SearchDailyExec() {
        JPanel headerPanel = new JPanel();
        prevDay = new JButton("◀");
        nextDay = new JButton("▶");
        dateLabel = new JLabel("");
        dateLabel.setHorizontalAlignment(JLabel.CENTER);
        dateLabel.setPreferredSize(new Dimension(150, 30));

        currentCalendar = Calendar.getInstance();
        updateDateLabel();

        prevDay.addActionListener(this);
        nextDay.addActionListener(this);

        dateLabel.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                JDialog datePickerDialog = createDatePickerDialog();
                datePickerDialog.setVisible(true);
            }

            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });

        headerPanel.add(prevDay);
        headerPanel.add(dateLabel);
        headerPanel.add(nextDay);

        panel.add(headerPanel, BorderLayout.NORTH);

        panel.add(bottompanel,BorderLayout.SOUTH);

        return panel;
    }

    // 날짜 라벨 업데이트 메서드
    private void updateDateLabel() {
        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH) + 1; // 월은 0부터 시작하므로 +1 필요
        int day = currentCalendar.get(Calendar.DAY_OF_MONTH);

        dateLabel.setText(year + "-" + month + "-" + day);
        try{
            Connection conn;
            conn = DriverManager.getConnection(dburl, dbusr, dbpass);
            String sql = "SELECT Execid,Execname FROM UserExec WHERE Userid=? AND Date = ?";
            PreparedStatement ps1 = conn.prepareStatement(sql);
            ps1.setString(1, loginedid);
            ps1.setString(2, dateLabel.getText());
            ResultSet rs1 = ps1.executeQuery();

            int count = 0;
            while(rs1.next()){
                count++;
            }

            execbtn = new JButton[count];
            bottompanel.setLayout(new GridLayout(count, 1));

            PreparedStatement ps2 = conn.prepareStatement(sql);
            ps2.setString(1, loginedid);
            ps2.setString(2, dateLabel.getText());
            ResultSet rs2 = ps2.executeQuery();
            int i=0;
            bottompanel.removeAll();
            while(rs2.next()) {
                execbtn[i]=new JButton();
                execbtn[i].setText(rs2.getString("Execname"));
                bottompanel.add(execbtn[i]);
                i++;
            }
            bottompanel.validate();

            rs1.close();
            rs2.close();
            ps1.close();
            ps2.close();
            conn.close();
        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    // 날짜 선택 팝업창 생성 메서드
    private JDialog createDatePickerDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("날짜 선택");
        dialog.setModal(true);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);

        JPanel datePanel = new JPanel();

        JComboBox<Integer> yearCombo = new JComboBox<>();
        for (int year = 2000; year <= 2030; year++) {
            yearCombo.addItem(year);
        }

        JComboBox<Integer> monthCombo = new JComboBox<>();
        for (int month = 1; month <= 12; month++) {
            monthCombo.addItem(month);
        }

        JComboBox<Integer> dayCombo = new JComboBox<>();
        for (int day = 1; day <= 31; day++) {
            dayCombo.addItem(day);
        }

        datePanel.add(new JLabel("연도:"));
        datePanel.add(yearCombo);
        datePanel.add(new JLabel("월:"));
        datePanel.add(monthCombo);
        datePanel.add(new JLabel("일:"));
        datePanel.add(dayCombo);

        // 확인 버튼
        JButton selectButton = new JButton("확인");
        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int year = (int) yearCombo.getSelectedItem();
                int month = (int) monthCombo.getSelectedItem();
                int day = (int) dayCombo.getSelectedItem();

                // Calendar 업데이트
                currentCalendar.set(Calendar.YEAR, year);
                currentCalendar.set(Calendar.MONTH, month - 1);
                currentCalendar.set(Calendar.DAY_OF_MONTH, day);

                updateDateLabel();
                dialog.dispose();
            }
        });

        dialog.setLayout(new BorderLayout());
        dialog.add(datePanel, BorderLayout.CENTER);
        dialog.add(selectButton, BorderLayout.SOUTH);

        return dialog;
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == prevDay) {
            currentCalendar.add(Calendar.DAY_OF_MONTH, -1);
        } else if (e.getSource() == nextDay) {
            currentCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        updateDateLabel();
    }

    private JPanel ExecRecordPanel() {
        return new JPanel(); // 구현 내용 추가 필요
    }
}
