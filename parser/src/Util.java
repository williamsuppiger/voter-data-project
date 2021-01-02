import java.util.List;
import java.util.stream.Collectors;

public class Util {
    public static String listToString(List<String> namesList) {
        return '[' + String.join(", ", namesList
                .stream()
                .map(name -> ('"' + name + '"'))
                .collect(Collectors.toList())) + ']';
    }
}
