package seokee;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
//jdbc:mysql://localhost/dbname?user=(생략)&password=(생략)&Unicode=true&characterEncoding=UTF-
//String dbPath = String.format(
//        "jdbc:mysql://%s:%d/%s?user=%s&password=%s&characterEncoding=utf-8&" + 
//        "useUnicode=true", ci.host, ci.port, ci.dbName, ci.user, ci.password);
public class DBCon {
	static final String JDBC_DRIVER ="com.mysql.cj.jdbc.Driver";
    static final String url = "jdbc:mysql://seokee0503.c9p1xpsot2og.ap-northeast-2.rds.amazonaws.com/seokee";
    static String USERNAME = "indexoutofrange";
    static String PASSWORD = "12341234";
    //static String ENCODEING = "Unicode=true&characterEncoding=UTF-8";
    public static Connection conn = null;
    public DBCon(){
    	try
        {
            System.out.println("접속중입니다...");
            Class.forName(JDBC_DRIVER);
            //conn = DriverManager.getConnection(url, USERNAME, PASSWORD);
            conn = DriverManager.getConnection( "jdbc:mysql://seokee0503.c9p1xpsot2og.ap-northeast-2.rds.amazonaws.com/seokee"
            		+ "?user=indexoutofrange&password=12341234&characterEncoding=utf-8&" 
            		+ "useUnicode=true");
            if(conn !=null ) System.out.println("DB Conncted.");
            else System.out.println("DB connect fail.");
        } catch (ClassNotFoundException e) {
            System.out.println("Class Not Found Exection");
            e.printStackTrace();
            } catch (SQLException e) {
                System.out.println("SQL Exception :" + e.getMessage());
                e.printStackTrace();
                }
    	
    }
    // Talk 한 열을 DB의 talk테이블로 삽입
    public void insertTalk(Talk a) {
    	String query = "insert into talk (room,time,msg,id) values(?,?,?,?);";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, a.room);
    		pstmt.setString(2, a.time);
    		pstmt.setString(3, a.msg);
    		pstmt.setString(4, a.id);
    		pstmt.executeUpdate();
    		System.out.println("Insert success");
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    }
    // String room 을 매개변수로 room의 모든 열을 ArrayList로 반환
    public ArrayList<Talk> serachMessageRoom(String room) {
    	ArrayList<Talk> data = new ArrayList<>();
    	String query = "select * from talk where room = ? order by indexnum";
    	
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, room);
    		ResultSet rs = pstmt.executeQuery();
    		while(rs.next())
    		{
    			data.add(new Talk(rs.getString("room"), rs.getString("time"), rs.getString("msg"), rs.getString("id")));
    		}
    		System.out.println("Calling success");
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return data;
    	
    }
    // String id 를 매개변수로 room의 모든 열을 ArrayList로 반환
    public ArrayList<Talk> serachMessageID(String id) {
    	ArrayList<Talk> data = new ArrayList<>();
    	String query = "select * from talk where id = ? order by indexnum";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, id);
    		ResultSet rs = pstmt.executeQuery();
    		while(rs.next())
    		{
    			data.add(new Talk(rs.getString("room"), rs.getString("time"), rs.getString("msg"), rs.getString("id")));
    		}
    		System.out.println("Calling success");
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return data;
    }
    // String id 가 존재하는지 boolean 리턴
    public boolean memberIDCheck(String id) {
    	String query = "select count(*) from member where id = ?";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, id);
    		ResultSet rs = pstmt.executeQuery();
    		
    		if (rs.getInt("count(*)")!=0) {
    			System.out.println("Already existing ID");
    			return false;
    		}
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    		return false;
    	}
    	return true;
    }
    // Member (id,password,gender,birth, nickname) 를 DB에 삽입, 성공하면 true 실패하면 false
    public boolean memberResgisterID(Member a) {
    	if (memberIDCheck(a.getID())) {
    		String query = "insert into member (id,password,gender,birth, nickname) values(?,?,?,?,?);";
        	PreparedStatement pstmt = null;
        	try {
        		pstmt = conn.prepareStatement(query);
        		pstmt.setString(1, a.getID());
        		pstmt.setString(2, a.getPassword());
        		pstmt.setString(3, a.getGender());
        		pstmt.setString(4, a.getBirth());
        		pstmt.setString(5, a.getNickname());
        		pstmt.executeUpdate();
        		System.out.println("Insert Member success");
        	}catch(SQLException e)
        	{
        		e.printStackTrace();
        		return false;
        	}
    	}
    	else {
    		System.out.println("Register Fail, Already existing ID.");
    		return false;
    	}
    	
    	System.out.println("Register Complete.");
    	return true;
    }
    
    // id, password를 매개변수로 id와 password가 맞는지 확인, true false 반환
    public boolean memberLoginCheck(String id, String password) {
    	String query = "select password from member where id = ?";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, id);
    		ResultSet rs = pstmt.executeQuery();
    		rs.next();
    		String x; // DB에서 password를 받아오는 변수
    		x = rs.getString("password");
    		if (x.contentEquals(password)) {
    			System.out.println("id equals password");
    			return true;
    		}
    		System.out.println("Calling success");
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	System.out.println("id inequals password");
    	return false;
    }

    // id를 매개변수로 받아 멤버의 모든 정보를 class로 반환
    public Member searchMyInfo(String id) {
    	Member data = new Member();
    	String query = "select * from member where id = ?";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, id);
    		ResultSet rs = pstmt.executeQuery();
    		rs.next();
			data.setID(rs.getString("id"));
			data.setPassword(rs.getString("password"));
			data.setGender(rs.getString("gender"));
			data.setBirth(rs.getString("birth"));
			data.setNickname(rs.getString("nickname"));
    		
    		System.out.println("Calling success22");
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return data;
    }
    // room을 매개변수로 하여 회의에 참여한 id을 반환 
    public ArrayList<String> searchIDByRoom(String room){
    	ArrayList<String> data = new ArrayList<>();
    	String query = "select id from talk where room = ? group by id;";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, room);
    		ResultSet rs = pstmt.executeQuery();
    		while(rs.next())
    		{
    			data.add(new String(rs.getString("id")));
    		}
    		System.out.println("Calling success");
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return data;
    }
    // room을 매개변수로 하여 회의의 Message을 반환 
    public ArrayList<String> searchMessageByRoom(String room){
    	ArrayList<String> data = new ArrayList<>();
    	String query = "select 'msg' from talk where room = ?";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, room);
    		ResultSet rs = pstmt.executeQuery();
    		while(rs.next())
    		{
    			data.add(new String(rs.getString("msg")));
    		}
    		System.out.println("Calling success");
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return data;
    }
    
    // room을 매개변수로 회의가 종료된것인지 true-> End, false-> not End or Error?
    public boolean isTalkEnded(String room) {
    	String data;
    	String query = "select * from talk where room= ? order by indexnum desc;";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, room);
    		ResultSet rs = pstmt.executeQuery();
    		rs.next();
    		data = rs.getString("msg");
    		if (data.compareTo("END")==0) {
    			return true; // 회의가 끝남
    		}else {
    			return false;
    		}
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return false;
    }
    // input ID, return where participated room 
    public ArrayList<String> searchRoomByID(String id){
    	ArrayList<String> data = new ArrayList<>();
    	String query = "select room from talk where id = ? group by room;";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, id);
    		ResultSet rs = pstmt.executeQuery();
    		while(rs.next())
    		{
    			data.add(new String(rs.getString("room")));
    		}
    		System.out.println("Calling success");
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return data;
    }
    
    
    
}



class Talk {
	int indexnum = 0;
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
	
	public int getIndexnum() {
		return indexnum;
	}
	public void setIndexnum(int indexnum) {
		this.indexnum= indexnum;
	}
	public void setRoom(String room){
		this.room = room;
	}
	public String getRoom(){
		return room;
	}
	public void setTime(String time){
		this.time = time;
	}
	public String getTime(){
		return time;
	}
	public void setMsg(String msg){
		this.msg = msg;
	}
	public String getMsg(){
		return msg;
	}
	public void setID(String id){
		this.id = id;
	}
	public String getID(){
		return id;
	}
}

class Member {
	String id;
	String password;
	String gender;
	String birth;
	String nickname;
	public String getID() {
		return id;
	}
	public void setID(String id) {
		this.id = id;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getBirth() {
		return birth;
	}
	public void setBirth(String birth) {
		this.birth = birth;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

}