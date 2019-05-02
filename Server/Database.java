import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database {
    static final String JDBC_DRIVER ="com.mysql.cj.jdbc.Driver";
    static final String url = "jdbc:mysql://seokee2.c9p1xpsot2og.ap-northeast-2.rds.amazonaws.com/seok";
    static String USERNAME = "indexoutofrange";
    static String PASSWORD = "12341234";
    public static Connection conn = null;

    public Database(){
        try
        {
            System.out.println("접속중입니다...");
            Class.forName(JDBC_DRIVER);
            System.out.println("접속중입니다...2");
            conn = DriverManager.getConnection(url, USERNAME, PASSWORD);
            if(conn !=null ) System.out.println("연결되었습니다.");
            else System.out.println("실패했습니다.");
        } catch (ClassNotFoundException e) {
            System.out.println("Class Not Found Exception");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("SQL Exception :" + e.getMessage());
            e.printStackTrace();
        }
    }

    public void query(String a, String b){
        PreparedStatement pstmt = null;
        String query = "insert into test (msg, name) value (?,?);";

        try {
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, a);
            pstmt.setString(2, b);
            pstmt.executeUpdate();
            System.out.println("저장 완료");
        }catch(SQLException e)
        {
            e.printStackTrace();
        }
    }

}
