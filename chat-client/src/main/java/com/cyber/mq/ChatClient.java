package com.cyber.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;
import com.rabbitmq.client.RpcClient;
import com.rabbitmq.client.RpcClientParams;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ChatClient {
    private static final String REGISTRATION_EXCHANGE = "example.chat.reg";
    private static final String MESSAGES_EXCHANGE = "example.chat.msg";
    private static final String CMD_LOGIN = "CMD_LOGIN";
    private static final String STR_DELIMITER = "##";

    private final Channel channel;
    private final String exclusiveQueue;
    private String nickName = "Anonymous";

    public ChatClient(Channel channel) throws IOException {
        this.channel = channel;
        this.exclusiveQueue = channel.queueDeclare().getQueue();
        initBindings();
        initCallbacks();
    }

    private void initBindings() throws IOException {
        channel.queueBind(exclusiveQueue, MESSAGES_EXCHANGE, "");
    }

    private void initCallbacks() throws IOException {
        channel.basicConsume(exclusiveQueue, false, (consumerTag, message) -> {
            printMessage(message);
            channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
        }, consumerTag -> {
        });
    }

    public String requestLogin() throws IOException {
        RpcClientParams rpcClientParams = new RpcClientParams()
                .channel(channel)
                .exchange(REGISTRATION_EXCHANGE)
                .routingKey("");
        RpcClient rpcClient = new RpcClient(rpcClientParams);

        try {
            return rpcClient.stringCall(CMD_LOGIN + STR_DELIMITER + STR_DELIMITER + exclusiveQueue);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String message) {
        sendString(MESSAGES_EXCHANGE, "", getNickName() + ": " + message);
    }

    private void printMessage(Delivery message) {
        System.out.println(new String(message.getBody(), UTF_8));
    }

    private void sendString(String exchangeName, String routingKey, String message) {
        try {
            channel.basicPublish(exchangeName, routingKey, null, message.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}
