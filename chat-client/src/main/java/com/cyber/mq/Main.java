package com.cyber.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Scanner;

public class Main {
    private static final String RABBITMQ_HOST = "localhost";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RABBITMQ_HOST);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel();
             Scanner scanner = new Scanner(System.in);
        ) {
            ChatClient chatClient = new ChatClient(channel);

            String nickName = chatClient.requestLogin();
            chatClient.setNickName(nickName);

            System.out.println("logged in as " + nickName);

            if (args.length > 0 && "auto".equals(args[0])) {
                for (int i = 1; ; i++) {
                    chatClient.sendMessage("message #" + i);
                    Thread.sleep(5000);
                }
            }

            while (true) {
                chatClient.sendMessage(scanner.nextLine());
            }

        }
    }
}
