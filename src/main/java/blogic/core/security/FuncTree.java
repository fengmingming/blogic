package blogic.core.security;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private Map<String, FuncTree> child = new ConcurrentHashMap<>();
    private boolean matchAll = false;
    private Map<String, List<Query>> methods = new ConcurrentHashMap<>();

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
                q.getParams().addAll(Stream.of(kvArray).filter(it -> StrUtil.isNotBlank(it)).map(it -> it.trim()).map(it -> {
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

    /**
     * 构建路径树
     * */
    public static void buildFuncTree(Map<String, FuncTree> funcTree, String url) {
        //url格式校验
        int colonIndex = url.indexOf(":");
        int quMark = url.indexOf("?");
        int pathS = 0;
        int pathE = url.length();
        String method = MATCH_ALL_KEY;
        String query = null;
        if(colonIndex > 0) {
            method = url.substring(0, colonIndex).toUpperCase();
            pathS = colonIndex + 1;
        }
        if(quMark > 0 && quMark < url.length() - 1) {
            query = url.substring(quMark + 1);
            pathE = quMark;
        }
        if(log.isDebugEnabled()) {
            log.debug("colonIndex {}, quMark {}, method {}, query {}, pathS {}, pathE {}",
                    colonIndex, quMark, method, query, pathS, pathE);
        }
        String path = url.substring(pathS, pathE);
        String[] nodeArray = path.split("/");
        List<String> nodeList = Stream.of(nodeArray).filter(it -> StrUtil.isNotBlank(it)).map(it -> it.trim()).collect(Collectors.toList());
        Map<String, FuncTree> child = funcTree;
        FuncTree parent = null;
        int deepIndex = nodeList.size();
        for(int i = 0;i < deepIndex;i++) {
            String nodeName = nodeList.get(i);
            FuncTree node = child.get(nodeName);
            if(node == null) {
                node = new FuncTree();
                node.setNodeName(nodeName);
                node.setBranch(String.format("%s/%s", parent==null?"":parent.getBranch(), nodeName));
                if(child.putIfAbsent(nodeName, node) != null) {
                    node = child.get(nodeName);
                }
            }
            if(i == deepIndex - 1) {
                List<Query> queryList = node.getMethods().get(method);
                if(queryList == null) {
                    queryList = new ArrayList<>();
                    if(node.getMethods().putIfAbsent(method, queryList) != null) {
                        queryList = node.getMethods().get(method);
                    }
                }
                Query queryObj = Query.buildQuery(query);
                synchronized (queryList) {
                    if(!queryList.contains(queryObj)) {
                        queryList.add(queryObj);
                        queryList.sort((a, b) -> a.compareTo(b));
                    }
                }
            }
            parent = node;
            child = node.getChild();
        }
    }

    /**
     * 搜索路径树中是否存在给定的路径树
     * */
    public static boolean match(Map<String, FuncTree> funcTrees, FuncTree funcTree) {
        FuncTree ft = funcTrees.get(funcTree.getNodeName());
        if(ft == null) {
            ft = funcTrees.get(MATCH_ALL_KEY);
        }
        if(ft == null) return false;
        if(funcTree.child.size() == 0) {
            String method = funcTree.getMethods().keySet().stream().findFirst().get();
            Query query = funcTree.getMethods().get(method).get(0);
            List<Query> queryList = ft.getMethods().get(method);
            if(queryList == null) {
                method = MATCH_ALL_KEY;
                queryList = ft.getMethods().get(method);
            }
            if(queryList == null) return false;
            Optional<Query> queryOpt = queryList.stream().filter(it -> it.match(query)).findFirst();
            if(log.isDebugEnabled() && queryOpt.isPresent()) {
                log.debug("匹配路径 {} 源路径 {}", String.format("%s:%s%s", method, ft.getBranch(), queryOpt.get().toString()),
                        String.format("%s:%s%s", funcTree.getMethods().keySet().stream().findFirst().get(),
                                funcTree.getBranch(), query.toString()));
            }
            return queryOpt.isPresent();
        }else {
            return match(ft.getChild(), funcTree.getChild().entrySet().stream().findFirst().get().getValue());
        }
    }

}
