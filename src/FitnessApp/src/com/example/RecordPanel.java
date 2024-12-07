package JavaProject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.transform.Result;
import java.awt.*;
import java.awt.List;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List.*;
import java.awt.event.*;
import java.util.Calendar;
import java.sql.*;

public class RecordPanel extends JPanel implements ActionListener {
    private JPanel ExecGrid; // 운동버튼 그리드 패널
    private JPanel RecordGrid; // 운동기록 패널
    private Calendar currentCalendar; // 날짜 활용 캘린더 객체
    private int selectedDay, selectedMonth, selectedYear; // 선택한 날짜 값
    private String loginedid, loginedpass; // 받아온 유저 정보
    private JLabel dateLabel; // 날짜 표시 레이블
    private JButton prevDay, nextDay; // 날짜 변경 버튼
    private JPanel bottompanel = new JPanel(); // ExecGrid 속 운동 패널
    private JPanel panel = new JPanel(new BorderLayout()); //
    private ArrayList<JButton>[][] execbuttons = new ArrayList[5][366]; // 운동 버튼 리스트
    private ArrayList<JPanel>[][] execPanels = new ArrayList[5][366];// 운동 패널 리스트
    private java.util.List<String> execlist = new ArrayList<>();

    private StatsPanel statsPanel;
    // 각 버튼에 해당하는 패널 리스트 선언
    java.util.List<JPanel> panels = new ArrayList<>();
    Connection conn;



    public RecordPanel(String id, String passwd, Connection conn,StatsPanel statsPanel) { //Record 패널 생성자
        this.loginedid = id; // Main에서 받아온 유저아이디
        this.loginedpass = passwd; // Main에서 받아온 비밀번호
        this.conn = conn; // MySQL을 활용하기 위한 Main에서 나온 Connection 객체
        this.statsPanel = statsPanel; // for 초기값


        try {
            String execid = "SELECT Execid ,Execname FROM Exec ORDER BY Execid ASC"; // 운동테이블에서 운동목록 조회
            PreparedStatement ps2 = conn.prepareStatement(execid); // 운동목록 조회 실행문
            ResultSet rs = ps2.executeQuery(); // 운동목록 조회 결과

            while(rs.next()){ // next() 메소드를 활용하여 결과값 가져오기
                execlist.add(rs.getString("Execname")); //
            }

        }

        catch(SQLException e){ // 데이터베이스 예외처리
            e.printStackTrace();
        }

        for (int i = 0; i < execbuttons.length; i++) {
            for (int j = 0; j < execbuttons[i].length; j++) {
                execbuttons[i][j] = new ArrayList<>(); // 각 위치에 ArrayList 객체를 생성
            }
        }
        for (int i = 0; i < execPanels.length; i++) {
            for (int j = 0; j < execPanels[i].length; j++) {
                execPanels[i][j] = new ArrayList<>(); // 각 위치에 ArrayList 객체를 생성
            }
        }
        setLayout(new GridLayout(1, 2,20,0));

        selectedYear = Calendar.getInstance().get(Calendar.YEAR); // 현재 년도 가져오기
        selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1; // 현재 월 날짜 가져오기
        selectedDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH); // 현재 일 날짜 가져오기
        ExecGrid = SearchDailyExec(); // Record 패널 왼쪽 화면 구성 패널
        add(ExecGrid); // Record 패널에 왼쪽 패널 추가

        RecordGrid = new JPanel(new BorderLayout()); // 운동기록 패널 BorderLayout으로 설정
        JLabel placeholderLabel = new JLabel("기록하실 운동을 선택해주세요."); // 기본적인 운동 선택 안내 메세지
        placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER); // 수평 중앙 정렬
        placeholderLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 20)); // 맑은고딕 폰트 사용 (한글사용을 위함)
        RecordGrid.add(placeholderLabel, BorderLayout.NORTH); // 레이블 객체 패널내의 상단에 배치
        add(RecordGrid); // 레이블 객체 추가

        updateDateLabel();
    }

    private JPanel SearchDailyExec() { // Record 패널 왼쪽에 위치하는 패널 생성 메소드
        JPanel headerPanel = new JPanel(); // 생성 패널의 윗 패널 객체
        prevDay = new JButton("◀"); // 날짜 데이터 이전 버튼
        nextDay = new JButton("▶"); // 날짜 데이터 이후 버튼
        dateLabel = new JLabel(""); // 날짜를 담을 레이블
        dateLabel.setHorizontalAlignment(JLabel.CENTER); // 수평 중앙정렬
        dateLabel.setPreferredSize(new Dimension(150, 50)); // 날짜 레이블 선호 크기 설정

        currentCalendar = Calendar.getInstance(); // 캘린더 객체 가져오기
        updateDateLabel(); // 날짜변경시 패널 업데이트 메소드

        prevDay.addActionListener(this); // 날짜 레이블 변경 이벤트 부여
        nextDay.addActionListener(this);

        dateLabel.setFont(new Font("Malgun Gothic", Font.BOLD, 20)); // 날짜 레이블 맑은 고딕 설정

        dateLabel.addMouseListener(new MouseListener() { // 날짜 레이블 마우스리스너 부여

            public void mouseClicked(MouseEvent e) { // 날짜 레이블 클릭시 날짜 선택 Dialog창 오픈
                JDialog datePickerDialog = createDatePickerDialog(); // Dialog창 생성 메소드
                datePickerDialog.setVisible(true); // Dialog창 Visible 설정
            }

            // Mouselistenr 설정을 위한 메소드 설정
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });

        // 왼쪽 상단 패널에 날짜 관련 데이터 추가
        headerPanel.add(prevDay);
        headerPanel.add(dateLabel);
        headerPanel.add(nextDay);

        // 왼쪽 패널에 상단 패널 추가
        panel.add(headerPanel, BorderLayout.NORTH);

        bottompanel.setLayout(new BoxLayout(bottompanel, BoxLayout.Y_AXIS));
        bottompanel.setPreferredSize(new Dimension(400, 600));
        bottompanel.setMinimumSize(new Dimension(400, 600));
        bottompanel.setMaximumSize(new Dimension(400, 600));
        // 상단 패널 밑의 하단 패널 추가
        panel.add(bottompanel,BorderLayout.CENTER);

        return panel;
    }

    // 날짜 라벨 업데이트 메서드
    protected void updateDateLabel() {
        // 현재 변경되거나 설정된 날짜 데이터 가져오기
        int year = currentCalendar.get(Calendar.YEAR);
        int month = currentCalendar.get(Calendar.MONTH) + 1;
        int day = currentCalendar.get(Calendar.DAY_OF_MONTH);

        // 날짜 변경시 오른쪽 패널 클리어
        resetRecordGrid();
        // 날짜 레이블 텍스트 수정
        dateLabel.setText(year + "-" + month + "-" + day);
        String date = dateLabel.getText();
        String[] parts = date.split("-"); // 문자열을 "-"로 분리
        String dyear = parts[0];
        String dmonth = parts[1].length() == 1 ? "0" + parts[1] : parts[1]; // 월이 한 자리면 0 추가
        String dday = parts[2].length() == 1 ? "0" + parts[2] : parts[2];   // 일이 한 자리면 0 추가

        String inputdate = dyear + "-" + dmonth + "-" + dday;
        int yearIndex = yearToIndex(year); // 단순 년도 데이터 가져오기
        int dayIndex = dateToIndex(year, month, day); // 년, 월, 일에 대한 0~365에 대한 dayindex 설정

            try {
                // UserExec 테이블에서 해당 유저의 해당 날짜에 해당하는 운동들 조회
                String sql = "SELECT Execid,Execname FROM UserExec WHERE Userid=? AND RecordDate = DATE(?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, loginedid);
                ps.setString(2, String.valueOf(Date.valueOf(inputdate)));
                bottompanel.removeAll();
                bottompanel.revalidate();
                bottompanel.repaint();
                ResultSet rs = ps.executeQuery();
                if (!rs.isBeforeFirst()) {
                    rs.close();
                    bottompanel.removeAll();
                    return;
                }

                while (rs.next()) {
                    //해당 운동 이름이 부여된 운동 버튼 생성

                    JButton button = new JButton(rs.getString("Execname"));
                    button.setMaximumSize(new Dimension(bottompanel.getWidth(), 80)); // 높이 50 설정
                    button.setAlignmentX(Component.CENTER_ALIGNMENT);

                    // 하단 패널에 버튼 객체들 추가
                    bottompanel.add(button);
                    execbuttons[yearIndex][dayIndex].add(button);

                    // 각 운동버튼에 해당하는 오른쪽 패널 객체
                    JPanel panel = new JPanel(new BorderLayout());
                    JLabel execlabel =new JLabel(button.getText());
                    execlabel.setFont(new Font("Malgun Gothic", Font.BOLD, 38));
                    execlabel.setHorizontalAlignment(SwingConstants.CENTER);
                    // 패널 정보에 해당하는 레이블 상단에 배치 (운동이름)
                    panel.add(execlabel, BorderLayout.NORTH);
                    // 오른쪽 패널의 하단 패널 생성
                    JScrollPane rcpanel =execdetails(execlabel.getText()); // 기록화면 생성 메소드
                    panel.add(rcpanel, BorderLayout.CENTER); // 생성된 패널 하단 패널에 추가
                    panels.add(panel); // 패널 리스트에 만들어진 패널 저장

                    //해당 운동버튼에 이벤트리스너 부여
                    button.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // 패널 표시
                            JButton evenbtn = (JButton) e.getSource(); // 클릭한 버튼 객체 가져오기
                            String execname = evenbtn.getText(); // 가져온 버튼 객체 운동 이름 가져오기
                            ExecRecordPanel(panel,execname,rcpanel); // 오른쪽 패널 생성 및 재배치
                        }
                    });
                }

                // 오른쪽 하단 패널의 정보들을 저장하는 운동기록패널 배열에 정보 저장
                execPanels[yearIndex][dayIndex].addAll(panels);

                bottompanel.revalidate();
                bottompanel.repaint();
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }



    private int yearToIndex(int year) { // 2024 년 데이터를 사용하기 위한 메소드
        // 2024년 선택시 index 값은 0 , 2025년은 1 ~~~
        return year - 2024; // 기준 연도를 2024로 설정
    }

    private int dateToIndex(int year, int month, int day) { // 해당 년에 일 데이터 가져오기 위한 메소드
        Calendar cal = Calendar.getInstance(); // 캘린더 객체 가져오기
        cal.set(year, month - 1, day); // 선택 날짜에 해당하는 날짜 저장, month는 0부터 시작
        return cal.get(Calendar.DAY_OF_YEAR) - 1; // 해당 날짜에 해당하는 Day index 부여
        // 1월 1일은 1이다. 따라서 -1을 시키면 index는 0
    }


    // 날짜 선택 팝업창 생성 메서드
    private JDialog createDatePickerDialog() { // Dialog 창 생성 메소드
        JDialog dialog = new JDialog(); // JDialog객체 생성
        dialog.setTitle("날짜 선택"); // 타이틀 네임 설정
        dialog.setModal(true); // Dialog창 외에 창 조작 불가능 설정
        dialog.setSize(300, 150); // Size 설정
        dialog.setLocationRelativeTo(this); // Dialog창 출력시 화면 중앙 배치

        JPanel datePanel = new JPanel(); // 날짜 패널

        // 날짜 변경을 위한 년, 월, 일에 해당하는 콤보박스 생성
        JComboBox<Integer> yearCombo = new JComboBox<>();
        for (int year = 2024; year <= 2030; year++) {
            yearCombo.addItem(year);
        }

        JComboBox<Integer> monthCombo = new JComboBox<>();
        for (int month = 1; month <= 12; month++) {
            monthCombo.addItem(month);
        }

        JComboBox<Integer> dayCombo = new JComboBox<>();
        for (int day = 1; day <= 31; day++) {
            dayCombo.addItem(day);
        }

        // 날자 패널에 콤보박스 추가
        datePanel.add(new JLabel("연도:"));
        datePanel.add(yearCombo);
        datePanel.add(new JLabel("월:"));
        datePanel.add(monthCombo);
        datePanel.add(new JLabel("일:"));
        datePanel.add(dayCombo);

        // 확인 버튼
        JButton selectButton = new JButton("확인");
        selectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int year = (int) yearCombo.getSelectedItem(); // 콤보박스에 해당하는 날짜 값 가져오기
                int month = (int) monthCombo.getSelectedItem();
                int day = (int) dayCombo.getSelectedItem();

                // Calendar 업데이트
                currentCalendar.set(Calendar.YEAR, year);
                currentCalendar.set(Calendar.MONTH, month - 1);
                currentCalendar.set(Calendar.DAY_OF_MONTH, day);

                // 날짜를 바꿨다면 자동으로 왼쪽 패널 데이터 업데이트
                updateDateLabel();
                dialog.dispose(); // 확인 버튼 누르면 날짜 변경 후 dialog 창 닫기
            }
        });

        // 각 객체들 배열
        dialog.setLayout(new BorderLayout());
        dialog.add(datePanel, BorderLayout.CENTER);
        dialog.add(selectButton, BorderLayout.SOUTH);

        return dialog;
    }

    // 날짜 변경 버튼 이벤트 처리
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == prevDay) {
            currentCalendar.add(Calendar.DAY_OF_MONTH, -1);
            updateDateLabel(); // 날짜 변경 시 각 패널 업데이트
        } else if (e.getSource() == nextDay) {
            currentCalendar.add(Calendar.DAY_OF_MONTH, 1);
            updateDateLabel(); // 날짜 변경 시 각 패널 업데이트
        }
    }

    //  오른쪽 하단 패널 생성 및 재배치 RecordGrid는 오른쪽 패널
    private void ExecRecordPanel(JPanel jpanel, String execname, JScrollPane jsp) {
        RecordGrid.removeAll();

        RecordGrid.add(jpanel, BorderLayout.CENTER);
        RecordGrid.revalidate();
        RecordGrid.repaint();
    }

    // 오른쪽 패널 초기화 메소드
    private void resetRecordGrid() {
        // 오른쪽 패널 기본값 설정
        if (RecordGrid == null) {
            RecordGrid = new JPanel(new BorderLayout());
        }
        // 기존 내용 클리어
        RecordGrid.removeAll();
        // 초기화 후 기본 안내문
        JLabel placeholderLabel = new JLabel("기록하실 운동을 선택해주세요.");
        placeholderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        placeholderLabel.setFont(new Font("Malgun Gothic", Font.PLAIN, 20));
        RecordGrid.add(placeholderLabel, BorderLayout.NORTH);

        RecordGrid.revalidate();
        RecordGrid.repaint();
    }
    // 데이터베이스의 이미지 파일 가져오는 메소드
    private BufferedImage getImageFromDatabase(String execname) {
        try {
            // Exec_images 테이블에 저장된 LONGBLOB 타입의 이미지 데이터 조회
            String query = "SELECT image FROM Exec_images WHERE Execname = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, execname);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // BLOB타입의 이미지 데이터 가져오기
                InputStream is = rs.getBinaryStream("image");
                return ImageIO.read(is); // BLOB 데이터를 BufferedImage로 변환
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // 이미지가 없거나 오류 발생 시 null 반환
    }

    // 이미지 파일 활용 객체
    class CustomImagePanel extends JPanel {
        private BufferedImage image;

        // 이미지 설정 메서드
        public void setImage(BufferedImage img) {
            this.image = img;
            repaint(); // 이미지를 다시 그리기 paintComponent 호출
        }

        @Override
        // 이미지 파일 그리기 메소드 repaint() 시에 자동 호출
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                // 패널 크기에 맞게 이미지 그리기
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    // 오른쪽 하단 패널 생성 메소드
    private JScrollPane execdetails(String execname){
        // 레이아웃 매니저 사용 X, null로 설정
        JPanel execdetail = new JPanel();
        execdetail.setLayout(null);
        execdetail.setBackground(Color.WHITE);
        execdetail.setPreferredSize(new Dimension(800, 1000)); // 초기 크기 설정

        // 데이터베이스에 기록을 저장하기 위한 확인 버튼
        JButton okbtn = new JButton("확인");
        okbtn.setBackground(Color.white);
        okbtn.setBounds(620,30,70,50);

        execdetail.add(okbtn);

        // 이미지 파일 객체 생성
        CustomImagePanel execimgpanel = new CustomImagePanel();
        execimgpanel.setBounds(220, 80, 300, 300);
        execimgpanel.setBackground(Color.WHITE);

        // 데이터베이스에서 이미지 로드
        BufferedImage img = getImageFromDatabase(execname);
        if (img != null) {
            execimgpanel.setImage(img); // CustomImagePanel의 setImage 호출
        } else {
            System.out.println("이미지를 찾을 수 없습니다: " + execname);
        }

        execimgpanel.setVisible(true);


        execdetail.add(execimgpanel);

        // 운동 소개 레이블
        JLabel execmemo = new JLabel();
        try{
            // 운동 목록 테이블에서 운동 소개문 조회
            String execm = "SELECT Memo FROM Exec WHERE Execname = ? ";
            PreparedStatement mmps = conn.prepareStatement(execm);
            mmps.setString(1,execname);
            ResultSet mmrs = mmps.executeQuery();

            if(mmrs.next()){
                execmemo.setText(mmrs.getString(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        execmemo.setFont(new Font("Malgun Gothic", Font.BOLD, 20));
        execmemo.setBounds(270, 370, 300, 70);
        execmemo.setVisible(true);

        execdetail.add(execmemo);

        // 운동 기록 패널 추가 버튼
        JButton pluspanel;

        // 각 운동 버튼에 해당하는
        // 오른쪽 패널의 하단 패널 속 운동 기록 패널
        // 리스트를 활용하여 기존 정보 저장 및 조회 가능
        java.util.List<JPanel> recordexecList = new ArrayList<>();


        try {
            // 운동 기록용 테이블에서 해당 유저, 해당 날짜에 해당하는 운동 관련 데이터 가져오기
            String sql = "SELECT SetCount, Kg, Count FROM ExecRecord WHERE Userid = ? AND ExecDate =DATE(?) AND Execid = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, loginedid); // 로그인된 사용자 ID
            ps.setDate(2, Date.valueOf(dateLabel.getText())); // 현재 날짜

            // 운동 목록 리스트에서 운동 인덱스 활용
            int execIndex = execlist.indexOf(execname);
            ps.setInt(3, execIndex + 1);

            ResultSet rs = ps.executeQuery();

            // 해당 날짜의 해당 운동에 대한 세트수 조회 및 가져오기
            String check = "SELECT COUNT(SetCount) FROM ExecRecord WHERE Userid = ? AND ExecDate = DATE(?) AND Execid = ?";
            PreparedStatement ckps = conn.prepareStatement(check);
            ckps.setString(1, loginedid);
            ckps.setDate(2, Date.valueOf(dateLabel.getText()));
            ckps.setInt(3, execIndex+1);

            ResultSet ckrs = ckps.executeQuery();

            // 데이터가 없을 경우 기본 패널 추가
            if (ckrs.next()) {
                // 데이터가 있다면 기존 데이터 조회 및 재배치
                int checknum = ckrs.getInt(1);
                // 기본 세트 수가 1로 저장되어있으므로 checknum == 1 일시 기존 데이터 없음
                if(checknum==1){
                    // 첫 번째 패널에 기록 패널 추가를 위한 if문
                    JPanel recordexec = createRecordExecPanel(0);
                    // 운동 기록 정보들을 보관하는 리스트에 각 패널 추가
                    recordexecList.add(recordexec);
                    // 오른쪽 패널의 하단 패널에 운동 기록 패널 추가
                    execdetail.add(recordexec);
                    // 패널 추가 버튼 가져오기 운동기록용 패널의 7번째 컴포넌트
                    pluspanel = (JButton) recordexec.getComponent(6);

                    // 패널 추가 버튼에 이벤트 등록
                    pluspanel.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            int index = recordexecList.size(); // 자동으로 recordexeclist 다음 인덱스 부여
                            // 기존에 생성된 패널이 있을시에 그 다음 패널로 등록되게끔 함

                            // 해당 index에 해당하는 패널 생성
                            // index가 0이 아니라면 패널 추가 버튼은 가지고 있지 않음
                            JPanel newRecordExec = createRecordExecPanel(index);

                            // 기록용 리스트에 새롭게 만들어진 패널 추가
                            recordexecList.add(newRecordExec);
                            // 새롭게 만든 패널 배치
                            execdetail.add(newRecordExec);

                            try  {
                                // 추가 기록 정보를 위한 ExecRecord 테이블에 해당 세트에 인덱스 관련 튜플 생성
                                String sql = "INSERT INTO ExecRecord (Userid, Execid, ExecDate, SetCount) VALUES (?, ?, ?, ?)";
                                PreparedStatement ps = conn.prepareStatement(sql);
                                ps.setString(1, loginedid);
                                int i  = execlist.indexOf(execname);
                                ps.setInt(2, i+1);// 운동 리스트에서 해당 운동 id 값
                                ps.setDate(3, Date.valueOf(dateLabel.getText())); // 현재 날짜
                                ps.setInt(4, index + 1); // 순서 저장
                                ps.executeUpdate();
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }

                            // 오른쪽 패널의 하단 패널에 대한 사이즈 조정 및 재배치
                            execdetail.setPreferredSize(new Dimension(800, 500 + recordexecList.size() * 120));
                            execdetail.revalidate();
                            execdetail.repaint();
                        }
                    });
                }
                else{
                    // 데이터가 있다면 기존 데이터 조회 및 재배치

                    // 패널 생성용 인덱스 값 설정
                    int panelIndex = 0;
                    // 조회된 SetCount, Kg, Count 값을 활용하여 기존의 정보 활용
                    while (rs.next()) {
                        int setCount = rs.getInt("SetCount");
                        int kg = rs.getInt("Kg");
                        int count = rs.getInt("Count");

                        // RecordExec 패널 생성 및 데이터 복원
                        JPanel recordexec = createRecordExecPanel(panelIndex++);
                        // Set
                        ((JTextField) recordexec.getComponent(0)).setText(String.valueOf(setCount));
                        // Kg
                        ((JTextField) recordexec.getComponent(2)).setText(String.valueOf(kg));
                        // Count
                        ((JTextField) recordexec.getComponent(4)).setText(String.valueOf(count));
                        // 첫번째 패널인지 그 이후 생성된 패널인지 확인
                        if (recordexec.getComponentCount() > 6) {
                            // 첫번째 패널일때 패널 추가 버튼 객체 가져오기 및 이벤트 부여
                            pluspanel = (JButton) recordexec.getComponent(6);
                            pluspanel.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    int index = recordexecList.size(); // 자동으로 recordexecList 다음 인덱스 부여
                                    JPanel newRecordExec = createRecordExecPanel(index);

                                    // 새롭게 생성된 패널을 리스트에 추가
                                    recordexecList.add(newRecordExec);
                                    execdetail.add(newRecordExec);

                                    try {
                                        // DB 테이블에서 운동 세트 중복 여부 확인 후 삽입
                                        String checkSql = "SELECT * FROM ExecRecord WHERE Userid = ? AND Execid = ? AND ExecDate = DATE(?) AND SetCount = ?";
                                        PreparedStatement checkPs = conn.prepareStatement(checkSql);
                                        checkPs.setString(1, loginedid);
                                        int i = execlist.indexOf(execname);
                                        checkPs.setInt(2, i + 1);
                                        checkPs.setDate(3, Date.valueOf(dateLabel.getText()));
                                        checkPs.setInt(4, index + 1);

                                        ResultSet checkRs = checkPs.executeQuery();
                                        // 그 다음 세트 수 등록
                                        if (!checkRs.next()) {
                                            String sql = "INSERT INTO ExecRecord (Userid, Execid, ExecDate, SetCount) VALUES (?, ?, ?, ?)";
                                            PreparedStatement ps = conn.prepareStatement(sql);
                                            ps.setString(1, loginedid);
                                            ps.setInt(2, i + 1);
                                            ps.setDate(3, Date.valueOf(dateLabel.getText()));
                                            ps.setInt(4, index + 1);
                                            ps.executeUpdate();
                                        }

                                        checkRs.close();
                                        checkPs.close();
                                    } catch (SQLException ex) {
                                        ex.printStackTrace();
                                    }

                                    execdetail.setPreferredSize(new Dimension(800, 500 + recordexecList.size() * 120));
                                    execdetail.revalidate();
                                    execdetail.repaint();
                                }
                            });
                        }

                        // 생성된 패널 운동 기록 패널 리스트에 저장
                        recordexecList.add(recordexec);
                        // 생성된 패널 오른쪽 패널의 하단 패널에 등록
                        execdetail.add(recordexec);
                    }
                }
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }





        // 해당 메소드의 반환 객체
        JScrollPane scrollPane = new JScrollPane(execdetail);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // 확인 버튼 이벤트 부여
        okbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try  {
                    // DB테이블에 totvolume 속성에 저장할 값 설정
                    int totvolume = 0;
                    // 각 운동에 해당하는 기록 패널들 사이즈 측정 후 반복
                    for (int i = 0; i < recordexecList.size(); i++) {
                        // 기록 패널 리스트에서의 해당 인덱스 해당하는 패널 할당
                        JPanel recordexec = recordexecList.get(i);

                        // JTextField 값 가져오기
                        int set = Integer.parseInt(((JTextField) recordexec.getComponent(0)).getText());
                        int kg = Integer.parseInt(((JTextField) recordexec.getComponent(2)).getText());
                        int count = Integer.parseInt(((JTextField) recordexec.getComponent(4)).getText());

                        // 데이터 업데이트
                        String sql = "INSERT INTO ExecRecord (Userid, Execid, ExecDate, SetCount, Kg, Count) VALUES (?, ?, ?, ?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE Kg = VALUES(Kg), Count = VALUES(Count)";
                        PreparedStatement ps = conn.prepareStatement(sql);
                        ps.setString(1, loginedid);
                        ps.setInt(2,execlist.indexOf(execname)+1);
                        ps.setDate(3, Date.valueOf(dateLabel.getText()));
                        ps.setInt(4,i+1);
                        ps.setInt(5, kg);
                        ps.setInt(6, count);

                        // 반복 시 마다 totvolume값 증가시킴.
                        totvolume = totvolume + (kg * set * count);
                        ps.executeUpdate();
                    }

                    // 기록시에 UserExec 테이블에 해당 운동 완료 값과 TotalCal, TotalVol 값 업데이트
                    String complete = "UPDATE UserExec SET Complete = ?, Totalcalories = ?, Totalvolume = ? WHERE RecordDate = DATE(?) AND Execid = ? AND Userid = ?";

                    // 총 칼로리 계산을 위한 totalset 가져오기
                    String totalset = "SELECT COUNT(SetCount) FROM ExecRecord WHERE ExecDate =DATE(?) AND Execid = ? AND Userid = ?";
                    // 해당 유저의 몸무게 가져오기
                    String weight = "SELECT Weight FROM User WHERE Userid = ?";
                    // 각 운동별 기본 소모 칼로리 가져오기
                    String cal = "SELECT Calories FROM Exec WHERE Execid = ?";

                    PreparedStatement comps = conn.prepareStatement(complete);

                    int i  = execlist.indexOf(execname);

                    PreparedStatement setps = conn.prepareStatement(totalset);

                    setps.setDate(1,Date.valueOf(dateLabel.getText()));
                    setps.setInt(2, i+1);
                    setps.setString(3, loginedid);

                    PreparedStatement weightps = conn.prepareStatement(weight);

                    weightps.setString(1, loginedid);

                    PreparedStatement  calps = conn.prepareStatement(cal);

                    calps.setInt(1, i);

                    ResultSet weightrs = weightps.executeQuery();
                    ResultSet setrs = setps.executeQuery();
                    ResultSet calrs = calps.executeQuery();


                    int setnum=0, weightnum=0, calnum=0;

                    if(setrs.next()){
                        setnum = setrs.getInt(1);
                    }

                    if(weightrs.next()){
                        weightnum = weightrs.getInt(1);
                    }

                    if(calrs.next()){
                        calnum = calrs.getInt(1);
                    }

                    // Complete값 1로 저장
                    comps.setInt(1,1);
                    // 사용자 정보와 운동정보를 이용하여 칼로리 계산
                    comps.setDouble(2, setnum * (calnum * weightnum * 3.5) / 200);
                    // 세트반복에 해당하는 총 볼륨값 저장
                    comps.setInt(3, totvolume);
                    // 해당 날짜 레이블의 값을 저장
                    comps.setDate(4,Date.valueOf(dateLabel.getText()));
                    // 운동 id
                    comps.setInt(5,i+1);
                    // 유저 아이디
                    comps.setString(6,loginedid);
                    comps.executeUpdate();

                    JOptionPane.showMessageDialog(null, "운동기록 성공", "성공", JOptionPane.INFORMATION_MESSAGE);

                    statsPanel.updateMonth(); // stats 날짜 업뎃

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "운동기록 실패", "실패", JOptionPane.INFORMATION_MESSAGE);
                }

            }
        });

        return scrollPane;
    }

    // 운동 기록용 패널 생성 메소드
    public static JPanel createRecordExecPanel(int index) {
        // 반환용 패널 객체
        JPanel recordexec = new JPanel();
        recordexec.setBounds(80, 450 + index * 80, 600, 70);
        recordexec.setLayout(null);


        JLabel setinput = new JLabel("Set");
        setinput.setBounds(130, 20, 40, 40);

        // Setcount 정보를 위한 텍스트 필드 객체
        JTextField set = new JTextField();
        set.setBounds(70, 20, 40, 40);
        set.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel kginput = new JLabel("Kg");
        kginput.setBounds(260, 20, 40, 40);

        // Kgt 정보를 위한 텍스트 필드 객체
        JTextField kg = new JTextField();
        kg.setBounds(200, 20, 40, 40);
        kg.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel cntinput = new JLabel("회");
        cntinput.setBounds(380, 20, 40, 40);

        // count 정보를 위한 텍스트 필드 객체
        JTextField cnt = new JTextField();
        cnt.setBounds(320, 20, 40, 40);
        cnt.setHorizontalAlignment(SwingConstants.CENTER);

        // 첫 번째 패널시에 추가 버튼 가지고 있음.
        if(index==0) {
            JButton pluspanel = new JButton("+");
            pluspanel.setBounds(480, 20, 40, 40);
            pluspanel.setFont(new Font("Malgun Gothic", Font.PLAIN, 7));

            // 각 컴포넌트들 배치
            recordexec.add(set);
            recordexec.add(setinput);
            recordexec.add(kg);
            recordexec.add(kginput);
            recordexec.add(cnt);
            recordexec.add(cntinput);
            recordexec.add(pluspanel);
        }
        else {
            // 각 컴포넌트들 배치
            recordexec.add(set);
            recordexec.add(setinput);
            recordexec.add(kg);
            recordexec.add(kginput);
            recordexec.add(cnt);
            recordexec.add(cntinput);
        }

        return recordexec;
    }





}

