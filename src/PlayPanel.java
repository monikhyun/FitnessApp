package JavaProject;

import javax.swing.*;

public class PlayPanel extends JPanel {
    private String loginedid,loginedpass;

    PlayPanel(String loginedid, String loginedpass) {
        this.loginedid = loginedid;
        this.loginedpass = loginedpass;
    }
}
