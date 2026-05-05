package org.example.queue;

import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 固定容量队列
 * 1. 长度<=fixedSize：正常入队
 * 2. 长度>fixedSize：加入新元素，自动弹出队首会话
 */
public class FixedSizeConversationQueue extends LinkedList<JSONObject> {
    private final int fixedSize;
    private int curSize = 0;

    public FixedSizeConversationQueue(int fixedSize) {
        if (fixedSize <= 0) {
            throw new IllegalArgumentException("队列长度必须大于0");
        }
        this.fixedSize = fixedSize;
    }

    /**
     * 添加元素
     */
    public List<JSONObject> add(JSONObject message, Predicate<JSONObject> isConversationEnd) {
        offer(message);
        if (isConversationEnd.test(message)) {
            curSize++;
        }

        List<JSONObject> polledConversation = new ArrayList<>();
        // poll出一个会话
        if (curSize > fixedSize) {
            do {
                polledConversation.add(poll());
            } while (!isConversationEnd.test(peek()));
            curSize--;
        }

        return polledConversation;
    }
}
