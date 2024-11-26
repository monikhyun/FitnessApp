package JavaProject;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
class DietPanel extends JPanel {
    public DietPanel() {
        setLayout(new BorderLayout());

        // 식사 시간대 선택
        JComboBox<String> mealTimeCombo = new JComboBox<>(
                new String[]{"아침", "점심", "저녁"});

        // 음식 검색
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("검색");

        // 음식 목록 테이블
        String[] columns = {"음식명", "칼로리", "탄수화물", "단백질", "지방"};
        JTable foodTable = new JTable(new DefaultTableModel(columns, 0));
        JScrollPane scrollPane = new JScrollPane(foodTable);

        // 선택한 음식 추가 버튼
        JButton addButton = new JButton("추가하기");

        JPanel topPanel = new JPanel();
        topPanel.add(mealTimeCombo);
        topPanel.add(searchField);
        topPanel.add(searchButton);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(addButton, BorderLayout.SOUTH);
    }
}