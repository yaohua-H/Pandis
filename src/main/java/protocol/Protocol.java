package protocol;

import utils.SafeEncoder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @Description 该类表示pandis命令格式协议相关的内容
 * @Author huzihan
 * @Date 2021/7/26
 **/
public class Protocol {
    // 客户端和服务器发送的命令或数据一律以\r\n（CRLF）结尾
    public static final String DELIMITER = "\r\n";
    public static final String BULK_PREFIX = "$";
    public static final String MULTI_BULK_PREFIX = "*";
    public static final Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * 根据Panis的协议格式，使用给定的参数数组argv来构建命令
     * @param startIndex 参数数组开始解析的位置
     * @param argv 参数数组
     * @return 构建的命令
     */
    public static String formatCommand(int startIndex, final String [] argv) {
        if (startIndex >= argv.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        // 实际要解析的数组长度
        int argc = argv.length - startIndex;

        // 根据协议来构建命令
        StringBuilder cmd = new StringBuilder();
        cmd.append(MULTI_BULK_PREFIX).append(argc).append(DELIMITER);
        for (int index = startIndex; index < argv.length; index++) {
            // note: "$n"中填入的length其实是参数数据的字节长度，但由于我们是用string表示的数据，所以要先按utf8转换成字节数组，获取字节数组长度
            cmd.append(BULK_PREFIX).append(SafeEncoder.encode(argv[index]).length).append(DELIMITER);
            cmd.append(argv[index]).append(DELIMITER);
        }

        return cmd.toString();
    }
}
