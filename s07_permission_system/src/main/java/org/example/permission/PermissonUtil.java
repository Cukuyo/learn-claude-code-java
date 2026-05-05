package org.example.permission;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PermissonUtil {
    public static List<PermissonRule> read(Path path) throws IOException{
         List<PermissonRule> list=new ArrayList<>();
         for (String line : Files.readAllLines(path)) {
            String[] arr = line.split(",");
            list.add(new PermissonRule(arr[0].trim(), arr[1].trim(),Pattern.compile(arr[1].trim()), arr[2].trim(), arr[3].trim(), arr[4].trim(), arr[5].trim(), arr[6].trim(), arr[7].trim()));
         }

         return list;
    }
    
    public static void write(Path path,List<PermissonRule> list) throws IOException{
        Files.write(path,list.stream().map(cv->cv.toString()).toList().iterator(),StandardCharsets.UTF_8);
    }
}
