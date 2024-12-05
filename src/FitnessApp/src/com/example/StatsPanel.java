package com.example;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;


public class StatsPanel extends JPanel {   // flowLayout이 적용된거임
    private JLabel workoutStatLabel;
    private  Connection conn;
    private String loginedid;
    private JTable volumeTable;
    private JTable nutritionTable;
    private DefaultTableModel volumeTableModel;
    private DefaultTableModel nutritionTableModel;

    public StatsPanel(String loginedid,Connection conn) {
        this.loginedid = loginedid;
        this.conn = conn;
        setLayout(new GridLayout(3, 1)); // 세 개의 구간으로 나눔

        // 운동량 통계 구간
        JPanel workoutStatPanel = new JPanel();
        workoutStatPanel.setBorder(BorderFactory.createTitledBorder("운동 횟수"));
        workoutStatLabel = new JLabel();
        workoutStatPanel.add(workoutStatLabel);
        add(workoutStatPanel);


        // 운동 부위별 볼륨 테이블
        JPanel volumePanel = new JPanel(new BorderLayout());
        volumePanel.setBorder(BorderFactory.createTitledBorder("운동 볼륨 통계"));
        String[] volumeColumns = {"부위", "총 볼륨 (kg)"};
        String[] volumeRows = {"전체", "등", "가슴", "어깨", "하체"};
        volumeTableModel = new DefaultTableModel(volumeColumns, 0);
        volumeTable = new JTable(volumeTableModel);
        volumePanel.add(new JScrollPane(volumeTable), BorderLayout.CENTER);
        add(volumePanel);


        // 영양소 섭취량 테이블
        JPanel nutritionPanel = new JPanel(new BorderLayout());
        nutritionPanel.setBorder(BorderFactory.createTitledBorder("영양소 섭취 통계"));
        String[] nutritionColumns = {"영양소", "섭취량 (g)"};
        String[] nutritionRows = {"단백질", "탄수화물", "지방"};
        nutritionTableModel = new DefaultTableModel(nutritionColumns, 0);
        nutritionTable = new JTable(nutritionTableModel);
        nutritionPanel.add(new JScrollPane(nutritionTable), BorderLayout.CENTER);
        add(nutritionPanel);

        // 데이터 초기화
        initializeVolumeTable(volumeRows);
        initializeNutritionTable(nutritionRows);

        // 데이터 갱신
        updateStatsFromDatabase();
    }
    private void initializeVolumeTable(String[] rows) {
        for (String row : rows) {
            volumeTableModel.addRow(new Object[]{row, ""});
        }
    }

    private void initializeNutritionTable(String[] rows) {
        for (String row : rows) {
            nutritionTableModel.addRow(new Object[]{row, ""});
        }
    }

    public void updateStatsFromDatabase() {
        try {

            // 운동량 데이터 가져오기
            String workoutQuery = """
                SELECT 
                    COUNT(*) AS CurrentMonthCount,
                    (SELECT COUNT(*) FROM UserExec WHERE MONTH(RecordDate) = MONTH(CURDATE()) - 1 AND Userid = ?) AS LastMonthCount
                FROM UserExec
                WHERE MONTH(RecordDate) = MONTH(CURDATE()) AND Userid = ?;
            """;
            try (PreparedStatement stmt = conn.prepareStatement(workoutQuery)) {
                stmt.setString(1, loginedid);
                stmt.setString(2, loginedid);
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
            // 부위별 볼륨 데이터 갱신
            String volumeQuery = """
                SELECT Category, SUM(TotalVolume) AS TotalVolume
                FROM UserExec UE
                JOIN Exec E ON UE.Execid = E.Execid
                WHERE MONTH(UE.RecordDate) = MONTH(CURDATE()) AND UE.Userid = ?
                GROUP BY Category WITH ROLLUP;
            """;
            try (PreparedStatement stmt = conn.prepareStatement(volumeQuery)) {
                stmt.setString(1, loginedid);
                try (ResultSet rs = stmt.executeQuery()) {
                    int rowIndex = 0;
                    while (rs.next()) {
                        String category = rs.getString("Category") != null ? rs.getString("Category") : "전체";
                        int totalVolume = rs.getInt("TotalVolume");
                        volumeTableModel.setValueAt(totalVolume, rowIndex++, 1);
                    }
                }
            }

            // 영양소 섭취량 데이터 갱신
            String nutritionQuery = """
                SELECT '단백질' AS Nutrient, SUM(TotalProtein) AS Total
                FROM NutritionSummary WHERE MONTH(RecordDate) = MONTH(CURDATE()) AND Userid = ?
                UNION ALL
                SELECT '탄수화물', SUM(TotalCarbo)
                FROM NutritionSummary WHERE MONTH(RecordDate) = MONTH(CURDATE()) AND Userid = ?
                UNION ALL
                SELECT '지방', SUM(TotalFat)
                FROM NutritionSummary WHERE MONTH(RecordDate) = MONTH(CURDATE()) AND Userid = ?;
            """;
            try (PreparedStatement stmt = conn.prepareStatement(nutritionQuery)) {
                stmt.setString(1, loginedid);
                stmt.setString(2, loginedid);
                stmt.setString(3, loginedid);
                try (ResultSet rs = stmt.executeQuery()) {
                    int rowIndex = 0;
                    while (rs.next()) {
                        String nutrient = rs.getString("Nutrient");
                        int total = rs.getInt("Total");
                        nutritionTableModel.setValueAt(total, rowIndex++, 1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "데이터를 불러오는 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

}
