package org.jam.DB;



public class Todo {
    public int primary_key = -1;
    public String title;
    public String detail;
    public String time;
    public Boolean isFinish = false;

    public Todo(String title, String time,String detail){
        this.title = title;
        this.time = time;
        this.detail = detail;
    }

    public int getPrimary_key(){
        return primary_key;
    }

    public void setPrimary_key(int primary_key){
        this.primary_key = primary_key;
    }
    public void setFinish(){
        this.isFinish = !this.isFinish;
    }



}
