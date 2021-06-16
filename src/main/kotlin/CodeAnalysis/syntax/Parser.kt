package CodeAnalysis.syntax

import java.io.Reader
import java.util.*
import kotlin.collections.ArrayList

class RDParser {
    private var tokens = ArrayList<SyntaxToken>()
    private var position = 0

    private var lexerDiagnostics: LinkedList<String>
    private var diagnostics = LinkedList<String>()

    constructor(data: Reader) {
        var lexer = Lexer(data)
        while(true) {
            val tok = lexer.scan()
            tokens.add(tok)
            if(tok.kind == SyntaxToken.EndOfFileToken.kind) break
        }
        for(tok in tokens) {
            print("$tok  ")
        }
        println()
        // 将词法分析器所有的错误信息添加到diagnostics中
        this.lexerDiagnostics = lexer.diagnostics
    }

    /**
     * 返回token
     */
    private fun peek(offset: Int): SyntaxToken {
        var index = position + offset
        if(index >= tokens.size) return tokens[tokens.size - 1]
        return tokens[index]
    }

    private var current: SyntaxToken
        get() {
            return peek(0)
        }
        set(value) {}

    private fun nextToken(): SyntaxToken {
        val tok = current
        ++position
        return tok
    }

    private fun matchToken(kind: TokenKind): SyntaxToken {
        if(current.kind == kind) {
            return nextToken()
        }
        diagnostics.add("error: Unexpected token <${current.kind}>, expected <$kind>. row: ${current.row}, col: ${current.col}")
        return current
    }

    fun parse(): SyntaxTree {
        var result = parseProgram()
        var endOfFileToken = matchToken(TokenKind.EOF)
        // 只保留语法分析器的第一个错误
        if(diagnostics.isNotEmpty()) {
            return SyntaxTree(this.lexerDiagnostics + this.diagnostics[0], result, endOfFileToken)
        }
        return SyntaxTree(this.lexerDiagnostics, result, endOfFileToken)
    }

    private fun parseProgram(): SyntaxNode {
        if(current.kind == TokenKind.PROGRAM) {
            var head = parseProgramHead()
            var declarePart = parseDeclarePart()
            var body = parseProgramBody()
            var dotToken = matchToken(TokenKind.DOT)
            return CombinedSyntax(TokenKind.ProgramSyntax, arrayListOf(head, declarePart, body, dotToken))
        }
        return BadSyntax(current)
    }

    private fun parseProgramHead(): SyntaxNode {
        if(current.kind == TokenKind.PROGRAM) {
            var programToken = matchToken(TokenKind.PROGRAM)
            var programName = parseProgramName()
            return CombinedSyntax(TokenKind.ProgramHeadSyntax, arrayListOf(programToken, programName))
        }
        return BadSyntax(current)
    }

    private fun parseProgramName(): SyntaxNode {
        if(current.kind == TokenKind.ID) {
            var name = matchToken(TokenKind.ID)
            return CombinedSyntax(TokenKind.ProgramNameSyntax, arrayListOf(name))
        }
        return BadSyntax(current)
    }

    private fun parseDeclarePart(): SyntaxNode = when(current.kind) {
        TokenKind.TYPE, TokenKind.VAR, TokenKind.PROCEDURE, TokenKind.BEGIN -> {
            var typedecPart = parseTypeDecPart()
            var varDecPart = parseVarDecPart()
            var procDecPart = parseProcDecpart()
            CombinedSyntax(TokenKind.DeclarePartSyntax, arrayListOf(typedecPart, varDecPart, procDecPart))
        }
        else -> BadSyntax(current)
    }

    private fun parseTypeDecPart(): SyntaxNode = when(current.kind) {
        TokenKind.VAR, TokenKind.PROCEDURE, TokenKind.BEGIN -> CombinedSyntax(TokenKind.TypeDeclarePartSyntax, arrayListOf(SyntaxToken.EmptyToken))

        TokenKind.TYPE -> {
            var typeDecSyntax = parseTypeDec()
            CombinedSyntax(TokenKind.TypeDeclarePartSyntax, arrayListOf(typeDecSyntax))
        }
        else -> BadSyntax(current)
    }

    private fun parseTypeDec(): SyntaxNode = when(current.kind) {
        TokenKind.TYPE -> {
            var token = matchToken(TokenKind.TYPE)
            var typeDecList = parseTypeDecListSyntax()
            CombinedSyntax(TokenKind.TypeDeclareSyntax, arrayListOf(token, typeDecList))
        }
        else -> BadSyntax(current)
    }

    private fun parseTypeDecListSyntax(): SyntaxNode = when(current.kind) {
        TokenKind.ID -> {
            var typeId = parseTypeId()
            var equalsToken = matchToken(TokenKind.EqualsToken)
            var typeDef = parseTypeDefine()
            var semiToken = matchToken(TokenKind.SemiToken)
            var typeDecMore = parseTypeDecMore()
            CombinedSyntax(TokenKind.TypeDeclareListSyntax, arrayListOf(typeId, equalsToken, typeDef, semiToken, typeDecMore))
        }
        else -> BadSyntax(current)
    }

    private fun parseTypeDecMore(): SyntaxNode = when(current.kind) {
        TokenKind.VAR, TokenKind.PROCEDURE, TokenKind.BEGIN -> CombinedSyntax(TokenKind.TypeDecMoreSyntax, arrayListOf(SyntaxToken.EmptyToken))

        TokenKind.ID -> {
            var typeDecList = parseTypeDecListSyntax()
            CombinedSyntax(TokenKind.TypeDecMoreSyntax, arrayListOf(typeDecList))
        }

        else -> BadSyntax(current)
    }

    private fun parseTypeId(): SyntaxNode = when(current.kind) {
        TokenKind.ID -> {
            var variableToken = matchToken(TokenKind.ID)
            CombinedSyntax(TokenKind.TypeIdSyntax, arrayListOf(variableToken))
        }

        else -> BadSyntax(current)
    }

    private fun parseTypeDefine(): SyntaxNode = when(current.kind) {
        TokenKind.INTEGER, TokenKind.CHAR -> {
            var baseTypeSyntax = parseBaseTypeSyntax()
            CombinedSyntax(TokenKind.TypeDefineSyntax, arrayListOf(baseTypeSyntax))
        }

        TokenKind.ARRAY -> {
            var structureTypeSyntax = parseStructureType()
            CombinedSyntax(TokenKind.TypeDefineSyntax, arrayListOf(structureTypeSyntax))
        }

        TokenKind.ID -> {
            var variableToken = matchToken(TokenKind.ID)
            CombinedSyntax(TokenKind.TypeDefineSyntax, arrayListOf(variableToken))
        }

        else -> BadSyntax(current)
    }


    private fun parseStructureType(): SyntaxNode = when(current.kind) {
        TokenKind.ARRAY -> {
            var arrayType = parseArrayType()
            CombinedSyntax(TokenKind.StructureTypeSyntax, arrayListOf(arrayType))
        }
        // TODO 添加Record
        else -> BadSyntax(current)
    }

    private fun parseArrayType(): SyntaxNode = when(current.kind) {
        TokenKind.ARRAY -> {
            var arrayToken = matchToken(TokenKind.ARRAY)
            var lbToken = matchToken(TokenKind.LBRACKET)
            var low = parseLow()
            var dotdotToken = matchToken(TokenKind.DotDotToken)
            var high = parseHigh()
            var rbToken = matchToken(TokenKind.RBRACKET)
            var ofToken = matchToken(TokenKind.OF)
            var baseType = parseBaseTypeSyntax()
            CombinedSyntax(TokenKind.ArrayTypeSyntax, arrayListOf(arrayToken, lbToken, low, dotdotToken, high, rbToken, ofToken, baseType))
        }

        else -> BadSyntax(current)
    }

    private fun parseHigh(): SyntaxNode = when(current.kind) {
        TokenKind.NumberToken -> {
            var token = matchToken(TokenKind.NumberToken)
            CombinedSyntax(TokenKind.HighSyntax, arrayListOf(token))
        }
        else -> BadSyntax(current)
    }

    private fun parseLow(): SyntaxNode = when(current.kind) {
        TokenKind.NumberToken -> {
            var token = matchToken(TokenKind.NumberToken)
            CombinedSyntax(TokenKind.LowSyntax, arrayListOf(token))
        }
        else -> BadSyntax(current)
    }

    private fun parseBaseTypeSyntax(): SyntaxNode = when(current.kind) {
        TokenKind.INTEGER, TokenKind.CHAR -> {
            var token = matchToken(current.kind)
            CombinedSyntax(TokenKind.BaseTypeSyntax, arrayListOf(token))
        }
        else -> BadSyntax(current)
    }

    private fun parseVarDecPart(): SyntaxNode = when(current.kind) {
        TokenKind.PROCEDURE, TokenKind.BEGIN -> SyntaxToken.EmptyToken
        TokenKind.VAR -> {
            var varDec = parVarDec()
            CombinedSyntax(TokenKind.VarDeclarePartSyntax, arrayListOf(varDec))
        }
        else -> BadSyntax(current)
    }

    private fun parVarDec(): SyntaxNode = when(current.kind) {
        TokenKind.VAR -> {
            var varToken = matchToken(TokenKind.VAR)
            var varDecList = parseVarDecList()
            CombinedSyntax(TokenKind.VarDeclareSyntax, arrayListOf(varToken, varDecList))
        }
        else -> BadSyntax(current)
    }

    private fun parseVarDecList(): SyntaxNode = when(current.kind) {
        // TODO 添加RECORD
        TokenKind.INTEGER, TokenKind.CHAR, TokenKind.ARRAY, TokenKind.ID -> {
            var typeDef = parseTypeDefine()
            var varIdList = parseVarIdList()
            var semiToken = matchToken(TokenKind.SemiToken)
            var varDecMore = parseVarDecMore()
            CombinedSyntax(TokenKind.VarDecListSyntax, arrayListOf(typeDef, varIdList, semiToken, varDecMore))
        }
        else -> BadSyntax(current)
    }

    private fun parseVarDecMore(): SyntaxNode = when(current.kind) {
        TokenKind.PROCEDURE, TokenKind.BEGIN -> SyntaxToken.EmptyToken
        // TODO 添加RECORD
        TokenKind.INTEGER, TokenKind.CHAR, TokenKind.ARRAY, TokenKind.ID -> {
            var varDecList = parseVarDecList()
            CombinedSyntax(TokenKind.VarDeclareMoreSyntax, arrayListOf(varDecList))
        }
        else -> BadSyntax(current)
    }

    private fun parseVarIdList(): SyntaxNode = when(current.kind) {
        TokenKind.ID -> {
            var variableToken = matchToken(TokenKind.ID)
            var varIdMore = parseVarIdMore()
            CombinedSyntax(TokenKind.VarIdListSyntax, arrayListOf(variableToken, varIdMore))
        }
        else -> BadSyntax(current)
    }

    private fun parseVarIdMore(): SyntaxNode = when(current.kind) {
        TokenKind.SemiToken -> SyntaxToken.EmptyToken
        TokenKind.COMMA -> {
            var commaToken = matchToken(TokenKind.COMMA)
            var varIdList = parseVarIdList()
            CombinedSyntax(TokenKind.VarIdMoreSyntax, arrayListOf(commaToken, varIdList))
        }
        else -> BadSyntax(current)
    }

    // 过程声明
    private fun parseProcDecpart(): SyntaxNode = when(current.kind) {
        TokenKind.BEGIN -> SyntaxToken.EmptyToken
        TokenKind.PROCEDURE -> {
            var procDec = parseProcDec()
            CombinedSyntax(TokenKind.ProcDecpartSyntax, arrayListOf(procDec))
        }
        else -> BadSyntax(current)
    }

    private fun parseProcDec(): SyntaxNode = when(current.kind) {
        TokenKind.PROCEDURE -> {
            var procedureToken = matchToken(TokenKind.PROCEDURE)
            var procName = parseProcName()
            var openParenToken = matchToken(TokenKind.OpenParenToken)
            var paramList = parseParamList()
            var closedParenToken = matchToken(TokenKind.ClosedParenToken)
            var semiToken = matchToken(TokenKind.SemiToken)
            var procDecPart = parseProcDecPart()
            var procBody = parseProcBody()
            var procProcDecMore = parseProcDecMore()
            CombinedSyntax(TokenKind.ProcDeclareSyntax, arrayListOf(procedureToken, procName,
                    openParenToken, paramList, closedParenToken, semiToken, procDecPart, procBody, procProcDecMore))
        }

        else -> BadSyntax(current)
    }

    private fun parseProcDecMore(): SyntaxNode = when(current.kind) {
        TokenKind.BEGIN -> SyntaxToken.EmptyToken
        TokenKind.PROCEDURE -> {
            var procDec = parseProcDec()
            CombinedSyntax(TokenKind.ProcDeclareMoreSyntax, arrayListOf(procDec))
        }
        else -> BadSyntax(current)
    }

    // 参数声明
    private fun parseParamList(): SyntaxNode = when(current.kind) {
        TokenKind.ClosedParenToken -> SyntaxToken.EmptyToken
        // TODO 添加RECORD
        TokenKind.INTEGER, TokenKind.CHAR, TokenKind.ARRAY, TokenKind.ID, TokenKind.VAR -> {
            var paramDecList = parseParamDecList()
            CombinedSyntax(TokenKind.ParamListSyntax, arrayListOf(paramDecList))
        }

        else -> BadSyntax(current)
    }

    private fun parseParamDecList(): SyntaxNode = when(current.kind){
        // TODO 添加RECORD
        TokenKind.INTEGER, TokenKind.CHAR, TokenKind.ARRAY, TokenKind.ID, TokenKind.VAR -> {
            var param = parseParam()
            var paramMore = parseParamMore()
            CombinedSyntax(TokenKind.ParamDecListSyntax, arrayListOf(param, paramMore))
        }
        else -> BadSyntax(current)
    }

    private fun parseParamMore(): SyntaxNode = when(current.kind) {
        TokenKind.ClosedParenToken -> SyntaxToken.EmptyToken
        TokenKind.SemiToken -> {
            var semiToken = matchToken(TokenKind.SemiToken)
            var paramDecList = parseParamDecList()
            CombinedSyntax(TokenKind.ParamMoreSyntax, arrayListOf(semiToken, paramDecList))
        }
        else -> BadSyntax(current)
    }

    private fun parseParam(): SyntaxNode = when(current.kind) {
        // TODO 添加RECORD
        TokenKind.INTEGER, TokenKind.CHAR, TokenKind.ARRAY, TokenKind.ID -> {
            var typeDef = parseTypeDefine()
            var formList = parseFormList()
            CombinedSyntax(TokenKind.ParamSyntax, arrayListOf(typeDef, formList))
        }
        TokenKind.VAR -> {
            var varToken = matchToken(TokenKind.VAR)
            var typeDef = parseTypeDefine()
            var formList = parseFormList()
            CombinedSyntax(TokenKind.ParamSyntax, arrayListOf(varToken, typeDef, formList))
        }
        else -> BadSyntax(current)
    }

    private fun parseFormList(): SyntaxNode = when(current.kind) {
        TokenKind.ID -> {
            var variableToken = matchToken(TokenKind.ID)
            var fidMore = parseFidMore()
            CombinedSyntax(TokenKind.FormListSyntax, arrayListOf(variableToken, fidMore))
        }
        else -> BadSyntax(current)
    }

    private fun parseFidMore(): SyntaxNode = when(current.kind) {
        TokenKind.SemiToken, TokenKind.ClosedParenToken -> SyntaxToken.EmptyToken
        TokenKind.COMMA -> {
            var commaToken = matchToken(TokenKind.COMMA)
            var formList = parseFormList()
            CombinedSyntax(TokenKind.FidMoreSyntax, arrayListOf(commaToken, formList))
        }
        else -> BadSyntax(current)
    }

    private fun parseProcName(): SyntaxNode = when(current.kind){
        TokenKind.ID -> {
            var variableToken = matchToken(TokenKind.ID)
            CombinedSyntax(TokenKind.ProcNameSyntax, arrayListOf(variableToken))
        }
        else -> BadSyntax(current)
    }

    // 过程中的声明部分
    private fun parseProcDecPart(): SyntaxNode = when(current.kind) {
        TokenKind.TYPE, TokenKind.VAR, TokenKind.PROCEDURE, TokenKind.BEGIN -> {
            var declarePart = parseDeclarePart()
            CombinedSyntax(TokenKind.ProcDecpartSyntax, arrayListOf(declarePart))
        }
        else -> SyntaxToken.EmptyToken
    }

    private fun parseProcBody(): SyntaxNode = when(current.kind) {
        TokenKind.BEGIN -> {
            var programBody = parseProgramBody()
            CombinedSyntax(TokenKind.ProcBodySyntax, arrayListOf(programBody))
        }
        else -> BadSyntax(current)
    }

    private fun parseProgramBody(): SyntaxNode = when(current.kind) {
        TokenKind.BEGIN -> {
            var beginToken = matchToken(TokenKind.BEGIN)
            var stmtList = parseStmList()
            var endToken = matchToken(TokenKind.END)
            CombinedSyntax(TokenKind.ProgramBodySyntax, arrayListOf(beginToken, stmtList, endToken))
        }
        else -> BadSyntax(current)
    }

    private fun parseStmList(): SyntaxNode = when(current.kind){
        TokenKind.ID, TokenKind.IF, TokenKind.WHILE, TokenKind.RETURN, TokenKind.READ, TokenKind.WRITE -> {
            var stm = parseStm()
            var stmMore = parseStmMore()
            CombinedSyntax(TokenKind.StmtListSyntax, arrayListOf(stm, stmMore))
        }

        else -> BadSyntax(current)
    }

    private fun parseStmMore(): SyntaxNode = when(current.kind) {
        TokenKind.ELSE, TokenKind.FI, TokenKind.END, TokenKind.ELIHW -> SyntaxToken.EmptyToken
        TokenKind.SemiToken -> {
            var semiToken = matchToken(TokenKind.SemiToken)
            var stmtList = parseStmList()
            CombinedSyntax(TokenKind.StmMore, arrayListOf(semiToken, stmtList))
        }
        else -> BadSyntax(current)
    }

    private fun parseStm(): SyntaxNode = when(current.kind) {
        TokenKind.IF -> {
            var conditionalStm = parseConditionalStm()
            CombinedSyntax(TokenKind.StmSyntax, arrayListOf(conditionalStm))
        }
        TokenKind.WHILE -> {
            var loopStm = parseLoopStm()
            CombinedSyntax(TokenKind.StmSyntax, arrayListOf(loopStm))
        }
        TokenKind.READ -> {
            var inputStm = parseInputStm()
            CombinedSyntax(TokenKind.StmSyntax, arrayListOf(inputStm))
        }
        TokenKind.WRITE -> {
            var outputStm = parseOutputStm()
            CombinedSyntax(TokenKind.StmSyntax, arrayListOf(outputStm))
        }
        TokenKind.RETURN -> {
            var returnStm = parseReturnStm()
            CombinedSyntax(TokenKind.StmSyntax, arrayListOf(returnStm))
        }
        TokenKind.ID -> {
            var variableToken = matchToken(TokenKind.ID)
            var assCall = parseAssCall()
            CombinedSyntax(TokenKind.StmSyntax, arrayListOf(variableToken, assCall))
        }
        else -> BadSyntax(current)
    }

    private fun parseAssCall(): SyntaxNode = when(current.kind) {
        TokenKind.LBRACKET, TokenKind.DOT, TokenKind.ASSIGN -> {
            var assignmentRest = parseAssignmentRest()
            CombinedSyntax(TokenKind.AssCallSyntax, arrayListOf(assignmentRest))
        }

        TokenKind.OpenParenToken -> {
            var callStmRest = parseCallStmRest()
            CombinedSyntax(TokenKind.AssCallSyntax, arrayListOf(callStmRest))
        }
        else -> BadSyntax(current)
    }

    // 赋值语句
    private fun parseAssignmentRest(): SyntaxNode = when(current.kind) {
        TokenKind.LBRACKET, TokenKind.DOT, TokenKind.ASSIGN -> {
            var variMore = parseVariMore()
            var assignToken = matchToken(TokenKind.ASSIGN)
            var exp = parseExp()
            CombinedSyntax(TokenKind.AssignmentRestSyntax, arrayListOf(variMore, assignToken, exp))
        }
        else -> BadSyntax(current)
    }

    // 返回语句
    private fun parseReturnStm(): SyntaxNode = when(current.kind) {
        TokenKind.RETURN -> {
            var returnToken = matchToken(TokenKind.RETURN)
            CombinedSyntax(TokenKind.ReturnStmSyntax, arrayListOf(returnToken))
        }
        else -> BadSyntax(current)
    }

    private fun parseOutputStm(): SyntaxNode = when(current.kind) {
        TokenKind.WRITE -> {
            var writeToken = matchToken(TokenKind.WRITE)
            var openParenToken = matchToken(TokenKind.OpenParenToken)
            var exp = parseExp()
            var closedParenToken = matchToken(TokenKind.ClosedParenToken)
            CombinedSyntax(TokenKind.OutputStmSyntax, arrayListOf(writeToken, openParenToken, exp, closedParenToken))
        }
        else -> BadSyntax(current)
    }

    private fun parseInputStm(): SyntaxNode = when(current.kind) {
        TokenKind.READ -> {
            var readToken = matchToken(TokenKind.READ)
            var openParenToken = matchToken(TokenKind.OpenParenToken)
            var invar = parseInvar()
            var closedParenToken = matchToken(TokenKind.ClosedParenToken)
            CombinedSyntax(TokenKind.InputStmSyntax, arrayListOf(readToken, openParenToken, invar, closedParenToken))
        }
        else -> BadSyntax(current)
    }

    private fun parseInvar(): SyntaxNode = when(current.kind) {
        TokenKind.ID -> {
            var token = matchToken(TokenKind.ID)
            CombinedSyntax(TokenKind.InvarSyntax, arrayListOf(token))
        }
        else -> BadSyntax(current)
    }

    // 循环语句
    private fun parseLoopStm(): SyntaxNode = when(current.kind) {
        TokenKind.WHILE -> {
            var whileToken = matchToken(TokenKind.WHILE)
            var relExp = parseRelExp()
            var doToken = matchToken(TokenKind.DO)
            var stmtList = parseStmList()
            var elihw = matchToken(TokenKind.ELIHW)
            CombinedSyntax(TokenKind.LoopStmSyntax, arrayListOf(whileToken, relExp, doToken, stmtList, elihw))
        }
        else -> BadSyntax(current)
    }

    // 条件语句
    private fun parseConditionalStm(): SyntaxNode = when(current.kind){
        TokenKind.IF -> {
            var ifToken = matchToken(TokenKind.IF)
            var relExp = parseRelExp()
            var thenToken = matchToken(TokenKind.THEN)
            var stmtList1 = parseStmList()
            var elseToken = matchToken(TokenKind.ELSE)
            var stmtList2 = parseStmList()
            var fiToken = matchToken(TokenKind.FI)
            CombinedSyntax(TokenKind.ConditionalStmSyntax, arrayListOf(ifToken, relExp, thenToken, stmtList1, elseToken, stmtList2, fiToken))
        }

        else -> BadSyntax(current)
    }


    // 过程调用语句
    private fun parseCallStmRest(): SyntaxNode = when(current.kind) {
        TokenKind.OpenParenToken -> {
            var openParenToken = matchToken(TokenKind.OpenParenToken)
            var actParamList = parseActParamList()
            var closedParenToken = matchToken(TokenKind.ClosedParenToken)
            CombinedSyntax(TokenKind.CallStmRestSyntax, arrayListOf(openParenToken, actParamList, closedParenToken))
        }
        else -> BadSyntax(current)
    }

    private fun parseActParamList(): SyntaxNode = when(current.kind) {
        TokenKind.ClosedParenToken -> SyntaxToken.EmptyToken
        TokenKind.OpenParenToken, TokenKind.NumberToken, TokenKind.ID -> {
            var exp = parseExp()
            var actParamMore = parseActParamMore()
            CombinedSyntax(TokenKind.ActParamListSyntax, arrayListOf(exp, actParamMore))
        }
        else -> BadSyntax(current)
    }

    private fun parseActParamMore(): SyntaxNode = when(current.kind) {
        TokenKind.ClosedParenToken -> SyntaxToken.EmptyToken
        TokenKind.COMMA -> {
            var commaToken = matchToken(TokenKind.COMMA)
            var actParamList = parseActParamList()
            CombinedSyntax(TokenKind.ActParamMoreSyntax, arrayListOf(commaToken, actParamList))
        }
        else -> BadSyntax(current)
    }

    private fun parseRelExp(): SyntaxNode = when(current.kind) {
        TokenKind.OpenParenToken, TokenKind.NumberToken, TokenKind.ID -> {
            var exp = parseExp()
            var otherRelE = parseOtherRelE()
            CombinedSyntax(TokenKind.RelExpSyntax, arrayListOf(exp, otherRelE))
        }
        else -> BadSyntax(current)
    }

    private fun parseOtherRelE(): SyntaxNode = when(current.kind) {
        TokenKind.EqualsToken, TokenKind.LT -> {
            var cmpOp = parseCmpOp()
            var exp = parseExp()
            CombinedSyntax(TokenKind.OtherRelESyntax, arrayListOf(cmpOp, exp))
        }
        else -> BadSyntax(current)
    }



    private fun parseExp(): SyntaxNode = when(current.kind) {
        TokenKind.OpenParenToken, TokenKind.NumberToken, TokenKind.ID -> {
            var term = parseTerm()
            var otherTerm = parseOtherTerm()
            CombinedSyntax(TokenKind.ExpSyntax, arrayListOf(term, otherTerm))
        }
        else -> BadSyntax(current)
    }

    private fun parseOtherTerm(): SyntaxNode = when(current.kind) {
        TokenKind.LT, TokenKind.EqualsToken, TokenKind.RBRACKET, TokenKind.THEN, TokenKind.ELSE,
            TokenKind.FI, TokenKind.DO, TokenKind.ELIHW, TokenKind.ClosedParenToken, TokenKind.END,
            TokenKind.SemiToken, TokenKind.COMMA -> SyntaxToken.EmptyToken
        TokenKind.PlusToken, TokenKind.MinusToken -> {
            var addOp = parseAddOp()
            var exp = parseExp()
            CombinedSyntax(TokenKind.OtherTermSyntax, arrayListOf(addOp, exp))
        }
        else -> BadSyntax(current)
    }



    // 项
    private fun parseTerm(): SyntaxNode = when(current.kind) {
        TokenKind.OpenParenToken, TokenKind.NumberToken, TokenKind.ID -> {
            var factor = parseFactor()
            var otherFactor = parseOtherFactor()
            CombinedSyntax(TokenKind.TermSyntax, arrayListOf(factor, otherFactor))
        }
        else -> BadSyntax(current)
    }

    private fun parseOtherFactor(): SyntaxNode = when(current.kind) {
        TokenKind.PlusToken, TokenKind.MinusToken, TokenKind.LT, TokenKind.EqualsToken, TokenKind.RBRACKET,
            TokenKind.THEN, TokenKind.ELSE, TokenKind.FI, TokenKind.DO, TokenKind.ELIHW, TokenKind.ClosedParenToken,
            TokenKind.END, TokenKind.SemiToken, TokenKind.COMMA -> SyntaxToken.EmptyToken

        TokenKind.StarToken, TokenKind.SlashToken -> {
            var multOp = parseMultOp()
            var term = parseTerm()
            CombinedSyntax(TokenKind.OtherFactorSyntax, arrayListOf(multOp, term))
        }
        else -> BadSyntax(current)
    }



    private fun parseFactor(): SyntaxNode = when(current.kind) {
        TokenKind.OpenParenToken -> {
            var openParenToken = matchToken(TokenKind.OpenParenToken)
            var exp = parseExp()
            var closedParenToken = matchToken(TokenKind.ClosedParenToken)
            CombinedSyntax(TokenKind.FactorSyntax, arrayListOf(openParenToken, exp, closedParenToken))
        }

        TokenKind.NumberToken -> {
            var numberToken = matchToken(TokenKind.NumberToken)
            CombinedSyntax(TokenKind.FactorSyntax, arrayListOf(numberToken))
        }

        TokenKind.ID -> {
            var variable = parseVariable()
            CombinedSyntax(TokenKind.FactorSyntax, arrayListOf(variable))
        }
        else -> BadSyntax(current)
    }

    private fun parseVariable(): SyntaxNode = when(current.kind) {
        TokenKind.ID -> {
            var idToken = matchToken(TokenKind.ID)
            var variableMore = parseVariableMore()
            CombinedSyntax(TokenKind.VariableSyntax, arrayListOf(idToken, variableMore))
        }

        else -> BadSyntax(current)
    }

    private fun parseVariableMore(): SyntaxNode = when(current.kind) {
        TokenKind.RBRACKET, TokenKind.ASSIGN, TokenKind.StarToken, TokenKind.SlashToken, TokenKind.PlusToken,
            TokenKind.MinusToken, TokenKind.LT, TokenKind.EqualsToken, TokenKind.THEN, TokenKind.ELSE,
            TokenKind.FI, TokenKind.DO, TokenKind.ELIHW, TokenKind.ClosedParenToken, TokenKind.END,
            TokenKind.SemiToken, TokenKind.COMMA -> SyntaxToken.EmptyToken

        TokenKind.LBRACKET -> {
            var lbToken = matchToken(TokenKind.LBRACKET)
            var exp = parseExp()
            var rbToken = matchToken(TokenKind.RBRACKET)
            CombinedSyntax(TokenKind.VariableMoreSyntax, arrayListOf(lbToken, exp, rbToken))
        }

        TokenKind.DOT -> {
            var dotToken = matchToken(TokenKind.DOT)
            var fieldVar = parseFieldVar()
            CombinedSyntax(TokenKind.VariableMoreSyntax, arrayListOf(dotToken, fieldVar))
        }

        else -> BadSyntax(current)

    }

    private fun parseFieldVar(): SyntaxNode = when(current.kind) {
        TokenKind.ID -> {
            var idToken = matchToken(TokenKind.ID)
            var fieldVarMore = parseFieldVarMore()
            CombinedSyntax(TokenKind.FieldVarSyntax, arrayListOf(idToken, fieldVarMore))
        }

        else -> BadSyntax(current)
    }

    private fun parseFieldVarMore(): SyntaxNode = when(current.kind) {
        TokenKind.ASSIGN, TokenKind.StarToken, TokenKind.SlashToken, TokenKind.PlusToken, TokenKind.MinusToken,
            TokenKind.LT, TokenKind.EqualsToken, TokenKind.THEN, TokenKind.ELSE, TokenKind.FI, TokenKind.DO,
            TokenKind.ELIHW, TokenKind.ClosedParenToken, TokenKind.END, TokenKind.SemiToken, TokenKind.COMMA
            -> SyntaxToken.EmptyToken

        TokenKind.LBRACKET -> {
            var lbToken = matchToken(TokenKind.LBRACKET)
            var exp = parseExp()
            var rbToken = matchToken(TokenKind.RBRACKET)
            CombinedSyntax(TokenKind.FieldVarMoreSyntax, arrayListOf(lbToken, exp, rbToken))
        }
        else -> BadSyntax(current)
    }

    private fun parseVariMore(): SyntaxNode = when(current.kind){
        TokenKind.RBRACKET, TokenKind.ASSIGN, TokenKind.StarToken, TokenKind.SlashToken,
            TokenKind.PlusToken, TokenKind.MinusToken, TokenKind.LT, TokenKind.EqualsToken,
            TokenKind.THEN, TokenKind.ELSE, TokenKind.FI, TokenKind.DO, TokenKind.ELIHW,
            TokenKind.ClosedParenToken, TokenKind.END, TokenKind.SemiToken, TokenKind.COMMA
            -> SyntaxToken.EmptyToken

        TokenKind.LBRACKET -> {
            var lbToken = matchToken(TokenKind.LBRACKET)
            var exp = parseExp()
            var rbToken = matchToken(TokenKind.RBRACKET)
            CombinedSyntax(TokenKind.VariMoreSyntax, arrayListOf(lbToken, exp, rbToken))
        }

        TokenKind.DOT -> {
            var dotToken = matchToken(TokenKind.DOT)
            var fieldVar = parseFieldVar()
            CombinedSyntax(TokenKind.VariMoreSyntax, arrayListOf(dotToken, fieldVar))
        }

        else -> BadSyntax(current)
    }

    private fun parseCmpOp(): SyntaxNode = when(current.kind) {
        TokenKind.LT -> {
            var ltToken = matchToken(TokenKind.LT)
            CombinedSyntax(TokenKind.CompOpSyntax, arrayListOf(ltToken))
        }
        TokenKind.EqualsToken -> {
            var eqToken = matchToken(TokenKind.EqualsToken)
            CombinedSyntax(TokenKind.CompOpSyntax, arrayListOf(eqToken))
        }
        else -> BadSyntax(current)
    }


    private fun parseAddOp(): SyntaxNode = when(current.kind) {
        TokenKind.PlusToken -> {
            var plusToken = matchToken(TokenKind.PlusToken)
            CombinedSyntax(TokenKind.AddOpSyntax, arrayListOf(plusToken))
        }

        TokenKind.MinusToken -> {
            var minusToken = matchToken(TokenKind.MinusToken)
            CombinedSyntax(TokenKind.AddOpSyntax, arrayListOf(minusToken))
        }
        
        else -> BadSyntax(current)
    }

    private fun parseMultOp(): SyntaxNode = when(current.kind) {
        TokenKind.StarToken -> {
            var starToken = matchToken(TokenKind.StarToken)
            CombinedSyntax(TokenKind.MultOpSyntax, arrayListOf(starToken))
        }

        TokenKind.SlashToken -> {
            var slashToken = matchToken(TokenKind.SlashToken)
            CombinedSyntax(TokenKind.MultOpSyntax, arrayListOf(slashToken))
        }

        else -> BadSyntax(current)
    }
}