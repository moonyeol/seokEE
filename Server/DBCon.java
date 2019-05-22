import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.sql.PreparedStatement;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.Token;

import java.sql.ResultSet;

public class DBCon {
	static final String JDBC_DRIVER ="org.mariadb.jdbc.Driver";
    static final String url = "jdbc:mariadb://localhost:3306/seokee";
    static String USERNAME = "root";
    static String PASSWORD = "dkakwhs12";
    //static String ENCODEING = "Unicode=true&characterEncoding=UTF-8";
    public static Connection conn = null;
    public DBCon(){
    	try
        {
            System.out.println("Connecting...");
            Class.forName(JDBC_DRIVER);
            //conn = DriverManager.getConnection(url, USERNAME, PASSWORD);
            conn = DriverManager.getConnection( url, USERNAME, PASSWORD);

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

    		//System.out.println("Insert success");
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    }
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
    		//System.out.println("Calling success");
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return data;
    	
    }
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
    		//System.out.println("Calling success");
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return data;
    }

	public boolean memberIDCheck(String id) {
       String query = "select count(*) as cnt from member where id = ?";
       try {
          PreparedStatement pstmt = null;
          pstmt = conn.prepareStatement(query);
          pstmt.setString(1, id);
          ResultSet rs = pstmt.executeQuery();
          if (rs.next()) {
             if(rs.getInt("cnt")!=0) {
                System.out.println("Already existing ID");
                return false;
             }
          }
       }catch(SQLException e)
       {
          e.printStackTrace();
          return false;
       }
       return true;
    }
    
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
        		//System.out.println("Insert Member success");
        	}catch(SQLException e)
        	{
        		e.printStackTrace();
        		return false;
        	}
    	}
    	else {
    		System.out.println("[memberRegisterID] Register Fail, Already existing ID.");
    		return false;
    	}
    	
    	System.out.println("[memberRegisterID] Register Complete.");
    	return true;
    }
    public boolean memberLoginCheck(String id, String password) {
    	String query = "select password from member where id = ?";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, id);
    		ResultSet rs = pstmt.executeQuery();
    		
			if(rs.next()){
				String x; 
				x = rs.getString("password");
				if (x.contentEquals(password)) {
					System.out.println("[memberRegisterID] Assertion Success.");
					return true;
				}
			}
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	System.out.println("[memberRegisterID] Assertion Failed.");
    	return false;
    }

    public Member searchMyInfo(String id) {
    	Member data = new Member();
    	String query = "select * from member where id = ?";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, id);
    		ResultSet rs = pstmt.executeQuery();

    		if(rs.next()){
				data.setID(rs.getString("id"));
				data.setPassword(rs.getString("password"));
				data.setGender(rs.getString("gender"));
				data.setBirth(rs.getString("birth"));
				data.setNickname(rs.getString("nickname"));
				
				//System.out.println("Calling success22");
			}
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return data;
    }
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
    		//System.out.println("Calling success");
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return data;
    }
    public ArrayList<String> searchMessageByRoom(String room){
    	ArrayList<String> data = new ArrayList<>();
    	String query = "select msg from talk where room = ?";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, room);
    		ResultSet rs = pstmt.executeQuery();
    		while(rs.next())
    		{
    			data.add(new String(rs.getString("msg")));
    		}
    		//System.out.println("Calling success");
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return data;
    }
    
    public boolean isTalkEnded(String room) {
    	String data;
    	String query = "select * from talk where room= ? order by indexnum desc;";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, room);
    		ResultSet rs = pstmt.executeQuery();
    		
			if(rs.next()){
				data = rs.getString("msg");
				if (data.compareTo("END")==0) {
					return true;
				}else {
					return false;
				}
			}
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return false;
    }
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
    		//System.out.println("Calling success");
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return data;
    }
    
    public String searchStartByRoom(String room) {
    	String data = "";
    	String query = "select time from talk where room = ? order by time asc;";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, room);
    		ResultSet rs = pstmt.executeQuery();
    		if(rs.next()){
    			data = rs.getString("time");
			}
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return data;
    }

	public boolean insertMarkData(String id, String number, String marked) {
    	String query = "insert into mark (id,number,marked) values(?,?,?);";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, id);
    		pstmt.setString(2, number);
    		pstmt.setString(3, marked);
    		pstmt.executeUpdate();


			return true;
    		//System.out.println("Insert success");
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}

		return false;
    }

	public String IdAndRoomForMarked(String id, String room) {
       String data = "";
       String query = "select marked from mark where id=? and number = ?;";
       PreparedStatement pstmt = null;
       try {
          pstmt = conn.prepareStatement(query);
          pstmt.setString(1, id);
          pstmt.setString(2, room);
          ResultSet rs = pstmt.executeQuery();
          if(rs.next()){
          	data = rs.getString("marked");
		  }
       }catch(SQLException e)
       {
          e.printStackTrace();
       }
       return data;
    }  

	public List<String> sortByValue(final Map <String , Integer> map){
        List<String> list = new ArrayList<>();
        list.addAll(map.keySet());
         
        Collections.sort(list,new Comparator(){
             
            public int compare(Object o1,Object o2){
                Object v1 = map.get(o1);
                Object v2 = map.get(o2);
                 
                return ((Comparable) v1).compareTo(v2);
            }
             
        });
        Collections.reverse(list);
        return list;
    }

	public ArrayList<String> extractFiveKeyWordByNLPHashMap(HashMap<String , Integer> tmpMap){
    	ArrayList<String> data = new ArrayList<>();
    	Iterator it = sortByValue(tmpMap).iterator();
    	int i = 0;
    	while(it.hasNext()){
    		if (i>5) {
    			break;
    		}
            String temp = (String) it.next();
            data.add(temp);
            i++;
        }
    	return data;
    }

	public HashMap<String , Integer> NLPHashmapByRoom(String room) {
		System.out.println("[DBCon] MAKE String");
    	String query = "select msg from talk where room = ?;";
    	PreparedStatement pstmt = null;
		StringBuilder sb = new StringBuilder();

    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, room);
    		ResultSet rs = pstmt.executeQuery();
  
      		while(rs.next()) {
    			sb.append(rs.getString("msg")).append(" ");
    		}    		
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	// NLP
		System.out.println("[DBCon] Analyze " + sb.toString().length());

    	Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);
    	List<Token> result = komoran.analyze(sb.toString()).getTokenList();
		HashMap<String , Integer> data = new HashMap <String , Integer>();
	
		System.out.println("[DBCon] Counting..");

		for(Token token : result){
			if(token.getPos().equals("NNG")){
				String morph = token.getMorph();
				if(data.containsKey(morph)){
					data.put(morph, data.get(morph)+1);
				} else data.put(morph,1);
			}
		}

		System.out.println("[DBCon] Success.");
		/*for (List<Pair<String, String>> eojeolResult : result) {
			for (Pair<String, String> wordMorph : eojeolResult) {
				if (wordMorph.getSecond().equals("NNG")) {
					if (data.containsKey(wordMorph.getFirst())) {
						tmp = data.get(wordMorph.getFirst());
						data.put(wordMorph.getFirst(), tmp+1);
					}else {
						data.put(wordMorph.getFirst(),1);
					}
				}
			}
		}*/
    	return data;
    }


	// calculate contribution who id / from room (0.0 < data < 1.0)
	public double contributionByRoomAndId(String room, String id) {
    	double data = 0;
    	String query = "select count(*)/(select count(*) from talk where room=\"?\") as cnt from talk where id=\"?\" and room = \"?\";";
    	
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, room);
    		pstmt.setString(2, id);
    		pstmt.setString(3, room);
    		ResultSet rs = pstmt.executeQuery();
    		if(rs.next())
    		{
    			data = rs.getDouble("cnt");
    		}
    		//System.out.println("Calling success");
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return data;
    }
	// Contribution from Room : HashMap<id : contribution> (0.0 < contribution < 1.0)  
	public HashMap<String , Double> calculateContributionByRoom(String room){
		HashMap<String , Double> data = new HashMap <String , Double>();
    	String query = "select id, count(*)/(select count(*) from talk where room = ?) as cnt from talk where room  = ? group by id";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, room);
    		pstmt.setString(2, room);
    		ResultSet rs = pstmt.executeQuery();
    		
    		while(rs.next()) {
    			data.put(rs.getString("id"), rs.getDouble("cnt"));
    		}
    		
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return data;
	}
		// Someone who talked with me : HashMap <id, frequency> limit 5 
	public HashMap<String , Integer> whoTalkedWithMe(String id){
		HashMap<String , Integer> data = new HashMap <String , Integer>();
    	String query = "select id, count(*) as cnt from "
    					+ "(select distinct id,room from talk where room in "
    						+ "(select room from talk where id = ? group by room) "
    					+ "and id!=?)tmp "
    				+ "group by id order by cnt desc limit 5;";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, id);
    		pstmt.setString(2, id);
    		ResultSet rs = pstmt.executeQuery();
    		
    		while(rs.next()) {
    			data.put(rs.getString("id"), rs.getInt("cnt"));
    		}
    		
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return data;
	}

	public Double contributionById(String id) {
    	Double data = 1.0;
    	List <Double> tmp1 = new ArrayList<>();
    	List <Double>tmp2 = new ArrayList<>();
    	String query = "select room, count(*) as cnt from talk where room in (select room from talk where id = ? group by room) group by room;";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, id);
    		ResultSet rs = pstmt.executeQuery();
    		
    		while (rs.next()){
    			tmp1.add(rs.getDouble("cnt"));
    		}
    		
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	query = "select room, count(*) as cnt from talk where room in (select room from talk where id = ? group by room) and id=? group by room";
    	pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, id);
    		pstmt.setString(2, id);
    		ResultSet rs = pstmt.executeQuery();
    		
    		while (rs.next()){
    			tmp2.add(rs.getDouble("cnt"));
    		}
    		
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	for (int i=0; i<tmp1.size(); i++) {
    		data = data*(tmp2.get(i)/tmp1.get(i));
    	}
    	
    	data = Math.pow(data, 1.0/(double) tmp1.size());
    	data = Math.round(data*1000)/1000.0;
    	insertContributionToStats(id,data);
    	return data;
    }
    public boolean memberIDCheckInStats(String id) {
    	String query = "select count(*) from stats where id = ?";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, id);
    		ResultSet rs = pstmt.executeQuery();
    		rs.next();
    		if (rs.getInt("count(*)")!=0) {
    			return false;
    		}
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    		return false;
    	}
    	return true;
    }
    
    public void insertContributionToStats(String id, Double data) {
    	if(!memberIDCheckInStats(id)) {
        	String query = "update stats set contribution = ? where id = ?;";	
        	PreparedStatement pstmt = null;
        	try {
        		pstmt = conn.prepareStatement(query);
        		pstmt.setDouble(1, data);
        		pstmt.setString(2, id);

        		pstmt.executeUpdate();
        	}catch(SQLException e)
        	{
        		e.printStackTrace();
        	}
    	}else {
    	String query = "insert into stats (id,contribution) values(?,?);";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, id);
    		pstmt.setDouble(2, data);

    		pstmt.executeUpdate();
    	}catch(SQLException e){
    		e.printStackTrace();
    	}
    	}
    }
    public double totalUserContributionMean() {
    	double data = 1;
    	double tmp=0.0;
    	String query = "select contribution from stats;";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		ResultSet rs = pstmt.executeQuery();
    		
    		while (rs.next()){
    			data *= rs.getDouble(1);
    			tmp+=1.0;
    		}
    		
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	data = Math.pow(data, 1.0/(double) tmp);
    	return data;
    }
    public Double contributionRank(String id) {
    	double data=1.0;
    	String query = "select (select count(*)+1 from stats where contribution>t.contribution)/(select count(*) from stats) as rank from stats as t where id =? ;";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, id);
    		ResultSet rs = pstmt.executeQuery();
    		while (rs.next()){
    			data = rs.getDouble(1);
    		}
    		
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
    	return data;
    }
        public String myPageContributionById(String id) {
    	String data;
    	double myMean = contributionById(id);
    	double totalUserMean = totalUserContributionMean();
    	double myRank = contributionRank(id);
    	
    	data = "이용자의 평균 회의 기여도는 "+ Math.round(totalUserMean*1000.0)/10.0 + "%입니다."+ 
    			"당신의 평균 기여도는 "+ myMean *100.0 +"%입니다."+ 
    			"당신은 상위 "+ myRank * 100.0 + "%의 기여도를 보이고 있습니다.";
    	return data;
    }
    public void insertRoom(String room, String title) {
    	String query = "insert into roomName (roomPin,roomTitle) values(?,?);";
    	PreparedStatement pstmt = null;
    	try {
    		pstmt = conn.prepareStatement(query);
    		pstmt.setString(1, room);
    		pstmt.setString(2, title);
    		pstmt.executeUpdate();
    		// System.out.println("Insert success");
    	}catch(SQLException e)
    	{
    		e.printStackTrace();
    	}
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

	Member(){

	}
	Member(String id, String pw, String gender, String birth ,String nick ){
		this.id = id;
		this.password = pw;
		this.gender = gender;
		this.birth = birth;
		this.nickname = nick;
	}

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