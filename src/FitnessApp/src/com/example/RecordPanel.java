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
    private java.util.List<String> execlist = new java.util.ArrayList<>();

    Connection conn;


    public RecordPanel(String id, String passwd, Connection conn) {
        this.loginedid = id;
        this.loginedpass = passwd;
        this.conn = conn;

        setLayout(new GridLayout(1, 2,20,0));

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

        try {
            String execid = "SELECT Execname FROM Exec";
            PreparedStatement ps2 = conn.prepareStatement(execid);
            ResultSet rs = ps2.executeQuery();

            while(rs.next()){
                execlist.add(rs.getString("Execname"));
            }

        }

        catch(SQLException e){
            e.printStackTrace();
        }
    }

    private JPanel SearchDailyExec() {
        JPanel headerPanel = new JPanel();
        prevDay = new JButton("◀");
        nextDay = new JButton("▶");
        dateLabel = new JLabel("");
        dateLabel.setHorizontalAlignment(JLabel.CENTER);
        dateLabel.setPreferredSize(new Dimension(150, 50));

        currentCalendar = Calendar.getInstance();
        updateDateLabel();

        prevDay.addActionListener(this);
        nextDay.addActionListener(this);

        dateLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 20));

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
                    execlabel.setFont(new Font("Malgun Gothic", Font.BOLD, 38));
                    execlabel.setHorizontalAlignment(SwingConstants.CENTER);
                    panel.add(execlabel, BorderLayout.NORTH);
                    JScrollPane rcpanel =execdetails(execlabel.getText());
                    panel.add(rcpanel, BorderLayout.CENTER);
                    panels.add(panel);

                    button.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // 패널 표시
                            JButton evenbtn = (JButton) e.getSource();
                            String execname = evenbtn.getText();
                            ExecRecordPanel(panel,execname,rcpanel);
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

    private void ExecRecordPanel(JPanel jpanel, String execname, JScrollPane jsp) {
        RecordGrid.removeAll();

        java.util.List<JPanel> recordexecList = new java.util.ArrayList<>(); // 새로운 recordexec 리스트 생성
        loadRecordExecs(execname, jpanel, recordexecList, jsp);
        RecordGrid.add(jpanel, BorderLayout.CENTER);
        RecordGrid.revalidate();
        RecordGrid.repaint();
    }

    private String getExecNameFromPanel(JPanel jpanel) {
        JLabel label = (JLabel) jpanel.getComponent(0);
        return label.getText();
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

    private JScrollPane execdetails(String execname){
        JPanel execdetail = new JPanel();
        execdetail.setLayout(null);
        execdetail.setBackground(Color.WHITE);
        execdetail.setPreferredSize(new Dimension(800, 1000)); // 초기 크기 설정


        JButton okbtn = new JButton("확인");
        okbtn.setBackground(Color.white);
        okbtn.setBounds(620,30,70,50);

        execdetail.add(okbtn);

        JPanel execimgpanel = new JPanel();
        execimgpanel.setBounds(220, 100,300,300);
        execimgpanel.setBackground(Color.RED);
        execimgpanel.setVisible(true);


        execdetail.add(execimgpanel);

        java.util.List<JPanel> recordexecList = new java.util.ArrayList<>();
        JPanel recordexec = createRecordExecPanel(0);
        recordexecList.add(recordexec);
        execdetail.add(recordexec);


        JButton pluspanel = (JButton) recordexec.getComponent(6);

        pluspanel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = recordexecList.size(); // 자동으로 recordexeclist 다음 인덱스 부여
                JPanel newRecordExec = createRecordExecPanel(index);

                recordexecList.add(newRecordExec);
                execdetail.add(newRecordExec);

                try  {
                    String sql = "INSERT INTO UserExec (Userid, Execid,Execname, Date, Duration) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, loginedid);
                    int i  = execlist.indexOf(execname);
                    ps.setInt(2, i);
                    ps.setString(3, execname); // 예제 운동 이
                    ps.setDate(4, Date.valueOf(dateLabel.getText())); // 현재 날짜
                    ps.setInt(5, index + 1); // 순서 저장
                    ps.executeUpdate();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

                execdetail.setPreferredSize(new Dimension(800, 500 + recordexecList.size() * 120));
                execdetail.revalidate();
                execdetail.repaint();
            }
        });
        JScrollPane scrollPane = new JScrollPane(execdetail);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        okbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try  {
                    for (int i = 0; i < recordexecList.size(); i++) {
                        JPanel recordexec = recordexecList.get(i);

                        // JTextField 값 가져오기
                        int set = Integer.parseInt(((JTextField) recordexec.getComponent(0)).getText());
                        int kg = Integer.parseInt(((JTextField) recordexec.getComponent(2)).getText());
                        int count = Integer.parseInt(((JTextField) recordexec.getComponent(4)).getText());

                        // 데이터 업데이트
                        String sql = "UPDATE UserExec SET Kg = ?, Count = ? , Complete = ? WHERE Userid = ? AND Date = ? AND Duration = ?";
                        PreparedStatement ps = conn.prepareStatement(sql);
                        ps.setInt(1, kg);
                        ps.setInt(2, count);
                        ps.setInt(3,1);
                        ps.setString(4, loginedid);
                        ps.setDate(5, Date.valueOf(dateLabel.getText())); // 현재 날짜
                        ps.setInt(6, i + 1); // 순서 저장
                        ps.executeUpdate();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }

            }
        });

        return scrollPane;
    }

    public static JPanel createRecordExecPanel(int index) {
        JPanel recordexec = new JPanel();
        recordexec.setBackground(Color.GRAY);
        recordexec.setBounds(80, 500 + index * 120, 600, 100);
        recordexec.setLayout(null);

        JLabel setinput = new JLabel("Set");
        setinput.setBounds(130, 30, 50, 50);

        JTextField set = new JTextField();
        set.setBounds(70, 30, 50, 50);
        set.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel kginput = new JLabel("Kg");
        kginput.setBounds(260, 30, 50, 50);

        JTextField kg = new JTextField();
        kg.setBounds(200, 30, 50, 50);
        kg.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel cntinput = new JLabel("회");
        cntinput.setBounds(380, 30, 50, 50);

        JTextField cnt = new JTextField();
        cnt.setBounds(320, 30, 50, 50);
        cnt.setHorizontalAlignment(SwingConstants.CENTER);

        if(index==0) {
            JButton pluspanel = new JButton("+");
            pluspanel.setBounds(480, 30, 50, 50);
            pluspanel.setFont(new Font("Malgun Gothic", Font.PLAIN, 20));

            recordexec.add(set);
            recordexec.add(setinput);
            recordexec.add(kg);
            recordexec.add(kginput);
            recordexec.add(cnt);
            recordexec.add(cntinput);
            recordexec.add(pluspanel);
        }
        else {
            recordexec.add(set);
            recordexec.add(setinput);
            recordexec.add(kg);
            recordexec.add(kginput);
            recordexec.add(cnt);
            recordexec.add(cntinput);
        }

        return recordexec;
    }

    private void loadRecordExecs(String execname, JPanel jpanel, java.util.List<JPanel> recordexecList, JScrollPane jsp) {
        try  {
            String sql = "SELECT Duration, Execname, Kg, Count " +
                    "FROM UserExec WHERE Userid = ? AND Date = ? AND Execname = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, loginedid);  // 로그인 ID
            ps.setDate(2, Date.valueOf(dateLabel.getText()));  // 선택 날짜
            ps.setString(3, execname);  // 운동 이름
            ResultSet rs = ps.executeQuery();


            recordexecList.clear(); // 기존 리스트 초기화

            while (rs.next()) {
                int setcount = rs.getInt("Duration");
                int kgValue = rs.getInt("Kg");
                int count = rs.getInt("Count");

                // recordexec 패널 생성 및 복원
                JPanel recordexec = createRecordExecPanel(setcount - 1);
                recordexecList.add(recordexec); // 리스트에 추가
                jsp.add(recordexec); // 패널에 추가

                // JTextField 값 복원
                ((JTextField) recordexec.getComponent(0)).setText(String.valueOf(setcount)); // Set
                ((JTextField) recordexec.getComponent(2)).setText(String.valueOf(kgValue)); // Kg
                ((JTextField) recordexec.getComponent(4)).setText(String.valueOf(count));  // 회
            }

            jsp.revalidate();
            jsp.repaint();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }



}

