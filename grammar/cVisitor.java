// Generated from grammar/c.g4 by ANTLR 4.13.0
package grammar;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link cParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface cVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link cParser#primaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryExpression(cParser.PrimaryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#postfixExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostfixExpression(cParser.PostfixExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#argumentExpressionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgumentExpressionList(cParser.ArgumentExpressionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#unaryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryExpression(cParser.UnaryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#unaryOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryOperator(cParser.UnaryOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#castExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCastExpression(cParser.CastExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicativeExpression(cParser.MultiplicativeExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#additiveExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditiveExpression(cParser.AdditiveExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#shiftExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShiftExpression(cParser.ShiftExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#relationalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationalExpression(cParser.RelationalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#equalityExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqualityExpression(cParser.EqualityExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#andExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndExpression(cParser.AndExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#exclusiveOrExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExclusiveOrExpression(cParser.ExclusiveOrExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#inclusiveOrExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInclusiveOrExpression(cParser.InclusiveOrExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#logicalAndExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalAndExpression(cParser.LogicalAndExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#logicalOrExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOrExpression(cParser.LogicalOrExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#conditionalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditionalExpression(cParser.ConditionalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#assignmentExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentExpression(cParser.AssignmentExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#assignmentOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentOperator(cParser.AssignmentOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(cParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#constantExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantExpression(cParser.ConstantExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#declaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclaration(cParser.DeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#declarationSpecifiers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclarationSpecifiers(cParser.DeclarationSpecifiersContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#declarationSpecifiers2}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclarationSpecifiers2(cParser.DeclarationSpecifiers2Context ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#declarationSpecifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclarationSpecifier(cParser.DeclarationSpecifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#initDeclaratorList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInitDeclaratorList(cParser.InitDeclaratorListContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#initDeclarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInitDeclarator(cParser.InitDeclaratorContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#storageClassSpecifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStorageClassSpecifier(cParser.StorageClassSpecifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#typeSpecifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeSpecifier(cParser.TypeSpecifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#structOrUnionSpecifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructOrUnionSpecifier(cParser.StructOrUnionSpecifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#structOrUnion}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructOrUnion(cParser.StructOrUnionContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#structDeclarationList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructDeclarationList(cParser.StructDeclarationListContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#structDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructDeclaration(cParser.StructDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#specifierQualifierList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecifierQualifierList(cParser.SpecifierQualifierListContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#structDeclaratorList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructDeclaratorList(cParser.StructDeclaratorListContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#structDeclarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructDeclarator(cParser.StructDeclaratorContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#enumSpecifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumSpecifier(cParser.EnumSpecifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#enumeratorList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumeratorList(cParser.EnumeratorListContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#enumerator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumerator(cParser.EnumeratorContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#typeQualifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeQualifier(cParser.TypeQualifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#declarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclarator(cParser.DeclaratorContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#directDeclarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirectDeclarator(cParser.DirectDeclaratorContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#pointer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPointer(cParser.PointerContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#typeQualifierList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeQualifierList(cParser.TypeQualifierListContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#parameterTypeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterTypeList(cParser.ParameterTypeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#parameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterList(cParser.ParameterListContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#parameterDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterDeclaration(cParser.ParameterDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#identifierList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierList(cParser.IdentifierListContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#typeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeName(cParser.TypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#abstractDeclarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAbstractDeclarator(cParser.AbstractDeclaratorContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#directAbstractDeclarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirectAbstractDeclarator(cParser.DirectAbstractDeclaratorContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#typedefName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypedefName(cParser.TypedefNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#initializer}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInitializer(cParser.InitializerContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#initializerList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInitializerList(cParser.InitializerListContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(cParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#labeledStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLabeledStatement(cParser.LabeledStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#compoundStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompoundStatement(cParser.CompoundStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#declaration_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclaration_list(cParser.Declaration_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#statement_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement_list(cParser.Statement_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#expressionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionStatement(cParser.ExpressionStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#selectionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectionStatement(cParser.SelectionStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#iterationStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIterationStatement(cParser.IterationStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#jumpStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJumpStatement(cParser.JumpStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#compilationUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompilationUnit(cParser.CompilationUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#translationUnit}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTranslationUnit(cParser.TranslationUnitContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#externalDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExternalDeclaration(cParser.ExternalDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#functionDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionDefinition(cParser.FunctionDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link cParser#declarationList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclarationList(cParser.DeclarationListContext ctx);
}