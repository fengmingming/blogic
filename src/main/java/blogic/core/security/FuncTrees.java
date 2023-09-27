package blogic.core.security;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class FuncTrees extends HashMap<String, FuncTree> {

    public Optional<FuncTree> firstFuncTree() {
        return this.values().stream().findFirst();
    }

    public static FuncTrees buildFuncTrees(Collection<String> urls) {
        FuncTrees funcTrees = new FuncTrees();
        urls.stream().forEach(url -> doBuildFuncTree(funcTrees, url));
        funcTrees.values().stream().forEach(it -> doQueriesSort(it));
        return funcTrees;
    }

    public static Mono<FuncTrees> buildFuncTrees(Flux<String> urls) {
        return urls.collectList().map(it -> FuncTrees.buildFuncTrees(it));
    }

    protected static void doQueriesSort(FuncTree ft) {
        ft.getMethods().values().stream().forEach(it -> it.stream().sorted(FuncTree.Query::compareTo));
        if(ft.getChild().size() > 0) {
            ft.getChild().values().stream().forEach(it -> doQueriesSort(it));
        }
    }

    /**
     * 构建路径树
     * */
    protected static void doBuildFuncTree(FuncTrees funcTrees, String url) {
        //url格式校验
        int colonIndex = url.indexOf(":");
        int quMark = url.indexOf("?");
        if(quMark > 0 && colonIndex > quMark) {
            colonIndex = -1;
        }
        int pathS = 0;
        int pathE = url.length();
        String method = FuncTree.MATCH_ALL_KEY;
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
        FuncTrees child = funcTrees;
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
                List<FuncTree.Query> queryList = node.getMethods().get(method);
                if(queryList == null) {
                    queryList = new ArrayList<>();
                    if(node.getMethods().putIfAbsent(method, queryList) != null) {
                        queryList = node.getMethods().get(method);
                    }
                }
                FuncTree.Query queryObj = FuncTree.Query.buildQuery(query);
                synchronized (queryList) {
                    Optional<FuncTree.Query> existQuery = queryList.stream().filter(it -> it.equals(queryObj)).findFirst();
                    if(existQuery.isPresent()) {
                        existQuery.get().getAuthorities().getAuthorities().addAll(queryObj.getAuthorities().getAuthorities());
                    }else {
                        queryList.add(queryObj);
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
    public static Optional<FuncTree.Authorities> match(FuncTrees funcTrees, FuncTree funcTree) {
        FuncTree ft = funcTrees.get(funcTree.getNodeName());
        if(ft == null) {
            ft = funcTrees.get(FuncTree.MATCH_ALL_KEY);
        }
        if(ft == null) return Optional.empty();
        if(funcTree.getChild().size() == 0) {
            String method = funcTree.getMethods().keySet().stream().findFirst().get();
            FuncTree.Query query = funcTree.getMethods().get(method).get(0);
            List<FuncTree.Query> queryList = ft.getMethods().get(method);
            if(queryList == null) {
                method = FuncTree.MATCH_ALL_KEY;
                queryList = ft.getMethods().get(method);
            }
            if(queryList == null) return Optional.empty();
            Optional<FuncTree.Query> queryOpt = queryList.stream().filter(it -> it.match(query)).findFirst();
            if(log.isDebugEnabled() && queryOpt.isPresent()) {
                log.debug("匹配路径 {} 源路径 {}", String.format("%s:%s%s", method, ft.getBranch(), queryOpt.get().toString()),
                        String.format("%s:%s%s", funcTree.getMethods().keySet().stream().findFirst().get(),
                                funcTree.getBranch(), query.toString()));
            }
            return queryOpt.isPresent()?Optional.of(queryOpt.get().getAuthorities()):Optional.empty();
        }else {
            return match(ft.getChild(), funcTree.getChild().entrySet().stream().findFirst().get().getValue());
        }
    }

}
