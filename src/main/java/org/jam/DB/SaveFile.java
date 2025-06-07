package org.jam.DB;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.time.LocalDate;

import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.lang.reflect.Type;



public class SaveFile {
    static String filePath = "src/main/Json/data.json";
    static Gson gson = new Gson();
//    public static void main(String[] args) {
//        SaveFile saveFile = new SaveFile();
//        saveFile.writeFile();
//    }

    public static void writeFile(Todo newtodo){
        LocalDate localDate = LocalDate.now();
        ArrayList<Todo> todoList = readFile();
        if(todoList == null){
//            newtodo.setPrimary_key(0);
        }
        else{
//            int pr = todoList.get(todoList.size() - 1).getPrimary_key();
//            newtodo.setPrimary_key(pr+1);
        }
//        Todo todo = new Todo("HW4",localDate.toString());

        todoList.add(newtodo);
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(todoList, writer);
            System.out.println("成功寫入 JSON 至 " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static ArrayList<Todo> readFile(){
        ArrayList<Todo> todoList = null;
        try (FileReader reader = new FileReader(filePath)) {
            Type listType = new TypeToken<ArrayList<Todo>>(){}.getType();
            todoList = gson.fromJson(reader, listType);

            System.out.println("成功讀取 JSON，筆數：" + todoList.size());
            for (Todo t : todoList) {
                System.out.println("Todo: " + t.title + " / " + t.time);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return todoList;
    }

}