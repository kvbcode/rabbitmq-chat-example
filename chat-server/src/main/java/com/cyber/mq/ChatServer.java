package com.cyber.mq;

import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.rabbitmq.client.BuiltinExchangeType.DIRECT;
import static com.rabbitmq.client.BuiltinExchangeType.FANOUT;

public class ChatServer {
    private static final String CMD_LOGIN = "CMD_LOGIN";
    private static final String MESSAGES_EXCHANGE = "example.chat.msg";
    private static final String REGISTRATION_EXCHANGE = "example.chat.reg";
    private static final String REGISTRATION_QUEUE = "example.chat.reg.server_queue";

    private final AtomicInteger userCounter = new AtomicInteger(1);
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private final Channel channel;
    private final LoginRpcServer loginRpcServer;

    public ChatServer(Channel channel) throws IOException {
        this.channel = channel;
        initBindings();
        initScheduledTasks();
        loginRpcServer = new LoginRpcServer(channel, REGISTRATION_QUEUE);
        initRpcHandlers();
    }

    private void initBindings() throws IOException {
        channel.exchangeDeclare(MESSAGES_EXCHANGE, FANOUT);

        channel.exchangeDeclare(REGISTRATION_EXCHANGE, DIRECT);
        channel.queueDeclare(REGISTRATION_QUEUE, false, false, false, null);
        channel.queueBind(REGISTRATION_QUEUE, REGISTRATION_EXCHANGE, "");
    }

    private void initScheduledTasks() {
        Runnable serverTimeAnnouncer = () -> sendNotice("server time: " + OffsetDateTime.now());
        scheduledExecutorService.scheduleAtFixedRate(serverTimeAnnouncer, 60, 60, TimeUnit.SECONDS);
    }

    private void initRpcHandlers() {
        Function<String[], String> loginNameHandler = requestParts -> {
            if (requestParts.length < 3) return "";

            String userName = requestParts[1].isEmpty()
                    ? "User-" + userCounter.getAndIncrement()
                    : requestParts[1];

            String userQueueName = requestParts[2];
            System.out.println("new user: " + userName + " (queue: " + userQueueName + ")");
            return userName;
        };
        loginRpcServer.addHandler(CMD_LOGIN, loginNameHandler);
    }

    public void mainLoop() throws IOException {
        loginRpcServer.mainloop();
    }

    public void sendNotice(String message) {
        sendString(MESSAGES_EXCHANGE, "", message);
    }

    public void sendString(String exchangeName, String routingKey, String message) {
        try {
            channel.basicPublish(exchangeName, routingKey, null, message.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
