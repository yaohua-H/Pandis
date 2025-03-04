package command;

/**
 * @Description Command接口的骨架实现，具体Command实现都应该继承该骨架实现
 * @Author huzihan
 * @Date 2021-07-21
 */
public abstract class AbstractCommand implements Command{

    // 命令名字
    private String name;
    // 参数个数
    private int arity;
    // 表示参数数量是否 >= arity的值
    private boolean isGreaterThanArity;
    // 字符串表示的 FLAG，记录了命令的属性
    private String stringFlags;
    // 实际的 FLAG，对stringFlags进行分析得到
    private int flags;

    public AbstractCommand(String name, int arity,boolean isGreaterThanArity,  String stringFlags) {
        this.name = name;
        this.arity = arity;
        this.isGreaterThanArity = isGreaterThanArity;
        this.stringFlags = stringFlags;
        this.flags = 0;
    }

    /**
     * 获取命令名称
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * 获取命令参数个数
     * @return
     */
    public int getArity() {
        return this.arity;
    }

    /**
     * 命令参数是否可能大于等于设置的数值
     * @return
     */
    public boolean isGreaterThanArity() {
        return this.isGreaterThanArity;
    }

}
