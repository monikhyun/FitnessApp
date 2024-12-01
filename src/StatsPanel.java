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
    private Calendar currentMonth, lastMonth;
    private String loginedid, loginedpass;

    Connection conn;

    public StatsPanel(String loginedid, String loginedpass, Connection conn) {
        this.loginedid = loginedid;
        this.loginedpass = loginedpass;
        this.conn = conn;
        currentMonth = Calendar.getInstance(); // 현재 날짜와 시간을 가져옴
        currentMonth.set(Calendar.DAY_OF_MONTH, 1);// 해당 달의 첫 번째 날로 설정

        lastMonth = Calendar.getInstance();
        lastMonth.set(Calendar.DAY_OF_MONTH, -1);

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
        String[] volumeColumns = {"부위", "이번 달 총 볼륨 (kg)", "저번 달 총 볼륨 (kg)", "상태"};
        volumeTableModel = new DefaultTableModel(volumeColumns, 0);
        volumeTable = new JTable(volumeTableModel);
        volumeTable.setRowHeight(30);
        volumePanel.add(new JScrollPane(volumeTable), BorderLayout.CENTER);
        centerPanel.add(volumePanel);

        // 영양소 섭취량 테이블
        JPanel nutritionPanel = new JPanel(new BorderLayout());
        nutritionPanel.setBorder(BorderFactory.createTitledBorder("영양소 섭취 통계"));
        String[] nutritionColumns = {"영양소", "이번 달 섭취량 (g)", "저번 달 섭취량 (g)", "상태"};
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
        lastMonth = (Calendar) currentMonth.clone();
        lastMonth.add(Calendar.MONTH, -1);
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
                SELECT COUNT(*) AS CurrentMonthCount,
                    (SELECT COUNT(*) FROM UserExec WHERE MONTH(RecordDate) = ? AND YEAR(RecordDate) = ? AND Userid = ?) AS LastMonthCount
                FROM UserExec
                WHERE MONTH(RecordDate) = ? AND YEAR(RecordDate) = ? AND Userid = ?;
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
                                currentMonthCount >  lastMonthCount ? "더" : "덜"
                        ));
                    }
                }
            }

            String volumeQuery = """
            SELECT 
                CASE 
                    WHEN E.Category = 'Back' THEN '등'
                    WHEN E.Category = 'Chest' THEN '가슴'
                    WHEN E.Category = 'Shoulder' THEN '어깨'
                    WHEN E.Category = 'Lower-body' THEN '하체'
                    ELSE E.Category
                END AS Category,
                SUM(CASE WHEN MONTH(UE.RecordDate) = ? AND YEAR(UE.RecordDate) = ? THEN UE.TotalVolume ELSE 0 END) AS CurrentMonthVolume,
                SUM(CASE WHEN MONTH(UE.RecordDate) = ? AND YEAR(UE.RecordDate) = ? THEN UE.TotalVolume ELSE 0 END) AS LastMonthVolume
            FROM Exec E
            LEFT JOIN UserExec UE ON E.Execid = UE.Execid AND UE.Userid = ?
            WHERE E.Category IN ('Back', 'Chest', 'Shoulder', 'Lower-body')
            GROUP BY E.Category
            UNION ALL
            SELECT 
                '전체' AS Category,
                SUM(CASE WHEN MONTH(UE.RecordDate) = ? AND YEAR(UE.RecordDate) = ? THEN UE.TotalVolume ELSE 0 END) AS CurrentMonthVolume,
                SUM(CASE WHEN MONTH(UE.RecordDate) = ? AND YEAR(UE.RecordDate) = ? THEN UE.TotalVolume ELSE 0 END) AS LastMonthVolume
            FROM UserExec UE
            JOIN Exec E ON UE.Execid = E.Execid
            WHERE UE.Userid = ?;
            """;

            try (PreparedStatement stmt = conn.prepareStatement(volumeQuery)) {
                // 첫 번째 세트: 이번 달과 지난 달의 데이터를 위한 파라미터
                stmt.setInt(1, currentMonth.get(Calendar.MONTH) + 1); // 이번 달 월
                stmt.setInt(2, currentMonth.get(Calendar.YEAR));       // 이번 달 연도
                stmt.setInt(3, lastMonth.get(Calendar.MONTH) + 1);     // 지난 달 월
                stmt.setInt(4, lastMonth.get(Calendar.YEAR));           // 지난 달 연도

                stmt.setString(5, loginedid); // 로그인된 사용자 ID

                // 두 번째 세트: '전체' 카테고리 데이터를 위한 파라미터
                stmt.setInt(6, currentMonth.get(Calendar.MONTH) + 1); // 이번 달 월
                stmt.setInt(7, currentMonth.get(Calendar.YEAR));       // 이번 달 연도
                stmt.setInt(8, lastMonth.get(Calendar.MONTH) + 1);     // 지난 달 월
                stmt.setInt(9, lastMonth.get(Calendar.YEAR));           // 지난 달 연도

                stmt.setString(10, loginedid); // 로그인된 사용자 ID

                try (ResultSet rs = stmt.executeQuery()) {
                    volumeTableModel.setRowCount(0); // 기존 데이터 초기화
                    while (rs.next()) {
                        String category = rs.getString("Category");
                        int currentMonthVolume = rs.getInt("CurrentMonthVolume");
                        int lastMonthVolume = rs.getInt("LastMonthVolume");
                        // 상태 계산
                        String status = "잘 유지중이에요!";
                        if (currentMonthVolume > lastMonthVolume) {
                            status = "아주 좋아요!! 잘 하고 있어요";
                        } else if (currentMonthVolume < lastMonthVolume) {
                            status = "아쉬워요.. 조금 더 열심히 해봐요";
                        }
                        // 데이터 행 추가
                        volumeTableModel.addRow(new Object[]{category, currentMonthVolume, lastMonthVolume, status});
                    }
                }
            }

            // 영양소 섭취량 데이터 가져오기
            String nutritionQuery = """
                SELECT '단백질' AS Nutrient, 
                       SUM(CASE WHEN MONTH(Date) = ? AND YEAR(Date) = ? THEN TotalProtein ELSE 0 END) AS CurrentMonthTotal,
                       SUM(CASE WHEN MONTH(Date) = ? AND YEAR(Date) = ? THEN TotalProtein ELSE 0 END) AS LastMonthTotal
                FROM NutritionSummary WHERE Userid = ?
                UNION ALL
                SELECT '탄수화물', 
                       SUM(CASE WHEN MONTH(Date) = ? AND YEAR(Date) = ? THEN TotalCarbo ELSE 0 END),
                       SUM(CASE WHEN MONTH(Date) = ? AND YEAR(Date) = ? THEN TotalCarbo ELSE 0 END)
                FROM NutritionSummary WHERE Userid = ?
                UNION ALL
                SELECT '지방', 
                       SUM(CASE WHEN MONTH(Date) = ? AND YEAR(Date) = ? THEN TotalFat ELSE 0 END),
                       SUM(CASE WHEN MONTH(Date) = ? AND YEAR(Date) = ? THEN TotalFat ELSE 0 END)
                FROM NutritionSummary WHERE Userid = ?;
            """;

            try (PreparedStatement stmt = conn.prepareStatement(nutritionQuery)) {

                stmt.setInt(1, currentMonth.get(Calendar.MONTH) + 1);
                stmt.setInt(2, currentMonth.get(Calendar.YEAR));
                stmt.setInt(3, lastMonth.get(Calendar.MONTH) + 1);
                stmt.setInt(4, lastMonth.get(Calendar.YEAR));
                stmt.setString(5, loginedid);

                stmt.setInt(6, currentMonth.get(Calendar.MONTH) + 1);
                stmt.setInt(7, currentMonth.get(Calendar.YEAR));
                stmt.setInt(8, lastMonth.get(Calendar.MONTH) + 1);
                stmt.setInt(9, lastMonth.get(Calendar.YEAR));
                stmt.setString(10, loginedid);

                stmt.setInt(11, currentMonth.get(Calendar.MONTH) + 1);
                stmt.setInt(12, currentMonth.get(Calendar.YEAR));
                stmt.setInt(13, lastMonth.get(Calendar.MONTH) + 1);
                stmt.setInt(14, lastMonth.get(Calendar.YEAR));
                stmt.setString(15, loginedid);

                try (ResultSet rs = stmt.executeQuery()) {
                    nutritionTableModel.setRowCount(0); // 기존 데이터 초기화
                    while (rs.next()) {
                        String nutrient = rs.getString("Nutrient");
                        int currentMonthTotal = rs.getInt("CurrentMonthTotal");
                        int lastMonthTotal = rs.getInt("LastMonthTotal");
                        // 상태 계산
                        String status = "잘 유지중이에요";

                        // 유저의 목표에 따른 상태 변화
                        String TypeQuery = """
                        SELECT GoalType, Weight
                        FROM User
                        WHERE Userid = ?;
                        """;
                        try (PreparedStatement stmt2 = conn.prepareStatement(TypeQuery)) {
                            stmt2.setString(1, loginedid);
                            try (ResultSet rs2 = stmt2.executeQuery()) {
                                while (rs2.next()) {
                                    String goalType = rs2.getString("GoalType");
                                    int weight = rs2.getInt("Weight");

                                    // 단백질, 탄수화물, 지방 기준 설정
                                    int proteinThreshold = 0; int carboThreshold = 0; int fatThreshold = 0;

                                    // 목표에 따른 섭취 기준 설정
                                    if (goalType.equals("다이어트")) {
                                        // 다이어트의 경우 몸무게 1kg당 단백질 1g, 탄수화물 2g, 지방 0.8g 권장
                                        proteinThreshold = weight * 1; // 단백질
                                        carboThreshold = weight * 2;  // 탄수화물
                                        fatThreshold = weight * 1;    // 지방
                                    } else { // 목표가 벌크업
                                        // 벌크업의 경우 몸무게 1kg당 단백질 2g, 탄수화물 4g, 지방 1g 권장
                                        proteinThreshold = weight * 2; // 단백질
                                        carboThreshold = weight * 4;   // 탄수화물
                                        fatThreshold = weight * 1;     // 지방
                                    }
                                    // 각 영양소별 상태 설정
                                    switch (nutrient) {
                                        case "단백질":
                                            if (proteinThreshold <= currentMonthTotal && currentMonthTotal <= proteinThreshold + 20)
                                                status = "단백질 섭취를 아주 잘하고 있어요!!";
                                            else if (currentMonthTotal < proteinThreshold)
                                                status = "단백질을 너무 적게 먹었어요! ";
                                            else
                                                status = "단백질을 과도 섭취했어요! ";
                                            break;
                                        case "탄수화물":
                                            if (carboThreshold - 10 <= currentMonthTotal && currentMonthTotal <= carboThreshold + 10)
                                                status = "탄수화물 섭취를 아주 잘하고 있어요!!";
                                            else if (currentMonthTotal < carboThreshold)
                                                status = "탄수화물을 너무 적게 먹었어요!";
                                            else
                                                status = "탄수화물을 과도 섭취했어요!";
                                            break;
                                        case "지방":
                                            if (fatThreshold - 10 <= currentMonthTotal && currentMonthTotal <= fatThreshold + 10)
                                                status = "지방 섭취를 아주 잘하고 있어요!!";
                                            else if (currentMonthTotal < fatThreshold)
                                                status = "지방 섭취를 너무 적게 먹었어요!";
                                            else
                                                status = "지방을 과도 섭취했어요!";
                                            break;
                                    }
                                }
                            }
                        }
                        nutritionTableModel.addRow(new Object[]{nutrient, currentMonthTotal, lastMonthTotal, status});
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "데이터를 불러오는 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }
}