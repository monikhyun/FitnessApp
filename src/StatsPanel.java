package JavaProjects;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Calendar;

public class StatsPanel extends JPanel {
    private JLabel workoutStatLabel;
    private JTable volumeTable;
    private JTable nutritionTable;
    private DefaultTableModel volumeTableModel;
    private DefaultTableModel nutritionTableModel;
    private JLabel monthLabel;
    private Calendar currentMonth;
    private String loginedid, loginedpass;

    Connection conn;

    public StatsPanel(String loginedid, String loginedpass, Connection conn) {
        this.loginedid = loginedid;
        this.loginedpass = loginedpass;
        this.conn = conn;
        this.currentMonth = Calendar.getInstance(); // 현재 날짜와 시간을 가져옴
        this.currentMonth.set(Calendar.DAY_OF_MONTH, 1); // 해당 달의 첫 번째 날로 설정

        setLayout(new BorderLayout());

        // 상단 패널에 월 표시와 좌우 버튼 추가
        JPanel topPanel = new JPanel();
        JButton prevButton = new JButton("<");
        JButton nextButton = new JButton(">");
        monthLabel = new JLabel(formatMonth(currentMonth), SwingConstants.CENTER);

        prevButton.addActionListener(e -> {
            currentMonth.add(Calendar.MONTH, -1); // 이전 달
            updateMonth();
        });

        nextButton.addActionListener(e -> {
            currentMonth.add(Calendar.MONTH, 1); // 다음 달
            updateMonth();
        });

        topPanel.add(prevButton);
        topPanel.add(monthLabel);
        topPanel.add(nextButton);
        add(topPanel, BorderLayout.NORTH);

        // 중앙에 운동량 및 영양소 테이블
        JPanel centerPanel = new JPanel(new GridLayout(3, 1));

        // 운동량 통계 구간
        JPanel workoutStatPanel = new JPanel();
        workoutStatPanel.setBorder(BorderFactory.createTitledBorder("운동 횟수"));
        workoutStatLabel = new JLabel();
        workoutStatPanel.add(workoutStatLabel);
        centerPanel.add(workoutStatPanel);

        // 운동 부위별 볼륨 테이블
        JPanel volumePanel = new JPanel(new BorderLayout());
        volumePanel.setBorder(BorderFactory.createTitledBorder("운동 볼륨 통계"));
        String[] volumeColumns = {"부위", "이번 달 총 볼륨 (kg)", "저번 달 총 볼륨 (kg)"};
        volumeTableModel = new DefaultTableModel(volumeColumns, 0);
        volumeTable = new JTable(volumeTableModel);
        volumeTable.setRowHeight(30);
        volumePanel.add(new JScrollPane(volumeTable), BorderLayout.CENTER);
        centerPanel.add(volumePanel);

        // 영양소 섭취량 테이블
        JPanel nutritionPanel = new JPanel(new BorderLayout());
        nutritionPanel.setBorder(BorderFactory.createTitledBorder("영양소 섭취 통계"));
        String[] nutritionColumns = {"영양소", "이번 달 섭취량 (g)", "저번 달 섭취량 (g)"   };
        nutritionTableModel = new DefaultTableModel(nutritionColumns, 0);
        nutritionTable = new JTable(nutritionTableModel);
        nutritionTable.setRowHeight(30);
        nutritionPanel.add(new JScrollPane(nutritionTable), BorderLayout.CENTER);
        centerPanel.add(nutritionPanel);

        add(centerPanel, BorderLayout.CENTER);

        // 데이터 초기화
        updateStatsFromDatabase();
    }

    private void updateMonth() {
        monthLabel.setText(formatMonth(currentMonth));
        updateStatsFromDatabase();
    }

    private String formatMonth(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH는 0부터 시작
        return String.format("%d년 %02d월", year, month); // 형식: "YYYY년 MM월"
    }

    public void updateStatsFromDatabase() {
        try {
            // 운동량 데이터 가져오기
            String workoutQuery = """
                SELECT 
                    COUNT(*) AS CurrentMonthCount,
                    (SELECT COUNT(*) FROM UserExec WHERE MONTH(Date) = ? AND YEAR(Date) = ? AND Userid = ?) AS LastMonthCount
                FROM UserExec
                WHERE MONTH(Date) = ? AND YEAR(Date) = ? AND Userid = ?;
            """;

            try (PreparedStatement stmt = conn.prepareStatement(workoutQuery)) {
                stmt.setInt(1, currentMonth.get(Calendar.MONTH));
                stmt.setInt(2, currentMonth.get(Calendar.YEAR));
                stmt.setString(3, loginedid);
                stmt.setInt(4, currentMonth.get(Calendar.MONTH) + 1); // 이번 달
                stmt.setInt(5, currentMonth.get(Calendar.YEAR));
                stmt.setString(6, loginedid);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int currentMonthCount = rs.getInt("CurrentMonthCount");
                        int lastMonthCount = rs.getInt("LastMonthCount");
                        workoutStatLabel.setText(String.format(
                                "이번 달 %d회 운동했어요. 지난 달보다 %d회 %s 운동했어요.",
                                currentMonthCount,
                                Math.abs(currentMonthCount - lastMonthCount),
                                currentMonthCount >= lastMonthCount ? "더" : "덜"
                        ));
                    }
                }
            }

            // 부위별 볼륨 데이터 가져오기
            String volumeQuery = """
    SELECT '전체' AS Category, SUM(TotalVolume) AS TotalVolume
    FROM UserExec UE
    JOIN Exec E ON UE.Execid = E.Execid
    WHERE MONTH(UE.Date) = ? AND YEAR(UE.Date) = ? AND UE.Userid = ?
    UNION ALL 
    SELECT '등', SUM(TotalVolume)
    FROM UserExec UE
    JOIN Exec E ON UE.Execid = E.Execid
    WHERE MONTH(UE.Date) = ? AND YEAR(UE.Date) = ? AND UE.Userid = ? AND Category = 'Back'
    UNION ALL
    SELECT '가슴', SUM(TotalVolume)
    FROM UserExec UE
    JOIN Exec E ON UE.Execid = E.Execid
    WHERE MONTH(UE.Date) = ? AND YEAR(UE.Date) = ? AND UE.Userid = ? AND Category = 'Chest'
    UNION ALL
    SELECT '어깨', SUM(TotalVolume)
    FROM UserExec UE
    JOIN Exec E ON UE.Execid = E.Execid
    WHERE MONTH(UE.Date) = ? AND YEAR(UE.Date) = ? AND UE.Userid = ? AND Category = 'Shoulder'
    UNION ALL
    SELECT '하체', SUM(TotalVolume)
    FROM UserExec UE
    JOIN Exec E ON UE.Execid = E.Execid
    WHERE MONTH(UE.Date) = ? AND YEAR(UE.Date) = ? AND UE.Userid = ? AND Category = 'Lower-body';
""";

            try (PreparedStatement stmt = conn.prepareStatement(volumeQuery)) {
                for (int i = 1; i <= 15; i += 3) {
                    stmt.setInt(i, currentMonth.get(Calendar.MONTH) + 1);
                    stmt.setInt(i + 1, currentMonth.get(Calendar.YEAR));
                    stmt.setString(i + 2, loginedid);
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    volumeTableModel.setRowCount(0); // 기존 데이터 초기화
                    while (rs.next()) {
                        String category = rs.getString("Category");
                        int totalVolume = rs.getInt("TotalVolume");
                        volumeTableModel.addRow(new Object[]{category, totalVolume == 0 ? 0 : totalVolume, 0});
                        // totalVolume이 0인지 확인 후 참이면 0, 거짓이면 totalVolume을 그대로 사용
                    }
                }
            }

            // 영양소 섭취량 데이터 가져오기
            String nutritionQuery = """
                SELECT '단백질' AS Nutrient, SUM(TotalProtein) AS Total
                FROM NutritionSummary WHERE MONTH(Date) = ? AND YEAR(Date) = ? AND Userid = ?
                UNION ALL
                SELECT '탄수화물', SUM(TotalCarbo)
                FROM NutritionSummary WHERE MONTH(Date) = ? AND YEAR(Date) = ? AND Userid = ?
                UNION ALL
                SELECT '지방', SUM(TotalFat)
                FROM NutritionSummary WHERE MONTH(Date) = ? AND YEAR(Date) = ? AND Userid = ?;
            """;

            try (PreparedStatement stmt = conn.prepareStatement(nutritionQuery)) {
                stmt.setInt(1, currentMonth.get(Calendar.MONTH) + 1);
                stmt.setInt(2, currentMonth.get(Calendar.YEAR));
                stmt.setString(3, loginedid);
                stmt.setInt(4, currentMonth.get(Calendar.MONTH) + 1);
                stmt.setInt(5, currentMonth.get(Calendar.YEAR));
                stmt.setString(6, loginedid);
                stmt.setInt(7, currentMonth.get(Calendar.MONTH) + 1);
                stmt.setInt(8, currentMonth.get(Calendar.YEAR));
                stmt.setString(9, loginedid);

                try (ResultSet rs = stmt.executeQuery()) {
                    nutritionTableModel.setRowCount(0); // 기존 데이터 초기화
                    while (rs.next()) {
                        String nutrient = rs.getString("Nutrient");
                        int total = rs.getInt("Total");
                        nutritionTableModel.addRow(new Object[]{nutrient, total, 0});
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "데이터를 불러오는 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}