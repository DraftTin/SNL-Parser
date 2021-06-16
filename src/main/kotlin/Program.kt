import CodeAnalysis.syntax.Lexer
import CodeAnalysis.syntax.SyntaxToken
import CodeAnalysis.syntax.SyntaxNode
import CodeAnalysis.syntax.SyntaxTree
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

fun prettyPrint(node: SyntaxNode, indent: String = "", isLast: Boolean = true) {
    // └──
    // │
    // ├──
    var marker = if(isLast) "└──" else "├──"
    print(indent)
    print(marker)
    print(node.kind)

    if(node is SyntaxToken) {
        print(" ")
        print(node.text)
    }
    println()
    var newIndent = StringBuffer(indent)
    newIndent.append(if(isLast) "   " else "|  ")
    for(child in node.getChildren()) {
        if(child == SyntaxToken.EmptyToken) continue
        prettyPrint(child, newIndent.toString(), child == node.getChildren().last())
    }
}

fun main() {
    var file = File("test.txt")
    file.createNewFile()
    var fileReader = InputStreamReader(FileInputStream(file))
    var syntaxTree = SyntaxTree.createSyntaxTree(fileReader)

    prettyPrint(syntaxTree.root)
    if(syntaxTree.diagnostics.isNotEmpty()) {
        for(diagnostic in syntaxTree.diagnostics) {
            println("\u001b[38;5;196m$diagnostic")
            print("\u001b[0m")
        }
    }
}