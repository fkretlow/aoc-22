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
                monkey.inspectItem();
                Long item = monkey.getItem();
                item = preInspectionWorryIncrease(monkey.getOperation(), item);
                if (doPostInspectionRelief) item = postInspectionRelief(item);
                Integer recipient = monkey.getRecipient(item % monkey.getTestDivisor() == 0);
                monkeys.get(recipient).catchItem(item % productOfDivisors);
            }
        }
    }

    private Long preInspectionWorryIncrease(Expression operation, Long worryLevel) {
        return operation.evaluate(Map.of("old", worryLevel));
    }

    private Long postInspectionRelief(Long worryLevel) {
        return worryLevel / POST_INSPECTION_RELIEF_FACTOR;
    }

    public Long getMonkeyBusiness() {
        return monkeys
            .stream()
            .map(Monkey::getItemsInspected)
            .sorted((count1, count2) -> Long.compare(count2, count1))
            .limit(2)
            .reduce(1L, (x,y) -> x * y);
    }

    public void addMonkey(String src) {
        Monkey monkey = Monkey.fromString(src);
        monkeys.add(monkey);
        productOfDivisors *= monkey.getTestDivisor();
    }

    public void showMonkeys() { monkeys.forEach(m -> System.out.println(m.toString())); }
    public void setDoPostInspectionRelief(Boolean b) { doPostInspectionRelief = b; }

    private List<Monkey> monkeys = new ArrayList<>();
    private Long productOfDivisors = 1L;
    private Boolean doPostInspectionRelief = true;
    private static Long POST_INSPECTION_RELIEF_FACTOR = 3L;

    public static class Monkey {
        public Monkey(Queue<Long> items, Expression operation, Long testDivisor, Integer[] recipients) {
            this.items = items;
            this.operation = operation;
            this.testDivisor = testDivisor;
            this.recipients = recipients;
        }

        public Boolean hasItems() { return !items.isEmpty(); }
        public Long getItem() { return items.poll(); }
        public void catchItem(Long item) { items.add(item); }

        public void inspectItem() { ++itemsInspected; }
        public Long getItemsInspected() { return itemsInspected; }

        public Expression getOperation() { return operation; }
        public Integer getRecipient(Boolean testResult) { return testResult ? recipients[0] : recipients[1]; }
        public Long getTestDivisor() { return testDivisor; }

        private final Queue<Long> items;
        private final Expression operation;
        private final Long testDivisor;
        private final Integer[] recipients;
        private Long itemsInspected = 0L;

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
            var testDivisor = parseTestDivisor(lines[3]);
            var recipients = parseRecipients(lines[4], lines[5]);
            return new Monkey(items, operation, testDivisor, recipients);
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

        private static Long parseTestDivisor(String line) {
            Pattern p = Pattern.compile("  Test: divisible by (?<divisor>\\d+)");
            Matcher m = p.matcher(line);
            if (!m.matches()) throw new IllegalArgumentException(String.format("cannot parse test: '%s'", line));
            return Long.parseLong(m.group("divisor"));
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
}


public class Day11 {
    public static void main(String[] argv) throws IOException {
        String input = new String(Files.readAllBytes(Paths.get("input.txt")));
        part1(input);
        part2(input);
    }

    private static void part1(String input) {
        MonkeyShenanigansEngine engine = new MonkeyShenanigansEngine();
        for (String src : input.split("\n\n")) { engine.addMonkey(src); }
        for (int remaining = 20; remaining > 0; --remaining) engine.playRound();
        System.out.println(String.format("1. Monkey business: %d", engine.getMonkeyBusiness()));
    }

    private static void part2(String input) {
        MonkeyShenanigansEngine engine = new MonkeyShenanigansEngine();
        engine.setDoPostInspectionRelief(false);
        for (String src : input.split("\n\n")) { engine.addMonkey(src); }
        for (int remaining = 10000; remaining > 0; --remaining) engine.playRound();
        System.out.println(String.format("2. Monkey business: %d", engine.getMonkeyBusiness()));
    }
}
