package org.example.agents.context.queues;

import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 固定容量队列
 * 1. 长度<=fixedSize：正常入队
 * 2. 长度>fixedSize：加入新元素，自动弹出队首会话
 */
public class FixedSizeConversationQueue {
    private final int fixedSize;
    private int curSize = 0;
    private final List<List<JSONObject>> conversationQueue = new ArrayList<>();

    public FixedSizeConversationQueue(int fixedSize) {
        if (fixedSize <= 0) {
            throw new IllegalArgumentException("队列长度必须大于0");
        }
        this.fixedSize = fixedSize;
        conversationQueue.add(new ArrayList<>());
    }

    /**
     * 添加元素
     */
    public List<JSONObject> add(JSONObject message, boolean finished) {
        conversationQueue.getLast().add(message);

        if (finished) {
            curSize++;
            conversationQueue.add(new ArrayList<>());
        }

        // poll出一个会话
        if (curSize > fixedSize) {
            curSize--;
            return conversationQueue.removeFirst();
        }

        return new ArrayList<>();
    }

    /**
     * 获取元素总量
     */
    public int size() {
        AtomicInteger size = new AtomicInteger();
        conversationQueue.forEach(cv -> size.addAndGet(cv.size()));
        return size.get();
    }

    /**
     * 汇总元素
     */
    public List<JSONObject> collect() {
        List<JSONObject> list = new LinkedList<>();
        conversationQueue.forEach(list::addAll);
        return list;
    }
}
