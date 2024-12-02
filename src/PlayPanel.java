package JavaProject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.*;

public class PlayPanel extends JPanel {

    private String loginedid;
    private JPanel plantPanel, achievementPanel, achievementLatePanel, lateGraphPanel;
    private ImageIcon plantIcon;
    private JLabel plantLabel, achievementLabel, currentLabel, nextLabel;
    private int totalCom, currentCom, nextCom;
    private JButton update;

    public PlayPanel(String loginedid, String loginedpass,Connection con) {
        this.loginedid = loginedid;
        setLayout(new GridLayout(1, 2));

        // 데이터베이스 연결을 try-with-resources로 안전하게 관리
        try {
            initializeComponents(con);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "데이터베이스 연결 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 왼쪽 성장식물 패널
    private void initializeComponents(Connection con) throws SQLException {
        // 식물 패널 초기화
        plantPanel = new JPanel(new BorderLayout());
        plantPanel.setBorder(BorderFactory.createTitledBorder("성장 Plant"));
        plantLabel = new JLabel();
        plantPanel.add(plantLabel);
        add(plantPanel);

        // 성과 패널 초기화
        achievementPanel = new JPanel();
        achievementPanel.setLayout(null);
        achievementPanel.setBorder(BorderFactory.createTitledBorder("성장률"));

        add(achievementPanel);

        // 총 Complete 계산
        totalCom = calculateTotalComplet(con);
        // 총 Complete forComplete 릴레이션에 저장
        insertotalCom(con, totalCom);
        // 식물 등급 및 이미지 설정
        int grade = determineGrade(totalCom);
        plantIcon = getPlantIcon(con, grade);
        // null인지 검사
        if (plantIcon != null) {
            plantLabel.setIcon(plantIcon);
        }

        // 현재 및 다음 Complement 설정
        currentCom = getCurrentComplet(con);
        nextCom = getNextComplet(currentCom);

        // 성과 표시 패널 설정
        setupAchievementPanel(con);
    }

    private int calculateTotalComplet(Connection con) throws SQLException {
        String sumSql = "SELECT SUM(Complete) FROM UserExec WHERE Userid = ?";
        try (PreparedStatement ps = con.prepareStatement(sumSql)) {
            ps.setString(1, loginedid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    // forComplete에 totalCom저장
    private void insertotalCom(Connection con, int totalCom) {
        String sql = "UPDATE forComplete SET totalCom = ? WHERE Userid = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, totalCom);
            ps.setString(2, loginedid);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // 등급나누기
    private int determineGrade(int totalCom) {
        if (totalCom <= 5) return 1;
        else if (totalCom <= 10) return 2;
        else if (totalCom <= 15) return 3;
        return 4; // 50 이상일 경우
    }

    // 등급에 맞는 이미지 가져오기
    private ImageIcon getPlantIcon(Connection con, int grade) throws SQLException {
        String imageSql = "SELECT image FROM Play_images WHERE Grade = ?";

        try (PreparedStatement ps = con.prepareStatement(imageSql)) {
            ps.setInt(1, grade);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String address = rs.getString("image");
                    return new ImageIcon(address);
                }
            }
        }
        return null;
    }

    // 현재 Complete 총량 가져오기
    private int getCurrentComplet(Connection con) throws SQLException {
        String sql = "SELECT totalCom FROM forComplete WHERE Userid = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, loginedid);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("totalCom");
                }
            }
        }
        return 0;
    }

    private int getNextComplet(int currentCom) {
        if (currentCom < 100) return 100;
        if (currentCom < 500) return 500;
        if (currentCom < 900) return 900;
        return 1000; // 다음 단계가 없을 경우
    }

    // 오른쪽 달성률 패널
    private void setupAchievementPanel(Connection con) throws SQLException {
        achievementLatePanel = new JPanel(new BorderLayout());

        // achievementPanel 이 null이니 배치를 위한 Bounds
        achievementLatePanel.setBounds(70,150 ,600,200);
        add(achievementLatePanel);

        // achievementLatePanel에 들어갈 부속 Label 설정
        achievementLabel = new JLabel("\n다음 단계까지", JLabel.CENTER);
        achievementLabel.setForeground(Color.BLUE);
        achievementLatePanel.add(achievementLabel, BorderLayout.SOUTH);
        currentLabel = new JLabel(String.valueOf(currentCom));
        nextLabel = new JLabel(String.valueOf(nextCom));
        JLabel cando = new JLabel("할 수 있 다 !\n",JLabel.CENTER);
        cando.setSize(150,100);
        cando.setForeground(Color.RED);

        // 커스텀 그래프 패널 생성
        lateGraphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // 진행률 계산
                double progress = nextCom > 0 ? (double) currentCom / nextCom * 100 : 0;

                // 배경 그리기
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(0, 0, getWidth(), getHeight());

                // 진행률 그래프 그리기 (파란색)
                g.setColor(Color.BLUE);
                int progressWidth = (int) (getWidth() * progress / 100);
                g.fillRect(0, 0, progressWidth, getHeight());

                // 퍼센트 텍스트 추가
                g.setColor(Color.BLACK);
                g.drawString(String.format("%.1f%%", progress), 5, getHeight() - 10);
            }
        };

        // 고정된 크기 대신 비율에 맞게 동적으로 크기 조정
        lateGraphPanel.setPreferredSize(new Dimension(300, 20));

        // DB에서 바뀐 값 업데이트 버튼
        update = new JButton("update");
        update.setBounds(325, 600,80,40);
        achievementPanel.add(update);
        // 업데이트 이벤트 리스너
        update.addActionListener(e -> {
            try {
                // totalCom 업데이트
                totalCom = calculateTotalComplet(con);
                insertotalCom(con, totalCom);

                // 현재,다음 Complete 계산
                currentCom = getCurrentComplet(con);
                nextCom = getNextComplet(currentCom);

                // UI 갱신
                currentLabel.setText(String.valueOf(currentCom));
                nextLabel.setText(String.valueOf(nextCom));

                // 그래프 다시 그리기
                lateGraphPanel.repaint();

            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        achievementLatePanel.add(cando, BorderLayout.NORTH);
        achievementLatePanel.add(currentLabel, BorderLayout.WEST);
        achievementLatePanel.add(nextLabel, BorderLayout.EAST);
        achievementLatePanel.add(lateGraphPanel, BorderLayout.CENTER);

        achievementPanel.add(achievementLatePanel, BorderLayout.CENTER);
    }
}