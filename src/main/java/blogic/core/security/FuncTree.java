package blogic.core.security;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * no thread-safe
 * func tree
 * 通过特定url构建一个基于RBAC的鉴权类
 * url: methodName:url?param[=|!=|!]value&authorities=ROLE_A,ROLE_B,p1,p2,p3
 * eg: POST:/a/b/c?id=1&!name&name!=value
 * */
@Setter
@Getter
@Slf4j
public class FuncTree {

    //匹配所有的key
    protected static final String MATCH_ALL_KEY = "*";

    //节点名称
    private String nodeName;
    private String branch;
    //子节点
    private FuncTrees child = new FuncTrees();
    private boolean matchAll = false;
    private Map<String, List<Query>> methods = new HashMap<>();

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
        if(nodeName.startsWith("{") && nodeName.endsWith("}")) {
            this.matchAll = true;
            this.nodeName = MATCH_ALL_KEY;
        }
    }

    @Setter
    @Getter
    public static class Query implements Comparable<Query>{
        private List<Param> params = new ArrayList<>();
        private Authorities authorities = new Authorities();

        @Override
        public int compareTo(Query two) {
            return two.params.size() - this.params.size();
        }

        @Override
        public boolean equals(Object two) {
            if(this == two) return true;
            if(two == null || !(two instanceof Query)) return false;
            Query other = (Query) two;
            if(this.params.size() != other.params.size()) return false;
            for(int i = 0,j = this.params.size();i < j;i++) {
                if(!Objects.equals(this.params.get(i), other.params.get(i))) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hash(params.toArray());
        }

        public boolean match(Query query) {
            for(Param p : this.params) {
                switch(p.getOp()) {
                    case Param.EQ:
                        if(StrUtil.isNotBlank(p.getValue())) {
                            if(!query.params.stream().filter(it -> p.getKey().equals(it.getKey())
                                    && p.getValue().equals(it.getValue())).findAny().isPresent()) {
                                return false;
                            }
                        }else {
                            if(!query.params.stream().filter(it -> p.getKey().equals(it.getKey()))
                                    .findAny().isPresent()) {
                                return false;
                            }
                        }
                        break;
                    case Param.NE:
                        if(query.params.stream().filter(it -> p.getKey().equals(it.getKey())
                                && p.getValue().equals(it.getValue())).findAny().isPresent()) {
                            return false;
                        }
                        break;
                    case Param.NOT:
                        if(query.params.stream().filter(it -> p.getKey().equals(it.getKey())).findAny().isPresent()) {
                            return false;
                        }
                        break;
                }
            }
            return true;
        }

        @Override
        public String toString() {
            if(this.params.size() == 0) {
                return StrUtil.EMPTY;
            }
            StringBuilder sb = new StringBuilder("?");
            this.params.stream().forEach(it -> {
                switch(it.getOp()) {
                    case Param.EQ:
                        sb.append(it.getKey()).append(it.getOp()).append(it.getValue() == null?"":it.getValue());
                        break;
                    case Param.NE:
                        sb.append(it.getKey()).append(it.getOp()).append(it.getValue());
                        break;
                    case Param.NOT:
                        sb.append(it.getOp()).append(it.getKey());
                        break;
                }
                sb.append("&");
            });
            return sb.deleteCharAt(sb.length() - 1).toString();
        }

        public static Query buildQuery(String query) {
            Query q = new Query();
            if(StrUtil.isNotBlank(query)) {
                StringBuilder error = new StringBuilder(" 格式异常");
                String[] kvArray = query.split("&");
                if(log.isDebugEnabled()) {
                    log.debug("build query kvArray {}", Stream.of(kvArray).collect(Collectors.joining(",")));
                }
                Stream<String> stream = Stream.of(kvArray).filter(it -> StrUtil.isNotBlank(it)).map(it -> it.trim());
                q.getAuthorities().getAuthorities().addAll(stream.filter(it -> it.startsWith("authorities"))
                        .flatMap(it -> Stream.of(it.split(","))).filter(it -> StrUtil.isNotBlank(it))
                        .map(it -> it.trim()).collect(Collectors.toSet()));
                q.getParams().addAll(stream.filter(it -> !it.startsWith("authorities")).map(it -> {
                    int notIndex = it.indexOf(Param.NOT);
                    int eqIndex = it.indexOf(Param.EQ);
                    Param p = new Param();
                    if(notIndex == 0) {
                        if(eqIndex > 0) throw new RuntimeException(error.insert(0, it).toString());
                        p.setKey(it.substring(notIndex + 1).trim());
                        p.setOp(Param.NOT);
                    }else if(notIndex > 0) {
                        if(eqIndex != (notIndex + 1) || eqIndex == (it.length() - 1)) throw new RuntimeException(error.insert(0, it).toString());
                        p.setKey(it.substring(0, notIndex).trim());
                        p.setOp(Param.NE);
                        p.setValue(it.substring(eqIndex + 1).trim());
                    }else {
                        if(eqIndex == 0) throw new RuntimeException(error.insert(0, it).toString());
                        p.setOp(Param.EQ);
                        if(eqIndex > 0) {
                            p.setKey(it.substring(0, eqIndex).trim());
                            if(eqIndex < it.length() - 1) {
                                p.setValue(it.substring(eqIndex + 1));
                            }
                        }else {
                            p.setKey(it);
                        }
                    }
                    return p;
                }).sorted((a, b) -> a.getKey().compareTo(b.getKey())).collect(Collectors.toList()));
            }
            return q;
        }

    }

    @Data
    public static class Param {

        public static final String NOT = "!";
        public static final String EQ = "=";
        public static final String NE = "!=";

        private String key;
        private String value;
        //操作符 a=b a!=b !a a=
        private String op;
    }

    @Setter
    @Getter
    public static class Authorities {

        private Set<String> authorities = new HashSet<>();

        /**
         * @param authorities 用户拥有的权限
         * */
        public boolean authenticate(List<String> authorities) {
            if(this.authorities.size() == 0)return true;
            return this.authorities.stream().filter(authority -> authorities.contains(authority)).findAny().isPresent();
        }
    }

}
