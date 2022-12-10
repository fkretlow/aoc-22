import java.util.function.BiConsumer;

class CPU {
    public void handle(String instruction) {
        var components = instruction.split(" ");
        if (components[0].equals("noop")) {
            noop();
        } else if (components[0].equals("addx")) {
            addX(Integer.parseInt(components[1]));
        } else {
            throw new IllegalArgumentException("cannot parse instruction " + instruction);
        }
    }

    public void setPreCycleHook(BiConsumer<Integer, Integer> f) {
        preCycleHook = f;
    }

    public enum Op { ADDX, NOOP }

    private int cycle = 0;
    private int register = 1;
    private Op op = Op.NOOP;
    private int argument = 0;
    private int remainingCycles = 0;
    private BiConsumer<Integer, Integer> preCycleHook = null;

    public void tick() {
        cycle = cycle + 1;
        if (preCycleHook != null) preCycleHook.accept(cycle, register);
        remainingCycles = remainingCycles - 1;
    }

    private void addX(int x) {
        op = Op.ADDX;
        argument = x;
        remainingCycles = 2;
        tick();
        tick();
        register = register + x;
    }

    private void noop() {
        op = Op.NOOP;
        remainingCycles = 1;
        tick();
    }
}

public class Main {
    public static void main(String[] args) {
        CPU cpu = new CPU();
        cpu.setPreCycleHook((cycle, register) -> {
            System.out.println(cycle.toString() + ":" + register.toString());
        });
        cpu.handle("noop");
        cpu.handle("addx -2");
        cpu.tick();
    }
}
