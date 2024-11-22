
import javax.swing.*;
import java.awt.*;

public class FitnessApp extends JFrame {
    public FitnessApp() {
        setTitle("나야 헬린이");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // JTabbedPane 생성
        JTabbedPane tabbedPane = new JTabbedPane();

        // 각 탭에 패널 추가
        tabbedPane.addTab("Calendar", new CalendarPanel());
        tabbedPane.addTab("Record", new RecordPanel());
        tabbedPane.addTab("Stats", new StatsPanel());
        tabbedPane.addTab("Diet", new DietPanel());
        tabbedPane.addTab("Play", createPanel("Play 화면")); // Play 화면은 아직 미구현
        tabbedPane.addTab("User", new UserPanel());

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FitnessApp app = new FitnessApp();
            app.setVisible(true);
        });
    }
}
