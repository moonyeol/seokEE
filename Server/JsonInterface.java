import java.util.ArrayList;
import java.util.HashMap;

public class JsonInterface {}

class LoginInfo{
    String id;
    String pw;
}

class Member {
    String id;
    String password;
    String gender;
    String birth;
    String nickname;

    Member() {

    }

    Member(String id, String pw, String gender, String birth, String nick) {
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
class RequestResult {
    public HashMap<String, Integer> wordFrequency;
    public HashMap<String, Integer> fiveKeyWord;
    public HashMap<String, Double> contrib;
    public HashMap<String, Double> keywordContrib;
    public ArrayList<Talk> cont;
    public String markData;
    public String roomName;
    public String date;
    public String end;
}
class RequestUserInfo {
    public int func;
    public String id;
    public String nickname;
    public ArrayList<History> histories;
    public String talkWithMe;
    public ArrayList<Double> contributionData;
}
class RequestUserList {
    public int func;
    public ArrayList<String> username;

    public void setFunc(int func){
        this.func = func;
    }
    public int getFunc(){
        return this.func;
    }
    public void setUserName(ArrayList<String> userName){
        this.username = userName;
    }
    public ArrayList<String> getUserName(){
        return this.username;
    }
}
class SocketMessage {
    public int func;
    public String message;

    SocketMessage(){

    }

    SocketMessage(int func, String message){
        this.func = func;
        this.message = message;
    }

    public String toString(){
        return this.func + " " + this.message;
    }
}
class Talk {
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
class History {
    public String number;
    public String content;
    public String date;
    public String members;
    public String title;

    public void setNumber(String number){
        this.number = number;
    }
    public String getNumber(){
        return this.number;
    }
    public void setContent(String content){
        this.content = content;
    }
    public String getContent(){
        return this.content;
    }
    public void setDate(String date){
        this.date = date;
    }
    public String getDate(){
        return this.date;
    }
    public void setMembers(String members){
        this.members = members;
    }
    public String getMembers(){
        return this.members;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public String getTitle(){
        return this.title;
    }
}
class MemberList{
    ArrayList<String> list;
}
class SimpleTalk{
    public String talker;
    public String content;
}
class Title{
    public String title;
    public String pincode;
}