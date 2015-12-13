package sagex.plugin.resolver;

public interface IOutput {
    void msg(String msg);

    void msg(Throwable t, boolean fatal);
}
