package exam;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

class DietPanel extends JPanel {
    private JCheckBox breakfastCheck, lunchCheck, dinnerCheck;
    private JTextField searchField;
    private JButton searchButton, addButton;
    private JTable foodTable;
    private DefaultTableModel tableModel;
    private JTextArea breakfastArea, lunchArea, dinnerArea;
    private JLabel totalCaloriesLabel;
    private int totalCalories = 0;
    private int count=1;

    private Calendar calendar = Calendar.getInstance();
    private JLabel dateDisplay;
    private List<FoodDataForDate> foodDataList;  // ArrayList로 변경
    private String loginedid;
    private String loginedpass;

    public DietPanel(String loginedid, String loginedpass) {
        this.loginedid = loginedid;
        this.loginedpass = loginedpass;
        setLayout(new BorderLayout());

        // 날짜별 음식 데이터 관리
        foodDataList = new ArrayList<>();

        // 왼쪽 패널
        JPanel leftPanel = new JPanel(new GridLayout(0, 1));

        // 날짜 표시 라벨
        dateDisplay = new JLabel(getCurrentDateString());
        dateDisplay.setHorizontalAlignment(SwingConstants.CENTER);
        dateDisplay.setFont(new Font("Arial", Font.BOLD, 24));

        // 월, 일 변경 버튼
        JButton prevMonthButton = new JButton("<");
        prevMonthButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeMonth(-1);
            }
        });
        JButton nextMonthButton = new JButton(">");
        nextMonthButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeMonth(1);
            }
        });
        JButton prevDayButton = new JButton("<");
        prevDayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeDay(-1);
            }
        });
        JButton nextDayButton = new JButton(">");
        nextDayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeDay(1);
            }
        });
        
        // 날짜 변경 버튼 패널 위에 4개의 버튼에 대한 위치
        JPanel dateButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        dateButtonPanel.add(prevMonthButton);
        dateButtonPanel.add(nextMonthButton);
        dateButtonPanel.add(prevDayButton);
        dateButtonPanel.add(nextDayButton);

        // 아침, 점심, 저녁 체크박스
        breakfastCheck = new JCheckBox("아침");
        lunchCheck = new JCheckBox("점심");
        dinnerCheck = new JCheckBox("저녁");

        // 먹은 음식들 text area
        breakfastArea = new JTextArea(7, 20);
        lunchArea = new JTextArea(7, 20);
        dinnerArea = new JTextArea(7, 20);
        breakfastArea.setEditable(false);
        lunchArea.setEditable(false);
        dinnerArea.setEditable(false);

        // JScrollPane 생성
        JScrollPane breakfastScroll = new JScrollPane(breakfastArea);
        JScrollPane lunchScroll = new JScrollPane(lunchArea);
        JScrollPane dinnerScroll = new JScrollPane(dinnerArea);

        // 총 칼로리 라벨
        totalCaloriesLabel = new JLabel("Total Calories: 0 kcal");

        // 왼쪽 패널에 컴포넌트 추가
        leftPanel.add(dateButtonPanel);
        leftPanel.add(dateDisplay);
        leftPanel.add(breakfastCheck);
        leftPanel.add(breakfastScroll);
        leftPanel.add(lunchCheck);
        leftPanel.add(lunchScroll);
        leftPanel.add(dinnerCheck);
        leftPanel.add(dinnerScroll);
        leftPanel.add(totalCaloriesLabel);

        // 오른쪽 패널 (음식 목록 및 추가 버튼)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("음식 목록"));

        // 검색 필드와 버튼을 포함한 상단 패널
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(15);
        searchButton = new JButton("검색");
        topPanel.add(searchField);
        topPanel.add(searchButton);

        // 음식 테이블
        String[] columns = {"음식명", "카테고리", "탄수화물", "단백질", "지방", "칼로리", "Foodid"};
        tableModel = new DefaultTableModel(columns, 0);
        foodTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(foodTable);

        // 추가 버튼을 포함한 하단 패널
        JPanel bottomPanel = new JPanel();
        addButton = new JButton("추가하기");
        bottomPanel.add(addButton);

        // 오른쪽 패널에 상단, 중앙(테이블), 하단 패널 추가
        rightPanel.add(topPanel, BorderLayout.NORTH);
        rightPanel.add(scrollPane, BorderLayout.CENTER);
        rightPanel.add(bottomPanel, BorderLayout.SOUTH);

        // 메인 패널에 왼쪽, 오른쪽 패널 추가
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        // 이벤트 핸들러 설정
        addButton.addActionListener(new AddButtonListener());
        searchButton.addActionListener(new SearchButtonListener());

        // 초기 데이터 로딩
        loadInitialFoodData();
    }
    // 초기 데이터 로딩
    private void loadInitialFoodData() {
        Vector<Vector<String>> foodData = fetchFoodDataFromDatabase("");

        for (Vector<String> row : foodData) {
            tableModel.addRow(row);
        }
        tableModel.fireTableDataChanged();
    }
    // 현재 날짜 반환
    private String getCurrentDateString() {
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        return String.format("%d-%02d-%02d", year, month, day);
    }

    // 월 변경 메소드
    private void changeMonth(int delta) {
        calendar.add(Calendar.MONTH, delta);
        dateDisplay.setText(getCurrentDateString());
    }

    // 일 변경 메소드
    private void changeDay(int delta) {
        calendar.add(Calendar.DAY_OF_MONTH, delta);
        dateDisplay.setText(getCurrentDateString());
    }
    //음식 추가하기 버튼을 눌렀을시 이 데이터가 유저 테이블에서도 INSERT되도록 하는 메소드임
    private void addFoodToUserDiet(String userId, int foodId, String date, String timeCategory, int count, double totalCalories) {
        String query = "INSERT INTO Userdiet (Userid, Foodid, Date, Timecategory, Count, TotalCalories) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://fitnessapp.chqw04eu8yfk.ap-southeast-2.rds.amazonaws.com:3306/fitnessapp",
                "mih", "ansxoddl123");
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, userId);
            pstmt.setInt(2, foodId);
            pstmt.setDate(3, java.sql.Date.valueOf(date));
            pstmt.setString(4, timeCategory);
            pstmt.setInt(5, count);
            pstmt.setDouble(6, totalCalories);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("음식이 성공적으로 추가되었습니다.");
            } else {
                System.out.println("음식 추가 실패.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /*지금 해당 추가하기 버튼을 누르면 음식이름과 칼로리값을 가져와서 체크박스 상태가 단일인지 확인하고 이 테이블은 USERDIET테이블과 연결이 되어있기 떄문에 USERDIET에 Food ID를 넘겨주기 위하여 별도의
     foodid int형 변수를 만들어 그 값을 가져옴
    */
    private class AddButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = foodTable.getSelectedRow();
            if (selectedRow != -1) {
                String foodName = (String) tableModel.getValueAt(selectedRow, 0);
                String foodKcal = (String) tableModel.getValueAt(selectedRow, 5);
                int foodId = Integer.parseInt(tableModel.getValueAt(selectedRow, 6).toString()); // Food ID를 가져온다고 가정

                // 체크박스 상태 확인
                if (breakfastCheck.isSelected() || lunchCheck.isSelected() || dinnerCheck.isSelected()) {
                    // 칼로리 계산
                    totalCalories += Double.parseDouble(foodKcal);
                    totalCaloriesLabel.setText("Total Calories: " + totalCalories + " kcal");

                    String currentDate = getCurrentDateString();

                    if (breakfastCheck.isSelected()) {
                        breakfastArea.append(foodName + "\n");
                        addFoodToUserDiet(loginedid, foodId, currentDate, "아침", 1, Double.parseDouble(foodKcal));// 칼로리 값을 double이므로 parse더블을 통해 변경
                    }
                    if (lunchCheck.isSelected()) {
                        lunchArea.append(foodName + "\n");
                        addFoodToUserDiet(loginedid, foodId, currentDate, "점심", 1, Double.parseDouble(foodKcal));
                    }
                    if (dinnerCheck.isSelected()) {
                        dinnerArea.append(foodName + "\n");
                        addFoodToUserDiet(loginedid, foodId, currentDate, "저녁", 1, Double.parseDouble(foodKcal));
                    }

                } else {
                    JOptionPane.showMessageDialog(null, "음식 종류를 선택해 주세요.");
                }
            }
        }
    }

    private class SearchButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String keyword = searchField.getText();
            Vector<Vector<String>> foodData = fetchFoodDataFromDatabase(keyword);

            tableModel.setRowCount(0); 
            for (Vector<String> row : foodData) {
                tableModel.addRow(row);
            }
            tableModel.fireTableDataChanged();  
        }
    }
    
    //모든음식의 정보를 불러오는 2차원 백터의 메소드로 음식의 모든정보를 반환해줌
    private Vector<Vector<String>> fetchFoodDataFromDatabase(String keyword) {
        Vector<Vector<String>> foodData = new Vector<>();
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://fitnessapp.chqw04eu8yfk.ap-southeast-2.rds.amazonaws.com:3306/fitnessapp",
                "mih", "ansxoddl123");
             Statement stmt = conn.createStatement()) {
        	
            String query = "SELECT Foodname, Category, Carbo, Protein, Fat, Kcal, Foodid FROM Diet WHERE Foodname LIKE '%" + keyword + "%'";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Vector<String> row = new Vector<>();
                row.add(rs.getString("Foodname"));
                row.add(rs.getString("Category"));
                row.add(rs.getString("Carbo"));
                row.add(rs.getString("Protein"));
                row.add(rs.getString("Fat"));
                row.add(rs.getString("Kcal"));
                row.add(rs.getString("Foodid")); 
                foodData.add(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return foodData;
    }
}

// 날짜별 데이터 남기는곳
class FoodDataForDate {
    private String date; // 날짜
    private List<String> breakfastMeals; // 아침 음식 리스트
    private List<String> lunchMeals;      // 점심 음식 리스트
    private List<String> dinnerMeals;     // 저녁 음식 리스트
    private double totalCalories;          // 총 칼로리

    public FoodDataForDate(String date) {
        this.date = date;
        breakfastMeals = new ArrayList<>();
        lunchMeals = new ArrayList<>();
        dinnerMeals = new ArrayList<>();
        totalCalories = 0;
    }

    public String getDate() {
        return date;
    }

    public List<String> getBreakfastMeals() {
        return breakfastMeals;
    }

    public List<String> getLunchMeals() {
        return lunchMeals;
    }

    public List<String> getDinnerMeals() {
        return dinnerMeals;
    }

    public double getTotalCalories() {
        return totalCalories;
    }

    public void addMeal(String mealType, String foodName) {
        switch (mealType) {
            case "breakfast":
                breakfastMeals.add(foodName);
                break;
            case "lunch":
                lunchMeals.add(foodName);
                break;
            case "dinner":
                dinnerMeals.add(foodName);
                break;
        }
    }

    public void incrementTotalCalories(double kcal) {
        totalCalories += kcal;
    }
}