package com.hejz;

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
