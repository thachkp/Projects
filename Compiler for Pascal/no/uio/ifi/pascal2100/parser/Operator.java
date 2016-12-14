package no.uio.ifi.pascal2100.parser;

import no.uio.ifi.pascal2100.scanner.*;
import no.uio.ifi.pascal2100.main.*;

public class Operator extends PascalSyntax{
	Operator nextOpr;
	Token oprToken ;
	
	Operator(int n) {
		super(n);
	}
	@Override
	public void genCode(CodeFile cf){

	}
	@Override
	public void check(Block b , Library lib ){

	}

	@Override
	public String identify() {
		return null;
	}
	@Override
	void prettyPrint() {
		
	}
}

class RelOpr extends Operator{
	RelOpr(int n) {
		super(n);
	}
	@Override
	public void genCode(CodeFile cf ){
		if (oprToken.kind == TokenKind.lessToken) { // < Token
				cf.genInstr("","setl","%al","Test " + oprToken.kind);

			}else if(oprToken.kind == TokenKind.notEqualToken){ // <> Token
				cf.genInstr("","setne","%al","Test " + oprToken.kind);

			}else if(oprToken.kind == TokenKind.equalToken){// = Token
				cf.genInstr("","sete","%al","Test " + oprToken.kind);

			}else if(oprToken.kind == TokenKind.lessEqualToken){ // <= Token
				cf.genInstr("","setle","%al","Test " + oprToken.kind);

			}else if(oprToken.kind == TokenKind.greaterToken){ // > Token
				cf.genInstr("","setg","%al","Test " + oprToken.kind);

			}else if(oprToken.kind == TokenKind.greaterEqualToken){ // >= Token
				cf.genInstr("","setge","%al","Test " + oprToken.kind);

			}else
				Main.error("token failed : " + oprToken.kind); 
	}
	public static RelOpr parse(Scanner s){
		enterParser("rel opr");
		
		RelOpr  ro = new RelOpr(s.curLine);
		ro.oprToken = s.curToken;
		s.skip(s.curToken.kind);
		leaveParser("rel opr");
		return ro;
	}
	@Override
	void prettyPrint(){
		if (oprToken.kind == TokenKind.equalToken) {
			Main.log.prettyPrint(" = ");
		}else if (oprToken.kind == TokenKind.notEqualToken){
			Main.log.prettyPrint(" <> ");
		}else if (oprToken.kind == TokenKind.lessToken){
			Main.log.prettyPrint(" < ");
		}else if (oprToken.kind == TokenKind.lessEqualToken){
			Main.log.prettyPrint(" <= ");
		}else if (oprToken.kind == TokenKind.greaterToken){
			Main.log.prettyPrint(" > ");
		}else if (oprToken.kind == TokenKind.greaterEqualToken){
			Main.log.prettyPrint(" >= ");
		}
	}
}

class PrefixOpr extends Operator{
	PrefixOpr(int n) {
		super(n);
	}
	@Override
	public void genCode(CodeFile cf){
		if (oprToken.kind == TokenKind.subtractToken) {
			cf.genInstr("","negl","%eax"," (prefix - )");
		}
	}
	public static PrefixOpr parse(Scanner s){
		enterParser("prefix opr");
		
		PrefixOpr  po = new PrefixOpr(s.curLine);
		po.oprToken = s.curToken;
		s.skip(s.curToken.kind);
		leaveParser("prefix opr");
		return po;
	}
	@Override
	void prettyPrint(){
		if(oprToken.kind == TokenKind.addToken){
			Main.log.prettyPrint(" + ");
		}else if(oprToken.kind == TokenKind.subtractToken){
			Main.log.prettyPrint(" - ");
		}
	}
}


class TermOpr extends Operator{
	TermOpr(int n) {
		super(n);
	}
	@Override
	public void genCode(CodeFile cf){
		if (oprToken.kind == TokenKind.addToken) {
			cf.genInstr("","addl","%ecx,%eax"," + ");
		}else if (oprToken.kind == TokenKind.subtractToken) {
			cf.genInstr("","subl","%ecx,%eax"," - ");
		}else if (oprToken.kind == TokenKind.orToken) {
			cf.genInstr("","orl","%ecx,%eax"," or ");
		}
	}

	public static TermOpr parse(Scanner s){
		enterParser("term Opr");
		
		TermOpr top = new TermOpr(s.curLine);
		top.oprToken = s.curToken;
		s.skip(s.curToken.kind);

		leaveParser("TermOpr");
		return top;

	}
	@Override
	void prettyPrint(){
		if(oprToken.kind == TokenKind.addToken){
			Main.log.prettyPrint(" + ");
		}else if(oprToken.kind == TokenKind.subtractToken){
			Main.log.prettyPrint(" - ");
		}else if(oprToken.kind == TokenKind.orToken){
			Main.log.prettyPrint(" or ");
		}
	}
	

}

class Facto extends Operator{
	Facto next= null;
	Facto(int n) {
		super(n);
	}
	@Override
	public void genCode(CodeFile cf){

		if (oprToken.kind  == TokenKind.divToken) {
			cf.genInstr("","cdq","","");
			cf.genInstr("","idivl","%ecx"," / ");
		}else if(oprToken.kind == TokenKind.multiplyToken){
			cf.genInstr("","imull","%ecx,%eax"," * ");
		}else if(oprToken.kind == TokenKind.andToken){
			cf.genInstr("","andl","%ecx,%eax"," and ");
		}else if(oprToken.kind == TokenKind.modToken){
			cf.genInstr("","cdq","","");
			cf.genInstr("","idivl","%ecx","");
			cf.genInstr("","movl","%edx,%eax","	mod");
		}
	}
	public static Facto parse(Scanner s){
		enterParser("factor Opr");
		
		Facto fopr = new Facto(s.curLine);
		fopr.oprToken = s.curToken;
		s.skip(s.curToken.kind);

		leaveParser("Factor opr");
		return fopr;
	}
	@Override
	void prettyPrint(){
		if(oprToken.kind == TokenKind.multiplyToken){
			Main.log.prettyPrint(" * ");
		}else if(oprToken.kind == TokenKind.divToken){
			Main.log.prettyPrint(" div ");
		}else if(oprToken.kind == TokenKind.modToken){
			Main.log.prettyPrint(" mod ");
		}else if(oprToken.kind == TokenKind.andToken){
			Main.log.prettyPrint(" and ");
		}
	}
}







