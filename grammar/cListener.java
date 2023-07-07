// Generated from grammar/c.g4 by ANTLR 4.13.0
package grammar;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link cParser}.
 */
public interface cListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link cParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryExpression(cParser.PrimaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#primaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryExpression(cParser.PrimaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#postfixExpression}.
	 * @param ctx the parse tree
	 */
	void enterPostfixExpression(cParser.PostfixExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#postfixExpression}.
	 * @param ctx the parse tree
	 */
	void exitPostfixExpression(cParser.PostfixExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#argumentExpressionList}.
	 * @param ctx the parse tree
	 */
	void enterArgumentExpressionList(cParser.ArgumentExpressionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#argumentExpressionList}.
	 * @param ctx the parse tree
	 */
	void exitArgumentExpressionList(cParser.ArgumentExpressionListContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpression(cParser.UnaryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#unaryExpression}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpression(cParser.UnaryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#unaryOperator}.
	 * @param ctx the parse tree
	 */
	void enterUnaryOperator(cParser.UnaryOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#unaryOperator}.
	 * @param ctx the parse tree
	 */
	void exitUnaryOperator(cParser.UnaryOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#castExpression}.
	 * @param ctx the parse tree
	 */
	void enterCastExpression(cParser.CastExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#castExpression}.
	 * @param ctx the parse tree
	 */
	void exitCastExpression(cParser.CastExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicativeExpression(cParser.MultiplicativeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#multiplicativeExpression}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicativeExpression(cParser.MultiplicativeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#additiveExpression}.
	 * @param ctx the parse tree
	 */
	void enterAdditiveExpression(cParser.AdditiveExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#additiveExpression}.
	 * @param ctx the parse tree
	 */
	void exitAdditiveExpression(cParser.AdditiveExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#shiftExpression}.
	 * @param ctx the parse tree
	 */
	void enterShiftExpression(cParser.ShiftExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#shiftExpression}.
	 * @param ctx the parse tree
	 */
	void exitShiftExpression(cParser.ShiftExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#relationalExpression}.
	 * @param ctx the parse tree
	 */
	void enterRelationalExpression(cParser.RelationalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#relationalExpression}.
	 * @param ctx the parse tree
	 */
	void exitRelationalExpression(cParser.RelationalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#equalityExpression}.
	 * @param ctx the parse tree
	 */
	void enterEqualityExpression(cParser.EqualityExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#equalityExpression}.
	 * @param ctx the parse tree
	 */
	void exitEqualityExpression(cParser.EqualityExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#andExpression}.
	 * @param ctx the parse tree
	 */
	void enterAndExpression(cParser.AndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#andExpression}.
	 * @param ctx the parse tree
	 */
	void exitAndExpression(cParser.AndExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#exclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterExclusiveOrExpression(cParser.ExclusiveOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#exclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitExclusiveOrExpression(cParser.ExclusiveOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#inclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterInclusiveOrExpression(cParser.InclusiveOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#inclusiveOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitInclusiveOrExpression(cParser.InclusiveOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#logicalAndExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalAndExpression(cParser.LogicalAndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#logicalAndExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalAndExpression(cParser.LogicalAndExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#logicalOrExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOrExpression(cParser.LogicalOrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#logicalOrExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOrExpression(cParser.LogicalOrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#conditionalExpression}.
	 * @param ctx the parse tree
	 */
	void enterConditionalExpression(cParser.ConditionalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#conditionalExpression}.
	 * @param ctx the parse tree
	 */
	void exitConditionalExpression(cParser.ConditionalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentExpression(cParser.AssignmentExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#assignmentExpression}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentExpression(cParser.AssignmentExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#assignmentOperator}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentOperator(cParser.AssignmentOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#assignmentOperator}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentOperator(cParser.AssignmentOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(cParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(cParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#constantExpression}.
	 * @param ctx the parse tree
	 */
	void enterConstantExpression(cParser.ConstantExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#constantExpression}.
	 * @param ctx the parse tree
	 */
	void exitConstantExpression(cParser.ConstantExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#declaration}.
	 * @param ctx the parse tree
	 */
	void enterDeclaration(cParser.DeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#declaration}.
	 * @param ctx the parse tree
	 */
	void exitDeclaration(cParser.DeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#declarationSpecifiers}.
	 * @param ctx the parse tree
	 */
	void enterDeclarationSpecifiers(cParser.DeclarationSpecifiersContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#declarationSpecifiers}.
	 * @param ctx the parse tree
	 */
	void exitDeclarationSpecifiers(cParser.DeclarationSpecifiersContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#declarationSpecifiers2}.
	 * @param ctx the parse tree
	 */
	void enterDeclarationSpecifiers2(cParser.DeclarationSpecifiers2Context ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#declarationSpecifiers2}.
	 * @param ctx the parse tree
	 */
	void exitDeclarationSpecifiers2(cParser.DeclarationSpecifiers2Context ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#declarationSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterDeclarationSpecifier(cParser.DeclarationSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#declarationSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitDeclarationSpecifier(cParser.DeclarationSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#initDeclaratorList}.
	 * @param ctx the parse tree
	 */
	void enterInitDeclaratorList(cParser.InitDeclaratorListContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#initDeclaratorList}.
	 * @param ctx the parse tree
	 */
	void exitInitDeclaratorList(cParser.InitDeclaratorListContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#initDeclarator}.
	 * @param ctx the parse tree
	 */
	void enterInitDeclarator(cParser.InitDeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#initDeclarator}.
	 * @param ctx the parse tree
	 */
	void exitInitDeclarator(cParser.InitDeclaratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#storageClassSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterStorageClassSpecifier(cParser.StorageClassSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#storageClassSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitStorageClassSpecifier(cParser.StorageClassSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#typeSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterTypeSpecifier(cParser.TypeSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#typeSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitTypeSpecifier(cParser.TypeSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#structOrUnionSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterStructOrUnionSpecifier(cParser.StructOrUnionSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#structOrUnionSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitStructOrUnionSpecifier(cParser.StructOrUnionSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#structOrUnion}.
	 * @param ctx the parse tree
	 */
	void enterStructOrUnion(cParser.StructOrUnionContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#structOrUnion}.
	 * @param ctx the parse tree
	 */
	void exitStructOrUnion(cParser.StructOrUnionContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#structDeclarationList}.
	 * @param ctx the parse tree
	 */
	void enterStructDeclarationList(cParser.StructDeclarationListContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#structDeclarationList}.
	 * @param ctx the parse tree
	 */
	void exitStructDeclarationList(cParser.StructDeclarationListContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#structDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterStructDeclaration(cParser.StructDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#structDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitStructDeclaration(cParser.StructDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#specifierQualifierList}.
	 * @param ctx the parse tree
	 */
	void enterSpecifierQualifierList(cParser.SpecifierQualifierListContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#specifierQualifierList}.
	 * @param ctx the parse tree
	 */
	void exitSpecifierQualifierList(cParser.SpecifierQualifierListContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#structDeclaratorList}.
	 * @param ctx the parse tree
	 */
	void enterStructDeclaratorList(cParser.StructDeclaratorListContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#structDeclaratorList}.
	 * @param ctx the parse tree
	 */
	void exitStructDeclaratorList(cParser.StructDeclaratorListContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#structDeclarator}.
	 * @param ctx the parse tree
	 */
	void enterStructDeclarator(cParser.StructDeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#structDeclarator}.
	 * @param ctx the parse tree
	 */
	void exitStructDeclarator(cParser.StructDeclaratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#enumSpecifier}.
	 * @param ctx the parse tree
	 */
	void enterEnumSpecifier(cParser.EnumSpecifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#enumSpecifier}.
	 * @param ctx the parse tree
	 */
	void exitEnumSpecifier(cParser.EnumSpecifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#enumeratorList}.
	 * @param ctx the parse tree
	 */
	void enterEnumeratorList(cParser.EnumeratorListContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#enumeratorList}.
	 * @param ctx the parse tree
	 */
	void exitEnumeratorList(cParser.EnumeratorListContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#enumerator}.
	 * @param ctx the parse tree
	 */
	void enterEnumerator(cParser.EnumeratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#enumerator}.
	 * @param ctx the parse tree
	 */
	void exitEnumerator(cParser.EnumeratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#typeQualifier}.
	 * @param ctx the parse tree
	 */
	void enterTypeQualifier(cParser.TypeQualifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#typeQualifier}.
	 * @param ctx the parse tree
	 */
	void exitTypeQualifier(cParser.TypeQualifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#declarator}.
	 * @param ctx the parse tree
	 */
	void enterDeclarator(cParser.DeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#declarator}.
	 * @param ctx the parse tree
	 */
	void exitDeclarator(cParser.DeclaratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#directDeclarator}.
	 * @param ctx the parse tree
	 */
	void enterDirectDeclarator(cParser.DirectDeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#directDeclarator}.
	 * @param ctx the parse tree
	 */
	void exitDirectDeclarator(cParser.DirectDeclaratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#pointer}.
	 * @param ctx the parse tree
	 */
	void enterPointer(cParser.PointerContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#pointer}.
	 * @param ctx the parse tree
	 */
	void exitPointer(cParser.PointerContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#typeQualifierList}.
	 * @param ctx the parse tree
	 */
	void enterTypeQualifierList(cParser.TypeQualifierListContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#typeQualifierList}.
	 * @param ctx the parse tree
	 */
	void exitTypeQualifierList(cParser.TypeQualifierListContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#parameterTypeList}.
	 * @param ctx the parse tree
	 */
	void enterParameterTypeList(cParser.ParameterTypeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#parameterTypeList}.
	 * @param ctx the parse tree
	 */
	void exitParameterTypeList(cParser.ParameterTypeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void enterParameterList(cParser.ParameterListContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#parameterList}.
	 * @param ctx the parse tree
	 */
	void exitParameterList(cParser.ParameterListContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#parameterDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterParameterDeclaration(cParser.ParameterDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#parameterDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitParameterDeclaration(cParser.ParameterDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#identifierList}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierList(cParser.IdentifierListContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#identifierList}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierList(cParser.IdentifierListContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#typeName}.
	 * @param ctx the parse tree
	 */
	void enterTypeName(cParser.TypeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#typeName}.
	 * @param ctx the parse tree
	 */
	void exitTypeName(cParser.TypeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#abstractDeclarator}.
	 * @param ctx the parse tree
	 */
	void enterAbstractDeclarator(cParser.AbstractDeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#abstractDeclarator}.
	 * @param ctx the parse tree
	 */
	void exitAbstractDeclarator(cParser.AbstractDeclaratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#directAbstractDeclarator}.
	 * @param ctx the parse tree
	 */
	void enterDirectAbstractDeclarator(cParser.DirectAbstractDeclaratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#directAbstractDeclarator}.
	 * @param ctx the parse tree
	 */
	void exitDirectAbstractDeclarator(cParser.DirectAbstractDeclaratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#typedefName}.
	 * @param ctx the parse tree
	 */
	void enterTypedefName(cParser.TypedefNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#typedefName}.
	 * @param ctx the parse tree
	 */
	void exitTypedefName(cParser.TypedefNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#initializer}.
	 * @param ctx the parse tree
	 */
	void enterInitializer(cParser.InitializerContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#initializer}.
	 * @param ctx the parse tree
	 */
	void exitInitializer(cParser.InitializerContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#initializerList}.
	 * @param ctx the parse tree
	 */
	void enterInitializerList(cParser.InitializerListContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#initializerList}.
	 * @param ctx the parse tree
	 */
	void exitInitializerList(cParser.InitializerListContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(cParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(cParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#labeledStatement}.
	 * @param ctx the parse tree
	 */
	void enterLabeledStatement(cParser.LabeledStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#labeledStatement}.
	 * @param ctx the parse tree
	 */
	void exitLabeledStatement(cParser.LabeledStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#compoundStatement}.
	 * @param ctx the parse tree
	 */
	void enterCompoundStatement(cParser.CompoundStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#compoundStatement}.
	 * @param ctx the parse tree
	 */
	void exitCompoundStatement(cParser.CompoundStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#declaration_list}.
	 * @param ctx the parse tree
	 */
	void enterDeclaration_list(cParser.Declaration_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#declaration_list}.
	 * @param ctx the parse tree
	 */
	void exitDeclaration_list(cParser.Declaration_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#statement_list}.
	 * @param ctx the parse tree
	 */
	void enterStatement_list(cParser.Statement_listContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#statement_list}.
	 * @param ctx the parse tree
	 */
	void exitStatement_list(cParser.Statement_listContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#expressionStatement}.
	 * @param ctx the parse tree
	 */
	void enterExpressionStatement(cParser.ExpressionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#expressionStatement}.
	 * @param ctx the parse tree
	 */
	void exitExpressionStatement(cParser.ExpressionStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#selectionStatement}.
	 * @param ctx the parse tree
	 */
	void enterSelectionStatement(cParser.SelectionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#selectionStatement}.
	 * @param ctx the parse tree
	 */
	void exitSelectionStatement(cParser.SelectionStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#iterationStatement}.
	 * @param ctx the parse tree
	 */
	void enterIterationStatement(cParser.IterationStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#iterationStatement}.
	 * @param ctx the parse tree
	 */
	void exitIterationStatement(cParser.IterationStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#jumpStatement}.
	 * @param ctx the parse tree
	 */
	void enterJumpStatement(cParser.JumpStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#jumpStatement}.
	 * @param ctx the parse tree
	 */
	void exitJumpStatement(cParser.JumpStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void enterCompilationUnit(cParser.CompilationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void exitCompilationUnit(cParser.CompilationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#translationUnit}.
	 * @param ctx the parse tree
	 */
	void enterTranslationUnit(cParser.TranslationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#translationUnit}.
	 * @param ctx the parse tree
	 */
	void exitTranslationUnit(cParser.TranslationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#externalDeclaration}.
	 * @param ctx the parse tree
	 */
	void enterExternalDeclaration(cParser.ExternalDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#externalDeclaration}.
	 * @param ctx the parse tree
	 */
	void exitExternalDeclaration(cParser.ExternalDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#functionDefinition}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDefinition(cParser.FunctionDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#functionDefinition}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDefinition(cParser.FunctionDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link cParser#declarationList}.
	 * @param ctx the parse tree
	 */
	void enterDeclarationList(cParser.DeclarationListContext ctx);
	/**
	 * Exit a parse tree produced by {@link cParser#declarationList}.
	 * @param ctx the parse tree
	 */
	void exitDeclarationList(cParser.DeclarationListContext ctx);
}