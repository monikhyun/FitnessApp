package JavaProject;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Objects;

public class PlayPanel extends JPanel {
    private static final String dburl = "jdbc:mysql://fitnessapp.chqw04eu8yfk.ap-southeast-2.rds.amazonaws.com:3306/fitnessapp";
    private static final String dbusr = "mih";
    private static final String dbpass = "ansxoddl123";
    private String loginedid, loginedpass;
    private JPanel Plant, achievement;
    private ImageIcon PlantIcon;
    private JLabel PlantLabel;
    private int totalCom;

    PlayPanel(String loginedid, String loginedpass) {
        this.loginedid = loginedid;
        this.loginedpass = loginedpass;

        setLayout(new GridLayout(1, 2));

        Plant = new JPanel(new BorderLayout());
        Plant.setBorder(BorderFactory.createTitledBorder("성장Plant"));
        PlantIcon = new ImageIcon(getClass().getResource("Plant.png"));
        PlantLabel = new JLabel(PlantIcon);
        add(Plant, BorderLayout.SOUTH);
        Plant.add(PlantLabel);

        try {
            totalCom = calculTotalCom();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            Connection con = DriverManager.getConnection(dburl, dbusr, dbpass);

            switch (dividIntoTotalCom(totalCom)){
                case 0: break;
                // DB에서 이미지 경로 찾아와서 imageicon으로 변환하는 구문
                case 1: break;
                // DB에서 이미지 경로 찾아와서 imageicon으로 변환하는 구문
                case 2: break;
                // DB에서 이미지 경로 찾아와서 imageicon으로 변환하는 구문
                case 3: break;
                // DB에서 이미지 경로 찾아와서 imageicon으로 변환하는 구문
                case 4: break;
                // DB에서 이미지 경로 찾아와서 imageicon으로 변환하는 구문
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        achievement = new JPanel();
        achievement.setBorder(BorderFactory.createTitledBorder("성장률"));
        add(achievement);

    }

    private int calculTotalCom() throws SQLException {
        try {
            Connection con = DriverManager.getConnection(dburl, dbusr, dbpass);
            String sumsql = "select Sum(Complment) as totalCom from User_Exec where Userid = ?";
            PreparedStatement ps = con.prepareStatement(sumsql);
            ps.setString(1, loginedid);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("totalCom");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int dividIntoTotalCom(int totalCom) {
        if (totalCom <= 10) {
            return 1;
        } else if (totalCom <= 40) {
            return 2;
        }else if (totalCom <= 80) {
            return 3;
        }
        return 0;
    }
}
