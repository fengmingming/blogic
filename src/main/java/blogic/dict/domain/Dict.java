package blogic.dict.domain;

import blogic.core.context.SpringContext;
import blogic.core.domain.ActiveRecord;
import blogic.dict.domain.repository.DictRepository;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.time.LocalDateTime;
import java.util.Locale;

@Setter
@Getter
@Table("dict")
public class Dict extends ActiveRecord<Dict, Long> {

    @Id
    @Column("id")
    private Long id;
    @Column("dict_type")
    private String dictType;
    @Column("code")
    private Integer code;
    @Column("code_desc")
    private String codeDesc;
    @Column("locale")
    private String locale;
    @Column("create_time")
    private LocalDateTime createTime;

    public void setLocale(Locale locale) {
        this.locale = Dict.localeToString(locale);
    }

    public static String localeToString(Locale locale) {
        if(locale == null) return null;
        StringBuilder sb = new StringBuilder(locale.getLanguage());
        if(StrUtil.isNotBlank(locale.getCountry())) {
            sb.append("-").append(locale.getCountry());
        }
        return sb.toString();
    }

    @Override
    protected ReactiveCrudRepository<Dict, Long> findRepository() {
        return SpringContext.getBean(DictRepository.class);
    }

    @Override
    protected <S extends Dict> S selfS() {
        return (S) this;
    }

}
