package JavaProject;
import javax.swing.*;
import java.awt.*;
class RecordPanel extends JPanel {
    public RecordPanel() {
        setLayout(new BorderLayout());

        // 운동 카테고리 선택
        JComboBox<String> categoryCombo = new JComboBox<>(
                new String[]{"Back", "Chest", "Shoulder", "Lower-body"});

        // 운동 선택
        JComboBox<String> exerciseCombo = new JComboBox<>();

        // 운동 기록 입력 폼
        JPanel inputPanel = new JPanel(new GridLayout(4, 2));
        inputPanel.add(new JLabel("무게(kg):"));
        JTextField weightField = new JTextField();
        inputPanel.add(weightField);

        inputPanel.add(new JLabel("횟수:"));
        JTextField repsField = new JTextField();
        inputPanel.add(repsField);

        inputPanel.add(new JLabel("세트:"));
        JTextField setsField = new JTextField();
        inputPanel.add(setsField);

        // 기록 저장 버튼
        JButton saveButton = new JButton("기록 저장");
        saveButton.addActionListener(e -> {
            // DB에 운동 기록 저장
            saveExerciseRecord(
                    categoryCombo.getSelectedItem().toString(),
                    exerciseCombo.getSelectedItem().toString(),
                    weightField.getText(),
                    repsField.getText(),
                    setsField.getText()
            );
        });

        JPanel topPanel = new JPanel();
        topPanel.add(categoryCombo);
        topPanel.add(exerciseCombo);

        add(topPanel, BorderLayout.NORTH);
        add(inputPanel, BorderLayout.CENTER);
        add(saveButton, BorderLayout.SOUTH);
    }

    private void saveExerciseRecord(String category, String exercise,
                                    String weight, String reps, String sets) {
        // DB 연동 코드 구현
    }
}
