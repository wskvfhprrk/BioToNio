# BioToNio
从BIO到NIO的深化

[githup地址]: (https://github.com/wskvfhprrk/BioToNio)	"githup源码地址"
[视频地下]: (https://live.csdn.net/room/weixin_48013460/sKuDf3uE?utm_source=1181338978)	"直播视频 "



## java BIO到NIO的演变

> IO是什么? input、output

![1610961009837](https://github.com/wskvfhprrk/BioToNio/blob/main/imgs/1610961009837.png)

- 硬盘交IO

![1610961054681](https://github.com/wskvfhprrk/BioToNio/blob/main/imgs/1610961054681.png)

- 浏览器IO

![1610961067823](https://github.com/wskvfhprrk/BioToNio/blob/main/imgs/1610961067823.png)

java必须通过操作系统与硬件进行IO操作

![1610961189763](https://github.com/wskvfhprrk/BioToNio/blob/main/imgs/1610961189763.png)

其实质是与操作系统的内核空间进行交换数据

![1610961429175](https://github.com/wskvfhprrk/BioToNio/blob/main/imgs/1610961429175.png)

方法一：阻塞式IO

1. java #read 基于Thread main、T1、T2 java虚拟机栈、栈帧表示 一个方法的执行

   thread:o 为什么会衩执行？CPU资源：Thread Lifecyle State

   java线程阻塞与不阻塞：IO层面，表示 当前线程除了此IO不能进行其他操作——阻塞

2. 内核空间接收到read请求，进一步跟硬盘要数据

3. 硬盘准备数据，把数据复制到内核 空间的缓冲区

4. 把内核空间缓冲上区的数据复制到用户空间

   ![1610962239661](https://github.com/wskvfhprrk/BioToNio/blob/main/imgs/1610962239661.png)

   ​		同步阻塞IO模型源于《UNIX网络编程卷1：套接字联网API（第3版）》

编写BIOServer

```java
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
```

打开cmd调试

![1611021230045](https://github.com/wskvfhprrk/BioToNio/blob/main/imgs/1611021230045.png)

使用线程池

1、建立BioServerThreadPool:

```java
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
```

2、处理客户端逻辑RequestHandler

```java
/**
 * 处理客户端请求数据
 *
 * @author hejz
 * @version 1.0
 * @date 2021/1/19 9:27
 */
public class RequestHandler {
    public String handler(String request) {
        return "From BioServer Hello" + request + ",\n";
    }
}
```

3、线程池处理客户端socket连接ClientHandler

```java
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
                String res = "client from" + request + ",\n";                clientSocket.getOutputStream().write(res.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}

```

4、测试：

> "C:\Program Files\Java\jdk1.8.0_131\bin\java.exe"。。。
> socket地址：0.0.0.0/0.0.0.0
> Connection from /0:0:0:0:0:0:0:1:64003
> Connection from /0:0:0:0:0:0:0:1:64003：asdasd
> Connection from /0:0:0:0:0:0:0:1:64003：asd
> Connection from /0:0:0:0:0:0:0:1:64003：sdf
> Connection from /0:0:0:0:0:0:0:1:64013
> Connection from /0:0:0:0:0:0:0:1:64013：sadf
> Connection from /0:0:0:0:0:0:0:1:64013：df
> Connection from /0:0:0:0:0:0:0:1:64013：sdf

**由于线程池大小是3，最多可连接3个客户端**



![1611022038370](https://github.com/wskvfhprrk/BioToNio/blob/main/imgs/1611022038370.png)

方法2 read、 recvfrom同步非阻塞

- (1)用户空间跟内核空间要数据的时候,如果数据没有准备好,那么 Thread不阻塞
- (2)Thread会主动不断地询问内核空间,数据有没有准备好
- (3)如果内核空间数据准备好了,那么 Thread就会将数据从内核空间复制到用户空间【阻塞】

![1611022532984](https://github.com/wskvfhprrk/BioToNio/blob/main/imgs/1611022532984.png)

​					同步非阻塞IO模型源于《UNIX网络编程卷1：套接字联网API（第3版）》

**同步和异步的区别是：应用程序是否关心操作系统内核是否准备好数据，同步与阻塞不是相等的，同步可以阻塞，也可以是非阻塞**

poll、epoll、select模型——可查相关资料（面试常问的）

jdk:IO、垃圾收集器

JDK1.0 ：java.o.XXX **[Blocking阻塞式的I0]**

​	InputStream#read->native int reado-c++read内核空间Outputstream

​	Reader

​	Writer

JDK1.4：

开始就支持java.nio.XXX

这些类就是基于同步非阻塞O模型&&?????

验证多个客户端同时连接当前的服务端,服务端的单线程是否可以同时处理?

java.io+socket+处理多个客户端的请求:

![1611022715673](https://github.com/wskvfhprrk/BioToNio/blob/main/imgs/1611022715673.png)

​					同步IO阻塞模型源于《UNIX网络编程卷1：套接字联网API（第3版）》

`serverSocket.accept()`本身也是阻塞的，使用线程

伪代码：

​	sockets  single thread

​		map. put(socketA, Accepted)

​		map. put(socketB, Accepted)

​		map.put(socketC, Accpted)

​		socketa--->read  map. put(socketa, readable)

使用线程来进行数据的交互处理

NIO中3个比较常见的类

​	Channel: ServerSocketChannel. Socketchannel

​	Buffer

​	Selectordemo

nio代码：

```java
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
```

其它使用到nio的tomcat源码中bind()中就是nio，直接用netty框架最好

netty:本质就是对NIO的封装和优化

​	NettyHandler

​		ctx:写数据

​		msg:读数据

有很多中间件，为了并发用到netty

​	dubbo、rocketmq、spring cloud getway、spring5 web flux、seata(阿里分布式事务)



jdk1.7之后出现 NIO2并不是异步NIO,是伪异步NIO

![1611129317983](https://github.com/wskvfhprrk/BioToNio/blob/main/imgs/1611129317983.png)

下面是异步NIO

![1611023388670](https://github.com/wskvfhprrk/BioToNio/blob/main/imgs/1611023388670.png)

​			异步IO模型源于《UNIX网络编程卷1：套接字联网API（第3版）》


