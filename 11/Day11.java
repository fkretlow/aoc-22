import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


abstract class Expression {
    abstract public Long evaluate(Map<String, Long> bindings);
}

class Operation extends Expression {
    public Operation(OpCode op, Expression lhexpr, Expression rhexpr) {
        this.op = op;
        this.lhexpr = lhexpr;
        this.rhexpr = rhexpr;
    }

    public Long evaluate(Map<String, Long> bindings) {
        Long lh = lhexpr.evaluate(bindings);
        Long rh = rhexpr.evaluate(bindings);
        return switch (op) {
            case ADD:      yield lh + rh;
            case SUBTRACT: yield lh - rh;
            case MULTIPLY: yield lh * rh;
            case DIVIDE:   yield lh / rh;
        };
    }

    private OpCode op;
    private Expression lhexpr;
    private Expression rhexpr;

    public enum OpCode {
        ADD, SUBTRACT, MULTIPLY, DIVIDE;
        public static OpCode fromChar(char symbol) {
            return switch (symbol) {
                case '+': yield ADD;
                case '-': yield SUBTRACT;
                case '*': yield MULTIPLY;
                case '/': yield DIVIDE;
                default: throw new IllegalArgumentException(String.format("cannot parse operation '%c'", symbol));
            };
        }
    }
}

class Variable extends Expression {
    public Variable(String name) { this.name = name; }
    public Long evaluate(Map<String, Long> bindings) { return bindings.get(name); }
    private final String name;
}

class Scalar extends Expression {
    public Scalar(Long value) { this.value = value; }
    public Long evaluate(Map<String, Long> bindings) { return value; }
    private final Long value;
}


class MonkeyShenanigansEngine {
    public void playRound() {
        for (Monkey monkey : monkeys) {
            while (monkey.hasItems()) {
                MonkeyThrow monkeyThrow = monkey.throwItem();
                monkeys.get(monkeyThrow.recipient).catchItem(monkeyThrow.item);
            }
        }
    }

    public Long getMonkeyBusiness() {
        return monkeys
            .stream()
            .map(Monkey::getItemsInspected)
            .sorted((count1, count2) -> Long.compare(count2, count1))
            .limit(2)
            .reduce(1L, (x,y) -> x * y);
    }

    public void addMonkey(String src) { monkeys.add(Monkey.fromString(src)); }
    public void showMonkeys() { monkeys.forEach(m -> System.out.println(m.toString())); }

    private List<Monkey> monkeys = new ArrayList<>();

    public static class Monkey {
        public Monkey(Queue<Long> items, Expression operation, Predicate<Long> test, Integer[] recipients) {
            this.items = items;
            this.operation = operation;
            this.test = test;
            this.recipients = recipients;
        }

        public MonkeyThrow throwItem() {
            if (items.isEmpty()) return null;
            ++itemsInspected;
            Long itemToThrow = postInspectionRelief(preInspectionWorryIncrease(items.remove()));
            return new MonkeyThrow(itemToThrow, determineRecipient(itemToThrow));
        }

        public Boolean hasItems() { return !items.isEmpty(); }
        public void catchItem(Long item) { items.add(item); }
        public Long getItemsInspected() { return itemsInspected; }
        private Long preInspectionWorryIncrease(Long worryLevel) { return operation.evaluate(Map.of("old", worryLevel)); }
        private Long postInspectionRelief(Long worryLevel) { return worryLevel / POST_INSPECTION_RELIEF_FACTOR; }
        private Integer determineRecipient(Long worryLevel) { return test.test(worryLevel) ? recipients[0] : recipients[1]; }

        private final Queue<Long> items;
        private final Expression operation;
        private final Predicate<Long> test;
        private final Integer[] recipients;
        private Long itemsInspected = 0L;
        public static Long POST_INSPECTION_RELIEF_FACTOR = 3L;

        public String toString() {
            return "Monkey("
                + "items=" + items.stream().map(Object::toString).collect(Collectors.joining(",", "[", "]"))
                + String.format(", inspectedItems=%d", itemsInspected)
                + ")";
        }

        public static Monkey fromString(String src) {
            var lines = src.split("\n");
            var items = parseItems(lines[1]);
            var operation = parseOperation(lines[2]);
            var test = parseTest(lines[3]);
            var recipients = parseRecipients(lines[4], lines[5]);
            return new Monkey(items, operation, test, recipients);
        }
        
        private static Queue<Long> parseItems(String line) {
            if (!line.startsWith("  Starting items:")) throw new IllegalArgumentException(String.format("cannot parse items: '%s'", line));
            Pattern p = Pattern.compile("\\d+");
            Matcher m = p.matcher(line);
            Queue<Long> items = new LinkedList<>();
            while (m.find()) {
                items.add(Long.parseLong(m.group()));
            }
            return items;
        }

        private static Expression parseOperation(String line) {
            Pattern p = Pattern.compile("  Operation: new = (?<lh>old|\\d+) (?<op>[*/+-]) (?<rh>old|\\d+)");
            Matcher m = p.matcher(line);
            if (!m.matches()) throw new IllegalArgumentException(String.format("cannot parse operation: '%s'", line));
            var op = Operation.OpCode.fromChar(m.group("op").charAt(0));
            var lhexpr = m.group("lh").equals("old") ? new Variable("old") : new Scalar(Long.parseLong(m.group("lh")));
            var rhexpr = m.group("rh").equals("old") ? new Variable("old") : new Scalar(Long.parseLong(m.group("rh")));
            return new Operation(op, lhexpr, rhexpr);
        }

        private static Predicate<Long> parseTest(String line) {
            Pattern p = Pattern.compile("  Test: divisible by (?<divisor>\\d+)");
            Matcher m = p.matcher(line);
            if (!m.matches()) throw new IllegalArgumentException(String.format("cannot parse test: '%s'", line));
            Long divisor = Long.parseLong(m.group("divisor"));
            return x -> x % divisor == 0;
        }

        private static Integer[] parseRecipients(String line1, String line2) {
            Pattern p = Pattern.compile("\\d+");
            Matcher m = p.matcher(line1);
            if (!m.find()) throw new IllegalArgumentException(String.format("cannot parse recipient: '%s'", line1));
            Integer r1 = Integer.parseInt(m.group());
            m = p.matcher(line2);
            if (!m.find()) throw new IllegalArgumentException(String.format("cannot parse recipient: '%s'", line2));
            Integer r2 = Integer.parseInt(m.group());
            return new Integer[] { r1, r2 };
        }
    }

    private static record MonkeyThrow(Long item, Integer recipient) {}
}


public class Day11 {
    public static void main(String[] argv) throws IOException {
        String input = new String(Files.readAllBytes(Paths.get("input.txt")));
        part1(input);
    }

    private static void part1(String input) {
        MonkeyShenanigansEngine engine = new MonkeyShenanigansEngine();
        for (String src : input.split("\n\n")) { engine.addMonkey(src); }
        for (int remaining = 20; remaining > 0; --remaining) engine.playRound();
        System.out.println(String.format("1. Monkey business: %d", engine.getMonkeyBusiness()));
    }
}
