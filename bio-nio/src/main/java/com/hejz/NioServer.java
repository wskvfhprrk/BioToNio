package com.hejz;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * nio演示——单线程
 *
 * @author hejz
 * @version 1.0
 * @date 2021/1/19 11:33
 */
public class NioServer {
    public static void main(String[] args) throws Exception {
        //01 创建了一个服务端的Channel：直接使用opne方法，底层源码根据不同的操作系统去建立不同的channel
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        //配置阻塞——false
        serverSocketChannel.configureBlocking(false);
        //serverChannel需要绑定端口
        serverSocketChannel.bind(new InetSocketAddress(6666));//现在客户端与服务器可以随意交换了

        //02 selector 专门进行轮询，判断socket状态
        Selector selector = Selector.open();
        //将一个个channel注册到selector上,并注册channel初化状态OP_ACCEPT
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        //创建一个byteBuffer进行临时存储
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        RequestHandler requestHandler = new RequestHandler();

        //对selector里面的channel进行轮询，判断谁将进行后续的IO操作
        while (true) {
            int select = selector.select();
            if (select == 0) {
                continue;
            }
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                //selectionKey保存了channel的所有信息
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) { //如果状态为初始化状态
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel clientChannel = channel.accept();
                    System.out.println("已连接客户端 %s：" + clientChannel.getRemoteAddress());
                    //配置阻塞——false
                    clientChannel.configureBlocking(false);
                    //将clientChannel状态(read或write)改变——再交给后续处理
                    clientChannel.register(selector, SelectionKey.OP_READ);
                }
                if (key.isReadable()) { //如果是被标记读的
                    //进行数据交互处理
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    //将客户端数据读取到buffer
                    clientChannel.read(byteBuffer);
                    String request = new String(byteBuffer.array()).trim();
                    byteBuffer.clear();
                    //写数据
                    System.out.println(String.format("from %s：%s", clientChannel.getRemoteAddress(), request));
                    //使用requestHandler处理数据
                    String res = requestHandler.handler(request);
                    clientChannel.write(ByteBuffer.wrap(res.getBytes(StandardCharsets.UTF_8)));
                }
                //移除iterator
                iterator.remove();

            }

        }
    }
}