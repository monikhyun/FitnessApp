package com.example;
import javax.swing.*;
import java.awt.*;
class UserPanel extends JPanel {
    public UserPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // 사용자 정보 입력 폼
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 5, 5));

        formPanel.add(new JLabel("이름:"));
        JTextField nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("나이:"));
        JTextField ageField = new JTextField();
        formPanel.add(ageField);

        formPanel.add(new JLabel("성별:"));
        JComboBox<String> genderCombo = new JComboBox<>(new String[]{"남", "여"});
        formPanel.add(genderCombo);

        formPanel.add(new JLabel("키(cm):"));
        JTextField heightField = new JTextField();
        formPanel.add(heightField);

        formPanel.add(new JLabel("체중(kg):"));
        JTextField weightField = new JTextField();
        formPanel.add(weightField);

        formPanel.add(new JLabel("목표:"));
        JComboBox<String> goalCombo = new JComboBox<>(
                new String[]{"근비대", "다이어트", "유지"});
        formPanel.add(goalCombo);

        // 사용자 정보 저장 버튼
        JButton saveButton = new JButton("저장");

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(formPanel, gbc);

        gbc.gridy = 1;
        gbc.weighty = 0.1;
        add(saveButton, gbc);
    }
}