package com.example;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.sql.*;

public class PlayPanel extends JPanel {

    private String loginedid;
    private JPanel plantPanel, achievementPanel, achievementLatePanel, lateGraphPanel;
    private JLabel achievementLabel, currentLabel, nextLabel;
    private BufferedImage plantImage;
    private CustomImagePanel imagePanel;
    private int totalCom, currentCom, nextCom;
    private JButton update;
    private static final int[] grow = {
            0, 20, 50, 100, 200, 400, 500, 600, 1000, 2000, Integer.MAX_VALUE
    };

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
    // 이미지 사용을 위한 class
    class CustomImagePanel extends JPanel {
        private BufferedImage image;

        // 이미지 설정 메서드
        public void setImage(BufferedImage img) {
            this.image = img;
            repaint(); // 이미지를 다시 그리기
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                // 패널 크기에 맞게 이미지 그리기
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
    // 왼쪽 성장식물 패널
    private void initializeComponents(Connection con) throws SQLException {
        // 식물 패널 초기화
        plantPanel = new JPanel(new BorderLayout());
        add(plantPanel);

        // 식물 패널의 이미지 CustomPanel
        imagePanel = new CustomImagePanel();
        plantPanel.add(imagePanel, BorderLayout.CENTER);

        // 성과 패널 초기화
        achievementPanel = new JPanel();
        achievementPanel.setLayout(null);

        add(achievementPanel);

        // 총 Complete 계산
        totalCom = calculateTotalComplet(con);
        // 총 Complete forComplete 릴레이션에 저장
        insertotalCom(con, totalCom);
        // 식물 등급 및 이미지 설정
        int grade = determineGrade(totalCom);
        // DB에서 이미지 가져오기
        plantImage = getPlantImage(con, grade);
        // null인지 검사
        if (plantImage != null) {
            imagePanel.setImage(plantImage);
        }

        // 현재 및 다음 Complement 설정
        currentCom = getCurrentComplet(con);
        nextCom = getNextComplet(currentCom);

        // 성과 표시 패널 설정
        setupAchievementPanel(con);
    }
    // totlaCom 값 계산
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
            JOptionPane.showMessageDialog(this, "데이터베이스 업데이트 오류"+e.getMessage());
        }
    }

    // 등급나누기
    private int determineGrade(int totalCom) {
        for(int i=1; i< grow.length; i++){
            if (totalCom<=grow[i]){
                return i;
            }
        }
        return grow.length-1;
    }

    // 등급에 맞는 이미지 가져오기
    private BufferedImage getPlantImage(Connection con,int grade) {
        try {
            String query = "SELECT image FROM Play_images WHERE Grade = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, grade);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                InputStream is = rs.getBinaryStream("image");
                return ImageIO.read(is); // BLOB 데이터를 BufferedImage로 변환
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // 이미지가 없거나 오류 발생 시 null 반환
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
    // 현재 Complete 값에 따른 다음 목표치 반환
    private int getNextComplet(int currentCom) {
        for (int i=1; i<grow.length; i++){
            if (currentCom <= grow[i]){
                return grow[i];
            }
        }
        return Integer.MAX_VALUE; // 최대 목표치를 넘겼을 경우
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

        // 커스텀 그래프 패널 생성 익명 클래스 사용
        lateGraphPanel = new JPanel() {
            @Override
            // JPanel에 그래픽 그리는데 사용디는 메서드 사용자정의
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // 진행률 계산
                double progress = nextCom > 0 ? (double) currentCom / nextCom * 100 : 0;

                // 배경 그리기
                g.setColor(Color.LIGHT_GRAY);
                //사각형을 채워서 그리는 기능 //현재 컴포넌트의 가로 세로 길이 가져옴
                g.fillRect(0, 0, getWidth(), getHeight());

                // 진행률 그래프 그리기 (파란색)
                g.setColor(Color.BLUE);
                int progressWidth = (int) (getWidth() * progress / 100);
                g.fillRect(0, 0, progressWidth, getHeight());

                // 퍼센트 텍스트 추가
                g.setColor(Color.BLACK);
                // 지정된 위치에 문자열 그리기
                g.drawString(String.format("%.1f%%", progress), 5, getHeight() - 10);
            }
        };

        // 크기 조정
        lateGraphPanel.setPreferredSize(new Dimension(300, 20));

        // DB에서 바뀐 값에 대한 패널 업데이트 버튼
        update = new JButton("update");
        update.setBounds(325, 600,80,40);
        achievementPanel.add(update);
        // 업데이트 이벤트 리스너
        update.addActionListener(e -> {
            try {
                // totalCom 업데이트
                totalCom = calculateTotalComplet(con);
                insertotalCom(con, totalCom);

                // 식물 등급 및 이미지 설정
                int grade = determineGrade(totalCom);
                // DB에서 이미지 가져오기
                plantImage = getPlantImage(con, grade);
                // null인지 검사
                if (plantImage != null) {
                    imagePanel.setImage(plantImage);
                }
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