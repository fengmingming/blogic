package blogic.core.exception;

public class IllegalEnumValueException extends RuntimeException implements CodedException{

    private final Class<? extends Enum> clazz;
    private final Object value;

    public IllegalEnumValueException(Class<? extends Enum> clazz, Integer value) {
        super();
        this.value = value;
        this.clazz = clazz;
    }


    @Override
    public int getCode() {
        return 1005;
    }

    @Override
    public Object[] getTemplateArgs() {
        return new Object[]{this.clazz.getSimpleName(), this.value};
    }

}
