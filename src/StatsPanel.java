
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class StatsPanel extends JPanel {
    private JLabel workoutStatLabel;
    private JLabel volumeStatLabel;
    private JLabel nutritionStatLabel;

    private JComboBox<String> volumeComboBox;
    private JComboBox<String> nutritionComboBox;

    public StatsPanel() {
        setLayout(new GridLayout(3, 1)); // 두 개의 구간으로 나눔

        // 운동량 통계 구간
        JPanel workoutStatPanel = new JPanel();
        workoutStatPanel.setBorder(BorderFactory.createTitledBorder("운동 횟수"));
        workoutStatLabel = new JLabel("데이터를 불러오는 중...", SwingConstants.LEFT);
        workoutStatPanel.add(workoutStatLabel);
        add(workoutStatPanel);

        // 부위별 볼륨 통계 구간
        JPanel volumeStatPanel = new JPanel();
        volumeStatPanel.setBorder(BorderFactory.createTitledBorder("볼륨 추이"));
        volumeComboBox = new JComboBox<>(new String[]{"전체", "등", "가슴", "어깨", "하체"});
        volumeStatLabel = new JLabel("데이터를 불러오는 중...", SwingConstants.LEFT);
        volumeStatPanel.add(volumeComboBox, BorderLayout.NORTH);
        volumeStatPanel.add(volumeStatLabel, BorderLayout.CENTER);
        add(volumeStatPanel);

        // 영양소 통계 구간
        JPanel nutritionStatPanel = new JPanel();
        nutritionStatPanel.setBorder(BorderFactory.createTitledBorder("영양소 추이"));
        nutritionComboBox = new JComboBox<>(new String[]{"단백질", "탄수화물", "지방"});
        nutritionStatLabel = new JLabel("데이터를 불러오는 중...", SwingConstants.LEFT);
        nutritionStatPanel.add(nutritionComboBox, BorderLayout.NORTH);
        nutritionStatPanel.add(nutritionStatLabel, BorderLayout.CENTER);
        add(nutritionStatPanel);

        volumeComboBox.addActionListener(e -> {
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://fitnessapp.chqw04eu8yfk.ap-southeast-2.rds.amazonaws.com:3306/fitnessapp",
                    "mih", "ansxoddl123")) {
                updateVolumeStats(conn);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "볼륨 데이터를 불러오는 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });
        nutritionComboBox.addActionListener(e -> {
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://fitnessapp.chqw04eu8yfk.ap-southeast-2.rds.amazonaws.com:3306/fitnessapp",
                    "mih", "ansxoddl123")) {
                updateVolumeStats(conn);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "볼륨 데이터를 불러오는 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });
    }


    public void updateStatsFromDatabase() {

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://fitnessapp.chqw04eu8yfk.ap-southeast-2.rds.amazonaws.com:3306/fitnessapp",
                "mih", "ansxoddl123")) {
            // 운동량 데이터 가져오기
            String workoutQuery = """
                SELECT 
                    COUNT(*) AS CurrentMonthCount,
                    (SELECT COUNT(*) FROM UserExec WHERE MONTH(Date) = MONTH(CURDATE()) - 1 AND Userid = ?) AS LastMonthCount
                FROM UserExec
                WHERE MONTH(Date) = MONTH(CURDATE()) AND Userid = ?;
            """;
            try (PreparedStatement stmt = conn.prepareStatement(workoutQuery)) {
                stmt.setString(1, "user1");
                stmt.setString(2, "user1");
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
            updateVolumeStats(conn);

            // 영양소 데이터 가져오기
            updateNutritionStats(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "데이터를 불러오는 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateVolumeStats(Connection conn) {
        try {
            String selectedCategory = (String) volumeComboBox.getSelectedItem();
            String categoryCondition = "전체".equals(selectedCategory) ? "" : " AND Category = ?";
            String volumeQuery = """
                SELECT SUM(Totalvolume) AS CurrentVolume
                FROM UserExec
                WHERE MONTH(Date) = MONTH(CURDATE()) AND Userid = ?
                """ + categoryCondition;
            try (PreparedStatement stmt = conn.prepareStatement(volumeQuery)) {
                stmt.setString(1, "user1");
                if (!"전체".equals(selectedCategory)) {
                    stmt.setString(2, selectedCategory);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int currentVolume = rs.getInt("CurrentVolume");
                        volumeStatLabel.setText(String.format(
                                "%s 부위 총 %dkg 들었어요.",
                                selectedCategory, currentVolume
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void updateNutritionStats(Connection conn) {
        try {
            String selectedNutrient = (String) nutritionComboBox.getSelectedItem();
            String nutrientField = switch (selectedNutrient) {
                case "단백질" -> "TotalProtein";
                case "탄수화물" -> "TotalCarbo";
                case "지방" -> "TotalFat";
                default -> "TotalProtein";
            };

            String nutritionQuery = String.format("""
                SELECT SUM(%s) AS CurrentNutrition
                FROM NutritionSummary
                WHERE MONTH(Date) = MONTH(CURDATE()) AND Userid = ?
            """, nutrientField);

            try (PreparedStatement stmt = conn.prepareStatement(nutritionQuery)) {
                stmt.setString(1, "user1");
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int currentNutrition = rs.getInt("CurrentNutrition");
                        nutritionStatLabel.setText(String.format(
                                "이번 달 총 %dg 섭취했어요.",
                                currentNutrition
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {

        JFrame frame = new JFrame("통계 화면");
        StatsPanel statsPanel = new StatsPanel();

        statsPanel.updateStatsFromDatabase();

        frame.add(statsPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setVisible(true);
    }
}
