package JavaProject.com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FitnessApp extends JFrame implements ActionListener {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    String loginedid,loginedpass;
    public FitnessApp(String loginedid, String loginedpass) {
        this.loginedid = loginedid;
        this.loginedpass = loginedpass;

        setTitle("나야 헬린이");
        setSize(1600, 1024);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 메뉴 패널 생성
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(1, 6));

        // 메뉴 버튼 생성
        String[] menuNames = {"Calendar", "Record", "Stats", "Diet", "Play", "User"};
        for (String name : menuNames) {
            JButton button = new JButton(name);
            button.addActionListener(this); // 버튼에 액션 리스너 추가
            menuPanel.add(button);
        }

        // CardLayout을 사용하는 메인 패널 생성
        // JTabbedPane jTabbedPane = new JTabbedPane(); - 이건 탭으로 만들기
        // jTabbedPane.addTab("회원", new JPanel());
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 각 화면 패널 추가
        mainPanel.add(new CalendarPanel(loginedid,loginedpass), "Calendar");
        mainPanel.add(new StatsPanel(), "Record");
        mainPanel.add(new StatsPanel(), "Stats");
        mainPanel.add(new StatsPanel(), "Diet");
        mainPanel.add(createPanel("Play 화면"), "Play"); // Play 화면은 아직 미구현
        mainPanel.add(new StatsPanel(), "User");

        // 메뉴와 메인 패널을 프레임에 추가
        add(menuPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
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

    // 버튼 클릭 시 해당 화면으로 전환
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        cardLayout.show(mainPanel, command);
    }
}
class StatsPanel extends JPanel {

    public StatsPanel() {

        setLayout(new GridLayout(3, 1)); // 화면을 세로로 3등분

        // 상단 패널 (월/주/일 운동량 그래프)
        JPanel workoutPanel = new WorkoutChartPanel();
        workoutPanel.setBorder(BorderFactory.createTitledBorder("운동량 통계 (월/주/일)"));

        // 중간 패널 (부위별 볼륨)
        JPanel volumePanel = new VolumeChartPanel();
        volumePanel.setBorder(BorderFactory.createTitledBorder("부위별 볼륨 통계 (월별)"));

        // 하단 패널 (영양소 통계)
        JPanel nutritionPanel = new NutritionChartPanel();
        nutritionPanel.setBorder(BorderFactory.createTitledBorder("영양소 통계 (월별)"));

        // 전체 레이아웃에 패널 추가
        add(workoutPanel);
        add(volumePanel);
        add(nutritionPanel);
    }

    // 운동량 차트 (라인 그래프 형태로 표시)
    class WorkoutChartPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int[] data = { 300, 200, 150 }; // 운동량 데이터 (월/주/일 단위로 예시 값)
            String[] labels = { "10월", "11월 첫째 주", "11월 10일" };
            int max = 300;

            int width = getWidth() - 40;
            int height = getHeight() - 40;
            int originX = 30, originY = height + 10;

            // Y축
            g.drawLine(originX, originY, originX, 20);
            g.drawString("운동 시간 (분)", 5, 20);
            // X축
            g.drawLine(originX, originY, width + 10, originY);

            // 데이터 표시
            int prevX = originX, prevY = originY - (data[0] * height / max);
            for (int i = 0; i < data.length; i++) {
                int x = originX + (i * width / (data.length - 1));
                int y = originY - (data[i] * height / max);
                g.fillOval(x - 3, y - 3, 6, 6);
                g.drawString(labels[i], x - 10, originY + 15);
                g.drawLine(prevX, prevY, x, y);
                prevX = x;
                prevY = y;
            }
        }
    }

    // 부위별 볼륨 차트 (막대 그래프 형태로 표시)
    class VolumeChartPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int[] data = { 1200, 800, 1000, 1500 }; // 볼륨 데이터 예시
            String[] labels = { "가슴", "등", "하체", "총 볼륨" };
            int max = 1500;

            int width = getWidth() - 40;
            int height = getHeight() - 40;
            int originX = 30, originY = height + 10;

            // Y축
            g.drawLine(originX, originY, originX, 20);
            g.drawString("볼륨 (kg)", 5, 20);
            // X축
            g.drawLine(originX, originY, width + 10, originY);

            // 막대 그래프
            int barWidth = (width - 20) / data.length;
            for (int i = 0; i < data.length; i++) {
                int barHeight = data[i] * height / max;
                int x = originX + (i * barWidth) + 10;
                int y = originY - barHeight;
                g.setColor(new Color(100, 150, 240));
                g.fillRect(x, y, barWidth - 10, barHeight);
                g.setColor(Color.BLACK);
                g.drawString(labels[i], x + 5, originY + 15);
                g.drawString(data[i] + " kg", x + 5, y - 5);
            }
        }
    }

    // 영양소 차트 (파이 차트 형태로 표시)
    class NutritionChartPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int[] data = { 50, 30, 20 }; // 탄수화물, 단백질, 지방 비율
            String[] labels = { "탄수화물", "단백질", "지방" };
            Color[] colors = { Color.YELLOW, Color.RED, Color.GREEN };

            int width = getWidth();
            int height = getHeight();
            int radius = Math.min(width, height) / 3;
            int centerX = width / 2;
            int centerY = height / 2;
            int startAngle = 0;

            for (int i = 0; i < data.length; i++) {
                int arcAngle = (int) (data[i] * 360.0 / 100);
                g.setColor(colors[i]);
                g.fillArc(centerX - radius, centerY - radius, 2 * radius, 2 * radius, startAngle, arcAngle);
                startAngle += arcAngle;
                g.setColor(Color.BLACK);
                g.drawString(labels[i] + " (" + data[i] + "%)", centerX + radius + 10, centerY - radius + 20 + i * 15);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StatsPanel view = new StatsPanel();
            view.setVisible(true);
        });
    }
}