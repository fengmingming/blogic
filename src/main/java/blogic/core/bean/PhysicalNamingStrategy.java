package blogic.core.bean;

import org.springframework.data.relational.core.mapping.NamingStrategy;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.util.ParsingUtils;
import org.springframework.util.Assert;

public class PhysicalNamingStrategy implements NamingStrategy {

    @Override
    public String getColumnName(RelationalPersistentProperty property) {
        Assert.notNull(property, "Property must not be null.");
        return ParsingUtils.reconcatenateCamelCase(property.getName(), "_");
    }

}
