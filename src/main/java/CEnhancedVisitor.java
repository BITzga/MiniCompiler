import ast.*;
import bit.minisys.minicc.parser.internal.antlr.A;
import bit.minisys.minicc.pp.internal.C;
import bit.minisys.minicc.pp.internal.S;
import org.antlr.v4.runtime.tree.ParseTree;
import org.python.antlr.AST;
import org.python.core.__builtin__;

import javax.print.DocFlavor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CEnhancedVisitor extends CBaseVisitor<ASTNode>{
    @Override
    public ASTNode visitCompilationUnit(CParser.CompilationUnitContext ctx) {
        ASTCompilationUnit.Builder builder = new ASTCompilationUnit.Builder();
        List<CParser.ExternalDeclarationContext> list= ctx.translationUnit().externalDeclaration();
        for(int i=0;i<list.size();i++){
            builder.addNode(visit(list.get(i)));
        }

        return (ASTNode)builder.build();
    }

    @Override
    public ASTNode visitFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        ASTFunctionDefine.Builder builder = new ASTFunctionDefine.Builder();
        for (int i=0;i<ctx.declarationSpecifiers().getChildCount();i++){
            builder.addSpecifiers(visit(ctx.declarationSpecifiers().getChild(i)));
        }
        builder.setBody((ASTCompoundStatement)visit(ctx.compoundStatement()));
        builder.setDeclarator((ASTDeclarator)visit(ctx.declarator()));
        return (ASTNode) builder.build();
    }

    @Override
    public ASTNode visitDeclarationSpecifier(CParser.DeclarationSpecifierContext ctx) {
        return (ASTNode) new ASTToken(ctx.start.getText(),ctx.start.getTokenIndex());
    }

    @Override
    public ASTNode visitDeclarator(CParser.DeclaratorContext ctx) {
        return  (ASTNode)visit(ctx.directDeclarator());
    }

    @Override
    public ASTNode visitDirectDeclarator(CParser.DirectDeclaratorContext ctx) {
        System.out.println("--------------------");
        System.out.println(ctx.getChildCount());
        System.out.println(ctx.LeftBracket());
        System.out.println("patter"+ctx.LeftParen());
        for(ParseTree parseTree:ctx.children){
            if(parseTree.getText().equals("(")){
                ASTFunctionDeclarator.Builder builder = new ASTFunctionDeclarator.Builder();
                if(ctx.parameterTypeList()!=null){
                    for(int i=0;i<ctx.parameterTypeList().parameterList().parameterDeclaration().size();i++) {
                        builder.addParams(visit(ctx.parameterTypeList().parameterList().parameterDeclaration().get(i)));
                    }
                }

                builder.setDecl((ASTDeclarator)visit(ctx.directDeclarator()));
                return (ASTNode) builder.build();
            }
            if(parseTree.getText().equals("[")){
                ASTArrayDeclarator.Builder builder = new ASTArrayDeclarator.Builder();
                builder.setDecl((ASTDeclarator)visit(ctx.directDeclarator()));
                builder.setExprs((ASTExpression)visit(ctx.assignmentExpression()));
                return (ASTNode)builder.build();
            }
        }
        System.out.println("iden+"+ctx.start.getText());
        return (ASTNode)new ASTVariableDeclarator(new ASTIdentifier(ctx.start.getText(),ctx.start.getTokenIndex()));
    }


    @Override
    public ASTNode visitParameterDeclaration(CParser.ParameterDeclarationContext ctx) {
        ASTParamsDeclarator.Builder builder =new ASTParamsDeclarator.Builder();
        builder.setDeclarator((ASTDeclarator)visit(ctx.declarator()));
        if(ctx.declarationSpecifiers()!=null){
            for(int i=0;i<ctx.declarationSpecifiers().declarationSpecifier().size();i++) {
                builder.addSpecfiers(visit(ctx.declarationSpecifiers().declarationSpecifier().get(i)));
            }
        }

        return (ASTNode) builder.build();
    }

    @Override
    public ASTNode visitParameterTypeList(CParser.ParameterTypeListContext ctx) {
        return (ASTNode) new ASTCompoundStatement();
    }

    @Override
    public ASTNode visitConditionalExpression(CParser.ConditionalExpressionContext ctx) {
        return  (ASTExpression)visit(ctx.logicalOrExpression().
                logicalAndExpression(0).
                inclusiveOrExpression(0).
                exclusiveOrExpression(0).
                andExpression(0).
                equalityExpression(0).
                relationalExpression(0).
                shiftExpression(0).
                additiveExpression(0).
                multiplicativeExpression(0).
                castExpression(0).
                unaryExpression().
                postfixExpression().
                primaryExpression());
    }


    @Override
    public ASTNode visitCompoundStatement(CParser.CompoundStatementContext ctx) {
        List<ASTNode> blockitems = new ArrayList<>();
        if(ctx.blockItemList()!=null){
            for(int i=0;i<ctx.blockItemList().blockItem().size();i++){
                blockitems.add((ASTNode) visit(ctx.blockItemList().blockItem().get(i)));
            }
        }

        return (ASTNode) new ASTCompoundStatement(blockitems);
    }

    @Override
    public ASTNode visitSelectionStatement(CParser.SelectionStatementContext ctx) {
        return super.visitSelectionStatement(ctx);
    }


    @Override
    public ASTNode visitExpressionStatement(CParser.ExpressionStatementContext ctx) {
        List<ASTExpression> list = new ArrayList<>();
        for(int i=0;i<ctx.expression().assignmentExpression().size();i++){
            if(ctx.expression().assignmentExpression().get(i).conditionalExpression()!=null)
            list.add((ASTExpression)visit(ctx.expression().assignmentExpression().get(i).conditionalExpression()));
        }
        return  new ASTExpressionStatement(list);
    }

    @Override
    public ASTNode visitAssignmentExpression(CParser.AssignmentExpressionContext ctx) {
        return (ASTNode) new ASTIntegerConstant();
    }

    @Override
    public ASTNode visitJumpStatement(CParser.JumpStatementContext ctx) {
        if(ctx.Return()!=null){
            LinkedList<ASTExpression> list = new LinkedList<>();
            list.add((ASTExpression) visit(ctx.expression().assignmentExpression(0).conditionalExpression()));
            return new ASTReturnStatement(list);
        }else if(ctx.Goto()!=null){
            return new ASTGotoStatement();
        }else if(ctx.Break()!=null){
            return new ASTBreakStatement();
        }else if(ctx.Identifier()!=null){
            return new ASTIdentifier();
        }else if(ctx.Continue()!=null){
            return new ASTContinueStatement();
        }else{
            return visit(ctx);
        }
    }

    @Override
    public ASTNode visitIterationStatement(CParser.IterationStatementContext ctx) {
        LinkedList<ASTExpression> init = new LinkedList<>();
        LinkedList<ASTExpression> cond = new LinkedList<>();
        LinkedList<ASTExpression> step = new LinkedList<>();
        ASTStatement stat;

        System.out.println("helo "+ctx.forCondition().forDeclaration());
        System.out.println("helo 1"+ctx.forCondition().forExpression());
        System.out.println("fs"+ctx.forCondition().expression());
        if(ctx.expression()!=null){
            stat= (ASTStatement)visit(ctx.expression());
        }
        else{
            List<ASTNode> list = new ArrayList<>();
            if(ctx.statement()!=null&&ctx.statement().selectionStatement()!=null&&ctx.statement().selectionStatement().expression()!=null)
                list.add(visit(ctx.statement().selectionStatement().expression()));
            if(ctx.statement()!=null&&ctx.statement().selectionStatement()!=null)
                list.add(visit(ctx.statement().selectionStatement().statement(0).compoundStatement()));
            ASTCompoundStatement statement = new ASTCompoundStatement(list);
            stat = statement;
        }
        return new ASTIterationStatement(init,cond,step,stat);
    }


    @Override
    public ASTNode visitDeclaration(CParser.DeclarationContext ctx) {
        ASTDeclaration.Builder builder = new ASTDeclaration.Builder();
        if(ctx.declarationSpecifiers()!=null&&ctx.declarationSpecifiers().declarationSpecifier()!=null){
            for(int i=0;i<ctx.declarationSpecifiers().declarationSpecifier().size();i++){
                builder.addSpecfiers(visit(ctx.declarationSpecifiers().declarationSpecifier(i)));
            }
        }
        if(ctx.initDeclaratorList()!=null&&ctx.initDeclaratorList().initDeclarator()!=null){
            for (int i=0;i<ctx.initDeclaratorList().initDeclarator().size();i++){
                builder.addInitList(visit(ctx.initDeclaratorList().initDeclarator(i)));
            }
        }
        return (ASTNode) builder.build();
    }


    @Override
    public ASTNode visitInitDeclarator(CParser.InitDeclaratorContext ctx) {
        ASTInitList.Builder builder = new ASTInitList.Builder();

        builder.setDeclarator((ASTDeclarator) visit(ctx.declarator()));
        if(ctx!=null&&ctx.initializer()!=null&&ctx.initializer().assignmentExpression()!=null){
            CParser.AdditiveExpressionContext context =
                    ctx.
                    initializer().
                    assignmentExpression().
                    conditionalExpression().
                    logicalOrExpression().
                    logicalAndExpression(0).
                    inclusiveOrExpression(0).
                    exclusiveOrExpression(0).
                    andExpression(0).
                    equalityExpression(0).
                    relationalExpression(0).
                    shiftExpression(0).
                    additiveExpression(0);
            List<CParser.MultiplicativeExpressionContext> list = context.multiplicativeExpression();


            for(int i=0;i<list.size();i++) {
                builder.addInitialize(visit(list.get(i)));
            }

        }

        return builder.build();
    }



    @Override
    public ASTNode visitMultiplicativeExpression(CParser.MultiplicativeExpressionContext ctx) {

            ASTToken token = new ASTToken();
            if(ctx.castExpression()==null)
                return new ASTBinaryExpression();
            if(ctx.castExpression().size()==1){
                CParser.PrimaryExpressionContext context =ctx.
                        castExpression(0).
                        unaryExpression().
                        postfixExpression().
                        primaryExpression();
                System.out.println("mak"+context.Constant());

                if(context.Constant()!=null){
                    Integer tokenId = ctx.start.getTokenIndex();
                    return new ASTIntegerConstant(Integer.parseInt(context.Constant().getText()), tokenId);
                }else if(context.Identifier()!=null){
                    return (ASTPostfixExpression)visit(ctx.castExpression(0).unaryExpression().postfixExpression());
                }else{
                    return new ASTPostfixExpression();
                }

            }else{
                String value =null;
                Integer tokenId;
                if(ctx.Star()!=null){
                    value = ctx.Star(0).getText();
                }else if(ctx.Div()!=null){
                    value=ctx.Div(0).getText();
                }else if(ctx.Mod()!=null){
                    value=ctx.Mod(0).getText();
                }
                tokenId = ctx.start.getTokenIndex();
                CParser.PrimaryExpressionContext contextA =
                        ctx.castExpression(0).
                        unaryExpression().
                        postfixExpression().
                        primaryExpression();
                CParser.PrimaryExpressionContext contextB =
                        ctx.castExpression(1).
                                unaryExpression().
                                postfixExpression().
                                primaryExpression();
                ASTIntegerConstant tempA = (ASTIntegerConstant)visit(contextA);
                ASTIntegerConstant tempB = (ASTIntegerConstant)visit(contextB);
                tokenId = ctx.start.getTokenIndex();
                tempA.tokenId = tokenId+1;
                tempB.tokenId = tokenId+2;
                return new ASTBinaryExpression(new ASTToken(value,tokenId),tempA,tempB);
            }
    }

    @Override
    public ASTNode visitPrimaryExpression(CParser.PrimaryExpressionContext ctx) {
        if(ctx.Constant()!=null){
            return new ASTIntegerConstant(Integer.parseInt(ctx.Constant().getText()),0);
        }else if(ctx.Identifier()!=null){
            return new ASTPostfixExpression(new ASTIdentifier(ctx.Identifier().getText(),ctx.start.getTokenIndex()),new ASTToken("postfix",ctx.start.getTokenIndex()));
        }
        return new ASTIntegerConstant();
    }

    @Override
    public ASTNode visitPostfixExpression(CParser.PostfixExpressionContext ctx) {
        ASTPostfixExpression expression = new ASTPostfixExpression();
        if(ctx.primaryExpression().Identifier()!=null)
            expression.op = new ASTToken(ctx.primaryExpression().Identifier().getText(),ctx.primaryExpression().start.getTokenIndex());
        if(ctx.primaryExpression().Identifier()!=null&&ctx.argumentExpressionList()!=null&&
                ctx.argumentExpressionList(0).assignmentExpression(0)!=null&&
                ctx.argumentExpressionList(0).assignmentExpression(0).conditionalExpression()!=null)
            expression.expr = (ASTExpression) visit(ctx.argumentExpressionList(0).assignmentExpression(0).conditionalExpression());
        return expression;
    }
}

