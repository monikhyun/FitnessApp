package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.*;
import java.io.*;

public class FitnessApp extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    String loginedid,loginedpass;
    public FitnessApp(String loginedid, String loginedpass, Connection conn) {
        this.loginedid = loginedid;
        this.loginedpass = loginedpass;

        setTitle("나야 헬린이");
        setSize(1600, 1024);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // JTabbedPane 생성
        // JTabbedPane jTabbedPane = new JTabbedPane(); - 이건 탭으로 만들기
        // jTabbedPane.addTab("회원", new JPanel());
        JTabbedPane tabbedPane = new JTabbedPane();

        // 각 화면 패널 추가
        tabbedPane.addTab("Calendar", new CalendarPanel(loginedid,loginedpass, conn));
        tabbedPane.addTab("Record", new RecordPanel(loginedid, loginedpass,conn));
        tabbedPane.addTab("Stats", new JPanel());
        tabbedPane.addTab("Diet", new JPanel());
        tabbedPane.addTab("Play", new JPanel()); // Play 화면은 아직 미구현
        tabbedPane.addTab("User", new JPanel());

        // 탭 패널을 프레임에 추가
        add(tabbedPane, BorderLayout.CENTER);
    }

    // 각 화면에 표시될 패널 생성 (여기서는 단순히 라벨로 예시)
    private JPanel createPanel(String text) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

}
