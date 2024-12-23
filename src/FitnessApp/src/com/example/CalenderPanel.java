package com.example;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;

import static java.awt.Font.PLAIN;

class CalendarPanel extends JPanel {
    private JLabel yearMonthLabel;
    private JPanel calendarGrid;
    private Calendar currentCalendar;
    private JTextArea summaryArea; // 운동 요약을 표시할 JTextArea
    private JButton lastClickedButton; // 마지막으로 클릭된 버튼 저장
    private JButton DefaltBackround = new JButton();
    private int selectedDay, selectedMonth, selectedYear;
    private String loginedid, loginedpass;
    private ArrayList<String> dailyExercises = new ArrayList<>();
    private RecordPanel recordPanel;

    Connection conn;


    public CalendarPanel(String loginedid, String loginedpass, Connection conn, RecordPanel recordPanel) {
        this.loginedid = loginedid;
        this.loginedpass = loginedpass;
        this.recordPanel = recordPanel;
        this.conn = conn;

        setLayout(new GridLayout(1, 2));  // 1행 2열의 그리드 레이아웃
        currentCalendar = Calendar.getInstance(); // 현재 날짜로 초기화

        selectedYear = currentCalendar.get(Calendar.YEAR);
        selectedMonth = currentCalendar.get(Calendar.MONTH) + 1;
        selectedDay = currentCalendar.get(Calendar.DAY_OF_MONTH);

        // 캘린더 부분 (왼쪽)
        JPanel calendarSection = createCalendarSection();
        add(calendarSection);

        // 정보 패널 (오른쪽)
        JPanel infoPanel = createInfoPanel();
        add(infoPanel);

        updateCalendar(); // 초기 캘린더 표시
    }

    private JPanel createCalendarSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("운동 캘린더"));

        // 달력 상단 (년/월 선택)
        JPanel headerPanel = new JPanel();
        JButton prevMonth = new JButton("◀");
        JButton nextMonth = new JButton("▶");
        yearMonthLabel = new JLabel("", SwingConstants.CENTER);
        yearMonthLabel.setPreferredSize(new Dimension(150, 30));

        // 이전/다음 달 버튼 이벤트 추가
        prevMonth.addActionListener(e -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });

        nextMonth.addActionListener(e -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        headerPanel.add(prevMonth);
        headerPanel.add(yearMonthLabel);
        headerPanel.add(nextMonth);

        // 달력 그리드 (요일 + 날짜)
        calendarGrid = new JPanel(new GridLayout(7, 7));

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(calendarGrid, BorderLayout.CENTER);

        return panel;
    }

    private void updateCalendar() {
        calendarGrid.removeAll(); // 기존 달력 지우기

        // 년월 레이블 업데이트
        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH) + 1;
        yearMonthLabel.setText(String.format("%d년 %d월", year, month));

        // 요일 헤더 추가
        String[] weekDays = {"일", "월", "화", "수", "목", "금", "토"};
        for (String day : weekDays) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setBorder(BorderFactory.createEtchedBorder());
            if (day.equals("일")) {
                dayLabel.setForeground(Color.RED);
            } else if (day.equals("토")) {
                dayLabel.setForeground(Color.BLUE);
            }
            calendarGrid.add(dayLabel);
        }

        // 이번 달의 1일이 무슨 요일인지 계산
        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // 이번 달 1일의 요일 (0: 일요일)

        // 이번 달의 마지막 날짜 계산
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 1일 전까지 빈 공간 추가
        for (int i = 0; i < firstDayOfWeek; i++) {
            calendarGrid.add(new JLabel(""));
        }

        // 날짜 버튼 추가
        for (int i = 1; i <= lastDay; i++) {
            JButton dateButton = new JButton(String.valueOf(i));
            dateButton.setPreferredSize(new Dimension(40, 40));
            dateButton.setFont(dateButton.getFont().deriveFont(PLAIN));
            // 주말 색상 설정
            int dayOfWeek = (firstDayOfWeek + i - 1) % 7;
            if (dayOfWeek == 0) { // 일요일
                dateButton.setForeground(Color.RED);
            } else if (dayOfWeek == 6) { // 토요일
                dateButton.setForeground(Color.BLUE);
            }

            // 오늘 날짜 강조
            Calendar today = Calendar.getInstance();
            if (today.get(Calendar.YEAR) == year &&
                    today.get(Calendar.MONTH) == month - 1 &&
                    today.get(Calendar.DAY_OF_MONTH) == i) {
                dateButton.setBackground(new Color(255, 255, 200));
                dateButton.setFont(dateButton.getFont().deriveFont(Font.BOLD));
                lastClickedButton = dateButton;
            }

            int finalI = i;
            int finalMonth = currentCalendar.get(Calendar.MONTH) + 1;
            dateButton.addActionListener(e -> {
                if (lastClickedButton != null) {
                    lastClickedButton.setBackground(DefaltBackround.getBackground());
                    lastClickedButton.setOpaque(true);
                    lastClickedButton.setFont(lastClickedButton.getFont().deriveFont(PLAIN));
                    selectedDay = finalI; // 선택된 날짜 저장
                    selectedMonth = finalMonth;
                    updateSummaryPanel(selectedDay, selectedMonth); // 선택한 날짜에 대한 정보 업데이트
                    dateButton.setBackground(new Color(255, 255, 200));
                    dateButton.setFont(dateButton.getFont().deriveFont(Font.BOLD));
                    lastClickedButton = dateButton;
                }
            });
            calendarGrid.add(dateButton);
        }

        // 남은 공간 채우기
        int remainingCells = 42 - (firstDayOfWeek + lastDay); // 7 * 6 그리드
        for (int i = 0; i < remainingCells; i++) {
            calendarGrid.add(new JLabel(""));
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setBorder(BorderFactory.createTitledBorder("운동 정보"));
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        Calendar today = Calendar.getInstance();
        // 오늘의 운동 요약
        JPanel summaryPanel = new JPanel(new BorderLayout());
        JPanel resetPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("오늘의 운동"));
        JButton reset = new JButton("초기화");
        summaryArea = new JTextArea(5, 20);
        summaryArea.setEditable(false);
        updateSummaryPanel(today.get(Calendar.DAY_OF_MONTH), today.get(Calendar.MONTH) + 1);
        summaryPanel.add(new JScrollPane(summaryArea));
        summaryPanel.add(resetPanel, BorderLayout.EAST);
        resetPanel.add(reset);
        infoPanel.add(summaryPanel);

        // 운동 추가 패널
        JPanel addexec = new JPanel(new BorderLayout());
        String[] exercate = {"All", "Back", "Chest", "Shoulder", "Lower-body"};
        JComboBox exercateBox = new JComboBox(exercate);
        addexec.setBorder(BorderFactory.createTitledBorder("운동 리스트"));

        infoPanel.add(exercateBox);
        infoPanel.add(addexec);
        // 초기화버튼 이벤트 리스너
        reset.addActionListener(e -> resetData(conn));

        try {
            String sql = "SELECT Execid,Execname,Category FROM Exec";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(0, 4, 10, 10)); // 4열로 배치

            Vector<JButton> exercises = new Vector<>();
            while (rs.next()) {
                int execid = rs.getInt("Execid");
                String exerciseName = rs.getString("Execname");
                String category = rs.getString("Category");
                // 버튼 텍스트는 운동 이름과 카테고리를 함께 표시
                JButton exerciseButton = new JButton(exerciseName + " (" + category + ")");
                exerciseButton.setPreferredSize(new Dimension(40, 30));
                exerciseButton.setMaximumSize(new Dimension(40, 30));  // 최대 크기 설정
                exerciseButton.setMinimumSize(new Dimension(40, 30));
                // 버튼에 카테고리 정보도 저장
                exerciseButton.putClientProperty("category", category);
                // 나중에 카테고리 정보가 필요할 때:
                // String buttonCategory = (String) exerciseButton.getClientProperty("category");;
                exerciseButton.addActionListener(e -> {
                    addExercise(execid, exerciseName);
                    updateSummaryPanel(selectedDay, selectedMonth);
                });
                exercises.add(exerciseButton);
                buttonPanel.add(exerciseButton);
            }
            exercateBox.addActionListener(e -> {
                String selectedCategory = (String) exercateBox.getSelectedItem();
                buttonPanel.removeAll();
                for (JButton button : exercises) {
                    String buttoncate = (String) button.getClientProperty("category");
                    if ("All".equals(selectedCategory) || buttoncate.equals(selectedCategory)) {
                        buttonPanel.add(button);
                    }
                }
                buttonPanel.revalidate(); // 레이아웃 재계산
                buttonPanel.repaint();
            });
            JScrollPane scrollPane = new JScrollPane(buttonPanel);
            scrollPane.setPreferredSize(new Dimension(400, 300));
            addexec.add(scrollPane, BorderLayout.CENTER);

            rs.close();
            ps.close();


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return infoPanel;
    }


    private void addExercise(int execid, String execname) {
        try {
            // UserExec 테이블에 운동 기록 추가
            String insertSql = "INSERT INTO UserExec (Userid,Execid,ExecName,RecordDate) VALUES (?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                // 선택된 날짜를 SQL date 형식으로 변환
                String dateStr = String.format("%d-%02d-%02d", selectedYear, selectedMonth, selectedDay);
                Date sqlDate = Date.valueOf(dateStr);
                ps.setString(1, loginedid);
                ps.setInt(2, execid);
                ps.setString(3, execname);
                ps.setDate(4, sqlDate);

                // 중복시 업데이트 처리
                String checkDateSql =  "INSERT INTO Calendar (Date, Userid) VALUES (?, ?) " +
                        "ON DUPLICATE KEY UPDATE Date=Date";;
                try (PreparedStatement psCheck = conn.prepareStatement(checkDateSql)) {
                    psCheck.setDate(1, sqlDate);
                    psCheck.setString(2, loginedid);
                    psCheck.executeUpdate();
                }


                ps.executeUpdate();

                // INSERT 또는 중복 시 업데이트 쿼리
                String insertCal = "INSERT INTO User_Calendar (Userid, Execid, Date, Complete) VALUES (?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE Complete = VALUES(Complete)";
                try (PreparedStatement psCal = conn.prepareStatement(insertCal)) {
                    psCal.setString(1, loginedid);
                    psCal.setInt(2, execid);
                    psCal.setDate(3, sqlDate);
                    psCal.setInt(4, 0);

                    psCal.executeUpdate();
                }

                String recordsql = "INSERT INTO ExecRecord(Userid, Execid, ExecDate) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE ExecDate = VALUES(ExecDate)";
                PreparedStatement pst = conn.prepareStatement(recordsql);

                pst.setString(1, loginedid);
                pst.setInt(2, execid);
                pst.setDate(3, sqlDate);
                pst.executeUpdate();


                recordPanel.updateDateLabel();
                JOptionPane.showMessageDialog(this, "운동이 추가되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
                // UI 업데이트를 위해 운동 목록에 추가
                dailyExercises.add(execname);


            }

        } catch (SQLException e) {
            e.printStackTrace(); // 예외 스택 추적 출력
            JOptionPane.showMessageDialog(this, "운동 추가 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSummaryPanel(int day, int month) {
        summaryArea.setText(String.format("%d월 %d일의 운동 기록:\n", month, day));

        // 데이터베이스에서 해당 날짜의 운동 기록을 조회

        String sql = "SELECT Execname FROM UserExec WHERE UserID = ? AND RecordDate = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String dateStr = String.format("%d-%02d-%02d", selectedYear, month, day);
            Date sqlDate = Date.valueOf(dateStr);

            ps.setString(1, loginedid);
            ps.setDate(2, sqlDate);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String exerciseName = rs.getString("ExecName");
                    summaryArea.append("- " + exerciseName + "\n");
                }
            }

        } catch (SQLException e) {
            summaryArea.append("\n운동 기록을 불러오는 중 오류가 발생했습니다.");
        }
    }

    private void resetData(Connection con) {
        int result = JOptionPane.showConfirmDialog(this,
                "모든 운동 기록이 삭제됩니다. 정말 초기화하시겠습니까?",
                "초기화 확인",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result != JOptionPane.YES_OPTION) {
            return; // 사용자가 '아니오'를 선택한 경우
        }

        String UserExecStr = String.format("%d-%02d-%02d", selectedYear, selectedMonth, selectedDay);
        Date sqlDate = Date.valueOf(UserExecStr);
        String delex = "DELETE FROM UserExec WHERE UserID = ? AND RecordDate = ?";
        try {
            PreparedStatement ps = con.prepareStatement(delex);
            ps.setString(1, loginedid);
            ps.setDate(2, sqlDate);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        String delUserCal = "DELETE FROM User_Calendar WHERE UserID = ? AND Date = ?";
        try {
            PreparedStatement ps = con.prepareStatement(delUserCal);
            ps.setString(1, loginedid);
            ps.setDate(2, sqlDate);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        String delrecord = "DELETE FROM ExecRecord WHERE UserID = ? AND ExecDate = ?";
        try {
            PreparedStatement ps = con.prepareStatement(delrecord);
            ps.setString(1, loginedid);
            ps.setDate(2, sqlDate);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        JOptionPane.showMessageDialog(null, "초기화 완료", "초기화", JOptionPane.INFORMATION_MESSAGE);
        dailyExercises.clear();
    }
}