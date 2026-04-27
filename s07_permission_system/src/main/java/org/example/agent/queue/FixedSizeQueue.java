package org.example.agent.queue;

import java.util.LinkedList;

/**
 * 固定容量队列
 * 1. 长度<=fixedSize：正常入队
 * 2. 长度>fixedSize：加入新元素，自动弹出队首
 *
 * @param <T> 泛型
 */
public class FixedSizeQueue<T> extends LinkedList<T> {
    private final int fixedSize;

    public FixedSizeQueue(int fixedSize) {
        this.fixedSize = fixedSize;
    }

    /**
     * 添加元素
     */
    public T addWithLimit(T item) {
        offer(item);
        // 超出容量：先删掉最先进来的
        return size() > fixedSize ? poll() : null;
    }
}
