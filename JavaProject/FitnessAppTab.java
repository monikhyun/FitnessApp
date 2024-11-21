package JavaProject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FitnessAppTab extends JFrame implements ActionListener {
    private JTabbedPane tabbedPane;
    String loginedid,loginedpass;

    public FitnessAppTab(String loginedid, String loginedpass) {
        this.loginedid = loginedid;
        this.loginedpass = loginedpass;
        setTitle("나야 헬린이");
        setSize(1500, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // JTabbedPane 생성
        tabbedPane = new JTabbedPane();

        // 각 탭 추가
        tabbedPane.addTab("Calendar", createPanel("Calendar 화면"));
        tabbedPane.addTab("Record", createPanel("Record 화면"));
        tabbedPane.addTab("Stats", new StatsPanel());
        tabbedPane.addTab("Diet", createPanel("Diet 화면"));
        tabbedPane.addTab("Play", createPanel("Play 화면"));
        tabbedPane.addTab("User", createPanel("User 화면"));

        // 탭의 위치 설정 (선택사항)
        // tabbedPane.setTabPlacement(JTabbedPane.TOP); // TOP, BOTTOM, LEFT, RIGHT 중 선택

        // 탭 패널을 프레임에 추가
        add(tabbedPane, BorderLayout.CENTER);
    }

    // 각 화면에 표시될 패널 생성
    private JPanel createPanel(String text) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    // ActionListener는 더 이상 필요하지 않지만, 다른 기능을 위해 유지할 수 있음
    @Override
    public void actionPerformed(ActionEvent e) {
        // 필요한 경우 다른 이벤트 처리
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

        });
    }
}
