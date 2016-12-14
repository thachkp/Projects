package no.uio.ifi.pascal2100.parser;
import java.util.HashMap;
import java.util.LinkedList;

import no.uio.ifi.pascal2100.scanner.Scanner;
import no.uio.ifi.pascal2100.scanner.TokenKind;
import no.uio.ifi.pascal2100.main.*;

public class Program extends PascalSyntax{
	String name= ""; 
	Block b  = null;
	Program(int n) {
		super(n);
	}
	
	@Override
	public void check(Block block, Library lib){
		b.lvl = 1;
		b.progName = name;
		b.label = "prog$";
		b.check(block,lib);
	}
	@Override
	public void genCode(CodeFile cf ){
		b.label  += cf.getLabel(name);
		b.label.toLowerCase();
		cf.genInstr("", ".globl", "_main","");
		cf.genInstr("", ".globl", "main","");
		cf.genInstr("_main","","","");
		cf.genInstr("main","call",b.label.toLowerCase(),"Start program");
		cf.genInstr("","movl","$0,%eax","Set status 0 and ");
		cf.genInstr("","ret","","terminate the Program "+ name.toLowerCase());
		
		b.genCode(cf);


	}

	public static Program parse(Scanner s) {
		s.readNextToken();

		enterParser("program");
		Program p = new Program(s.curLine);


		s.skip(TokenKind.programToken);

		p.name  = s.curToken.id.toLowerCase();

		s.skip(TokenKind.nameToken);
		

		s.skip(TokenKind.semicolonToken);
		
		p.b = Block.parse(s);
		p.b.progName = p.name;

		s.skip(TokenKind.dotToken);
		
		leaveParser("program");
		
		return p;
	}

	@Override
	public String identify() {
		return null;
	}

	@Override
	public void prettyPrint() {
		Main.log.prettyPrintLn("");
		Main.log.prettyPrintLn("");
		Main.log.prettyPrint("program ");
		Main.log.prettyPrint(name);
		Main.log.prettyPrintLn(";");
		b.prettyPrint();
		Main.log.prettyPrint(".");
	}

}

class Block extends PascalSyntax{
	Program prog= null;
	StatementList stml = null;
	VarDeclPart vlp = null ; 
	ConstDeclPart cdp = null;
	TypeDeclPart tdp = null;
	FuncAndProcList fapL = null;
	Block outerScope = null;
	HashMap<String,PascalDecl> decls = new HashMap<String,PascalDecl>();
	String progName,label;
	int lvl;
	boolean firstBlock = false;
	Block(int n) {
		super(n);
	}
	void addDecl(String id, PascalDecl d) {
		String temp = id.toLowerCase();
		if (decls.containsKey(temp)){
				d.error(temp + " declared twice in same block!");
		}
		decls.put(temp, d);
	}

	public PascalDecl findDecl(String id, PascalSyntax where){
		String temp = id.toLowerCase();

		PascalDecl d = decls.get(temp);
		if (decls.containsKey(temp)) {
			Main.log.noteBinding(temp,where,d);
			return d;
		}
		if (outerScope != null){
			return outerScope.findDecl(temp,where);
		}
		where.error("Name " + temp + " is unknown!");
		return null;
	}	
	PascalDecl getFromBlock(String s , Block b ){
		String id = s.toLowerCase();
		PascalDecl d = null;
		if (b.decls.containsKey(id)) {
			d = b.decls.get(id);
			return d ;
		}
		if (b.outerScope != null) {
			return getFromBlock(id, b.outerScope);
		}
		return d ;

	}

	@Override
	public void check(Block curScope, Library lib){
		outerScope = curScope;
		if (cdp != null) {
			cdp.check(this, lib);
			for (ConstDecl cd : cdp.cdl.constList) {
				String cdpName = cd.name.toLowerCase();
				addDecl(cdpName, cd);
			}
		}
		if (tdp != null) {
			tdp.check(this, lib);
			for (TypeDecl td : tdp.tdl.typeList) {
				String tdpName = td.name.toLowerCase();
				addDecl(tdpName, td);
			}
		}
		if (vlp != null) {
			vlp.check(this, lib);
			for (VarDecl vd : vlp.vdl.varDeclList) {
				String vlpName = vd.name.toLowerCase();
				addDecl(vlpName, vd);
			}
		}
		if (fapL != null) {
			for (PascalDecl decl : fapL.list) {
				if (decl instanceof FuncDecl) {
					FuncDecl fd = (FuncDecl) decl;
					fd.funBlock.outerScope = this;
				}else if (decl instanceof ProcDecl) {
					ProcDecl pd = (ProcDecl) decl;
					pd.procBlock.outerScope = this;

				}
				String fapName = decl.name.toLowerCase();
				addDecl(fapName , decl);	

			}	
			firstBlock = true;
			fapL.check(this, lib);

		}

		if (stml != null) {
			for (Statement st  : stml.statm) {
				st.check(this,lib);
			}
		}


	}
	@Override
	public void genCode(CodeFile cf ){

		if (cdp!=null) {
			cdp.genCode(cf);
		}
		if (tdp!=null) {
			tdp.genCode(cf);
		}
		if (vlp != null) {
			vlp.genCode(cf);
		}
		int enter = getEnterSpace() + 32;

		if (fapL != null) {
			fapL.genCode(cf);
		}

		cf.genInstr(label.toLowerCase(),"","","");
		cf.genInstr("","enter","$"+enter + ",$"+lvl,"Start of " +progName );

		if (stml!= null) {
			stml.genCode(cf);
		}
		cf.genInstr("","leave","","End of " + progName);
		cf.genInstr("","ret","","");

		
	
	}
	@Override
	public String identify() {
		return null;
	}
	public  int getEnterSpace(){
		int i = 0;
		if (vlp!=null){
			i += vlp.vdl.varDeclList.size() + vlp.vdl.getVarSpace();
		}
		return i * 4;  // 4 byte hver variabel
	}
	

	public static Block parse(Scanner s) {

		enterParser("block");
		Block b = new Block(s.curLine);
		if (s.curToken.kind == TokenKind.constToken) {
			b.cdp = ConstDeclPart.parse(s);

		}
		if (s.curToken.kind == TokenKind.typeToken) {
			b.tdp = TypeDeclPart.parse(s);

		}
		if (s.curToken.kind == TokenKind.varToken) {
			b.vlp = VarDeclPart.parse(s);

		}
		if (s.curToken.kind == TokenKind.procedureToken 
			|| s.curToken.kind == TokenKind.functionToken ) {
			b.fapL = FuncAndProcList.parse(s);

		}
	

		s.skip(TokenKind.beginToken);
		b.stml = StatementList.parse(s);
		s.skip(TokenKind.endToken);
		leaveParser("block");
				


		return b;
	}
	@Override
	void prettyPrint() {
		if (cdp!= null) {
			cdp.prettyPrint();
			Main.log.prettyPrintLn("");
		}
		if (tdp!= null) {
			tdp.prettyPrint();
			Main.log.prettyPrintLn("");
		}
		if (vlp!= null) {
			vlp.prettyPrint();
			Main.log.prettyPrintLn("");
		}
		if (fapL!= null) {
			fapL.prettyPrint();
		}
		Main.log.prettyPrint("begin");
		Main.log.prettyPrintLn("");
		Main.log.prettyIndent();
		stml.prettyPrint();
		Main.log.prettyPrintLn("");
		
		Main.log.prettyOutdent();

		Main.log.prettyPrint("end");
	
	}
	
}






















