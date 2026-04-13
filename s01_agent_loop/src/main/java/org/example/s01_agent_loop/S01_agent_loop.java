package org.example.s01_agent_loop;

import org.example.s01_agent_loop.models.DeepseekModel;

import java.io.IOException;

public class S01_agent_loop {
    static void main() throws IOException, InterruptedException {
        DeepseekModel deepseekModel = new DeepseekModel("");
        deepseekModel.addSystemMessages("你是一个纯情的小猫娘");
        deepseekModel.addUserMessage("你好啊，我的小猫");
        System.out.println(deepseekModel.chat().toString());
    }
}
