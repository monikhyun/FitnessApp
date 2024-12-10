package JavaProject;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;

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

        StatsPanel statsPanel = new StatsPanel(loginedid, loginedpass,conn);
        RecordPanel recordPanel = new RecordPanel(loginedid, loginedpass,conn, statsPanel);

        // 각 화면 패널 추가
        tabbedPane.addTab("Calendar", new CalendarPanel(loginedid,loginedpass, conn, recordPanel));
        tabbedPane.addTab("Record", recordPanel);
        tabbedPane.addTab("Stats", statsPanel);
        tabbedPane.addTab("Diet", new DietPanel(loginedid, loginedpass, conn, statsPanel));
        tabbedPane.addTab("Play", new PlayPanel(loginedid, loginedpass, conn)); // Play 화면은 아직 미구현
        tabbedPane.addTab("User", new UserPanel(loginedid, loginedpass, conn));

        // 탭 패널을 프레임에 추가
        add(tabbedPane, BorderLayout.CENTER);

        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if(tabbedPane.getSelectedIndex()==1){
                    recordPanel.updateDateLabel();
                }
            }
        });

    }

    // 각 화면에 표시될 패널 생성 (여기서는 단순히 라벨로 예시)

}
