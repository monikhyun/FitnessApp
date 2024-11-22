package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.List;
import java.util.List.*;
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
    private JButton[][][] execbuttons = new JButton[5][366][];
    private JPanel[][][] execPanels = new JPanel[5][366][];



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

        RecordGrid = new JPanel(new BorderLayout());
        JLabel placeholderLabel = new JLabel("기록하실 운동을 선택해주세요.");
        placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        placeholderLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 20));
        RecordGrid.add(placeholderLabel, BorderLayout.NORTH);
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

        panel.add(bottompanel,BorderLayout.CENTER);

        return panel;
    }

    // 날짜 라벨 업데이트 메서드
    private void updateDateLabel() {
        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH) + 1;
        int day = currentCalendar.get(Calendar.DAY_OF_MONTH);

        resetRecordGrid();
        dateLabel.setText(year + "-" + month + "-" + day);
        int yearIndex = yearToIndex(year);
        int dayIndex = dateToIndex(year, month, day);
        if (execbuttons[yearIndex][dayIndex] != null){
            execbtn = execbuttons[yearIndex][dayIndex];
            bottompanel.setLayout(new GridLayout(execbtn.length, 1));
            bottompanel.removeAll();
            for (JButton button : execbtn) {
                bottompanel.add(button);
            }
            bottompanel.revalidate();
            bottompanel.repaint();
        }
        else{
            try {
                Connection conn;
                conn = DriverManager.getConnection(dburl, dbusr, dbpass);
                String sql = "SELECT Execid,Execname FROM UserExec WHERE Userid=? AND Date = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, loginedid);
                ps.setString(2, dateLabel.getText());
                ResultSet rs = ps.executeQuery();


                java.util.List<JButton> buttons = new java.util.ArrayList<>();
                java.util.List<JPanel> panels = new java.util.ArrayList<>();
                bottompanel.removeAll();

                while (rs.next()) {
                    JButton button = new JButton(rs.getString("Execname"));
                    buttons.add(button);
                    bottompanel.add(button);

                    JPanel panel = new JPanel(new BorderLayout());
                    JLabel execlabel =new JLabel(button.getText());
                    execlabel.setFont(new Font("Malgun Gothic", Font.BOLD, 24));
                    execlabel.setHorizontalAlignment(SwingConstants.CENTER);
                    panel.add(execlabel, BorderLayout.NORTH);
                    panel.add(execdetails(), BorderLayout.CENTER);
                    panels.add(panel);

                    button.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // 패널 표시
                            ExecRecordPanel(panel);
                        }
                    });
                }

                execbtn = buttons.toArray(new JButton[0]);
                execbuttons[yearIndex][dayIndex] = execbtn;

                JPanel[] panelArray = panels.toArray(new JPanel[0]);
                execPanels[yearIndex][dayIndex] = panelArray;

                bottompanel.setLayout(new GridLayout(execbtn.length, 1));
                bottompanel.revalidate();
                bottompanel.repaint();

                rs.close();
                ps.close();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }



    private int yearToIndex(int year) {
        return year - 2024; // 기준 연도를 2024로 설정
    }

    private int dateToIndex(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day); // month는 0부터 시작
        return cal.get(Calendar.DAY_OF_YEAR) - 1; // 0-based index
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
        for (int year = 2024; year <= 2030; year++) {
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
            updateDateLabel();
        } else if (e.getSource() == nextDay) {
            currentCalendar.add(Calendar.DAY_OF_MONTH, 1);
            updateDateLabel();
        }
    }

    private void ExecRecordPanel(JPanel jpanel) {
        RecordGrid.removeAll();
        RecordGrid.add(jpanel, BorderLayout.CENTER);
        RecordGrid.revalidate();
        RecordGrid.repaint();
    }

    private void resetRecordGrid() {

        if (RecordGrid == null) {
            RecordGrid = new JPanel(new BorderLayout());
        }
        RecordGrid.removeAll();

        JLabel placeholderLabel = new JLabel("기록하실 운동을 선택해주세요.");
        placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        placeholderLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 20));
        RecordGrid.add(placeholderLabel, BorderLayout.NORTH);

        RecordGrid.revalidate();
        RecordGrid.repaint();
    }

    private JPanel execdetails(){
        JPanel execdetail = new JPanel(new BorderLayout());
        execdetail.setBackground(Color.WHITE);
        JPanel execimgpanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 50));
        execimgpanel.setPreferredSize(new Dimension(300, 300));
        execimgpanel.setBackground(Color.RED);
        execimgpanel.setVisible(true);
        JPanel execimg = new JPanel();
        execimg.setPreferredSize(new Dimension(200, 200));
        execimg.setBackground(Color.GREEN);
        execimgpanel.add(execimg, BorderLayout.CENTER);
        execdetail.add(execimgpanel, BorderLayout.NORTH);



        return execdetail;
    }
}

