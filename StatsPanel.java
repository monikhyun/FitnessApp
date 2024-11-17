package JavaProjects;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class StatsPanel extends JPanel {
    private LocalDate currentMonth;
    private JLabel monthLabel;
    private WorkoutChartPanel workoutPanel;
    private VolumeChartPanel volumePanel;
    private NutritionChartPanel nutritionPanel;

    // 월별 데이터를 저장하는 맵
    private Map<String, int[]> workoutDataMap;
    private Map<String, int[]> volumeDataMap;
    private Map<String, int[]> nutritionDataMap;

    public StatsPanel() {
        setLayout(new BorderLayout());

        // 현재 월을 초기화
        currentMonth = LocalDate.now().withDayOfMonth(1);

        // 월별 데이터를 초기화
        initMonthlyData();

        // 상단 패널에 월 표시와 좌우 버튼 추가
        JPanel topPanel = new JPanel();
        JButton prevButton = new JButton("<");
        JButton nextButton = new JButton(">");
        monthLabel = new JLabel(formatMonth(currentMonth), SwingConstants.CENTER);

        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentMonth = currentMonth.minusMonths(1);
                updateMonth();
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentMonth = currentMonth.plusMonths(1);
                updateMonth();
            }
        });

        topPanel.add(prevButton);
        topPanel.add(monthLabel);
        topPanel.add(nextButton);
        add(topPanel, BorderLayout.NORTH);

        // 각 차트 패널 생성 및 레이아웃 설정
        JTabbedPane tabbedPane = new JTabbedPane();

        workoutPanel = new WorkoutChartPanel();
        volumePanel = new VolumeChartPanel();
        nutritionPanel = new NutritionChartPanel();

        tabbedPane.addTab("운동량 통계", workoutPanel);
        tabbedPane.addTab("부위별 볼륨 통계", volumePanel);
        tabbedPane.addTab("영양소 통계", nutritionPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // 초기 데이터 설정
        updatePanels(currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM")));
    }

    private void initMonthlyData() {
        // 예제 데이터 초기화
        workoutDataMap = new HashMap<>();
        workoutDataMap.put("2024-03", new int[]{300, 250, 200});
        workoutDataMap.put("2024-04", new int[]{400, 350, 300});
        workoutDataMap.put("2024-05", new int[]{450, 400, 350});

        volumeDataMap = new HashMap<>();
        volumeDataMap.put("2024-03", new int[]{1200, 800, 1000, 1500});
        volumeDataMap.put("2024-04", new int[]{1300, 900, 1100, 1600});
        volumeDataMap.put("2024-05", new int[]{1400, 1000, 1200, 1700});

        nutritionDataMap = new HashMap<>();
        nutritionDataMap.put("2024-03", new int[]{50, 30, 20});
        nutritionDataMap.put("2024-04", new int[]{45, 35, 20});
        nutritionDataMap.put("2024-05", new int[]{40, 40, 20});
    }

    private void updateMonth() {
        // 월 표시 업데이트
        monthLabel.setText(formatMonth(currentMonth));
        String monthKey = currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        updatePanels(monthKey);
    }

    private void updatePanels(String month) {
        // 선택된 월에 해당하는 데이터를 차트에 반영
        workoutPanel.setWorkoutData(workoutDataMap.getOrDefault(month, new int[]{0, 0, 0}));
        volumePanel.setVolumeData(volumeDataMap.getOrDefault(month, new int[]{0, 0, 0, 0}));
        nutritionPanel.setNutritionData(nutritionDataMap.getOrDefault(month, new int[]{0, 0, 0}));

        workoutPanel.repaint();
        volumePanel.repaint();
        nutritionPanel.repaint();
    }

    private String formatMonth(LocalDate date) {
        // "YYYY년 MM월" 형식으로 월을 표시
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월");
        return date.format(formatter);
    }

    // 운동량 차트 패널
    class WorkoutChartPanel extends JPanel {
        private int[] workoutData = {0, 0, 0};

        public void setWorkoutData(int[] data) {
            workoutData = data;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            String[] labels = {"첫째 주", "둘째 주", "셋째 주"};
            int max = 500;

            int width = getWidth() - 40;
            int height = getHeight() - 40;
            int originX = 50;
            int originY = height + 20;

            // Y축
            g.drawLine(originX, originY, originX, 20);
            g.drawString("운동 시간 (분)", originX - 40, 20);

            // X축
            g.drawLine(originX, originY, width + 10, originY);

            // 데이터 표시
            int prevX = originX, prevY = originY - (workoutData[0] * height / max);
            for (int i = 0; i < workoutData.length; i++) {
                int x = originX + (i * width / (workoutData.length - 1));
                int y = originY - (workoutData[i] * height / max);
                g.fillOval(x - 3, y - 3, 6, 6);
                g.drawString(labels[i], x - 10, originY + 15);
                g.drawLine(prevX, prevY, x, y);
                prevX = x;
                prevY = y;
            }
        }
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
            String[] labels = {"가슴", "등", "하체", "총 볼륨"};
            int max = 2000;

            int width = getWidth() - 40;
            int height = getHeight() - 40;
            int originX = 50;
            int originY = height + 20;

            // Y축
            g.drawLine(originX, originY, originX, 20);
            g.drawString("볼륨 (kg)", originX - 40, 20);

            // X축
            g.drawLine(originX, originY, width + 10, originY);

            // 막대 그래프
            int barWidth = (width - 20) / volumeData.length;
            for (int i = 0; i < volumeData.length; i++) {
                int barHeight = volumeData[i] * height / max;
                int x = originX + (i * barWidth) + 10;
                int y = originY - barHeight;
                g.setColor(new Color(100, 150, 240));
                g.fillRect(x, y, barWidth - 10, barHeight);
                g.setColor(Color.BLACK);
                g.drawString(labels[i], x + 5, originY + 15);
                g.drawString(volumeData[i] + " kg", x + 5, y - 5);
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
