package org.example.agents_company;

/**
 * 聊天内容
 */
public class ChatMessage implements Comparable<ChatMessage> {
    public ChatMessageType chatMessageType;
    public String name;
    public String content;

    public ChatMessage(ChatMessageType chatMessageType, String name, String content) {
        this.chatMessageType = chatMessageType;
        this.name = name;
        this.content = content;
    }

    @Override
    public int compareTo(ChatMessage chatMessage) {
        return Integer.compare(chatMessageType.priority, chatMessage.chatMessageType.priority);
    }
}
