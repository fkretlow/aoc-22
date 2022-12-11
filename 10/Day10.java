import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public void addPreCycleHook(Consumer<State> f) {
        preCycleHooks.add(f);
    }

    private int cycle = -1; // 0-based counting for cycles internally
    private int register = 1;
    private List<Consumer<State>> preCycleHooks = new ArrayList<>();

    public void tick() {
        ++cycle;
        preCycleHooks.forEach(f -> f.accept(new State(cycle, register)));
    }

    private void addX(int x) {
        tick();
        tick();
        register += x;
    }

    private void noop() {
        tick();
    }

    public record State(
        int cycle,
        int register
    ) {}
}


class SignalStrengthMeter {
    public void onCycle(CPU.State state) {
        int cycle = state.cycle() + 1;
        if (cycle % 40 == 20 && cycle <= 220) {
            signalStrength += cycle * state.register();
        }
    }

    private Integer signalStrength = 0;
    public Integer getSignalStrength() { return signalStrength; }
}


class CRT {
    public void onCycle(CPU.State state) {
        spritePosition = state.register();
        drawPixel(isSpriteVisible(state.cycle()) ? '#' : '.', state.cycle());
    }

    public String getScreen() {
        return Stream.of(screen)
            .map(String::valueOf)
            .collect(Collectors.joining("\n"));
    }

    private boolean isSpriteVisible(int cycle) {
        int col = cycle % 40;
        return Math.abs(col - spritePosition) <= 1;
    }

    private void drawPixel(char pixel, int cycle) {
        int col = cycle % 40;
        int row = cycle / 40;
        if (col < WIDTH && row < HEIGHT) {
            screen[row][col] = pixel;
        } else {
            System.out.println(String.format( "pixel position off screen: cycle=%d -> row=%d, col=%d", cycle, row, col));
        }
    }

    private static final int WIDTH = 40;
    private static final int HEIGHT = 6;
    private int spritePosition = 0;
    private char[][] screen = new char[HEIGHT][WIDTH];
}


public class Day10 {
    public static void main(String[] args) {
        CPU cpu = new CPU();
        SignalStrengthMeter meter = new SignalStrengthMeter();
        CRT crt = new CRT();
        cpu.addPreCycleHook(meter::onCycle);
        cpu.addPreCycleHook(crt::onCycle);

        try (Stream<String> lines = Files.lines(Paths.get("input.txt"))) {
            lines.forEach(cpu::handle);
        } catch (IOException e) {
            e.printStackTrace();
        }
        cpu.tick(); // don't loose the effect of a trailing add instruction

        System.out.println("1. Accumulated signal strength: " + meter.getSignalStrength().toString());
        System.out.println("2. The screen of the CRT:");
        System.out.println(crt.getScreen());
    }
}
