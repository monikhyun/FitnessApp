package exam;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

class UserPanel extends JPanel {
    private JTextField nameField, ageField, heightField, weightField;
    private JComboBox<String> genderCombo, goalCombo;
    private String userId, userPasswd;

    public UserPanel(String userId, String userPasswd) {
        this.userId = userId;
        this.userPasswd = userPasswd;

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // 사용자 정보 입력 폼
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 5, 5));

        formPanel.add(new JLabel("이름:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("나이:"));
        ageField = new JTextField();
        formPanel.add(ageField);

        formPanel.add(new JLabel("성별:"));
        genderCombo = new JComboBox<>(new String[]{"남", "여"});
        formPanel.add(genderCombo);

        formPanel.add(new JLabel("키(cm):"));
        heightField = new JTextField();
        formPanel.add(heightField);

        formPanel.add(new JLabel("체중(kg):"));
        weightField = new JTextField();
        formPanel.add(weightField);

        formPanel.add(new JLabel("목표:"));
        goalCombo = new JComboBox<>(new String[]{"벌크업", "다이어트", "근력 강화"});
        formPanel.add(goalCombo);

        JButton saveButton = new JButton("저장");

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(formPanel, gbc);

        gbc.gridy = 1;
        gbc.weighty = 0.1;
        add(saveButton, gbc);

        loadUserInfo();

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveUserInfo();
            }
        });
    }

    // 데이터베이스에서 사용자 정보 로드
    private void loadUserInfo() {
        String query = "SELECT * FROM User WHERE Userid = ?";
        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://fitnessapp.c9uc026my60b.us-east-1.rds.amazonaws.com:3306/fitnessapp", "mih", "ansxoddl123");
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, userId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                nameField.setText(resultSet.getString("Username"));
                ageField.setText(String.valueOf(resultSet.getInt("Age")));
                genderCombo.setSelectedItem(resultSet.getString("Gender").equals("1") ? "남" : "여");
                heightField.setText(String.valueOf(resultSet.getDouble("Height")));
                weightField.setText(String.valueOf(resultSet.getDouble("Weight")));
                goalCombo.setSelectedItem(resultSet.getString("GoalType"));
            } else {
                JOptionPane.showMessageDialog(this, "미 확인된 사용자 정보입니다.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "데이터베이스 오류발생 " + e.getMessage());
        }
    }

    // 데이터베이스에 사용자 정보 저장
    private void saveUserInfo() {
        String gender = (String) genderCombo.getSelectedItem();
        String genderValue = gender.equals("남") ? "1" : "0";

        String goalType = (String) goalCombo.getSelectedItem();

        String query = "UPDATE User SET Username = ?, Age = ?, Gender = ?, Height = ?, Weight = ?, GoalType = ? WHERE Userid = ?";
        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://fitnessapp.c9uc026my60b.us-east-1.rds.amazonaws.com:3306/fitnessapp", "mih", "ansxoddl123");
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, nameField.getText());
            statement.setInt(2, Integer.parseInt(ageField.getText()));
            statement.setString(3, genderValue); 
            statement.setDouble(4, Double.parseDouble(heightField.getText()));
            statement.setDouble(5, Double.parseDouble(weightField.getText()));
            statement.setString(6, goalType);
            statement.setString(7, userId);

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "사용자 정보가 저장되었습니다.");
            } else {
                JOptionPane.showMessageDialog(this, "잘못된 값이 입력되었으니 다시 시도해주세요.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "데이터베이스 오류: " + e.getMessage());
        }
    }
}


