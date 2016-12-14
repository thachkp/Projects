package no.uio.ifi.pascal2100.parser;

import java.util.LinkedList;
import no.uio.ifi.pascal2100.scanner.*;
import no.uio.ifi.pascal2100.main.*;

public class Expression extends PascalSyntax{
	SimpleExpression smplE = null , smplE1 = null;
	boolean realOpr = false;
	RelOpr rop;
	Token token = null , nextToKen = null;
	Expression(int n) {
		super(n);
	}
	@Override
	public void check(Block b, Library lib){
	
		smplE.check(b,lib);
		if (realOpr) {
			smplE1.check(b,lib);
		}
	}
	@Override
	public void genCode(CodeFile cf){
		smplE.genCode(cf);

		if (realOpr) {
			cf.genInstr("","pushl","%eax","");
			smplE1.genCode(cf);
			cf.genInstr("","popl","%ecx","");
			cf.genInstr("","cmpl","%eax,%ecx","");
			cf.genInstr("","movl","$0,%eax","");
			rop.genCode(cf);
		}
	}
	@Override
	public String identify() {
		return null;
	}
	public static Expression parse(Scanner s){
		enterParser("expression");

		Expression epr = new Expression(s.curLine);
		if (TokenKind.isPrefixOpr(s.curToken) || s.curToken.kind == TokenKind.notToken) {
			epr.token = s.nextToken;
		}else
			epr.token = s.curToken;

		epr.smplE = SimpleExpression.parse(s);
		if (TokenKind.isRelOpr(s.curToken)) {
			epr.realOpr = true;
			epr.rop = RelOpr.parse(s);
			epr.smplE1 = SimpleExpression.parse(s);
		}

		leaveParser("expression");
		return epr;
	}
	@Override
	void prettyPrint() {
		smplE.prettyPrint();
		if (realOpr) {
			rop.prettyPrint();
			smplE1.prettyPrint();
		}
	}
	public boolean checkEmptyOperator(){
		if (realOpr && smplE.checkEmptyOperator() ) {
			return smplE1 == null ;
		}else return false;
	}

}

class SimpleExpression extends Expression{
	PrefixOpr prefixOp= null;
	boolean isPrefix = false, isTermOpr = false;
	Term term = null, term2 = null;
	LinkedList<Term> termList = null;
	LinkedList<TermOpr> termOpr = null;

	TermOpr topr = null;
	SimpleExpression(int n) {
		super(n);
	}
	@Override
	public void check(Block b, Library lib){
		if (!termList.isEmpty()) {
			for (Term t  : termList) {
				t.check(b , lib);
			}
		}
		
	}
	@Override
	public void genCode(CodeFile cf){
		term.genCode(cf);

		if (isPrefix) {
			prefixOp.genCode(cf);

		}
		if (isTermOpr) {
			for (int i = 0 ; i < termOpr.size(); i++ ) {
				cf.genInstr("","pushl","%eax","");
				termList.get(i+1).genCode(cf);
				cf.genInstr("","movl","%eax,%ecx","");
				cf.genInstr("","popl","%eax","");
				termOpr.get(i).genCode(cf);
			}
		}
	}
	public static SimpleExpression parse(Scanner s){
		enterParser("simple expr");
		SimpleExpression smple = new SimpleExpression(s.curLine);
		smple.termList = new LinkedList<Term>();

		if (TokenKind.isPrefixOpr(s.curToken)) {
			smple.prefixOp = PrefixOpr.parse(s);
			smple.isPrefix = true;
		}
		smple.term = Term.parse(s);
		smple.termList.add(smple.term);

		if (TokenKind.isTermOpr(s.curToken)) {
			smple.termOpr = new LinkedList<TermOpr>();
			while (TokenKind.isTermOpr(s.curToken)) {
				smple.topr = TermOpr.parse(s);
				smple.termOpr.add(smple.topr);

				smple.term2 = Term.parse(s);
				smple.termList.add(smple.term2);
				smple.isTermOpr = true;

			}
		}	

		leaveParser("simple expr");
		return smple;
	}
	@Override
	void prettyPrint() {

		if (isPrefix) {
			prefixOp.prettyPrint();
		}
		if (termList.size() >0) {
			termList.get(0).prettyPrint();

		}
		if (isTermOpr) {
			int len = termOpr.size();
			for (int i = 0; i < len ;i++ ) {
				termOpr.get(i).prettyPrint();
				termList.get(i+1).prettyPrint();
			}
		}
	}
	public boolean checkEmptyOperator(){
		if(isTermOpr && term.checkEmptyOperator()){
			return termOpr.size() == 0 ;
		}else return false ;
	}
	
	
}


class ExpressionList extends PascalSyntax{
	Expression expr = null;
	LinkedList<Expression> exprL  = null;

	ExpressionList(int n) {
		super(n);
	}
	@Override
	public void check(Block b, Library lib){
		for (Expression e  : exprL) {
			e.check(b,lib);
		}
	}
	@Override
	public void genCode(CodeFile cf){
	}
	@Override
	public String identify() {
		return null;
	}
	
	public static ExpressionList parse(Scanner s){
		ExpressionList eprList = new ExpressionList(s.curLine);
		enterParser("ExpressionList");
		eprList.exprL = new LinkedList<Expression>();

		if(s.curToken.kind != TokenKind.rightParToken){
			eprList.expr = Expression.parse(s);
			eprList.exprL.add(eprList.expr);

			while(s.curToken.kind == TokenKind.commaToken){
				
				s.skip(TokenKind.commaToken);
				eprList.expr = Expression.parse(s);
				eprList.exprL.add(eprList.expr);
			}
		}

		leaveParser("ExpressionList");
		return eprList;
	}
	@Override
	void prettyPrint() {
		exprL.remove().prettyPrint();
		for (Expression temp : exprL) {
			Main.log.prettyPrint(",");
			temp.prettyPrint();
		}

	}
	
}











