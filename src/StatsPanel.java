package JavaProjects;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.Calendar;

import static java.util.Calendar.*;


public class StatsPanel extends JPanel {
    private LocalDate currentMonth;
    private JLabel monthLabel;
    private Calendar currentCalendar;
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

        // 현재 날짜 초기화
        currentCalendar = Calendar.getInstance();

        // 상단 패널
        JPanel topPanel = new JPanel();
        JButton prevButton = new JButton("<");
        JButton nextButton = new JButton(">");
        monthLabel = new JLabel("", SwingConstants.CENTER);

        prevButton.addActionListener(e -> {
            currentCalendar.add(MONTH, -1);
            updateMonth();
        });

        nextButton.addActionListener(e -> {
            currentCalendar.add(MONTH, 1);
            updateMonth();
        });

        topPanel.add(prevButton);
        topPanel.add(monthLabel);
        topPanel.add(nextButton);
        add(topPanel, BorderLayout.NORTH);

        // 중앙 패널
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);

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
        updateMonth();
    }

    private void updateMonth() {

        int year = currentCalendar.get(YEAR);
        int month = currentCalendar.get(MONTH) + 1;

        // 년/월 레이블 업데이트
        monthLabel.setText(String.format("%d년 %d월", year, month));

        // 해당 월에 대한 데이터 패널 업데이트
        String cureentMonth = String.format("%d-%02d", year, month); // yyyy-MM 형식으로 월 키 생성

        try {
            // 데이터베이스에서 가져온 운동량, 볼륨, 영양소 데이터
            int[] workoutData = loadWorkoutData(cureentMonth);
            int[] volumeData = loadVolumeData(cureentMonth);
            int[] nutritionData = loadNutritionData(cureentMonth);

            // 패널에 데이터 설정
            workoutPanel.setWorkoutData(workoutData);
            volumePanel.setVolumeData(volumeData);
            nutritionPanel.setNutritionData(nutritionData);

            // 그래프를 새로 그림
            workoutPanel.repaint();
            volumePanel.repaint();
            nutritionPanel.repaint();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "데이터를 가져오는 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
        // 화면 갱신
        revalidate();
        repaint();
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

                // 각 부위에 맞게 총 볼륨을 계산
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
        // 같은 날짜에 중복된 운동 기록이 있더라도 한 날짜에 대해 하나의 RecordDate만 가져옴
        String query = """
            SELECT DISTINCT RecordDate
            FROM UserExec 
            WHERE Userid = ? 
            AND DATE_FORMAT(RecordDate, '%Y-%m') = ? 
            AND Complete = 1
            """;

        int[] workoutData = {0, 0, 0, 0, 0}; // 최대 5주까지

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, loginedid); // 첫번째 매개변수로 저장
            stmt.setString(2, month);   // 두번째 매개변수로 저장

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Date recordDate = rs.getDate("RecordDate");

                // 해당 날짜가 어느 주에 속하는지 구하기
                Calendar calendar = getInstance(); // 현재 날짜와 시간을 가져오는 메소드
                calendar.setTime(recordDate);   // recordDate를 calendar 객체에 설정
                int week = calendar.get(WEEK_OF_MONTH); // 해당 날짜가 해당 월의 몇 번째 주인지 반환

                if (week >= 1 && week <= 5) {
                    workoutData[week - 1] += 1; // 해당 주의 운동 횟수 증가
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

            // X축
            g.drawLine(originX, originY, width + 30, originY);

            // Y축
            g.drawLine(originX, originY, originX, 30);
            g.drawString("볼륨 (kg)", originX - 45, 30);

            // 막대 그래프 그리기
            int barWidth = (width - 20) / volumeData.length;  // 각 막대의 너비 계산 (그래프 영역 너비를 데이터 개수로 나누기)

            for (int i = 0; i < volumeData.length; i++) {
                // 막대의 높이 계산 (현재 데이터 값에 비례하여 화면에 비율로 그리기)
                int barHeight = volumeData[i] * height / max;

                // 각 막대의 X, Y 좌표 계산
                int x = originX + (i * barWidth) + 10;  // X좌표: 원점에서 막대의 순번에 맞게 위치 이동
                int y = originY - barHeight;  // Y좌표: 원점에서 막대 높이만큼 위로 이동

                // 막대를 채우기
                g.setColor(new Color(97, 155, 250));
                g.fillRect(x, y, barWidth - 10, barHeight);

                // 상세 데이터 표시
                g.setColor(Color.BLACK);
                g.drawString(labels[i], x + 5, originY + 15);  // 각 막대의 부위 표시
                g.drawString(volumeData[i] + " kg", x + 5, y - 5);  // 각 막대의 높이에 맞게 볼륨 표시
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
            String[] weekLabels = {"첫째 주", "둘째 주", "셋째 주", "넷째 주", "다섯째 주"};
            int max = 7;

            int width = getWidth() - 100;
            int height = getHeight() - 50;
            int originX = 50;
            int originY = height + 20 ;

            // X축
            g.drawLine(originX, originY, width + 10, originY);
            // Y축
            g.drawLine(originX, originY, originX, 30);
            g.drawString("운동 횟수", originX - 45, 30);

            // 데이터 표시
            // 이전 X, Y 좌표 초기화
            int prevX = originX, prevY = originY - (workoutData[0] * height / max);

            for (int i = 0; i < workoutData.length; i++) {
                // X 좌표를 계산: 각 주별로 일정 간격을 두고 그리기 위해 width를 workoutData.length-1로 나눔
                int x = originX + (i * width / (workoutData.length - 1));
                // Y 좌표를 계산: 운동 횟수를 최대값을 기준으로 그래프의 높이에 맞게 비율로 계산
                int y = originY - (workoutData[i] * height / max);

                g.fillOval(x - 3, y - 3, 6, 6); // 각 데이터 포인트에 작은 원을 그려 점처럼 표시
                g.drawString(weekLabels[i], x - 20, originY + 15); // X축에 몇째 주인지 표시
                g.drawString(workoutData[i] + "회", x + 5, y - 5); // 운동 횟수를 점 위에 표시
                g.drawLine(prevX, prevY, x, y);  // 이전 점과 현재 점을 이어주는 선

                // 현재 점 업데이트
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

            // 그래프 중심 및 크기 설정
            int width = getWidth();
            int height = getHeight();
            int radius = Math.min(width, height) / 4;
            int centerX = width / 2;
            int centerY = height / 2;

            // 각 영양소 합계 비율 계산
            int sum = 0;
            for (int value : nutritionData) sum += value;

            // 총합 퍼센트가 100%가 되도록 계산
            double[] percentages = new double[nutritionData.length];
            for (int i = 0; i < nutritionData.length; i++) {
                percentages[i] = sum == 0 ? 0 : (nutritionData[i] * 100.0 / sum); // 퍼센트 값 계산
            }

            // 원 그래프 그리기
            int startAngle = 0;
            for (int i = 0; i < nutritionData.length; i++) {
                int arcAngle = (int) Math.round(percentages[i] * 360 / 100); // 각도 계산
                g.setColor(colors[i]);
                g.fillArc(centerX - radius, centerY - radius, radius * 2, radius * 2, startAngle, arcAngle);
                startAngle += arcAngle;

                // 영양소 비율 출력
                g.setColor(Color.BLACK);
                g.drawString(labels[i] + " (" + String.format("%.1f", percentages[i]) + "%)",
                        centerX + radius + 10, centerY - radius + 20 + (i * 15));
            }
            // 적정 영양소 비율 출력
            g.setColor(Color.PINK);
            g.drawString("적정 탄수화물 비율: 50~60%", centerX + radius + 10, centerY + 50);
            g.drawString("적정 단백질 비율: 25~35%", centerX + radius + 10, centerY + 65);
            g.drawString("적정 지방 비율: 15~25%", centerX + radius + 10, centerY + 80);
        }
    }
}

