package com.naver.naverspeech.client;

public class Data {

    private String title;
    private String content;
    private String member;
    private String number;
    private String date;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(String date){this.date = date;}

    public void setMember(String member) {
        this.member = member;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getContent() {
        return content;
    }

    public String getMember() {
        return member;
    }

    public String getDate() {
        return date;
    }

    public String getNumber() {
        return number;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String toString(){
        return title + " " + content + " " + member + " " + number;
    }

}