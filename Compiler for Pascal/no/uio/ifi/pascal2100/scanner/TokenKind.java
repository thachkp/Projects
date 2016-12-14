package no.uio.ifi.pascal2100.scanner;

// Note that tokens found in standard Pascal but not in Pascal2100
// have been commented out.

public enum TokenKind {
    nameToken("name"),
    intValToken("number"),
    stringValToken("text string"),

    addToken("+"),
    assignToken(":="),
    colonToken(":"),
    commaToken(","),
 /* divideToken("/"), */
    dotToken("."),
    equalToken("="),
    greaterToken(">"),
    greaterEqualToken(">="),
    leftBracketToken("["),
    leftParToken("("),
    lessToken("<"),
    lessEqualToken("<="),
    multiplyToken("*"),
    notEqualToken("<>"),
    rangeToken(".."),
    rightBracketToken("]"),
    rightParToken(")"),
    semicolonToken(";"),
    subtractToken("-"),
 /* upArrowToken("^"), */

    andToken("and"), 
    arrayToken("array"),
    beginToken("begin"), 
 /* caseToken("case"), */ 
    constToken("const"),
    divToken("div"), 
    doToken("do"), 
 /* downtoToken("downto"), */
    elseToken("else"), 
    endToken("end"),
 /* fileToken("file"), */
 /* forToken("for"), */ 
    functionToken("function"),
 /* gotoToken("goto"), */
    ifToken("if"), 
 /* inToken("in"), */
 /* labelToken("label"), */
    modToken("mod"),
 /* nilToken("nil"), */ 
    notToken("not"),
    ofToken("of"), 
    orToken("or"),
 /* packedToken("packed"), */ 
    procedureToken("procedure"), 
    programToken("program"),
 /* recordToken("record"), */ 
 /* repeatToken("repeat"), */
 /* setToken("set"), */
    thenToken("then"), 
 /* toToken("to"), */
    typeToken("type"),
 /* untilToken("until"), */
    varToken("var"),
    whileToken("while"), 
 /* withToken("with"), */

    eofToken("e-o-f");

    private String image;

    TokenKind(String im) {
	image = im;
    }


    public String identify() {
	return image + " token";
    }

    @Override public String toString() {
	return image;
    }


    public static boolean isFactorOpr(Token t) {
	return t.kind==multiplyToken || t.kind==divToken ||
			t.kind==modToken || t.kind==andToken;
    }

    public static boolean isPrefixOpr(Token t) {
	return t.kind==addToken || t.kind==subtractToken;
    }

    public static boolean isRelOpr(Token t) {
	return t.kind==equalToken || t.kind==notEqualToken ||
	    t.kind==lessToken || t.kind==lessEqualToken ||
	    t.kind==greaterToken || t.kind==greaterEqualToken;
    }

    public static boolean isTermOpr(Token t) {
	return isPrefixOpr(t) || t.kind==orToken;
    }
}
