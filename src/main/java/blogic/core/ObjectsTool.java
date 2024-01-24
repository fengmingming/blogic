package blogic.core;

import java.util.Objects;

public class ObjectsTool {

    public static void requireNonNull(Object... args) {
        for(Object arg : args) {
            Objects.requireNonNull(arg);
        }
    }
}
