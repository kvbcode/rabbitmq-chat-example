package com.cyber.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.StringRpcServer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class LoginRpcServer extends StringRpcServer {
    private static final String STR_DELIMITER = "##";
    private final Map<String, Function<String[], String>> requestHandlerMap = new HashMap<>();

    public LoginRpcServer(Channel channel, String queueName) throws IOException {
        super(channel, queueName);
    }

    public void addHandler(String command, Function<String[], String> handler) {
        requestHandlerMap.put(command.toUpperCase(), handler);
    }

    @Override
    public String handleStringCall(String request) {
        String[] requestParts = request.split(STR_DELIMITER);
        if (requestParts.length < 1) return "";

        String command = requestParts[0].toUpperCase();
        return requestHandlerMap.getOrDefault(command, arg -> "").apply(requestParts);
    }

}
