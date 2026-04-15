package org.example.todo;

import java.util.List;

public class TodoManager {
    // public static
    public static record PlanItem(String content,String status) {

    }
    public String update(List<PlanItem> planItems){
        return "";
    }
}
