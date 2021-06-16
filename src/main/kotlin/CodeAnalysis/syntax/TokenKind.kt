package CodeAnalysis.syntax

enum class TokenKind {
    // Keywords
    VAR,
    IF,
    THEN,
    ELSE,
    FI,
    WHILE,
    DO,
    ELIHW,
    BEGIN,
    END,
    RETURN,
    PROGRAM,
    PROCEDURE,
    TYPE,
    CHAR,
    INTEGER,
    BOOL,
    REAL,
    ARRAY,
    OF,

    EqualsToken,             // =
    NotEqualsToken,         // !=
    GT,                     // >
    LT,                     // <
    GE,                     // >=
    LE,                     // <=
    OpenParenToken,         // (
    ClosedParenToken,       // )
    PlusToken,                   // +
    MinusToken,                  // -
    StarToken,                   // *
    SlashToken,                  // /
    ASSIGN,                 // :=
    OpenComment,                // {
    ClosedComment,                // }
    SemiToken,                   // ;
    MOD,                    // %
    LBRACKET,               // [
    RBRACKET,               // ]
    COMMA,                  // ,
    DOT,                    // .
    AmpersandAmpersandToken,    // &&
    PipePipeToken,              // ||
    BangToken,                  // !
    DotDotToken,                // ..

    // value token
    NumberToken,
    RealToken,
    CharToken,
    TrueToken,
    FalseToken,
    CommentToken,

    ID,

    EOF,            // end of file
    EmptyToken,
    BadToken,       // 未识别的token

    // Expressions
    BinaryExpression,
    LiteralExpression,
    ParenthesizedExpression,
    BadExpressionSyntax,
    UnaryExpressionSyntax,

    // Statements
    IfStatementSyntax,
    WhileStatementSyntax,

    // Node
    ProgramSyntax,
    ProgramHeadSyntax,
    DeclarePartSyntax,
    ProgramNameSyntax,
    VarDeclarePartSyntax,
    TypeDeclarePartSyntax,
    TypeDeclareSyntax,
    TypeDeclareListSyntax,
    TypeDecMoreSyntax,
    TypeIdSyntax,
    BaseTypeSyntax,
    LowSyntax,
    HighSyntax,
    TypeDefineSyntax,
    BadSyntax,
    StructureTypeSyntax,
    ArrayTypeSyntax,
    VarDeclareSyntax,
    VarDecListSyntax,
    VarDeclareMoreSyntax,
    VarIdListSyntax,
    VarIdMoreSyntax,
    ProcDecpartSyntax,
    ProcDeclareSyntax,
    ProcDeclareMoreSyntax,
    ProcNameSyntax,
    ParamListSyntax,
    ParamDecListSyntax,
    ParamMoreSyntax,
    ParamSyntax,
    FormListSyntax,
    FidMoreSyntax,
    ProcBodySyntax,
    ProgramBodySyntax,
    READ,
    WRITE,
    StmtListSyntax,
    StmMore,
    StmSyntax,
    AssCallSyntax,
    AssignmentRestSyntax,
    ConditionalStmSyntax,
    LoopStmSyntax,
    InputStmSyntax,
    InvarSyntax,
    OutputStmSyntax,
    ReturnStmSyntax,
    CallStmRestSyntax,
    ActParamListSyntax,
    ActParamMoreSyntax,
    RelExpSyntax,
    OtherRelESyntax,
    ExpSyntax,
    OtherTermSyntax,
    TermSyntax,
    OtherFactorSyntax,
    FactorSyntax,
    VariableSyntax,
    VariableMoreSyntax,
    FieldVarSyntax,
    FieldVarMoreSyntax,
    CompOpSyntax,
    AddOpSyntax,
    MultOpSyntax,
    VariMoreSyntax,

}