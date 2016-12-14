package no.uio.ifi.pascal2100.parser;

import no.uio.ifi.pascal2100.scanner.*;
import no.uio.ifi.pascal2100.main.*;
import java.util.LinkedList;

class Factor extends PascalDecl{
	Factor(String id,int n) {
		super(id, n);
	}

	@Override
	public void check(Block b, Library lib){

	}
	@Override
	public void genCode(CodeFile cf){
	}
	@Override
	public String identify() {
		return null;
	}
	public static Factor parse(Scanner s){
		enterParser("factor");
		Factor ft = null;

		if (s.curToken.kind == TokenKind.nameToken 
			&& s.nextToken.kind == TokenKind.leftParToken) {
			ft = FuncCall.parse(s);
		}else if (s.curToken.kind == TokenKind.leftParToken) {
			ft = InnerExpression.parse(s);
		}else if (s.curToken.kind == TokenKind.notToken) {
			ft = Negation.parse(s);
		}else if(s.curToken.kind == TokenKind.intValToken 
			|| s.curToken.kind ==TokenKind.stringValToken){
			ft = Constant.parse(s);
		}else if(s.curToken.kind == TokenKind.nameToken){
			ft = Variable.parse(s);

		}
		else{
			Main.error("No factor '" + s.curToken.kind + "' line : " + s.curLine);
		}

		leaveParser("factor");

		return ft;
	}
	@Override
	void prettyPrint() {
	}

}

class Negation extends Factor{
	Factor f = null;
	int line ;
	Negation(String id,int n) {
		super(id,n);
	}
	@Override
	public void check(Block b, Library lib){
		lib.findType("not",this,this);
		f.check(b,lib);
	}
	@Override
	public void genCode(CodeFile cf){
		f.genCode(cf);
		cf.genInstr("","xorl","$0x1,%eax","not");
	}
	@Override
	public String identify() {
		return "<negation> in line " + line;
	}

	public static Negation parse(Scanner s){
		enterParser("negation");
		Negation neg = new Negation("",s.curLine);
		neg.line = s.curLine;
		s.skip(TokenKind.notToken);
		neg.f = Factor.parse(s);

		leaveParser("negation");
		return neg;
	}
	@Override
	void prettyPrint() {
		Main.log.prettyPrint("not");
		f.prettyPrint();
	}
	
}
class FuncCall extends Factor{
	ExpressionList eprList;
	String name = "";
	FuncDecl fd = null;
	Block fcBlock = null;
	FuncCall(String id,int n) {
		super(id,n);
	}
	@Override
	public void genCode(CodeFile cf ){
		int size = eprList.exprL.size();
		int remove = size*4;
		PascalDecl pd = fcBlock.getFromBlock(name,fcBlock);
		for (int i  = size-1 ;i >=0  ; i--) {
			eprList.exprL.get(i).genCode(cf);			
			cf.genInstr("","pushl","%eax","Push param " + "#" +(i+1));
		}
		if (pd !=null) {
			fd = (FuncDecl) pd;
			String call_label = fd.funBlock.label.toLowerCase();
			cf.genInstr("","call",call_label,"");
			cf.genInstr("","addl","$"+remove+",%esp","pop parameters");
		}	
	}
	@Override
	public void check(Block b , Library lib){
		fcBlock= b;
		b.findDecl(name, this);
		eprList.check(b, lib);
	}

	@Override
	public String identify() {
		return null;
	}
	public static FuncCall parse(Scanner s){
		
		enterParser("FuncCall");
		
		FuncCall fc = new FuncCall("",s.curLine);
		fc.name = s.curToken.id;
		s.skip(TokenKind.nameToken);
		s.skip(TokenKind.leftParToken);
		if(s.curToken.kind != TokenKind.rightParToken){
			fc.eprList = ExpressionList.parse(s);
		}
		s.skip(TokenKind.rightParToken);
		
		leaveParser("FuncCall");
		
		return fc;
	}
	@Override
	void prettyPrint() {	
		Main.log.prettyPrint(name);
		Main.log.prettyPrint("( ");
		eprList.prettyPrint();
		Main.log.prettyPrint(" )");
	}
	
}
class InnerExpression extends Factor{
	Expression e;
	InnerExpression(String id,int n) {
		super(id,n);
	}
	@Override
	public void check(Block b , Library lib){
		e.check(b,lib);
	}
	@Override
	public void genCode(CodeFile cf){
		e.genCode(cf);
	}

	public static InnerExpression parse(Scanner s){
		enterParser("inner expr");
		
		InnerExpression iepr = new InnerExpression("",s.curLine);
		s.skip(TokenKind.leftParToken);
		iepr.e = Expression.parse(s);
		s.skip(TokenKind.rightParToken);
		
		leaveParser("inner expr");
		return iepr;
	}
	@Override
	void prettyPrint(){
		Main.log.prettyPrint("( ");
		e.prettyPrint();
		Main.log.prettyPrint(" )");

	}
	
}
class Variable extends Factor{
	VarDecl vd = null; ConstDecl cd = null; TypeDecl td = null;
	FuncDecl fd = null; ParamDecl pd = null; Expression exp = null;
	TypeBoolean tbool = null; ConstEol constE = null ;
	String name ;
	Variable(String id,int n) {
		super(id,n);
	}
	@Override
	public void check(Block b, Library lib){
		if (name.equals("eol")|| name.equals("false") || name.equals("true")) {
			PascalDecl d = lib.findType(name, this, this);
			if(d instanceof TypeBoolean){
				tbool = (TypeBoolean) d;
			}else if(d instanceof ConstEol){
				constE = (ConstEol)d;
			}
		}else{
			PascalDecl d = b.findDecl(name, this);
			if (exp != null) {
				exp.check(b,lib);
			}

			if (d instanceof ConstDecl) {
				cd = (ConstDecl) d;
			}else if(d instanceof VarDecl){
				vd = (VarDecl) d;
			}else if(d instanceof TypeDecl){
				td = (TypeDecl)d;
			}else if(d instanceof FuncDecl){
				fd = (FuncDecl)d;
			}else if (d instanceof ParamDecl){
				pd = (ParamDecl)d;
			}
		}
	}
	@Override
	public void genCode(CodeFile cf) {
		if (exp != null) {
			exp.genCode(cf);
		}
		if (name.equals("eol")) {
			int ascii = 10;
			cf.genInstr("","movl","$"+ascii +",%eax", "movl 10 = eol to %eax");
		}else if (name.equals("false")){
			cf.genInstr("","movl","$0" +",%eax", "movl 0 = false to %eax");

		}else if (name.equals("true")){
			cf.genInstr("","movl","$1" +",%eax", "movl 1 = true to %eax");

		}
		else if (vd!=null) {
			vd.genCode(cf);
			vd = null;
		}else if(fd!= null){
			fd.genCode(cf);
			fd = null;
		}else if (pd!= null){
			pd.genCode(cf);
			pd = null;
		}else if (cd != null){
			cd.genCode(cf);
			cd = null;
		}else if (td != null){
			td.genCode(cf);
			td = null;
		}
	}
	@Override
	public String identify() {
		return null;
	}

	public static Variable parse(Scanner s){
		enterParser("variable");


		Variable variable = new Variable("",s.curLine);
		variable.name = s.curToken.id;
		s.skip(TokenKind.nameToken);
		if (s.curToken.kind == TokenKind.leftBracketToken) {
			s.skip(TokenKind.leftBracketToken);
			variable.exp = Expression.parse(s);
			s.skip(TokenKind.rightBracketToken);

		}		
		leaveParser("variable");
		return variable;
	}

	@Override
	void prettyPrint() {
		Main.log.prettyPrint(name);
		if (exp != null) {
			Main.log.prettyPrint("[");
			exp.prettyPrint();
			Main.log.prettyPrint("]");
		}
	}
}
class Term extends PascalDecl{
	Factor factor1 = null, factor2 = null;
	Facto opr;
	Expression epr = null;
	boolean isFactorOpr = false;
	LinkedList<Factor> factorList = null;
	LinkedList<Facto> factorOprList = null;

	Term(String id,int n) {
		super(id,n);
	}
	@Override
	public void check(Block b , Library lib){
		if(!factorList.isEmpty()){
			for (Factor f: factorList ) {
				f.check(b,lib);
			}
		}
	}
	@Override
	public void genCode(CodeFile cf){
		factor1.genCode(cf);
		if (isFactorOpr) {
			for (int i = 0; i< factorOprList.size() ; i++ ) {
				cf.genInstr("","pushl","%eax","");
				factorList.get(i+1).genCode(cf);
				cf.genInstr("","movl","%eax,%ecx","");
				cf.genInstr("","popl","%eax","");
				factorOprList.get(i).genCode(cf);
			}
		}
	}
	@Override
	public String identify() {
		return null;
	}
	public static Term parse(Scanner s){
		enterParser("term");
		
		Term t = new Term("",s.curLine);
		t.factorList = new LinkedList<Factor>();
		t.factorOprList = new LinkedList<Facto>();

		t.factor1 = Factor.parse(s);
		t.factorList.add(t.factor1);

		if (TokenKind.isFactorOpr(s.curToken)) {
			while(TokenKind.isFactorOpr(s.curToken)){
				t.opr = Facto.parse(s);
				t.factorOprList.add(t.opr);

				t.factor2 = Factor.parse(s);
				t.factorList.add(t.factor2);
				t.isFactorOpr= true;
			}
			
		}

		leaveParser("term");
		return t;
	}
	@Override
	void prettyPrint() {
		if (factorList.size()> 0) {
			factorList.get(0).prettyPrint();
		}
		if (isFactorOpr) {
			int len = factorOprList.size();
			for (int i = 0;i<len ; i++) {
				factorOprList.get(i).prettyPrint();
				factorList.get(i+1).prettyPrint();
			}
		}
	}
	public boolean checkEmptyOperator(){

		return factorOprList.size() == 0 ;
	}
}



