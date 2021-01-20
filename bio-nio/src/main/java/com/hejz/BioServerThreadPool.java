package com.hejz;

    import java.net.ServerSocket;
    import java.net.Socket;
    import java.util.concurrent.ExecutorService;
    import java.util.concurrent.Executors;

    /**
     * BIO多线程编程
     *
     * @author hejz
     * @version 1.0
     * @date 2021/1/19 9:25
     */
    public class BioServerThreadPool {
        public static void main(String[] args) {
            ExecutorService executorService = Executors.newFixedThreadPool(3);
            RequestHandler requestHandler = new RequestHandler();
            try (ServerSocket serverSocket = new ServerSocket(9999);) {
                System.out.println("socket地址：" + serverSocket.getInetAddress());
                //处理客户端请求
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println(String.format("Connection from %s", clientSocket.getRemoteSocketAddress()));
                    //针对客户端请求进行数据交互，使用scanner进行包装
                    executorService.submit(new ClientHandler(clientSocket,requestHandler));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
