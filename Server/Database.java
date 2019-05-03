import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database {
    static final String JDBC_DRIVER ="com.mysql.cj.jdbc.Driver";
    static final String url = "jdbc:mysql://seokee0503.c9p1xpsot2og.ap-northeast-2.rds.amazonaws.com/seokee";
    static String USERNAME = "indexoutofrange";
    static String PASSWORD = "12341234";
    public static Connection conn = null;

    public Database(){
        try
        {
            System.out.println("Connecting");
            Class.forName(JDBC_DRIVER);
            System.out.println("Connecting...");
            conn = DriverManager.getConnection(url, USERNAME, PASSWORD);
            if(conn !=null ) System.out.println("Connection Success.");
            else System.out.println("Connection Failed.");
        } catch (ClassNotFoundException e) {
            System.out.println("Class Not Found Exception");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("SQL Exception :" + e.getMessage());
            e.printStackTrace();
        }
    }

    public void insertTalk(Talk a) {
        String query = "insert into test (room,time,msg,id) values(?,?,?,?);";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, a.msg);
            pstmt.setString(2, a.time);
            pstmt.setString(3, a.msg);
            pstmt.setString(4, a.id);
            pstmt.executeUpdate();
            System.out.println("Insert Success.");
        }catch(SQLException e)
        {
            e.printStackTrace();
        }

    }
   /*  *****serachMessageID ������*****
    *
    *
    public Talk[] serachMessageID(String id) {
    	String query = "select * from test where name = ?";
    	PreparedStatement pstmt = null;
    	Talk[] data = new Talk[3];
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, id);
    		ResultSet rs = pstmt.executeQuery();
    		int i = 0;
    		while(rs.next())
    		{
    			System.out.println()
    			data[i].index = rs.getInt("index");
    			data[i].room = rs.getInt("room");
    			data[i].time = rs.getString("time");
    			data[i].msg = rs.getString("msg");
    			data[i].name = rs.getString("name");
    			i++;
    		}
    		System.out.println("ȣ�� �Ϸ�");
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return data;

    }
    */

}
class Talk{
    int index = 0;
    String room;
    String time;
    String msg;
    String id;

    Talk(String room, String time, String msg, String id){
        this.room = room;
        this.time = time;
        this.msg = msg;
        this.id = id;
    }
}
