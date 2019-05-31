import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import com.google.gson.Gson;

/*import javax.crypto.Cipher;
import javax.crypto.CipherSpi;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import org.apache.commons.codec.binary.Base64;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;*/

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.Token;

public class TCPServer implements Runnable {

    public static final int ServerPort = 9000;
    public static final String ip = "13.209.64.113";
    public static final Gson gson = new Gson();

    private DBCon db;
    private int seed = 0;
    
    private HashMap<Socket, ClientInfo> clientList = new HashMap<>();
    private HashMap<String, ArrayList<Socket>> roomList = new HashMap<>();
    private HashMap<String, Boolean> roomRunning = new HashMap<>();
    private ArrayList<String> randomSeed = new ArrayList<>();
    private Komoran analyzer = new Komoran(DEFAULT_MODEL.FULL);

    @Override
    public void run() {
        HashMap<String, Boolean> randomNumber = new HashMap<>();
        Random r = new Random();

        for(int i=0; i<10000; i++){
            StringBuffer s = new StringBuffer();
            for(int j=0; j<5; j++) {
                if (r.nextInt(2) == 0) s.append((char) ((int) (r.nextInt(26)) + 97));
                else s.append(r.nextInt(10));
            }
            randomNumber.put(s.toString(), true);
        }

        for(Map.Entry<String, Boolean> entry : randomNumber.entrySet()) randomSeed.add(entry.getKey());
        System.out.println("PIN Length: " + randomSeed.size());

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
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
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

        ClientInfo(){
            this.id = "";
            this.enrolled = false;
            this.talker = "";
            this.number = "0";
        }
        ClientInfo(String talker, String number, String id){
            this.talker = talker;
            this.number= number;
            this.id = id;

            if(id.equals("")) enrolled = false;
            else enrolled = true;
        }

        void setAnonymous(){
            this.id = "";
            enrolled = false;
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
        BufferedReader in;
        PrintWriter out;
        docs doc = new docs();

        public ServerHandler(Socket conn){
            this.conn = conn;
        }

        public String makeMessage(int func, String message){
            SocketMessage msg = new SocketMessage();
            msg.func = func;
            msg.message = message;

            return gson.toJson(msg);
        }

        public void run(){
            SocketMessage msg = new SocketMessage();
            SocketMessage fromServer = new SocketMessage();

            String stringData = new String();

            List<Token> result = null;
            HashMap<String , Integer> data = null;

            StringBuilder sb = new StringBuilder();

            ArrayList<Socket> receiverList = null;
            ClientInfo info = null;

            Title title = null;

            try{
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(conn.getOutputStream())), true);
            } catch(Exception e){
                e.printStackTrace();
                return;
            }

            while(true) {
                try {             
                    String readValue = in.readLine();

                    msg = gson.fromJson(readValue, SocketMessage.class);

                    String number = clientList.get(conn).getNumber();
                    String talker = clientList.get(conn).getTalker();

                    System.out.println("[FROM Client] TIME:" + new Date() +" TALKER: "+ talker + " MESSAGE: " + msg.message + " FUNC: " + msg.func + " NUMBER: " + number);

                    long startTime = System.currentTimeMillis();

                    switch (msg.func) {
                        case Constant.MSG:
                            receiverList = roomList.get(number);

                            SimpleTalk t = new SimpleTalk();
                            t.talker = talker;
                            t.content = msg.message;

                            stringData = makeMessage(msg.func, gson.toJson(t));

                            System.out.print("[RECEIVER LIST] : ");
                            for(Socket s : receiverList){
                                PrintWriter o = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                                o.println(stringData);
                            }

                            result = analyzer.analyze(msg.message).getTokenList();
                            data = new HashMap <String , Integer>();
                
                            sb = new StringBuilder();

                            for(Token token : result){
                                if(token.getPos().equals("NNG") || token.getPos().equals("NNP")){
                                    String morph = token.getMorph();
                                    sb.append(morph).append(" ");

                                    if(data.containsKey(morph)){
                                        data.put(morph, data.get(morph)+1);
                                    } else data.put(morph,1);
                                }
                            }
                                                        
                            if(clientList.get(conn).getLogin()){
                                db.dbNLPCon(number,data,clientList.get(conn).getId());
                                db.insertTalk(new Talk(number, new Date().toString(), msg.message, clientList.get(conn).getId()));
                            }
                            else{
                                db.dbNLPCon(number,data,talker);
                                db.insertTalk(new Talk(number, new Date().toString(), msg.message, talker));
                            }
                            break;
                        case Constant.PINCODE:
                            stringData = randomSeed.get(seed++).toString();

                            clientList.get(conn).setNumber(stringData);

                            roomList.put(stringData,new ArrayList<>());
                            roomList.get(stringData).add(conn);

                            roomRunning.put(stringData, false);

                            db.insertRoom(stringData, msg.message);
                            
                            title = new Title();
                            title.title = msg.message;
                            title.pincode = stringData;

                            out.println(makeMessage(msg.func, gson.toJson(title)));
                            break;
                        case Constant.ENTER:
                            title = new Title();

                            if(roomList.get(msg.message) == null){
                                System.out.println("[requestEnter] NULL RESPONSE FALSE");

                                title.pincode = "false";
                                title.title = "";

                                out.println(makeMessage(msg.func, gson.toJson(title)));

                            } else {
                                System.out.println("[requestEnter] EXIST RESPONSE TRUE");

                                receiverList = roomList.get(msg.message);
                                for(Socket s : receiverList){
                                    PrintWriter o = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                                    o.println(makeMessage(msg.func,talker));
                                }

                                clientList.get(conn).setNumber(msg.message);
                                roomList.get(msg.message).add(conn);

                                title.title = db.getTitle(msg.message);

                                if(roomRunning.get(msg.message) == true){
                                    title.pincode = "running";
                                    System.out.println("[requestEnter] response running!!");
                                } else {
                                    title.pincode = "not running";
                                    System.out.println("[requestEnter] response not running!!");
                                } 

                                out.println(makeMessage(msg.func, gson.toJson(title)));                               
                            }
                            break;
                        case Constant.START:
                            // request Start
                            System.out.println("[REQUEST START] INSERT START MESSAGE TO DB");

                            if(clientList.get(conn).getLogin())
                                db.insertTalk(new Talk(number,new Date().toString(), "START", clientList.get(conn).getId()));
                            else 
                                db.insertTalk(new Talk(number,new Date().toString(), "START", talker));

                            System.out.print("[REQUEST START] RECEIVER LIST : ");
                            receiverList = roomList.get(number);

                            for(Socket s : receiverList){
                                if(clientList.get(s).getTalker().equals(talker)) continue;

                                System.out.print(clientList.get(s).getTalker() + " ");

                                PrintWriter o = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);

                                o.println(makeMessage(msg.func,"START"));
                            }

                            roomRunning.put(number, true);
                            break;
                        case Constant.SET_NICK:
                            // set Talker
                            System.out.println("[SET TALKER] nickname: " + msg.message);
                            clientList.get(conn).setTalker("G_" + msg.message);
                            clientList.get(conn).setAnonymous();
                            break;
                        case Constant.EXIT:
                            // request exit
                            System.out.println("[REQUEST EXIT] " + msg.message);

                            if(clientList.get(conn).getLogin()) db.insertMarkData(clientList.get(conn).getId(), number, msg.message);
                            else db.insertMarkData(talker, number, msg.message);

                            System.out.print("[REQUEST EXIT] RECEIVER LIST: ");

                            roomList.get(number).remove(conn);
                            clientList.get(conn).setNumber("");

                            receiverList = roomList.get(number);

                            for(Socket s : receiverList){
                                PrintWriter o = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                                o.println(makeMessage(msg.func,talker));
                            }

                            System.out.println("\n[REQUEST EXIT] APPLY MY INFO");                            

                            if(receiverList.isEmpty()){
                                System.out.println("[REQUEST EXIT] ROOM EMPTY!!");
                                roomRunning.remove(number);                                
                                roomList.remove(number);

                                if(clientList.get(conn).getLogin())
                                    db.insertTalk(new Talk(number,new Date().toString(), "END", clientList.get(conn).getId()));
                                else 
                                    db.insertTalk(new Talk(number,new Date().toString(), "END", talker));
                            }
                            out.println(makeMessage(msg.func, "exit"));
                            
                            break;
                        case Constant.ENROLL:
                            // request enroll
                            System.out.println("[REQUEST ENROLL]");

                            Member member = gson.fromJson(msg.message, Member.class);
                            
                            boolean rValue = db.memberResgisterID(member);
                            
                            System.out.println("[REQUEST ENROLL] SYSTEM: " + ((rValue) ? "Success" : "Fail") );

                            if(rValue) out.println(makeMessage(msg.func,"true"));
                            else out.println(makeMessage(msg.func,"false"));

                            break;
                        case Constant.LOGIN:
                            // request Login
                            LoginInfo loginInfo = gson.fromJson(msg.message, LoginInfo.class);
                            boolean loginCheck = db.memberLoginCheck(loginInfo.id, loginInfo.pw);
                            
                            if(loginCheck){
                                clientList.get(conn).setLogin(loginInfo.id);
                                clientList.get(conn).setTalker(db.searchMyInfo(loginInfo.id).getNickname());
                                out.println(makeMessage(msg.func,"true"));
                            } else out.println(makeMessage(msg.func,"false"));

                            break;
                        case Constant.DUPLICATE:
                            // check duplicate
                            boolean check = db.memberIDCheck(msg.message);

                            System.out.println("check: " + check);

                            if(check) out.println(makeMessage(msg.func,"true"));
                            else out.println(makeMessage(msg.func,"false"));

                            break;
                        case Constant.REQUEST_FILE:
                            // REQUEST FILE
                            
                            ArrayList<Talk> makeFile = db.searchMessageByRoom(msg.message);
                            ArrayList<HashMap<String,Integer>> fileWord = db.dbNLPSearch(msg.message);
                            HashMap<String , Double> keyContribFile = db.calculateKeywordContributionByRoom(msg.message);
                            HashMap<String , Double> contribFile = db.calculateContributionByRoom(msg.message);

                            ArrayList<String> fileContent = new ArrayList<>();
                            
                            fileContent.add("회의 제목: " + db.getTitle(msg.message));
                            fileContent.add("회의 일시: " + db.searchStartByRoom(msg.message));
                            fileContent.add("");

                            sb = new StringBuilder();
                            sb.append("회의 주요 키워드: ");
                            for (Map.Entry<String, Integer> entry : fileWord.get(1).entrySet()) sb.append(entry.getKey()).append(", ");
                            
                            fileContent.add(sb.toString());
                            fileContent.add("");

                            sb = new StringBuilder();
                            sb.append("회의 기여도(총 발언 비율): ");
                            for (Map.Entry<String, Double> entry : contribFile.entrySet()) sb.append(entry.getKey()).append("(").append(entry.getValue()).append(") ");
                            fileContent.add(sb.toString());

                            sb = new StringBuilder();
                            sb.append("회의 기여도(주요 키워드 발언 비율): ");
                            for (Map.Entry<String, Double> entry : keyContribFile.entrySet()) sb.append(entry.getKey()).append("(").append(entry.getValue()).append(") ");
                            fileContent.add(sb.toString());
                            fileContent.add("");

                            
                            fileContent.add("회의 내용");                            
                            for(Talk fTalk: makeFile){
                                sb = new StringBuilder();
                                if(fTalk.getMsg().equals("START"))
                                    fileContent.add(sb.append(fTalk.getID()).append(" : <").append(fTalk.getID()).append("> 입장.").toString());
                                else if(fTalk.getMsg().equals("END")) 
                                    fileContent.add(sb.append(fTalk.getID()).append(" : <").append(fTalk.getID()).append("> 퇴장.").toString());
                                else 
                                    fileContent.add(sb.append(fTalk.getID()).append(" : ").append(fTalk.getMsg()).toString());
                            }
                            

                            String tempFileName = "content_" + msg.message;
                            doc.mkdoc(fileContent, "/var/www/html/files", tempFileName);
                            out.println(makeMessage(msg.func,"http://"+ ip +"/files/content_"+msg.message+".docx"));

                            break;
                        case Constant.REQUEST_USERINFO:

                            RequestUserInfo userInfo = new RequestUserInfo();

                            Member me = db.searchMyInfo(clientList.get(conn).getId());
                            userInfo.id = me.getID();
                            userInfo.nickname = me.getNickname();

                            ArrayList<String> myRoomList = db.searchRoomByID(me.getID());
                            ArrayList<History> histories = new ArrayList<>();

                            for(String myRoomNumber : myRoomList){
                                History history = new History();
                                history.number = myRoomNumber;

                                ArrayList<String> roomMember = db.searchIDByRoom(myRoomNumber);

                                sb = new StringBuilder();
                                for(String s : roomMember) sb.append(s + " ");

                                history.members = sb.toString();

                                ArrayList<Talk> roomContent = db.searchMessageByRoom(myRoomNumber);

                                sb = new StringBuilder();
                                for(Talk s : roomContent){
                                    String roomMessage = s.getMsg();
                                    if(roomMessage.equals("START") || roomMessage.equals("END")) continue;
                                    sb.append(roomMessage + " ");
                                }
                                history.content = sb.toString();
                                history.date = db.searchStartByRoom(myRoomNumber);
                                history.title = db.getTitle(myRoomNumber);
                                if(history.title == null) history.title = "NULL";

                                histories.add(history);
                            }

                            userInfo.histories = histories;

                            HashMap<String , Integer> frequentUser = db.whoTalkedWithMe(me.getID());
                            StringBuilder fUser = new StringBuilder();
                            for (Map.Entry<String, Integer> entry : frequentUser.entrySet()) {
                                String id = entry.getKey();
                                fUser.append(id).append(" ");
                            }

                            userInfo.talkWithMe = fUser.toString();
                            userInfo.contributionData = db.myPageContributionById(me.getID());

                            System.out.println(gson.toJson(userInfo));
                            out.println(gson.toJson(userInfo));

                            break;
                        case Constant.REQUEST_USERLIST:
                            // request userlist

                            receiverList = roomList.get(number);

                            ArrayList<String> memList = new ArrayList<>();
                            for(Socket s : receiverList)
                                memList.add(clientList.get(s).getTalker());

                            MemberList mList = new MemberList();
                            mList.list = memList;

                            out.println(makeMessage(msg.func, gson.toJson(mList)));
                            break;
                        case Constant.REQUEST_RESULT:
                            //REQUEST RESULT

                            System.out.println("[REQUEST_RESULT] : " + msg.message);

                            RequestResult requestResult = new RequestResult();

                            ArrayList<HashMap<String,Integer>> words = db.dbNLPSearch(msg.message);
                            requestResult.wordFrequency = words.get(0);
                            requestResult.fiveKeyWord = words.get(1);
                            requestResult.roomName = db.getTitle(msg.message);
                            requestResult.date = db.searchStartByRoom(msg.message);
                            requestResult.end = db.searchEndByRoom(msg.message);

                            // 단순 talk 데이터로만 계산..
                            requestResult.contrib = db.calculateContributionByRoom(msg.message);
                            requestResult.keywordContrib = db.calculateKeywordContributionByRoom(msg.message);
                            requestResult.cont = db.searchMessageRoom(msg.message);

                            if(clientList.get(conn).getLogin())
                                requestResult.markData = db.IdAndRoomForMarked(clientList.get(conn).getId(),msg.message);
                            else
                                requestResult.markData = db.IdAndRoomForMarked(talker,msg.message);
                            
                            System.out.println(gson.toJson(requestResult));
                            out.println(gson.toJson(requestResult));

                            break;
                    }

                    long endTime = System.currentTimeMillis();
                    System.out.println("Elapsed Time: " + (endTime-startTime)/1000.0 + "s");
                } catch (Exception e) {
                    e.printStackTrace();

                    if(roomList.get(clientList.get(conn).getNumber()) != null) roomList.get(clientList.get(conn).getNumber()).remove(conn);
                    clientList.remove(conn);

                    System.out.println("Socket Close");
                    break;
                }
            }
        }

    }

}


