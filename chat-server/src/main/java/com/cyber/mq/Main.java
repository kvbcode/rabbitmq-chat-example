package com.cyber.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Main {
    private static final String RABBITMQ_HOST = "localhost";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBITMQ_HOST);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()
        ) {
            ChatServer server = new ChatServer(channel);
            server.mainLoop();
        }
    }


}
