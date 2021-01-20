package com.hejz;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 处理客户端连接
 *
 * @author hejz
 * @version 1.0
 * @date 2021/1/19 9:39
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final RequestHandler requestHandler;

    public ClientHandler(Socket clientSocket, RequestHandler requestHandler) {
        this.clientSocket = clientSocket;
        this.requestHandler = requestHandler;
    }

    @Override
    public void run() {
        //针对客户端请求进行数据交互，使用scanner进行包装
        try (Scanner input = new Scanner(clientSocket.getInputStream())) {
            //不断的和socket进行交互
            while (true) {
                String request = input.nextLine();
                //如果输入的是quit表示中断socket连接
                if ("quit".equals(request)) {
                    break;
                }
                System.out.println(String.format("Connection from %s：%s", clientSocket.getRemoteSocketAddress(), request));
                String res = "client from" + request + ",\n";
                clientSocket.getOutputStream().write(res.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
