package com.example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

class DietPanel extends JPanel {
   
    private JCheckBox breakfastCheck, lunchCheck, dinnerCheck; 
    private JTextField searchField; 
    private JButton searchButton, addButton, recommendButton, deleteButton; 
    private JTable foodTable; 
    private DefaultTableModel tableModel; 
    private JTextArea breakfastArea, lunchArea, dinnerArea; 
    private JLabel totalCaloriesLabel; 
    private int totalCalories = 0; 
    private JLabel dateDisplay; 

    // 날짜 및 데이터 관리 변수
    private Calendar calendar = Calendar.getInstance(); 
    private String loginedid; // 로그인한 사용자 ID
    private String loginedpass; // 로그인한 사용자 비밀번호

    
    public DietPanel(String loginedid, String loginedpass) {
        this.loginedid = loginedid;
        this.loginedpass = loginedpass;
        setLayout(new BorderLayout()); 

        initializePanels();
        initializeEventHandlers(); 

        // 초기 데이터 로딩
        loadInitialFoodData();
    }

    private void initializePanels() {
        // 왼쪽 패널
        JPanel leftPanel = createLeftPanel();
        add(leftPanel, BorderLayout.WEST); 

        // 오른쪽 패널
        JPanel rightPanel = createRightPanel(); 
        add(rightPanel, BorderLayout.CENTER); 
    }

    // 왼쪽 패널 생성
    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new GridLayout(0, 1)); 

        dateDisplay = new JLabel(getCurrentDateString(), SwingConstants.CENTER); 
        dateDisplay.setFont(new Font("Arial", Font.BOLD, 24)); 

        JPanel dateButtonPanel = createDateButtonPanel(); 

        breakfastCheck = new JCheckBox("아침");
        lunchCheck = new JCheckBox("점심");
        dinnerCheck = new JCheckBox("저녁");

        breakfastArea = new JTextArea(7, 20);
        lunchArea = new JTextArea(7, 20);
        dinnerArea = new JTextArea(7, 20);
        setTextAreaProperties(); 

        totalCaloriesLabel = new JLabel("Total Calories: 0 kcal");

        leftPanel.add(dateButtonPanel);
        leftPanel.add(dateDisplay);
        leftPanel.add(breakfastCheck);
        leftPanel.add(new JScrollPane(breakfastArea)); 
        leftPanel.add(lunchCheck);
        leftPanel.add(new JScrollPane(lunchArea)); 
        leftPanel.add(dinnerCheck);
        leftPanel.add(new JScrollPane(dinnerArea));
        leftPanel.add(totalCaloriesLabel);

        return leftPanel;
    }

    // 날짜 변경 버튼 패널 생성
    private JPanel createDateButtonPanel() {
        JPanel dateButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); 

        // 월, 일 변경 버튼 생성 및 이벤트 추가
        JButton prevMonthButton = new JButton("<");
        prevMonthButton.addActionListener(e -> changeMonth(-1)); 

        JButton nextMonthButton = new JButton(">");
        nextMonthButton.addActionListener(e -> changeMonth(1)); 

        JButton prevDayButton = new JButton("<");
        prevDayButton.addActionListener(e -> changeDay(-1));

        JButton nextDayButton = new JButton(">");
        nextDayButton.addActionListener(e -> changeDay(1)); 

        // 버튼들을 패널에 추가
        dateButtonPanel.add(prevMonthButton);
        dateButtonPanel.add(nextMonthButton);
        dateButtonPanel.add(prevDayButton);
        dateButtonPanel.add(nextDayButton);

        return dateButtonPanel; 
    }

    // 텍스트 영역 속성 설정
    private void setTextAreaProperties() {
        breakfastArea.setEditable(false);
        lunchArea.setEditable(false);
        dinnerArea.setEditable(false);
    }

    // 오른쪽 패널 생성
    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout()); 
        rightPanel.setBorder(BorderFactory.createTitledBorder("음식 목록")); 

        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); 
        searchField = new JTextField(15); 
        searchButton = new JButton("검색"); 
        topPanel.add(searchField);
        topPanel.add(searchButton);
        String[] columns = {"음식명", "카테고리", "탄수화물", "단백질", "지방", "칼로리"}; 
        tableModel = new DefaultTableModel(columns, 0); 
        foodTable = new JTable(tableModel); 
        JScrollPane scrollPane = new JScrollPane(foodTable); 

        // 추가 버튼과 추천 버튼,삭제버튼을 포함한 하단 패널
        JPanel bottomPanel = new JPanel(); 
        addButton = new JButton("추가하기"); 
        recommendButton = new JButton("음식 추천 받기"); 
        deleteButton = new JButton("삭제하기");
        bottomPanel.add(addButton); 
        bottomPanel.add(recommendButton); 
        bottomPanel.add(deleteButton);

        // 오른쪽 패널에 상단, 중앙(테이블), 하단 패널 추가
        rightPanel.add(topPanel, BorderLayout.NORTH);
        rightPanel.add(scrollPane, BorderLayout.CENTER);
        rightPanel.add(bottomPanel, BorderLayout.SOUTH);

        return rightPanel; 
    }

    // 이벤트 핸들러 초기화
    private void initializeEventHandlers() {
        addButton.addActionListener(new AddButtonListener());
        searchButton.addActionListener(new SearchButtonListener());
        recommendButton.addActionListener(new RecommendButtonListener());
        deleteButton.addActionListener(new DeleteButtonListener());
    }

    // 초기 데이터 로딩
    private void loadInitialFoodData() {
        Vector<Vector<String>> foodData = fetchFoodDataFromDatabase(""); 
        for (Vector<String> row : foodData) {
            tableModel.addRow(row); 
        }
        tableModel.fireTableDataChanged(); 
        loadUserFoodData(); 
    }

    private void loadUserFoodData() {
        String currentDate = getCurrentDateString(); 
        Vector<FoodDataForDate> userFoodData = fetchUserFoodDataFromDatabase(loginedid, currentDate); 

        // 텍스트 영역 초기화
        breakfastArea.setText(""); 
        lunchArea.setText(""); 
        dinnerArea.setText(""); 

        double totalCalories = 0.0; 

        for (FoodDataForDate foodData : userFoodData) {
            String mealType = foodData.getTimeCategory(); 
            String foodName = foodData.getFoodName(); 
            double calories = foodData.getCalories(); 

            switch (mealType) {
                case "아침":
                    breakfastArea.append(foodName + " (" + calories + " kcal)\n"); 
                    break;
                case "점심":
                    lunchArea.append(foodName + " (" + calories + " kcal)\n"); 
                    break;
                case "저녁":
                    dinnerArea.append(foodName + " (" + calories + " kcal)\n"); 
                    break;
            }
            totalCalories += calories; 
        }

        totalCaloriesLabel.setText("Total Calories: " + totalCalories + " kcal"); 
    }

    private void changeMonth(int delta) {
        calendar.add(Calendar.MONTH, delta); 
        dateDisplay.setText(getCurrentDateString()); 
        loadUserFoodData(); 
    }

    private void changeDay(int delta) {
        calendar.add(Calendar.DAY_OF_MONTH, delta); 
        dateDisplay.setText(getCurrentDateString()); 
        loadUserFoodData(); 
    }

    private String getCurrentDateString() {
        int day = calendar.get(Calendar.DAY_OF_MONTH); 
        int month = calendar.get(Calendar.MONTH) + 1; 
        int year = calendar.get(Calendar.YEAR); 
        return String.format("%d-%02d-%02d", year, month, day); 
    }

    private Vector<FoodDataForDate> fetchUserFoodDataFromDatabase(String userId, String date) {
        Vector<FoodDataForDate> userFoodData = new Vector<>();
        String query = "SELECT TimeCategory, FoodName, TotalCalories " +
                       "FROM Userdiet " +
                       "WHERE UserId = ? AND Date = ?";
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://fitnessapp.c9uc026my60b.us-east-1.rds.amazonaws.com:3306/fitnessapp",
                "mih", "ansxoddl123");
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, userId); // 사용자 ID 설정
            pstmt.setDate(2, java.sql.Date.valueOf(date)); // 날짜 설정

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String timeCategory = rs.getString("TimeCategory");
                String foodName = rs.getString("FoodName"); 
                double totalCalories = rs.getDouble("TotalCalories");
                // FoodDataForDate 객체 생성 후 리스트에 추가
                userFoodData.add(new FoodDataForDate(timeCategory, foodName, totalCalories));
            }
        } catch (SQLException ex) {
            ex.printStackTrace(); 
        }
        return userFoodData; 
    }

    // 음식 추가하기 버튼을 눌렀을시 이 데이터가 유저 테이블에서도 INSERT되도록 하는 메소드
    private void addFoodToUserDiet(String userId, String foodname, String date, String timeCategory, double totalCalories, int count) {
        String query = "INSERT INTO Userdiet (UserId, FoodName, Date, TimeCategory, Count, TotalCalories) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://fitnessapp.c9uc026my60b.us-east-1.rds.amazonaws.com:3306/fitnessapp",
                "mih", "ansxoddl123");
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, userId); 
            pstmt.setString(2, foodname); 
            pstmt.setDate(3, java.sql.Date.valueOf(date)); 
            pstmt.setString(4, timeCategory);
            pstmt.setInt(5, count);
            pstmt.setDouble(6, totalCalories); 

            int rowsAffected = pstmt.executeUpdate(); // 쿼리 실행
            System.out.println(rowsAffected > 0 ? "음식이 성공적으로 추가되었습니다." : "음식 추가 실패."); // 결과 출력
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
    }

    // 추가 버튼 클릭 시 동작하는 리스너
    private class AddButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = foodTable.getSelectedRow(); // 선택된 행 인덱스
            if (selectedRow > -1) { // 0 이상부터 쭉쭊
                String foodName = (String) tableModel.getValueAt(selectedRow, 0); // 음식 이름 가져옴
                String foodKcal = (String) tableModel.getValueAt(selectedRow, 5); // 음식 칼로리 가져옴

                // 체크박스 상태 확인
                if (breakfastCheck.isSelected() || lunchCheck.isSelected() || dinnerCheck.isSelected()) {
                    // 수량 입력 받기
                    String countStr = JOptionPane.showInputDialog(null, "추가할 수량을 입력하세요:", "수량 입력", JOptionPane.QUESTION_MESSAGE);
                    
                    if (countStr != null && !countStr.trim().isEmpty()) {
                        try {
                            int count = Integer.parseInt(countStr); // 수량
                            if (count > 0) { 
                                totalCalories += Double.parseDouble(foodKcal) * count; // 총 칼로리 업데이트
                                totalCaloriesLabel.setText("Total Calories: " + totalCalories + " kcal");

                                String currentDate = getCurrentDateString(); // 현재 날짜 가져오기
                                addMealToUserDiet(foodName, foodKcal, currentDate, count); // 음식 추가 메소드 호출
                            } else {
                                JOptionPane.showMessageDialog(null, "수량은 1 이상이어야 합니다.", "오류", JOptionPane.ERROR_MESSAGE); // 오류 메시지
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(null, "유효한 수량을 입력하세요.", "오류", JOptionPane.ERROR_MESSAGE); // 오류 메시지
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "수량 입력이 취소되었습니다.", "정보", JOptionPane.INFORMATION_MESSAGE); // 입력 취소 메시지
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "음식 종류를 선택해 주세요."); 
                }
            }
        }

        // 선택된 음식과 관련된 정보를 추가하는 메소드
        private void addMealToUserDiet(String foodName, String foodKcal, String currentDate, int count) {
            if (breakfastCheck.isSelected()) {
                breakfastArea.append(foodName + " (" + count + "개)\n"); // 아침 영역에 추가
                addFoodToUserDiet(loginedid, foodName, currentDate, "아침", Double.parseDouble(foodKcal) * count, count); // DB에 추가
                lunchCheck.setSelected(false); // 점심 체크 해제
                dinnerCheck.setSelected(false); // 저녁 체크 해제
            } else if (lunchCheck.isSelected()) {
                lunchArea.append(foodName + " (" + count + "개)\n"); 
                addFoodToUserDiet(loginedid, foodName, currentDate, "점심", Double.parseDouble(foodKcal) * count, count); // DB에 추가
                breakfastCheck.setSelected(false); 
                dinnerCheck.setSelected(false); 
            } else if (dinnerCheck.isSelected()) {
                dinnerArea.append(foodName + " (" + count + "개)\n"); 
                addFoodToUserDiet(loginedid, foodName, currentDate, "저녁", Double.parseDouble(foodKcal) * count, count); // DB에 추가
                breakfastCheck.setSelected(false); 
                lunchCheck.setSelected(false); 
            }
        }
    }

    // 검색 버튼 클릭 시 동작하는 리스너
    private class SearchButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String keyword = searchField.getText(); 
            Vector<Vector<String>> foodData = fetchFoodDataFromDatabase(keyword); 
            tableModel.setRowCount(0); 
            for (Vector<String> row : foodData) {
                tableModel.addRow(row); 
            }
            tableModel.fireTableDataChanged(); // 테이블 업데이트
        }
    }

    // 추천 버튼 클릭 시 동작하는 리스너
    private class RecommendButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            loadRecommendations(); // 추천 음식 로드
        }
    }

    // 삭제 버튼 클릭 시 동작하는 리스너
    private class DeleteButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String currentDate = getCurrentDateString(); // 현재 날짜 가져오기
            int confirmation = JOptionPane.showConfirmDialog(null, "현재 날짜의 모든 음식을 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
            if (confirmation == JOptionPane.YES_OPTION) {
                deleteUserFoodData(loginedid, currentDate); 
                loadUserFoodData(); 
                JOptionPane.showMessageDialog(null, "음식이 삭제되었습니다.");
            }
        }
    }

    // 사용자 음식 데이터를 삭제하는 메소드
    private void deleteUserFoodData(String userId, String date) {
        String query = "DELETE FROM Userdiet WHERE UserId = ? AND Date = ?"; // 쿼리

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://fitnessapp.c9uc026my60b.us-east-1.rds.amazonaws.com:3306/fitnessapp",
                "mih", "ansxoddl123");
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, userId);
            pstmt.setDate(2, java.sql.Date.valueOf(date));

            int rowsAffected = pstmt.executeUpdate(); 
            System.out.println(rowsAffected > 0 ? "음식이 성공적으로 삭제되었습니다." : "삭제할 음식이 없습니다."); // 결과 출력
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
    }

    // 추천 음식을 불러오는 메소드
    private void loadRecommendations() {
        Vector<Vector<String>> recommendations = fetchRecommendationsFromDatabase(); 
        tableModel.setRowCount(0); 
        for (Vector<String> row : recommendations) {
            tableModel.addRow(row); 
        }
        tableModel.fireTableDataChanged(); 
    }

    // 데이터베이스에서 추천 음식을 가져오는 메소드
    private Vector<Vector<String>> fetchRecommendationsFromDatabase() {
        Vector<Vector<String>> recommendations = new Vector<>();
        String userGoalType = fetchUserGoalType(loginedid); // 사용자 GoalType 가져오기
        String query = "SELECT Foodname, Category, Carbo, Protein, Fat, Kcal FROM Diet WHERE GoalType = ? ORDER BY RAND() LIMIT 4"; // GoalType에 맞춰 랜덤 추천
        
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://fitnessapp.c9uc026my60b.us-east-1.rds.amazonaws.com:3306/fitnessapp",
                "mih", "ansxoddl123");
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            pstmt.setString(1, userGoalType); 
            ResultSet rs = pstmt.executeQuery(); 
            while (rs.next()) {
                Vector<String> row = new Vector<>();
                row.add(rs.getString("Foodname")); // 음식
                row.add(rs.getString("Category")); // 카테고리
                row.add(rs.getString("Carbo")); // 탄수화물
                row.add(rs.getString("Protein")); // 단백질
                row.add(rs.getString("Fat")); // 지방
                row.add(rs.getString("Kcal")); // 칼로리
                recommendations.add(row); // 추천 음식 추가
            }
        } catch (SQLException ex) {
            ex.printStackTrace(); 
        }
        return recommendations; 
    }

    // 사용자 GoalType을 가져오는 메소드
    private String fetchUserGoalType(String userId) {
        String goalType = null;
        String query = "SELECT GoalType FROM User WHERE UserId = ?";

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://fitnessapp.c9uc026my60b.us-east-1.rds.amazonaws.com:3306/fitnessapp",
                "mih", "ansxoddl123");
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            pstmt.setString(1, userId); 
            ResultSet rs = pstmt.executeQuery(); 

            if (rs.next()) {
                goalType = rs.getString("GoalType");
            }
        } catch (SQLException ex) {
            ex.printStackTrace(); 
        }
        return goalType; 
    }

    // 데이터베이스에서 음식 데이터를 가져오는 메소드
    private Vector<Vector<String>> fetchFoodDataFromDatabase(String keyword) {
        Vector<Vector<String>> foodData = new Vector<>();
        String query = "SELECT Foodname, Category, Carbo, Protein, Fat, Kcal FROM Diet WHERE Foodname LIKE ?"; // 검색 쿼리
        
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://fitnessapp.c9uc026my60b.us-east-1.rds.amazonaws.com:3306/fitnessapp",
                "mih", "ansxoddl123");
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            pstmt.setString(1, "%" + keyword + "%"); 
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Vector<String> row = new Vector<>();
                row.add(rs.getString("Foodname")); // 음식 이름
                row.add(rs.getString("Category")); // 카테고리
                row.add(rs.getString("Carbo")); // 탄수화물
                row.add(rs.getString("Protein")); // 단백질
                row.add(rs.getString("Fat")); // 지방
                row.add(rs.getString("Kcal")); // 칼로리
                foodData.add(row); // 음식 데이터 추가
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return foodData; // 음식 데이터 반환
    }

    // 날짜별 데이터 남기는 클래스
    class FoodDataForDate {
        private String timeCategory; 
        private String foodName; 
        private double calories; 

        public FoodDataForDate(String timeCategory, String foodName, double calories) {
            this.timeCategory = timeCategory;
            this.foodName = foodName;
            this.calories = calories;
        }

        public String getTimeCategory() {
            return timeCategory;
        }

        public String getFoodName() {
            return foodName;
        }

        public double getCalories() {
            return calories;
        }
    }
}

