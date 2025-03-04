package server;

import client.PandisClient;
import event.handler.AcceptTcpHandler;
import event.EventLoop;
import org.apache.commons.logging.LogFactory;
import server.config.ServerConfig;
import org.apache.commons.logging.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @description:
 * @author: huzihan
 * @create: 2021-07-04
 */
public class PandisServer {

    private ServerConfig serverConfig;  // 服务端配置
    private EventLoop eventLoop;        // 事件循环
    private List<PandisClient> clients; // 保存了所有连接到服务器的客户端结构
    private static volatile PandisServer serverInstance; // 服务器实例

    private volatile PandisClient currentClient;    // 当前客户端，仅用于奔溃报告

    private Log logger = LogFactory.getLog(PandisServer.class);

    private PandisDatabase [] databases;

    public PandisServer() {
        super();
    }

    public static void main(String[] args) {
        PandisServer server = new PandisServer();

        PandisServer.serverInstance = server;

        // 初始化服务器默认配置
        server.initServerConfig();

        // 解析命令行参数
        String options = server.parsingCommandLineArgs(args);

        // 如果命令行参数中指定了配置文件，那么就从配置文件加载配置，配置文件的配置会覆盖同名默认配置
        if (server.getServerConfig().getConfigfile() != null) {
            server.loadServerConfigFromFile(server.getServerConfig().getConfigfile());
        }
        // 如果命令行中显式地设置来配置参数，那么命令行配置又会覆盖已有配置
        // 配置优先级：命令行配置 > 配置文件 > 默认配置
        if (options != null) {
            server.loadServerConfigFromString(options);
        }

        // 初始化服务器
        server.initServer();

        // 运行事件循环，不断处理事件，直到服务器关闭为止
        server.eventLoop.eventLoopMain();

    }

    /**
     * 初始化服务器
     */
    private void initServer() {
        // 创建事件循环对象
        this.eventLoop = EventLoop.createEventLoop();

        // 创建保存客户端结构的链表
        this.clients = new LinkedList<>();

        // 创建数据库
        this.databases = new PandisDatabase[this.serverConfig.getDbNumber()];
        for (int i = 0; i < this.serverConfig.getDbNumber(); i++) {
            this.databases[i] = new PandisDatabase(i);
        }

        // 打开TCP监听端口
        ServerSocketChannel serverSocketChannel = null;
        try {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(this.serverConfig.getPort()));
            serverSocketChannel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 创建并初始化数据库;

        // 向事件循环中的监听模块注册事件
        // 为 TCP 连接关联连接应答（accept）处理器
        // 用于接受并应答客户端的 connect() 调用（accept）处理器
        this.eventLoop.registerFileEvent(serverSocketChannel, SelectionKey.OP_ACCEPT, AcceptTcpHandler.getHandler(), null);
    }

    private void initServerConfig() {
        this.serverConfig = ServerConfig.build();
    }

    private void loadServerConfigFromFile(String filePath) {
        this.serverConfig.loadConfigFromFile(filePath);
    }

    private void loadServerConfigFromString(String options) {
        this.serverConfig.loadConfigFromString(options);
    }

    /**
     * 解析命令行参数
     * @param args
     */
    private String parsingCommandLineArgs(String [] args) {
        if (args.length == 0) {
            return null;
        }

        StringBuilder options = new StringBuilder();
        int index = 0;

        // 处理特殊选项 -h、-v
        if ("-v".equals(args[0]) || "--version".equals(args[0])) {

        }

        if ("-h".equals(args[0]) || "--help".equals(args[0])) {

        }

        /**
         * 判断第一个参数是否是配置文件
         * 如果想使用配置文件，必须在命令行第一个参数指定配置文件路径
         * 如果第一个参数不是以"--"开头，那么应该是一个配置文件
         */
        if (args[index].charAt(0) != '-' && args[index].charAt(1) != '-') {
            this.getServerConfig().setConfigfile(args[index]);
            index++;
        }


        /**
         * 对用户给定的其余选项进行分析，并将分析所得的字符串追加稍后载入的配置文件的内容之后
         * 比如 --port 6380 会被分析为 "port 6380\n"
         */
        while (index < args.length) {
            if (args[index].charAt(0) == '-' && args[index].charAt(1) == '-') {
                if (options.length() > 0) {
                    options.append("\n");
                }
                options.append(args[index].substring(2));
                options.append(" ");
            } else {
                options.append(args[index]);
                options.append(" ");
            }

            index++;
        }

        return options.toString();
    }

    public  EventLoop getEventLoop() {
        return this.eventLoop;
    }

    public void addClient(PandisClient client) {
        this.clients.add(client);
    }

    public static PandisServer getInstance() {
        return serverInstance;
    }

    public synchronized void setCurrentClient(PandisClient client) {
        this.currentClient = client;
    }

    public synchronized void clearCurrentClient() {
        this.currentClient = null;
    }

    /**
     * 销毁连接的客户端
     * @param client
     */
    public void distroyClient(SelectionKey key, PandisClient client) {
        if (this.currentClient.equals(client)) {
            this.clearCurrentClient();
        }

        this.clients.remove(client);

        if (client.getSocketChannel() != null) {
            this.eventLoop.unregisterFileEvent(key, SelectionKey.OP_READ);
            this.eventLoop.unregisterFileEvent(key, SelectionKey.OP_WRITE);
            client.distroy();
        }

        logger.info("disconnect with client.");
    }

    public ServerConfig getServerConfig() {
        return this.serverConfig;
    }

    public PandisDatabase[] getDatabases() {
        return this.databases;
    }

    private void version() {

    }

    private void usage() {

    }
}

