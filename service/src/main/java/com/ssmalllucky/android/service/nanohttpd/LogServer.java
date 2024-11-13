package com.ssmalllucky.android.service.nanohttpd;

import android.content.Context;
import android.util.Log;

import com.ssmalllucky.android.service.nanohttpd.http.IHTTPSession;
import com.ssmalllucky.android.service.nanohttpd.http.NanoHTTPD;
import com.ssmalllucky.android.service.nanohttpd.http.request.Method;
import com.ssmalllucky.android.service.nanohttpd.http.response.Response;
import com.ssmalllucky.android.service.nanohttpd.http.response.Status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;


/**
 * @ClassName LogServer
 * @Author shuaijialin
 * @Date 2024/11/7
 * @Description 本地日志监测服务器
 */
public class LogServer extends NanoHTTPD {

    private static final String TAG = "LogServer";

    private Context context;

    private String hostname;

    private int port;

    public LogServer(int port) {
        super(port);
        this.port = port;
    }

    public LogServer(String hostname, int port) {
        super(hostname, port);
        this.hostname = hostname;
        this.port = port;
    }

    public LogServer(Context context, String hostname, int port) {
        super(hostname, port);
        this.context = context;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Log.d("TAG", "serve: " + session.getUri());

        if (context == null) {
            return Response.newFixedLengthResponse(Status.OK, "text/plain", "Context is null.");
        }

        String logFilePath = context.getExternalCacheDir().getPath() + "/log.txt";
        File logFile = new File(logFilePath);

        if (!logFile.exists()) {
            return Response.newFixedLengthResponse(Status.OK, "text/plain", "Log File Not found.");
        }

        try {
            FileInputStream fileInputStream = new FileInputStream(logFile);
            byte[] buffer = new byte[(int) logFile.length()];
            fileInputStream.read(buffer);
            fileInputStream.close();
            return Response.newFixedLengthResponse(Status.OK, "text/plain", new String(buffer));
        } catch (Exception e) {
            Log.d(TAG, "exception: " + e.getCause());
            return Response.newFixedLengthResponse(Status.OK, "text/plain", "Not found.");
        }
    }

    /**
     * 启动服务器方法
     * <p>
     * 此方法尝试启动服务器，并在指定端口上监听连接
     * 如果服务器成功启动，它将在日志中记录服务器启动的消息
     * 如果启动过程中发生IOException，它将捕获异常并记录异常原因
     */
    public void startServer() {
        try {
            // 尝试启动服务器
            start();
            // 服务器启动成功后，打印日志信息，包含服务器启动的端口
            Log.d("TAG", "Server started on port " + port);
        } catch (IOException e) {
            // 捕获IOException，并打印异常原因
            Log.d("TAG", "startServer: exception: " + e.getCause());
        }
    }

    /**
     * 判断是否为CORS 预检请求请求(Preflight)
     *
     * @param session
     * @return
     */
    private static boolean isPreflightRequest(IHTTPSession session) {
        Map<String, String> headers = session.getHeaders();
        return Method.OPTIONS.equals(session.getMethod())
                && headers.containsKey("origin")
                && headers.containsKey("access-control-request-method")
                && headers.containsKey("access-control-request-headers");
    }
}
