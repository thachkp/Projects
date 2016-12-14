package no.uio.ifi.pascal2100.scanner;

import no.uio.ifi.pascal2100.main.*;

public class Scanner {
    public  Token curToken , nextToken , nNextToken ; 
    public  int curNum, nextNum, nNextNum;
    public 	String temp =  "";
    public  int curLine, nextLine, nNextLine;
    String sourceFileName = "";
    String anF = "'";
    CharGenerator cg;
    public Scanner(String fileName) {
    	init();
    	sourceFileName = fileName;
    	cg = new CharGenerator(fileName);

        readNextToken() ;readNextToken() ;

    }
    public  void init() {
    	curToken = null; nextToken = null; nNextToken = null; 
        curToken = nextToken; nextToken = nNextToken;
        curNum = 0; nextNum = 0; nNextNum = 0; 
        curNum = nextNum ; nextNum = nNextNum;
        curLine = 0; nextLine = 0; nNextLine = 0; 
        curLine = nextLine; nextLine = nNextLine;


     }

    public String identify() {
	return "Scanner reading " + sourceFileName;
    }


    public int curLineNum() {
	return curToken.lineNum;
    }

    
    private void error(String message) {
	Main.error("Scanner error on line " + curLineNum() + ": " + message);
    }
    
   
    public void readNextToken() {
	// Del 1 her
        curToken = nextToken; nextToken = nNextToken;
        curNum = nextNum ; nextNum = nNextNum;
        curLine = nextLine; nextLine = nNextLine;
        temp = "";
    	
    	nNextToken = null;
    	while(nNextToken == null){
    		nNextLine = cg.cLineNum;
    		if(!cg.isMoreToRead() && cg.lastTwoChar()){
    			nNextToken = new Token(TokenKind.eofToken, cg.cLineNum);
    		}else{
        		nNextLine = cg.cLineNum;
        		if(cg.curC == ' ' || cg.curC == '\t'){
        			cg.readNext();
        		//check if there is comment in the program which is  or {}
        		}else if((cg.curC == '/' && cg.nextC == '*') ){
        			while(cg.isMoreToRead()){
        				if((cg.curC == '*' && cg.nextC == '/')) break;
                        cg.readNext();

        			}
        			if((cg.curC != '*' && cg.nextC != '/')){
    					Main.error("uendelig kommentarer");
    				}
        			//we need to update both our current and next character because they hold '*' and '/' to end the comment
        			cg.readNext(); cg.readNext();
        			//check if there is name or an letter.
        		
        		}else if(cg.curC == '{'){
        			while(cg.isMoreToRead()){

        				if (cg.nextC == '}') {
							break;
						}

        				cg.readNext();
        			}

        			if(cg.nextC != '}'){
    					Main.error("uendelig kommentarer ");
    				}
        			cg.readNext(); cg.readNext();


        		}else if(isLetterAZ(cg.curC)){
        			String name = "" + cg.curC;
        			while(cg.isMoreToRead()){
        				if(cg.checkLineBetween() || !isLetterAZ(cg.nextC) && !isDigit(cg.nextC) ){
        					break;
        				}
        				name += cg.nextC;
        				cg.readNext();
        			}

                    nNextToken = new Token(name,cg.cLineNum  );
        			cg.readNext();

        		
        			// check integer 
        		}else if(isDigit(cg.curC) && !isLetterAZ(cg.curC)){
        			String num = "" + cg.curC;
        			while(cg.isMoreToRead()){
        				if(!isDigit(cg.nextC) || cg.checkLineBetween())break;
        				num += cg.nextC;
        				cg.readNext();
        			}
        			int integer =  Integer.parseInt(num);
        			nNextNum = integer;
    				nNextToken = new Token(integer, cg.cLineNum );
    				cg.readNext();
    				
        		}else if(cg.curC == ';'){
        			nNextToken =  new Token(TokenKind.semicolonToken, cg.cLineNum );
        			cg.readNext();
        		}else if(cg.curC == '.' && cg.nextC == '.'){

                    nNextToken =  new Token(TokenKind.rangeToken, cg.cLineNum );
                    cg.readNext(); cg.readNext();

                }else if(cg.curC == '.'){
                    nNextToken =  new Token(TokenKind.dotToken, cg.cLineNum );
                    cg.readNext();
                    
                }else if(cg.curC == '('){
        			nNextToken =  new Token(TokenKind.leftParToken, cg.cLineNum );
        			cg.readNext();
        		}
        		else if(cg.curC == ')'){
        			nNextToken =  new Token(TokenKind.rightParToken, cg.cLineNum );
        			cg.readNext();
        		}else if(cg.curC == '+'){
        			nNextToken =  new Token(TokenKind.addToken, cg.cLineNum );
        			cg.readNext();
        		}
        		else if(cg.curC == ':'){
        			if(cg.nextC == '='  && !cg.checkLineBetween()){
            			nNextToken =  new Token(TokenKind.assignToken, cg.cLineNum );
            			cg.readNext(); cg.readNext();
        			}else {
        				nNextToken =  new Token(TokenKind.colonToken, cg.cLineNum );
        				cg.readNext();
        			}
        		}else if(cg.curC == ','){
        			nNextToken =  new Token(TokenKind.commaToken, cg.cLineNum );
        			cg.readNext();
        		}else if(cg.curC == '='){
        			nNextToken =  new Token(TokenKind.equalToken, cg.cLineNum );
        			cg.readNext();
        		}else if(cg.curC == '>'){
        			if(cg.nextC == '='  && !cg.checkLineBetween()){
            			nNextToken =  new Token(TokenKind.greaterEqualToken, cg.cLineNum );
            			cg.readNext(); cg.readNext();
        			}else {
        				nNextToken =  new Token(TokenKind.greaterToken, cg.cLineNum );
        				cg.readNext();
        			}
        		}else if(cg.curC == '['){
        			nNextToken =  new Token(TokenKind.leftBracketToken, cg.cLineNum );
        			cg.readNext();
        		}
        		else if(cg.curC == '<'){
        			if(cg.nextC == '='  && !cg.checkLineBetween()){
            			nNextToken =  new Token(TokenKind.lessEqualToken, cg.cLineNum );
            			cg.readNext(); cg.readNext();
        			}else if (cg.nextC == '>'  && !cg.checkLineBetween()){
        				nNextToken =  new Token(TokenKind.notEqualToken, cg.cLineNum );
            			cg.readNext(); cg.readNext();
        			}
        			else {
        				nNextToken =  new Token(TokenKind.lessToken, cg.cLineNum );
        				cg.readNext();
        			}
        		}else if(cg.curC == '*'){
        			nNextToken =  new Token(TokenKind.multiplyToken, cg.cLineNum );
        			cg.readNext();
        		}else if(cg.curC == ']'){
        			nNextToken =  new Token(TokenKind.rightBracketToken, cg.cLineNum );
        			cg.readNext();
        		}else if(cg.curC == '-'){
        			nNextToken =  new Token(TokenKind.subtractToken, cg.cLineNum );
        			cg.readNext();
        		}
        		//Sjekker om det er en tekst streng
        		else if(cg.curC == anF.charAt(0)){
        			String s = "";
        			while(cg.isMoreToRead()){
        				if(cg.checkLineBetween() || cg.nextC == anF.charAt(0)){
                            cg.readNext();
                            if(cg.nextC == anF.charAt(0)){
                            }else break;
                        }
                        s+= cg.nextC;
        				cg.readNext();
        			}
                    if (cg.curC !=  anF.charAt(0)){
                        Main.error("On line " + cg.cLineNum + ": uendelig tekst");
                    }
                    //setter temp = s fordi her så lagre jeg navn globalt slik at jeg kan bruke det senere
                    temp = s;
        			nNextToken =  new Token(s,s, cg.cLineNum );
        			cg.readNext(); 
        		}else{
    				Main.error(cg.cLineNum , "unexpected token" +"'" + cg.curC +"'");
    			}

    		}
    	}
    	
        Main.log.noteToken(nNextToken);
    }
    
 
   
    // Character test utilities:

    private boolean isLetterAZ(char c) {
	return 'A'<=c && c<='Z' || 'a'<=c && c<='z';
    }


    private boolean isDigit(char c) {
	return '0'<=c && c<='9';
    }


    // Parser tests:

    public void test(TokenKind t) {
	if (curToken.kind != t)
	    testError(t.toString());
    }

    public void testError(String message) {
	Main.error(curLineNum(), 
		   "Expected a " + message +
		   " but found a " + curToken.kind + "!");
    }

    public void skip(TokenKind t) {
	test(t);  
	readNextToken();
    }
}
