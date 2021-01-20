package com.hejz;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 经典的BIO服务
 *
 * @author hejz
 * @version 1.0
 * @date 2021/1/18 17:39
 */
public class BioServer {
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8888);) {
            System.out.println("socket地址：" + serverSocket.getInetAddress());
            //处理客户端请求
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("BioServer has started,listening on port：" + clientSocket.getInetAddress());
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
                        String res = "From BioServer Hello" + request + ",\n";
                        clientSocket.getOutputStream().write(res.getBytes(StandardCharsets.UTF_8));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
