package JavaProjects;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;



public class StatsPanel extends JPanel {
    private LocalDate currentMonth;
    private JLabel monthLabel;
    private VolumeChartPanel volumePanel;
    private WorkoutChartPanel workoutPanel;
    private NutritionChartPanel nutritionPanel;
    private String loginedid, loginedpass;
    private Connection conn;

    public StatsPanel(String loginedid, String loginedpass, Connection conn) {
        this.loginedid = loginedid;
        this.loginedpass = loginedpass;
        this.conn = conn;

        setLayout(new BorderLayout());

        // 현재 월 초기화
        currentMonth = LocalDate.now().withDayOfMonth(1);

        // 상단 패널
        JPanel topPanel = new JPanel();
        JButton prevButton = new JButton("<");
        JButton nextButton = new JButton(">");
        monthLabel = new JLabel(formatMonth(currentMonth), SwingConstants.CENTER);

        prevButton.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            updateMonth();
        });

        nextButton.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            updateMonth();
        });

        topPanel.add(prevButton);
        topPanel.add(monthLabel);
        topPanel.add(nextButton);
        add(topPanel, BorderLayout.NORTH);

        // 중앙 패널
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null); // 레이아웃 매니저 해제

        volumePanel = new VolumeChartPanel();
        volumePanel.setBorder(BorderFactory.createTitledBorder("운동 볼륨 통계"));
        volumePanel.setBounds(50, 50, 700, 650);
        mainPanel.add(volumePanel);

        workoutPanel = new WorkoutChartPanel();
        workoutPanel.setBorder(BorderFactory.createTitledBorder("운동 횟수 통계"));
        workoutPanel.setBounds(800, 50, 500, 300);
        mainPanel.add(workoutPanel);

        nutritionPanel = new NutritionChartPanel();
        nutritionPanel.setBorder(BorderFactory.createTitledBorder("영양소 섭취 통계"));
        nutritionPanel.setBounds(800, 400, 500, 300);
        mainPanel.add(nutritionPanel);

        add(mainPanel, BorderLayout.CENTER);

        // 데이터 초기화
        updatePanels(currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")));
    }

    private void updateMonth() {
        monthLabel.setText(formatMonth(currentMonth));
        String monthKey = currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        updatePanels(monthKey);
    }
    private String formatMonth(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월");
        return date.format(formatter);
    }

    // 패널들 한번에 오류 처리
    private void updatePanels(String month) {
        try {
            int[] workoutData = loadWorkoutData(month);
            int[] volumeData = loadVolumeData(month);
            int[] nutritionData = loadNutritionData(month);

            workoutPanel.setWorkoutData(workoutData);
            volumePanel.setVolumeData(volumeData);
            nutritionPanel.setNutritionData(nutritionData);

            workoutPanel.repaint();
            volumePanel.repaint();
            nutritionPanel.repaint();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "데이터를 가져오는 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
    // 운동 볼륨 데이터 가져오기
    private int[] loadVolumeData(String month) throws SQLException {
        String query = """
        SELECT e.Category, SUM(ue.Totalvolume) AS total_volume 
        FROM UserExec ue
        JOIN Exec e ON ue.Execid = e.Execid
        WHERE ue.Userid = ? AND DATE_FORMAT(ue.RecordDate, '%Y-%m') = ? AND ue.Complete = 1 
        GROUP BY e.Category
        """;

        int[] volumeData = {0, 0, 0, 0, 0}; // 등, 가슴, 어깨, 하체, 전체 총 볼륨

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, loginedid);
            stmt.setString(2, month);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String category = rs.getString("Category");
                int totalVolume = rs.getInt("total_volume");

                if (category.equals("Back")) {
                    volumeData[0] = totalVolume;
                } else if (category.equals("Chest")) {
                    volumeData[1] = totalVolume;
                } else if (category.equals("Shoulder")) {
                    volumeData[2] = totalVolume;
                } else if (category.equals("Lower-body")) {
                    volumeData[3] = totalVolume;
                }
            }
            // "전체" 총 볼륨 계산 (모든 부위의 총합)
            volumeData[4] = volumeData[0] + volumeData[1] + volumeData[2] + volumeData[3];
        }
        return volumeData;
    }
    // 운동 횟수 데이터 가져오기
    private int[] loadWorkoutData(String month) throws SQLException {
        String query = """
            SELECT RecordDate, COUNT(DISTINCT Execid) AS workout_days
            FROM UserExec 
            WHERE Userid = ? 
            AND DATE_FORMAT(RecordDate, '%Y-%m') = ? 
            AND Complete = 1
            GROUP BY RecordDate
            ORDER BY RecordDate
            """;

        int[] workoutData = {0, 0, 0, 0};

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, loginedid);
            stmt.setString(2, month);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Date recordDate = rs.getDate("RecordDate");
                int workoutDays = rs.getInt("workout_days");

                // 해당 날짜가 어느 주에 속하는지 구하기
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(recordDate);
                int weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH);

                if (weekOfMonth >= 1 && weekOfMonth <= 4) {
                    workoutData[weekOfMonth - 1] += workoutDays;
                }
            }
        }
        return workoutData;
    }
    // 영양소 데이터 가져오기
    private int[] loadNutritionData(String month) throws SQLException {
        String query = """
            SELECT SUM(d.Carbo * ud.count) AS total_carbo, SUM(d.Protein * ud.count) AS total_protein, SUM(d.Fat * ud.count) AS total_fat
            FROM Userdiet ud
            JOIN Diet d ON ud.Foodname = d.Foodname
            WHERE ud.Userid = ? AND DATE_FORMAT(ud.Date, '%Y-%m') = ? 
            GROUP BY ud.Userid
            """;

        int[] nutritionData = {0, 0, 0};    // 탄수화물, 단백질, 지방

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, loginedid);
            stmt.setString(2, month);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                nutritionData[0] = rs.getInt("total_carbo");
                nutritionData[1] = rs.getInt("total_protein");
                nutritionData[2] = rs.getInt("total_fat");
            }
        }
        return nutritionData;
    }

    // 부위별 볼륨 차트 패널
    class VolumeChartPanel extends JPanel {
        private int[] volumeData = {0, 0, 0, 0};

        public void setVolumeData(int[] data) {
            volumeData = data;
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            String[] labels = {"등", "가슴", "어깨", "하체", "총 볼륨"};
            int max = 25000;

            int width = getWidth() - 70;
            int height = getHeight() - 40;
            int originX = 50;
            int originY = height + 20;

            // Y축
            g.drawLine(originX, originY, originX, 30);
            g.drawString("볼륨 (kg)", originX - 45, 30);

            // X축
            g.drawLine(originX, originY, width + 10, originY);

            // 막대 그래프
            int barWidth = (width - 20) / volumeData.length;
            for (int i = 0; i < volumeData.length; i++) {
                int barHeight = volumeData[i] * height / max;
                int x = originX + (i * barWidth) + 10;
                int y = originY - barHeight;
                g.setColor(new Color(97, 155, 250));
                g.fillRect(x, y, barWidth - 10, barHeight);
                g.setColor(Color.BLACK);
                g.drawString(labels[i], x + 5, originY + 15);
                g.drawString(volumeData[i] + " kg", x + 5, y - 5);

            }
        }
    }

    // 운동량 차트 패널
    class WorkoutChartPanel extends JPanel {
        private int[] workoutData = {0, 0, 0, 0};

        public void setWorkoutData(int[] data) {
            workoutData = data;
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            String[] weekLabels = {"첫째 주", "둘째 주", "셋째 주", "넷째 주"};
            int max = 31;

            int width = getWidth() - 100;
            int height = getHeight() - 50;
            int originX = 50;
            int originY = height + 20 ;

            // Y축
            g.drawLine(originX, originY, originX, 30);
            g.drawString("운동 횟수", originX - 45, 30);

            // X축
            g.drawLine(originX, originY, width + 10, originY);

            // 데이터 표시
            int prevX = originX, prevY = originY - (workoutData[0] * height / max);
            for (int i = 0; i < workoutData.length; i++) {
                int x = originX + (i * width / (workoutData.length - 1));
                int y = originY - (workoutData[i] * height / max);
                g.fillOval(x - 3, y - 3, 6, 6);
                g.drawString(weekLabels[i], x - 10, originY + 15);
                g.drawString(workoutData[i] + "회", x + 5, y - 5);
                g.drawLine(prevX, prevY, x, y);
                prevX = x;
                prevY = y;
            }
        }
    }

    // 영양소 차트 패널
    class NutritionChartPanel extends JPanel {
        private int[] nutritionData = {0, 0, 0};

        public void setNutritionData(int[] data) {
            nutritionData = data;
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            String[] labels = {"탄수화물", "단백질", "지방"};
            Color[] colors = {Color.YELLOW, Color.RED, Color.GREEN};

            int width = getWidth();
            int height = getHeight();
            int radius = Math.min(width, height) / 4;
            int centerX = width / 2;
            int centerY = height / 2;

            int sum = 0;
            for (int value : nutritionData) sum += value;

            int startAngle = 0;
            for (int i = 0; i < nutritionData.length; i++) {
                int arcAngle = (int) Math.round((double) nutritionData[i] / sum * 360);
                g.setColor(colors[i]);
                g.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, startAngle, arcAngle);
                startAngle += arcAngle;
                g.setColor(Color.BLACK);
                g.drawString(labels[i] + " (" + nutritionData[i] + "%)", centerX + radius + 10, centerY - radius + 20 + (i * 15));
            }
        }
    }
}

