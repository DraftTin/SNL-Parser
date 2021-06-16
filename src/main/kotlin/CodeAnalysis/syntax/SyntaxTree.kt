package CodeAnalysis.syntax

import java.io.Reader

class SyntaxTree(var diagnostics: List<String>, var root: SyntaxNode, var endOfFileToken: SyntaxToken) {
    companion object {
        fun createSyntaxTree(data: Reader): SyntaxTree {
            var parser = RDParser(data)
            return parser.parse()
        }
    }
}
