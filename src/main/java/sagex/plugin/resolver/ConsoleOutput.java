package sagex.plugin.resolver;

public class ConsoleOutput implements IOutput {
    @Override
    public void msg(String msg) {
        System.out.println("CONSOLE: " + msg);
    }

    @Override
    public void msg(Throwable t, boolean fatal) {
        t.printStackTrace();
    }
}
