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
import org.json.simple.parser.JSONParser;

public class TCPServer implements Runnable {

    public static final int ServerPort = 9000;
    //public static final String ServerIP = "18.223.143.140";

    private Database db;
    private int seed = 0;
    private HashMap<Socket, ClientInfo> clientList = new HashMap<>();
    private HashMap<String, ArrayList<Socket>> roomList = new HashMap<>();
    private Vector<String> randomSeed = new Vector<>();

    @Override
    public void run() {
        Random r = new Random();
        for(int i=0; i<100; i++){
            StringBuffer s = new StringBuffer();
            for(int j=0; j<5; j++) {
                if (r.nextInt(2) == 0) s.append((char) ((int) (r.nextInt(26)) + 97));
                else s.append(r.nextInt(10));
            }
            System.out.println(s.toString());
        }

        ////////// FOR TEST
        roomList.put("12345",new ArrayList<>());

        try {
            System.out.println("Server: DB Connecting..");
            db = new Database();
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
        private String talker;
        private String number;
        private boolean host;

        ClientInfo(){
            talker = "";
            number = "0";
            host = false;
        }
        ClientInfo(String talker, String number, boolean host){
            this.talker = talker;
            this.number= number;
            this.host = host;
        }

        void setHost(){
            this.host = true;
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
            JSONObject response = new JSONObject();

            response.put("func", func);
            response.put("message", message);
            response.put("time", new Date().toString());

            return response;
        }

        public void run(){
            String message = "";
            int func;
            ClientInfo myInfo = clientList.get(conn);

            while(true) {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(conn.getOutputStream())), true);

                    String str = in.readLine();

                    JSONParser parser = new JSONParser();
                    JSONObject response;

                    JSONObject msg = (JSONObject) parser.parse(str);

                    message = msg.get("message").toString();
                    func = Integer.parseInt(msg.get("func").toString());

                    String number = myInfo.getNumber();
                    String talker = myInfo.getTalker();

                    System.out.println("time: " + new Date() +" talker: "+ talker + " message: " + message + " func: " + func + " num: " + number);

                    switch (func) {
                        case 0:
                            // default message
                            ArrayList<Socket> receiver = roomList.get(number);

                            for(Socket s : receiver){
                                PrintWriter o = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                                o.println("[" + talker + "] : " + message );
                            }

                            db.insertTalk(new Talk(number, new Date().toString(), message, talker));
                            break;
                        case 1:
                            // generate pin number
                            String pin = randomSeed.elementAt(seed++);

                            response = setMSG(func, "[generatePin] "+ pin +" Done.");

                            myInfo = clientList.get(conn);
                            myInfo.setHost();
                            myInfo.setNumber(pin);

                            roomList.put(pin,new ArrayList<>());
                            roomList.get(pin).add(conn);

                            out.println(response.toString());
                            break;
                        case 2:
                            // request enter
                            if(message.equals("12345")){
				                System.out.println("[requestEnter] TEST");
				                //
                                clientList.get(conn).setNumber("12345");
                                roomList.get("12345").add(conn);

				                response = setMSG(func, "detail ");
			                }                    
			                else if(roomList.get(message) == null){
                                System.out.println("[requestEnter] NULL");
                                response = setMSG(func, "false");
                            } else {
                                // details of room
                                System.out.println("[requestEnter] EXIST");

                                clientList.get(conn).setNumber(message);
                                roomList.get(message).add(conn);

                                response = setMSG(func, "detail ");
                                
                            }
                            out.println(response.toString());
                            break;
                        case 3:
                            // request Start

                            //
                            db.insertTalk(new Talk(number,new Date().toString(), "START", talker));
                            break;
                        case 4:
                            // request Finish
                            db.insertTalk(new Talk(number,new Date().toString(), "END", talker));
                            break;
                        case 5:
                            // set Talker
                            clientList.get(conn).setTalker(message);
                            break;
                        case 6:
                            // request exit

                            ArrayList<Socket> part = roomList.get(myInfo.getNumber());
                            roomList.get(myInfo.getNumber()).remove(conn);

                            // broadcast to other participant.
                            if(part.isEmpty()) db.insertTalk(new Talk(number,new Date().toString(), "END", talker));
                            //
                            break;
                        case 7:
                            // request enroll
                            break;
                        case 8:
                            // request past Log


                            break;
                        case 9:
                            // request Login


                            break;
                    }
                } catch (Exception e) {
                    //e.printStackTrace();

                    // temporary.....
                    roomList.get(myInfo.getNumber()).remove(conn);
                    clientList.remove(conn);

                    System.out.println("Socket Close");


                    break;
                }
            }
        }

    }

}

