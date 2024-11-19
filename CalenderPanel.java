package JavaProject;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Calendar;

import static java.awt.Font.PLAIN;

class CalendarPanel extends JPanel {
    private JLabel yearMonthLabel;
    private JPanel calendarGrid;
    private Calendar currentCalendar;
    private JTextArea summaryArea; // 운동 요약을 표시할 JTextArea
    private int selectedDay = -1; // 선택된 날짜를 저장할 변수
    private JButton lastClickedButton; // 마지막으로 클릭된 버튼 저장
    private JButton DefaltBackround = new JButton();

    public CalendarPanel() {
        setLayout(new GridLayout(1, 2));  // 1행 2열의 그리드 레이아웃
        currentCalendar = Calendar.getInstance(); // 현재 날짜로 초기화

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
                if(lastClickedButton != null) {
                    lastClickedButton.setBackground(DefaltBackround.getBackground());
                    lastClickedButton.setOpaque(true);
                    lastClickedButton.setFont(lastClickedButton.getFont().deriveFont(PLAIN));
                    selectedDay = finalI; // 선택된 날짜 저장
                    int selectedMonth = finalMonth;
                    updateSummaryPanel(finalI, selectedMonth); // 선택한 날짜에 대한 정보 업데이트
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

        // 오늘의 운동 요약
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBorder(BorderFactory.createTitledBorder("오늘의 운동"));
        summaryArea = new JTextArea(5, 20);
        summaryArea.setEditable(false);
        summaryArea.setText("운동을 선택하세요.");
        summaryPanel.add(new JScrollPane(summaryArea));
        infoPanel.add(summaryPanel);

        // 운동 추가 패널
        JPanel addexec = new JPanel(new BorderLayout());
        addexec.setBorder(BorderFactory.createTitledBorder("운동 리스트"));
        infoPanel.add(addexec);

        String url = "jdbc:mysql://fitnessapp.chqw04eu8yfk.ap-southeast-2.rds.amazonaws.com:3306/fitnessapp";  // 데이터베이스 URL
        String username = "mih";
        String password = "ansxoddl123";
        Connection conn;

        try {
            conn = DriverManager.getConnection(url, username, password);
            String sql = "SELECT Execname FROM Exec";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(0, 4, 10, 10)); // 4열로 배치

            while (rs.next()) {
                String exerciseName = rs.getString("Execname");
                JButton exerciseButton = new JButton(exerciseName);

                exerciseButton.addActionListener(e -> {
                    if (selectedDay != -1) { // 날짜가 선택된 경우
                        summaryArea.append("\n" + exerciseName + " 추가됨");
                    } else {
                        JOptionPane.showMessageDialog(this, "날짜를 먼저 선택하세요.");
                    }
                });
                buttonPanel.add(exerciseButton);
            }

            JScrollPane scrollPane = new JScrollPane(buttonPanel);
            scrollPane.setPreferredSize(new Dimension(400, 300));
            addexec.add(scrollPane, BorderLayout.CENTER);

            rs.close();
            ps.close();
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return infoPanel;
    }

    private void updateSummaryPanel(int day, int month) {
        summaryArea.setText(String.format("%d월 %d일의 운동 기록:\n", month,day));
    }

    private void showDaySchedule(int day) {
        JDialog scheduleDialog = new JDialog();
        scheduleDialog.setTitle(currentCalendar.get(Calendar.YEAR) + "년 " +
                (currentCalendar.get(Calendar.MONTH) + 1) + "월 " +
                day + "일의 운동 계획");
        scheduleDialog.setSize(300, 400);
        scheduleDialog.setLocationRelativeTo(this);

        JPanel schedulePanel = new JPanel();
        schedulePanel.setLayout(new BoxLayout(schedulePanel, BoxLayout.Y_AXIS));

        String[] exercises = {"벤치프레스", "스쿼트", "데드리프트"};
        for (String exercise : exercises) {
            JCheckBox checkBox = new JCheckBox(exercise);
            schedulePanel.add(checkBox);
        }

        scheduleDialog.add(schedulePanel);
        scheduleDialog.setModal(true);
        scheduleDialog.setVisible(true);
    }
}