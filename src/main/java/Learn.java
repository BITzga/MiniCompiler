import ast.ASTCompilationUnit;
import ast.ASTDeclaration;
import ast.ASTNode;
import bit.minisys.minicc.pp.internal.O;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

public class Learn {
    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {
        String s = "int fibonacci ( int n );\n" +
                "int main ( ) {\n" +
                "  int a = 10;\n" +
                "  int res = fibonacci ( a ) ;\n" +
                "  _OUTPUT ( res ) ;\n" +
                "  return 0 ;\n" +
                "}\n" +
                "int fibonacci ( int n ) {\n" +
                "\tif ( n < 2 )\n" +
                "\t\treturn n;\n" +
                "\tint f = 0 , g = 1 ;\n" +
                "\tint result = 0 ;\n" +
                "\tfor ( int i = 0 ; i < n ; i ++ ) {\n" +
                "\t\tif(i==0){\n" +
                "\t\t    break;\n" +
                "\t\t}\n" +
                "\t}\n" +
                "\treturn result ;\n" +
                "}";
        ANTLRInputStream stream = new ANTLRInputStream(s);
        CLexer cLexer = new CLexer(stream);
        CommonTokenStream tokenStream = new CommonTokenStream(cLexer);

        CParser cParser = new CParser(tokenStream);
        CParser.CompilationUnitContext parseTree = cParser.compilationUnit();
       // System.out.println(parseTree.getClass());
        CEnhancedVisitor visitor = new CEnhancedVisitor();
        ASTCompilationUnit compilationUnit = (ASTCompilationUnit)visitor.visitCompilationUnit(parseTree);
//        for(ASTNode a:compilationUnit.items){
//            System.out.println("compilationUnit child："+a);
//        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(new File("res.json"),compilationUnit);
//        objectMapper.writeValue(new File("context.json"),parseTree);
//        ParseTreeWalker parseTreeWalker = new ParseTreeWalker();
//        CBaseListener cBaseListener = new CBaseListener();
//        parseTreeWalker.walk(cBaseListener,parseTree);
//        System.out.println(cBaseListener.compilationUnit);
//        map.put(CParser.RULE_externalDeclaration,ASTCompilationUnit.class);
//        map.put(CParser.RULE_declaration, ASTDeclaration.class);
//        Class<? extends ASTNode> aClass = map.get(CParser.RULE_externalDeclaration);
//
//        System.out.println(getInstance(aClass));


    }
    public static <T> T getInstance(Class<T> clz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        T t = clz.getDeclaredConstructor().newInstance();
        return t;
    }

    public static Map<Integer,Class<? extends ASTNode>> map = new HashMap<>();
    public ASTNode dfs(ParserRuleContext rootContext) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        int index=rootContext.getRuleIndex();
        ASTNode node = getInstance(map.get(rootContext.getClass()));
        Iterator iterator = rootContext.children.iterator();
        while (iterator.hasNext()){
            node.children.add(dfs((ParserRuleContext)iterator.next()));
        }
        return  node;
    }
}
