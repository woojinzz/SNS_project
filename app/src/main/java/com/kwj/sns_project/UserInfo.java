package com.kwj.sns_project;

public class UserInfo {

    String name;
    String hp;
    String birthday;
    String addr;
    String photoUrl;

    public UserInfo(String name, String hp, String birthday, String addr, String photoUrl){

        this.name = name;
        this.hp = hp;
        this.birthday = birthday;
        this.addr = addr;
        this.photoUrl = photoUrl;
    }

    public UserInfo(String name, String hp, String birthday, String addr){

        this.name = name;
        this.hp = hp;
        this.birthday = birthday;
        this.addr = addr;
    }

    public String getName(){
        return this.name;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getHp(){return this.hp;}
    public void setHp(String hp){
        this.hp = hp;
    }

    public String getBirthday(){
        return this.birthday;
    }
    public void setBirthday(String birthday){
        this.birthday = birthday;
    }

    public String getAddr(){
        return this.addr;
    }
    public void setAddr(String addr){
        this.addr = addr;
    }

    public String getPhotoUrl(){
        return this.photoUrl;
    }
    public void setPhotoUrl(String photoUrl){
        this.photoUrl = photoUrl;
    }
}
