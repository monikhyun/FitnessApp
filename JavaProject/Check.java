package JavaProject;

import javax.swing.*;

public class Check extends JFrame {
    public static void main(String[] args) {
        JFrame frame = new JFrame("JButton Example");
        JButton button = new JButton("Click Me");

        // 배경색을 출력하여 기본값을 확인
        System.out.println(button.getBackground());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);
        frame.add(button);
        frame.setVisible(true);
    }
}
