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
        private int number;
        private boolean host;

        ClientInfo(){
            talker = "";
            number = 0;
            host = false;
        }
        ClientInfo(String talker, int number, boolean host){
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

        void setNumber(int num){
            this.number = num;
        }
        int getNumber(){
            return this.number;
        }

    }
    class ServerHandler extends Thread{
        private Socket conn;

        public ServerHandler(Socket conn){
            this.conn = conn;
        }

        private JSONObject setMSG(int func, String number, String message){
            JSONObject response = new JSONObject();

            response.put("talker", "#Server");
            response.put("func", func);
            response.put("number", number);
            response.put("time", new Date().toString());
            response.put("message", message);

            return response;
        }

        public void run(){
            while(true) {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(conn.getOutputStream())), true);

                    String str = in.readLine();

                    JSONParser parser = new JSONParser();
                    JSONObject response;

                    JSONObject msg = (JSONObject) parser.parse(str);

                    String talker = msg.get("talker").toString();
                    String message = msg.get("message").toString();
                    int func = Integer.parseInt(msg.get("func").toString());
                    String number = msg.get("number").toString();
                    //Date time = new Date(msg.get("time").toString());

                    System.out.println("time: " + new Date()+" talker: "+talker + " message: " + message + " func: " + func + " num: " + number);

                    switch (func) {
                        case 0:
                            // default message

                            /*ArrayList<Socket> clist = roomList.get(number);

                            for(Socket t : clist){
                                String s = clientList.get(t).getTalker();
                                if(!s.equals(talker)){
                                    PrintWriter o = new PrintWriter(new BufferedWriter(new OutputStreamWriter(t.getOutputStream())), true);
                                    o.println(msg.toString());
                                }
                            }*/

                            break;
                        case 1:
                            // generate pin number
                            Random r = new Random();
                            String pin = randomSeed.elementAt(seed++);

                            response = setMSG(func, pin, "[generatePin] "+ pin +" Done.");

                            clientList.get(number).setHost();
                            roomList.put(pin,new ArrayList<>());
                            roomList.get(pin).add(conn);

                            out.println(response.toString());
                            break;
                        case 2:
                            // request enter
                            //db.query("hello", "bb");
                            if(number.equals("12345")){
				                System.out.println("[requestEnter] TEST");
				                response = setMSG(func, number, "detail ");
			                }                    
			                else if(roomList.get(number) == null){
                                System.out.println("[requestEnter] NULL");
                                response = setMSG(func, number, "false");
                            } else {
                                // details of room
                                System.out.println("[requestEnter] EXIST");
                                response = setMSG(func, number, "detail ");
                            }
                            out.println(response.toString());
                            break;
                        case 3:
                            // request exit

                            break;
                        case 4:
                            // request past log

                            break;
                        case 5:
                            // set Talker
                            clientList.get(conn).setTalker(talker);
                            // response = setMSG(func, number, "[setTalker] Done.");
                            // out.println(response.toString());
                            break;
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                    System.out.println("Socket Close");
                    break;
                }
            }
        }

    }

}


