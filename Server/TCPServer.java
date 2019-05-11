import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

public class TCPServer implements Runnable {

    public static final int ServerPort = 9000;
    //public static final String ServerIP = "18.223.143.140";

    private DBCon db;
    private int seed = 0;
    private HashMap<Socket, ClientInfo> clientList = new HashMap<>();
    private HashMap<String, ArrayList<Socket>> roomList = new HashMap<>();
    private HashMap<String, Boolean> roomRunning = new HashMap<>();

    private ArrayList<String> randomSeed = new ArrayList<>();

    @Override
    public void run() {
        Random r = new Random();
        for(int i=0; i<100; i++){
            StringBuffer s = new StringBuffer();
            for(int j=0; j<5; j++) {
                if (r.nextInt(2) == 0) s.append((char) ((int) (r.nextInt(26)) + 97));
                else s.append(r.nextInt(10));
            }
            randomSeed.add(s.toString());
        }

        try {
            System.out.println("Server: DB Connecting..");
            db = new DBCon();
            System.out.println("Server: Success DB Connect");

            System.out.println("Server: Socket Connecting...");
            ServerSocket serverSocket = new ServerSocket(ServerPort);

            while(true) {
                try{
                    Socket client = serverSocket.accept();

                    clientList.put(client, new ClientInfo());

                    System.out.println(new Date() + " Server: Accept " + client.getInetAddress().getHostAddress() + " " + client.getPort());

                    ServerHandler handler = new ServerHandler(client);
                    handler.start();

                } catch (Exception e) {
                    //System.out.println("S: Error");
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            //System.out.println("S: Error");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Thread desktopServerThread = new Thread(new TCPServer());
        desktopServerThread.start();
    }

    class ClientInfo {
        private boolean enrolled;
        private String id;
        private String talker;
        private String number;
        private boolean host;

        ClientInfo(){
            id = "";
            enrolled = false;
            talker = "";
            number = "0";
            host = false;
        }
        ClientInfo(String talker, String number, boolean host, String id){
            this.talker = talker;
            this.number= number;
            this.host = host;
            this.id = id;

            if(id.equals("")) enrolled = false;
            else enrolled = true;
        }

        void setLogin(String id){
            this.id = id;
            enrolled = true;
        }

        boolean getLogin(){
            return this.enrolled;
        }
        String getId(){
            return this.id;
        }

        void setHost(boolean v){
            this.host = v;
        }
        boolean getHost(){
            return this.host;
        }

        void setTalker(String t){
            this.talker = t;
        }
        String getTalker(){
            return this.talker;
        }

        void setNumber(String num){
            this.number = new String(num);
        }
        String getNumber(){
            return this.number;
        }

    }
    class ServerHandler extends Thread{
        private Socket conn;

        public ServerHandler(Socket conn){
            this.conn = conn;
        }

        private JSONObject setMSG(int func, String message){
            JSONArray jsonArray = new JSONArray();
            JSONObject response = new JSONObject();

            response.put("func", func);
            response.put("message", message);
            response.put("time", new Date().toString());

            jsonArray.add(response);

            JSONObject returnValue = new JSONObject();
            returnValue.put("server", jsonArray);

            return returnValue;
        }

        private JSONObject setMSGArr(int func, JSONArray message){
            JSONArray jsonArray = new JSONArray();
            JSONObject response = new JSONObject();

            response.put("func", func);

            JSONObject obj = new JSONObject();
            obj.put("con", message);

            response.put("message", obj.toString());
            response.put("time", new Date().toString());

            jsonArray.add(response);

            JSONObject returnValue = new JSONObject();
            returnValue.put("server", jsonArray);

            return returnValue; 
        }

        public void run(){
            String message = "";
            int func;

            while(true) {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(conn.getOutputStream())), true);

                    String str = in.readLine();
                    JSONParser parser = new JSONParser();
                    JSONObject response, info;

                    JSONObject msg = (JSONObject) parser.parse(str);

                    message = msg.get("message").toString();
                    func = Integer.parseInt(msg.get("func").toString());

                    String number = clientList.get(conn).getNumber();
                    String talker = clientList.get(conn).getTalker();

                    System.out.println("[FROM Client] TIME:" + new Date() +" TALKER: "+ talker + " MESSAGE: " + message + " FUNC: " + func + " NUMBER: " + number);

                    switch (func) {
                        case 0:
                            // default message
                            System.out.println("[DEFAULT MESSAGE]");

                            ArrayList<Socket> receiver = roomList.get(number);

                            System.out.print("[RECEIVER LIST] : ");
                            for(Socket s : receiver){
                                System.out.print(clientList.get(s).getTalker() + " ");

                                PrintWriter o = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                                o.println(setMSG(func,"[" + talker + "] : " + message).toString());
                            }

                            System.out.println("\n[SEND SUCCESS & INSERT MESSAGE TO DB]");
                            db.insertTalk(new Talk(number, new Date().toString(), message, talker));
                            break;
                        case 1:
                            // generate pin number
                            System.out.println("[generate PIN number]");

                            String pin = randomSeed.get(seed++).toString();

                            System.out.println("[assign PIN number]");
                            
                            clientList.get(conn).setHost(true);
                            clientList.get(conn).setNumber(pin);

                            System.out.println("[Set HOST and Number Success.]");

                            roomList.put(pin,new ArrayList<>());
                            roomList.get(pin).add(conn);

                            roomRunning.put(pin, false);

                            System.out.println("[Set RoomList Success]");

                            out.println(setMSG(func, pin));
                            break;
                        case 2:
                            // request enter
                            if(roomList.get(message) == null){
                                System.out.println("[requestEnter] NULL RESPONSE FALSE");
                                out.println(setMSG(func,"false").toString());
                            } else {
                                // details of room
                                System.out.println("[requestEnter] EXIST RESPONSE TRUE");

                                for(Socket s : roomList.get(message)){
                                    PrintWriter o = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                                    o.println(setMSG(func,talker).toString());
                                }

                                clientList.get(conn).setNumber(message);
                                roomList.get(message).add(conn);

                                if(roomRunning.get(message) == true){
                                    System.out.println("[requestEnter] response running!!");
                                    out.println(setMSG(func,"running").toString());        
                                } else {
                                    System.out.println("[requestEnter] response not running!!");
                                    out.println(setMSG(func,"not").toString()); 
                                }                                
                            }
                            break;
                        case 3:
                            // request Start
                            System.out.println("[REQUEST START] INSERT START MESSAGE TO DB");
                            db.insertTalk(new Talk(number,new Date().toString(), "START", talker));

                            System.out.print("[REQUEST START] RECEIVER LIST : ");
                            for(Socket s : roomList.get(number)){
                                if(clientList.get(s).getTalker().equals(talker)) continue;

                                System.out.print(clientList.get(s).getTalker() + " ");

                                PrintWriter o = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);

                                o.println(setMSG(func,"START").toString());
                            }

                            roomRunning.put(number, true);
                            System.out.println("\nSUCCESS SENDING START MESSAGE");

                            break;
                        case 4:

                            break;
                        case 5:
                            // set Talker
                            System.out.println("[SET TALKER] nickname: " + message);
                            clientList.get(conn).setTalker(message);
                            break;
                        case 6:
                            // request exit
                            System.out.print("[REQUEST EXIT] RECEIVER LIST: ");

                            for(Socket s : roomList.get(number)){  
                                System.out.print(clientList.get(s).getTalker() + " ");

                                PrintWriter o = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                                o.println(setMSG(func,talker).toString());
                            }

                            System.out.println("\n[REQUEST EXIT] APPLY MY INFO");

                            roomList.get(clientList.get(conn).getNumber()).remove(conn);
                            ArrayList<Socket> part = roomList.get(clientList.get(conn).getNumber());

                            if(part.isEmpty()){
                                System.out.println("[REQUEST EXIT] ROOM EMPTY!!");

                                roomRunning.remove(clientList.get(conn).getNumber());
                                
                                roomList.remove(clientList.get(conn).getNumber());
                                db.insertTalk(new Talk(number,new Date().toString(), "END", talker));
                            }

                            clientList.get(conn).setNumber("");
                            clientList.get(conn).setHost(false);
                            break;
                        case 7:
                            // request enroll
                            System.out.println("[REQUEST ENROLL]");

                            info = (JSONObject)parser.parse(message);

                            
                            boolean result = db.memberResgisterID(
                                new Member(
                                    info.get("id").toString(),
                                    info.get("pw").toString(), 
                                    info.get("gender").toString(), 
                                    info.get("birth").toString(), 
                                    info.get("nick").toString()
                                )
                            );
                            
                            System.out.println("[REQUEST ENROLL] SYSTEM: " + info.get("id") + " " + info.get("pw") + " " + ((result) ? "TRUE" : "FALSE") );

                            if(result) out.println(setMSG(func,"true").toString());
                            else out.println(setMSG(func,"false").toString());

                            break;
                        case 8:
                            // request past Log


                            break;
                        case 9:
                            // request Login
                            info = (JSONObject)parser.parse(message);

                            boolean loginCheck = db.memberLoginCheck(info.get("id").toString(), info.get("pw").toString());

                            clientList.get(conn).setLogin(info.get("id").toString());
                            
                            // retrieve nickname
                            if(loginCheck){
                                // temporary set id to nickname
                                clientList.get(conn).setTalker(info.get("id").toString());
                                out.println(setMSG(func,"true").toString());
                            } else out.println(setMSG(func,"false").toString());

                            break;
                        case 10:
                            // check duplicate
                            boolean check = db.memberIDCheck(message);

                            if(check) out.println(setMSG(func,"true").toString());
                            else out.println(setMSG(func,"false").toString());

                            break;
                        case 11:
                            // 
                            break;
                        case 12:
                            // REQUEST_USERINFO     
                            Member me = db.searchMyInfo(clientList.get(conn).getId());
                            JSONObject mInfo = new JSONObject();

                            JSONArray msgList = new JSONArray();
                    

                            mInfo.put("id", me.getID());
                            mInfo.put("nickname", me.getNickname());

                            System.out.println("[REQ_USERINFO] SEND INFO : " + me.getID() + " " + me.getNickname());
                            out.println(setMSG(func, mInfo.toString()));
                            
                            System.out.print("[REQ_USERINFO] My ROOM LIST: ");

                            ArrayList<String> myRoomList = db.searchRoomByID(me.getID());

                            for(String s: myRoomList) System.out.print(s + " ");
                            
                            System.out.println("\nGet Room CONTENT");

                            for(String myRoomNumber : myRoomList){
                                System.out.println("NUMBER: " + myRoomNumber);

                                JSONObject roomData = new JSONObject();
                                roomData.put("number", myRoomNumber);

                                ArrayList<String> roomMember = db.searchIDByRoom(myRoomNumber);
                                System.out.println("Get Room member List");

                                StringBuilder sb = new StringBuilder();
                                for(String s : roomMember){
                                    sb.append(s + " ");
                                }
                                roomData.put("members", sb.toString());
                                
                                System.out.println("MEMBERLIST : " + sb.toString());

                                ArrayList<String> roomContent = db.searchMessageByRoom(myRoomNumber);
                                System.out.println("Get Room Content");

                                sb = new StringBuilder();

                                for(String s : roomContent){
                                    sb.append(s + " ");
                                }

                                System.out.println("CONTENT: " + sb.toString());

                                roomData.put("content", sb.toString());
                                roomData.put("date", db.searchStartByRoom(myRoomNumber));

                                System.out.println("Get Start Time");

                                msgList.add(roomData);

                                System.out.println("Push to MSGLIST");
                            }

                            //System.out.println(setMSGArr(func, msgList).toString());   
                            out.println(setMSGArr(func, msgList).toString());                         
                            break;
                        case 13:
                            break;
                        case 14:
                            // request userlist

                            JSONArray memList = new JSONArray();

                            for(Socket s : roomList.get(number)){
                                JSONObject mem = new JSONObject();
                                mem.put("nick", clientList.get(s).getTalker());

                                memList.add(mem);
                            }
                            System.out.println(memList.toString());
    
                            out.println(setMSGArr(func, memList).toString());   
                            break;
                    }
                } catch (Exception e) {
                    if(roomList.get(clientList.get(conn).getNumber()) != null) roomList.get(clientList.get(conn).getNumber()).remove(conn);
                    clientList.remove(conn);

                    System.out.println("Socket Close");
                    break;
                }
            }
        }

    }

}


