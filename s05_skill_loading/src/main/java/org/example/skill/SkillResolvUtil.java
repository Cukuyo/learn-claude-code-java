package org.example.skill;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillResolvUtil {
    private static final String SEPARATOR = "---";

    public static List<SkillManifest> resolve(String path) throws IOException{
        List<SkillManifest> list = new ArrayList<>();

        Files.walk(Paths.get(path))
                 .filter(Files::isRegularFile)
                 .filter(file -> "skill.md".equalsIgnoreCase(file.getFileName().toString()))
                 .forEach(file -> list.add(resolve(file)));

        return list;
    }

    private static SkillManifest resolve(Path path) throws IOException{
        List<String> allLines = Files.readAllLines(path);
        int separatorNum = 0;
        Map<String,String> meta =new HashMap<>();
        for (String line : allLines) {
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith(SEPARATOR)) {
                separatorNum++;
            }
            if (separatorNum == 2) {
                break;
            }
            String[] arr = line.split(":");
            meta.put(arr[0].trim(), arr[1].trim());
        }

        return new SkillManifest(meta.get("name"),meta.get("description"), path.toFile().getParentFile().toPath());
    }
}
