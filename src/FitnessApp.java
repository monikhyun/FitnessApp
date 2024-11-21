import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class FitnessApp extends JFrame implements ActionListener {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public FitnessApp() {
        setTitle("나야 헬린이");
        setSize(1600, 1024);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 메뉴 패널 생성
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(1, 6));

        // 메뉴 버튼 생성
        String[] menuNames = {"Calendar", "Record", "Stats", "Diet", "Play", "User"};
        for (String name : menuNames) {
            JButton button = new JButton(name);
            button.addActionListener(this); // 버튼에 액션 리스너 추가
            menuPanel.add(button);
        }

        // CardLayout을 사용하는 메인 패널 생성
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 각 화면 패널 추가
        mainPanel.add(CalPanel(), "Calendar");
        mainPanel.add(createPanel("Record 화면"), "Record");
        mainPanel.add(createPanel("Stats 화면"), "Stats");
        mainPanel.add(createPanel("Diet 화면"), "Diet");
        mainPanel.add(createPanel("Play 화면"), "Play");
        mainPanel.add(createPanel("User 화면"), "User");

        // 메뉴와 메인 패널을 프레임에 추가
        add(menuPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    // 각 화면에 표시될 패널 생성 (여기서는 단순히 라벨로 예시)
    private JPanel createPanel(String text) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JPanel CalPanel(){
        CalendarPanel panel = new CalendarPanel();
        return panel;
    }

    // 진태 캘린더
    class CalendarPanel extends JPanel {
        private JLabel yearMonthLabel;
        private JPanel calendarGrid;
        private Calendar currentCalendar;
        private JTextArea summaryArea; // 운동 요약을 표시할 JTextArea
        private int selectedDay = -1; // 선택된 날짜를 저장할 변수

        public CalendarPanel() {
            setLayout(new GridBagLayout());
            currentCalendar = Calendar.getInstance(); // 현재 날짜로 초기화

            GridBagConstraints gbc = new GridBagConstraints();

            // 캘린더 부분 (왼쪽 2/3)
            JPanel calendarSection = createCalendarSection();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 0.67;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            add(calendarSection, gbc);

            // 정보 패널 (오른쪽 1/3)
            JPanel infoPanel = createInfoPanel();
            gbc.gridx = 1;
            gbc.weightx = 0.33;
            add(infoPanel, gbc);

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
                }

                int finalI = i;
                int finalMonth = currentCalendar.get(Calendar.MONTH) + 1;
                dateButton.addActionListener(e -> {
                    selectedDay = finalI; // 선택된 날짜 저장
                    int selectedMonth = finalMonth;
                    updateSummaryPanel(finalI, selectedMonth); // 선택한 날짜에 대한 정보 업데이트
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

    // 버튼 클릭 시 해당 화면으로 전환
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        cardLayout.show(mainPanel, command);
    }

    public static void main(String[] args) {

    }
}
