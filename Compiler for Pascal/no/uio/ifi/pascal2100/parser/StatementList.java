package no.uio.ifi.pascal2100.parser;
import java.util.*;

import no.uio.ifi.pascal2100.main.*;
import no.uio.ifi.pascal2100.scanner.Scanner;
import no.uio.ifi.pascal2100.scanner.TokenKind;

public class StatementList extends PascalDecl{
	
	LinkedList<Statement> statm= null ;
	Statement temp = null;
	StatementList(String id, int n) {
		super(id,n);
	}

	@Override
	public void check(Block curScope, Library lib){
		for (Statement st :statm ) {
			st.check(curScope,lib);
		}
	}
	@Override
	public void genCode(CodeFile cf){
		for (Statement st  :statm ) {
			st.genCode(cf);
		}
	}

	@Override
	public String identify() {
		return null;
	}
	
	public static StatementList parse(Scanner s){
		
		enterParser("statm list");
		StatementList stl = new StatementList("",s.curLine);
		stl.statm = new LinkedList<Statement>();

		stl.temp = Statement.parse(s);
		stl.statm.add(stl.temp);

		while(s.curToken.kind == TokenKind.semicolonToken) {
			s.skip(TokenKind.semicolonToken);
			stl.temp = Statement.parse(s);
			stl.statm.add(stl.temp);
		}

		leaveParser("statm list");
		return stl;
		
	}
	
	@Override
	void prettyPrint() {
		for (Statement st  :statm ) {
			st.prettyPrint();
		}
		
	}

}


class Statement extends PascalDecl{

	StatementList stl;
	String name = "";
	int count = 0;
	Statement(String id, int n) {
		super(id,n);
	}

	@Override
	public String identify() {
		return null;
	}
	@Override
	public void check(Block curScope, Library lib){

	}
	@Override
	public void genCode(CodeFile cf){
		
	}

	public static Statement parse(Scanner s){
		enterParser("statement");
		
		Statement stm = null;

		if((s.curToken.kind == TokenKind.nameToken 
			&& s.nextToken.kind == TokenKind.leftParToken ) ||
			 (s.curToken.kind == TokenKind.nameToken 
			&& s.nextToken.kind == TokenKind.semicolonToken )){
			stm = ProcCall.parse(s);
		}else if(s.curToken.kind ==TokenKind.endToken 
			|| s.curToken.kind == TokenKind.semicolonToken){

			stm = EmptyStatm.parse(s); 
		}else if(s.curToken.kind == TokenKind.beginToken){
			stm = CompoundStatm.parse(s);
		}else if(s.curToken.kind == TokenKind.whileToken) {
			stm = WhileStatm.parse(s);
		}else if(s.curToken.kind == TokenKind.ifToken) {
			stm = IfStatm.parse(s);
		}else if((s.curToken.kind == TokenKind.nameToken && s.nextToken.kind == TokenKind.assignToken) || 
			(s.curToken.kind == TokenKind.nameToken && s.nextToken.kind == TokenKind.leftBracketToken)){
			stm = AssignStatm.parse(s);
		}
		else {
			Main.error("Statement Failed : " + stm);
		}
		
		leaveParser("statement");
		return stm;
	}
	@Override
	void prettyPrint() {
	}
	
}

class ProcCall extends Statement{
	String name = "";
	ExpressionList eprL = null;
	boolean expTrue = false;
	int lineN ;
	Block block = null; 
	Library proclib = null;
	ProcDecl procDecl = null;
	ProcCall(String id,int n) {
		super(id,n);
	}
	
	@Override
	public void check(Block curScope, Library lib){
		block = curScope;
		proclib = lib;	
		if (name.equals("write")) {
			PascalDecl d = lib.findType(name, this, this);	

		}else {
			PascalDecl d = curScope.findDecl(name, this);	
			if (d instanceof ProcDecl) {
				procDecl = (ProcDecl) d;
			}
		}
		if (expTrue) {
			eprL.check(curScope,lib);

		}
	}
	@Override
	public void genCode(CodeFile cf){
		int i = 0;
		if (name.equals("write")) {
			writeProcGenCode(cf);
		}else{
			if (expTrue) {	
				int size = eprL.exprL.size();
				int paramId = size;

				for  (int j  = size-1 ;j>= 0 ; j-- ) {
					Expression e = eprL.exprL.get(j);
					e.genCode(cf);
					cf.genInstr("","pushl","%eax","Param #" + paramId);

				}
				String call_label = procDecl.procBlock.label.toLowerCase();
				cf.genInstr("","call",call_label,"");
				cf.genInstr("","addl","$"+size*4+",%esp ","pop " + size + " parameter");


			}else{
				cf.genInstr("","call",procDecl.procBlock.label.toLowerCase(),"");

			}
		}
	}

	private void writeProcGenCode(CodeFile cf){
		int paramId = 1;
		int size = eprL.exprL.size();
		for  (int j  = 0 ;j< size ; j++ ) {
			Expression e = eprL.exprL.get(j);
			e.genCode(cf);
			cf.genInstr("","pushl","%eax","Param #" +paramId);

			if(e.token.kind == TokenKind.stringValToken){
				if (e.token.strVal.length() == 1) {
					writeOut("char",cf);
				}else{
					writeOut("String", cf);
				}
			}else if(e.token.kind == TokenKind.intValToken){
					writeOut("integer", cf);

			}else if (e.token.kind == TokenKind.nameToken){
				writeOut(e.token.id,cf);
			}
			cf.genInstr("","addl","$4,%esp","pop parameter " + paramId++);

		}
	}
	
	public boolean writeTypeCheck (String s){
		if (s.equalsIgnoreCase("integer")|| s.equalsIgnoreCase("boolean") 
			||s.equalsIgnoreCase("String")|| s.equalsIgnoreCase("char") ) {
			return true;
		}
		return false;
	}
	public void writeOut(String s , CodeFile cf ){
		if (s.equalsIgnoreCase("integer") ||s.equalsIgnoreCase("boolean")) {
			cf.genInstr("","call","write_int","");
		}else if (s.equalsIgnoreCase("string")) {
			cf.genInstr("","call","write_string","");
		}else if (s.equalsIgnoreCase("char")|| s.equalsIgnoreCase("eol") ) {
			cf.genInstr("","call","write_char","");
		}else {
			String tmp = getTypeVariabel(s, block);
			if (tmp.isEmpty()) {
				return;
			}
			writeOut(tmp,cf);
		}

	}
	public String getTypeVariabel(String s, Block b ){
		String type_name = s.toLowerCase();
		PascalDecl d = null;
		ConstDecl constD = null;
		TypeDecl typeD = null;

		if (b.decls.containsKey(type_name)) {
			d = b.decls.get(type_name);
			// henter ut typeDecl 
			if (d != null && d instanceof TypeDecl) {
				typeD = (TypeDecl)d;
				if (typeD.t instanceof TypeName) {
					TypeName tn = (TypeName)typeD.t;
					if (writeTypeCheck(tn.name)){
						return tn.name;
					}else{
						return getTypeVariabel(tn.name,b);
					}
				}
			}
			// henter ut Constdecl
			else if(d != null && d instanceof ConstDecl){
				constD = (ConstDecl)d;
				if (constD.cstant instanceof NumericLiteral ) {
					return "integer";
				}else if (constD.cstant instanceof Name){
					Name n = (Name)constD.cstant;
					return getTypeVariabel(n.name,b);
				}
			}
			// henter ut varDecl
			else if (d != null && d instanceof VarDecl){
				VarDecl vd = (VarDecl)d;
				if (vd.t instanceof TypeName) {
					TypeName tn = (TypeName)vd.t;
					if (writeTypeCheck(tn.name)){
						return tn.name;
					}else{
						return getTypeVariabel(tn.name,b);
					}
				}
			}// henter ut Parameter
			else if (d != null && d instanceof ParamDecl) {
				ParamDecl pd = (ParamDecl)d;
				if (pd.t instanceof TypeName) {
					TypeName tn = (TypeName)pd.t;
					if (writeTypeCheck(tn.name)){
						return tn.name;
					}else{
						return getTypeVariabel(tn.name,b);
					}
				}
			}
		}
		// bla gjennom i en annen blokk
		if (b.outerScope != null) {
			return getTypeVariabel(type_name, b.outerScope);
		}
		return "";

	}

	public static ProcCall parse(Scanner s){
		enterParser("proc call");
				
		ProcCall pc = new ProcCall("",s.curLine);
		pc.lineN = s.curLine;
		pc.name = s.curToken.id;

		s.skip(TokenKind.nameToken);
		if(s.curToken.kind == TokenKind.leftParToken){
			pc.expTrue = true;
			s.skip(TokenKind.leftParToken);
			pc.eprL = ExpressionList.parse(s);
			s.skip(TokenKind.rightParToken);
		}
		
		
		leaveParser("proc call");
		return pc;
	}
	@Override
	void prettyPrint() {
		Main.log.prettyIndent();
		Main.log.prettyPrint(name);
		if (eprL!=null) {
			Main.log.prettyPrint("(");
			eprL.prettyPrint();
			Main.log.prettyPrint(")");
		}
		Main.log.prettyPrint("; ");
		Main.log.prettyPrintLn("");
		Main.log.prettyOutdent();

	}
	
}


class EmptyStatm extends Statement{
	
	EmptyStatm(String id,int n){
		super(id,n);
	}

	public static EmptyStatm parse(Scanner s){
		enterParser("empty-statm");

		EmptyStatm empt = new EmptyStatm("",s.curLine);

		leaveParser("empty-statm");
		return empt;
	}

	@Override
	void prettyPrint() {
		Main.log.prettyPrint("");
	}

}
class CompoundStatm extends Statement{
	StatementList stmList = null; 


	CompoundStatm(String id,int n){
		super(id,n);
	}

	@Override
	public void check(Block b, Library lib){
		stmList.check(b,lib);
	}
	@Override
	public void genCode(CodeFile cf){
		if (stmList!= null) {
			stmList.genCode(cf);
		}
	}

	public static CompoundStatm parse(Scanner s){
		enterParser("compound-statm");

		CompoundStatm cpstm = new CompoundStatm("",s.curLine);
		s.skip(TokenKind.beginToken);
		cpstm.stmList = StatementList.parse(s);
		s.skip(TokenKind.endToken);
		
		leaveParser("compound-statm");
		return cpstm;
	}

	@Override
	void prettyPrint() {
		Main.log.prettyPrintLn("begin");
		Main.log.prettyIndent();

		stmList.prettyPrint();

		Main.log.prettyPrintLn("");
		Main.log.prettyOutdent();

		Main.log.prettyPrint("end ;");


	}

}

class WhileStatm extends Statement{
	Expression epr = null;
	Statement stm = null;
	WhileStatm(String id,int n){
		super(id,n);
	}
	@Override
	public void genCode(CodeFile cf){
		String loop1 = cf.getLocalLabel();
		String loop2 = cf.getLocalLabel();
		cf.genInstr(loop1,"","","");
		epr.genCode(cf);
		if (epr.checkEmptyOperator()) {
			cf.genInstr("","pushl","%eax","");
		}
		cf.genInstr("","cmpl","$0,%eax","");
		cf.genInstr("","je",loop2,"");
		stm.genCode(cf);
		cf.genInstr("","jmp",loop1,"");
		cf.genInstr(loop2,"","","End of while-statm");


	}
	@Override
	public void check(Block b, Library lib){
		epr.check(b,lib);
		stm.check(b,lib);
	}
	public static WhileStatm parse(Scanner s){
		enterParser("while-statm");

		WhileStatm wStm = new WhileStatm("",s.curLine);
		s.skip(TokenKind.whileToken);

		wStm.epr = Expression.parse(s);

		s.skip(TokenKind.doToken);

		wStm.stm = Statement.parse(s);

		leaveParser("while-statm");
		return wStm;
	}
	@Override
	void prettyPrint(){
		Main.log.prettyPrint("while ");
		epr.prettyPrint();
		Main.log.prettyPrint(" do ");
		Main.log.prettyIndent();
		stm.prettyPrint();
		Main.log.prettyOutdent();
		Main.log.prettyPrintLn("");

	}
}


class IfStatm extends Statement{

	Expression exp = null;
	Statement stm1 = null, stm2 = null;
	boolean elseP = false;
	IfStatm(String id,int n){
		super(id,n);
	} 
	@Override
	public void genCode(CodeFile cf){
		cf.genInstr("","","","Start if-statement");
		exp.genCode(cf);
		if (exp.checkEmptyOperator()) {
			cf.genInstr("","pushl","%eax","");
		}

		if (elseP) {
			cf.genInstr("","cmpl","$0,%eax","");
			String loop1 = cf.getLocalLabel();
			String loop2 = cf.getLocalLabel();

			cf.genInstr("","je",loop1,"");
			stm1.genCode(cf);
			cf.genInstr("","jmp",loop2,"");
			cf.genInstr(loop1,"","","");
			stm2.genCode(cf);
			cf.genInstr(loop2,"","","");
			if (stm2 instanceof AssignStatm || stm1 instanceof AssignStatm ) {
				AssignStatm as1 = (AssignStatm)stm1;
				AssignStatm as2 = (AssignStatm)stm2;
				if (as1.v.fd != null || as2.v.fd != null ) {
					cf.genInstr("","movl","-32(%ebp),%eax","Fetch return value");
				}
			}

		}else{
			String loop1 = cf.getLocalLabel();

			cf.genInstr("","cmpl","$0,%eax","");
			cf.genInstr("","je", loop1,"");
			stm1.genCode(cf);
			cf.genInstr(loop1,"","","");
			if (stm1 instanceof AssignStatm) {
				AssignStatm as = (AssignStatm)stm1;
				if (as.v.fd != null) {
					cf.genInstr("","movl","-32(%ebp),%eax","Fetch return value");
				}
			}

		}

	}



	@Override
	public void check(Block b, Library lib){
		exp.check(b,lib);
		stm1.check(b,lib);

		if (elseP) {
			stm2.check(b,lib);
		}
	}

	public static IfStatm parse(Scanner s){

		enterParser("if-statm");
		IfStatm ifstat = new IfStatm("",s.curLine);

		s.skip(TokenKind.ifToken);

		ifstat.exp = Expression.parse(s);

		s.skip(TokenKind.thenToken);

		ifstat.stm1 = Statement.parse(s);


		if(s.curToken.kind == TokenKind.elseToken){
			ifstat.elseP = true;
			s.skip(TokenKind.elseToken);
			ifstat.stm2 = Statement.parse(s);

		}

		leaveParser("if-statm");
		return ifstat;
	}
	@Override
	void prettyPrint(){
		Main.log.prettyPrint("if ");
		exp.prettyPrint();
		Main.log.prettyPrint(" then ");
		Main.log.prettyPrintLn("");
		Main.log.prettyIndent();
		stm1.prettyPrint();
		Main.log.prettyOutdent();
		Main.log.prettyPrintLn("");
		if(elseP){
			Main.log.prettyPrint("else ");
			Main.log.prettyPrintLn("");
			Main.log.prettyIndent();
			stm2.prettyPrint();
			Main.log.prettyOutdent();
		}
	

	}

}
class AssignStatm extends Statement{
	Expression exp = null;
	Variable v = null;
	boolean semicolonToken = false;
	Block assBlock = null;
	AssignStatm(String id,int n){
		super(id,n);
	}

	@Override
	public void genCode(CodeFile cf){
		exp.genCode(cf);

		if (v.vd != null) {
			if (v.exp != null){
				cf.genInstr("","pushl","%eax","");
				if (v.vd.t  instanceof ArrayType) {
					ArrayType art = (ArrayType)v.vd.t;
					v.exp.genCode(cf);
					art.genCode(cf);
					cf.genInstr("","movl",-4 + "(%ebp),%edx","");
					cf.genInstr("","leal",v.vd.offset+"(%edx),%edx","");
					cf.genInstr("","popl","%ecx","");
					cf.genInstr("","movl","%ecx,0(%edx,%eax,4)",v.name + "[..] :=");
				}
			}else{
				cf.genInstr("","movl",-4 * assBlock.lvl+ "(%ebp),%edx","");
				cf.genInstr("","movl","%eax,"+v.vd.offset+("(%edx)"),v.name + " :=");

			}
		}else if (v.pd !=null){
			cf.genInstr("","movl",-4 * assBlock.lvl+ "(%ebp),%edx","");
			cf.genInstr("","movl","%eax,"+v.pd.offset+("(%edx)"),v.name + " :=");
		}else if(v.fd!=null){
			cf.genInstr("","movl","%eax,-32(%ebp)",v.name + ":=");
		}
	}

	@Override
	public void check(Block block, Library lib){
		assBlock = block;
		v.check(block,lib);
		exp.check(block,lib);
	}
	@Override
	public String identify() {
		return null;
	}

	public static AssignStatm parse(Scanner s){
		enterParser("assign statm");

		AssignStatm as = new AssignStatm("",s.curLine);

		as.semicolonToken= false;
		as.v = Variable.parse(s);

		s.skip(TokenKind.assignToken);

		as.exp = Expression.parse(s);
		if (s.curToken.kind == TokenKind.semicolonToken) {
			as.semicolonToken = true;
		}


		leaveParser("assign statm");
		return as;
	}
	@Override
	void prettyPrint() {
		v.prettyPrint();
		Main.log.prettyPrint(" := ");
		exp.prettyPrint();
		if (semicolonToken) {
			Main.log.prettyPrint(" ;");

		}
		Main.log.prettyPrintLn("");
	}

}

































