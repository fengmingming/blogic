package blogic.core.exception;

public class IllegalEnumValueException extends AbstractCodedException {

    public IllegalEnumValueException(Class<? extends Enum> clazz, Integer value) {
        super(1005, clazz.getSimpleName(), value);
    }

}
