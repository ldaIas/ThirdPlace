package com.thirdplace.roomchat;

public record ChatMessage(
    String username,
    String message,
    String conversationId
) {}