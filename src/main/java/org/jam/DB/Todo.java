package org.jam.DB;

import java.util.Date;

public class Todo {
    String title;
    String time;

    public Todo(String title, Date time){
        this.title = title;
        this.time = time.toString();
    }
}
