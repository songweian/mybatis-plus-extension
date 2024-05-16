import org.apache.ibatis.parsing.PropertyParser;

import java.util.Properties;

public class MainTest {
    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.setProperty("a", "world");
        String parse = PropertyParser.parse("hello ${a} world", properties);
        System.out.println(parse);
    }
}
