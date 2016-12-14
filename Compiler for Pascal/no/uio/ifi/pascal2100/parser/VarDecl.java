package no.uio.ifi.pascal2100.parser;

import java.util.*;

import no.uio.ifi.pascal2100.scanner.Scanner;
import no.uio.ifi.pascal2100.scanner.TokenKind;
import no.uio.ifi.pascal2100.main.*;

class VarDecl extends PascalDecl{
	Type t;
	String name ="";
	int l ,ebpOffset ,offset ;
	Block varBlock = null;
	VarDecl(String id, int lNum) {
		super(id, lNum);
	}

	@Override
	public void check(Block block, Library lib){
		t.check(block,lib);
		varBlock = block;
	}
	@Override
	public void genCode(CodeFile cf){
		if (t instanceof ArrayType) {
			ArrayType at = (ArrayType)t;
			at.genCode(cf);
			cf.genInstr("","movl",ebpOffset+ "(%ebp),%edx","");
			cf.genInstr("","leal",offset+"(%edx),%edx","");
			cf.genInstr("","movl","0(%edx,%eax,4),%eax",name + "[..]");
		}else{
			cf.genInstr("","movl",ebpOffset * varBlock.lvl+ "(%ebp),%edx","");
			cf.genInstr("","movl", offset+ "(%edx),%eax", name+"");
		}
	}

	@Override
	public String identify() {
		return "<var decl> in line " + l;
	}

	static VarDecl parse(Scanner s){
		enterParser("var decl");

		VarDecl vd = new VarDecl(s.curToken.id, s.curLine);
		vd.l = s.curLine;
		vd.name = s.curToken.id;
		s.skip(TokenKind.nameToken);

		s.skip(TokenKind.colonToken);

		vd.t = Type.parse(s);
		
		leaveParser("var decl");
		return vd;
	}
	@Override
	void prettyPrint() {
		Main.log.prettyPrint(name);
		Main.log.prettyPrint(" : ");
		t.prettyPrint();
		Main.log.prettyPrint("; ");
	}
}
class VarDeclList extends PascalDecl{
	VarDecl varDec = null;
	LinkedList<VarDecl> varDeclList  = null;
	Block varBlock = null;
	int arraySize = 0;
	ArrayType arType = null;
	
	VarDeclList(String id, int lNum) {
		super(id, lNum);
	}
	@Override
	public void check(Block block, Library lib){
		varBlock = block;
		for (VarDecl vd  : varDeclList ) {
			vd.check(block,lib);
		}
	}
	@Override
	public String identify() {
		return null;
	}
	@Override
	public void genCode(CodeFile cf){
		int tmp_off = -32 ;
		for (VarDecl vd  : varDeclList ) {
			if (vd.t instanceof ArrayType) {
				arType = (ArrayType)vd.t;
				arraySize =  arType.getRangeType() ;
				// +1 for a ta med egen index 
				tmp_off += (arType.getRangeType() +1) * (-4) ;
 
			}else{
				tmp_off += -4;	
			}
			vd.offset = tmp_off;
			vd.ebpOffset = -4;
		}
	}
	public int getVarSpace(){
		if(arType != null)
			return arType.getRangeType();
		else return 0;
	}

	public static VarDeclList parse(Scanner s){
		VarDeclList vdl = new VarDeclList ("var decl list ", s.curLine);
		vdl.varDeclList = new LinkedList<VarDecl> ();

		vdl.varDec= VarDecl.parse(s);
		vdl.varDeclList.add(vdl.varDec);

		while(s.curToken.kind == TokenKind.semicolonToken
			&& s.nextToken.kind == TokenKind.nameToken 
			&& s.nNextToken.kind == TokenKind.colonToken){
			s.skip(TokenKind.semicolonToken);

			vdl.varDec= VarDecl.parse(s);
			vdl.varDeclList.add(vdl.varDec);

		}
					
		s.skip(TokenKind.semicolonToken);


		return vdl;
	}
	
	
	@Override
	void prettyPrint() {
		if (varDeclList.size()> 0) {
			for (VarDecl vd  : varDeclList) {
				vd.prettyPrint();
			}
		}
		
	}
	
}
class VarDeclPart extends PascalDecl{
	VarDeclList vdl = null;

	VarDeclPart(String id, int lNum) {
		super(id, lNum);
	}


	@Override
	public void check(Block block, Library lib){
		if (vdl != null) {
			vdl.check(block,lib);
		}
	}
	
	@Override
	public String identify() {
		return null;
	}
	@Override
	public void genCode(CodeFile cf){
		vdl.genCode(cf);
	}

	public static VarDeclPart parse(Scanner s){

		if(s.curToken.kind == TokenKind.varToken){

			VarDeclPart  vdp = new VarDeclPart("var", s.curLine);

			enterParser("var decl part");
			s.skip(TokenKind.varToken);

			vdp.vdl = VarDeclList.parse(s);
			leaveParser("var decl part");	
		
			return vdp;
		}
		return null;

	}

	@Override
	void prettyPrint() {	
		Main.log.prettyPrint("var 	");
		vdl.prettyPrint();
	
	}
}

class ParamDecl extends PascalDecl{
	Type t; 
	String name =""; 
	int l ;
	int offset =0;
	int ebpOffset = 0;
	ParamDecl(String id, int lNum) {
		super(id, lNum);
	}
	@Override
	public void check(Block block, Library lib){
		block.addDecl(name, this);
		t.check(block,lib);
	}
	@Override
	public void genCode(CodeFile cf){
		cf.genInstr("","movl",ebpOffset+"(%ebp),%edx","");
		cf.genInstr("","movl",offset+"(%edx),%eax","Param : " + name);
	}

	@Override
	public String identify() {
		return "<param decl> on line " + l;	}

	public static ParamDecl parse(Scanner s){
		enterParser("param decl");
		
		ParamDecl pd = new ParamDecl(s.curToken.id, s.curLine);		
		pd.name = s.curToken.id;
		pd.l = s.curLine;

		s.skip(TokenKind.nameToken);
		s.skip(TokenKind.colonToken);
		pd.t = Type.parse(s);
		leaveParser("param decl");
		return pd;
		
	}
	
	@Override
	void prettyPrint() {
		Main.log.prettyPrint(name);
		Main.log.prettyPrint(" : ");
		t.prettyPrint();
	}
	
	
}



class ParamDeclList extends PascalDecl{
	
	LinkedList<ParamDecl> paramList  = null;
	ParamDecl paramdecl = null;
	Block paramBlock = null;

	ParamDeclList(String id, int lNum) {
		super(id, lNum);
	}
	@Override
	public void check(Block block, Library lib){
		paramBlock = block;
		for (ParamDecl pd  :paramList ) {
			pd.check(block,lib);
		}
	}
	@Override
	public void genCode(CodeFile cf){
		int tmp_offs = (paramList.size() * 4) + 4  ;

		int tmp_ebp = -4;
		for (int i = paramList.size() -1 ; i >= 0 ; i-- ) {
			ParamDecl pd = paramList.get(i);
			pd.offset = tmp_offs;
			tmp_offs -= 4;
			pd.ebpOffset = tmp_ebp * paramBlock.lvl;
		}

	}

	public static ParamDeclList parse(Scanner s){
		enterParser("param decl list");
		
		ParamDeclList pdl = new ParamDeclList("param decl list", s.curLine);
		pdl.paramList = new LinkedList<ParamDecl>();

		s.skip(TokenKind.leftParToken);

		
		if(s.curToken.kind != TokenKind.rightParToken){
			pdl.paramdecl= ParamDecl.parse(s);
			pdl.paramList.add(pdl.paramdecl);
			while(s.curToken.kind == TokenKind.semicolonToken){
				s.skip(TokenKind.semicolonToken);
				
				pdl.paramdecl= ParamDecl.parse(s);
				pdl.paramList.add(pdl.paramdecl);
			}
			
		}
		s.skip(TokenKind.rightParToken);
		
		leaveParser("param decl list");
		return pdl;
		
		
	}
	

	@Override
	public String identify() {
		return null;
	}

	@Override
	void prettyPrint() {
		int count = 0;
		if (paramList.size()== 1) {
			for (ParamDecl pd  : paramList) {
				pd.prettyPrint();
			}
		}else if (paramList.size()> 1) {
			for (ParamDecl pd  : paramList) {
				pd.prettyPrint();
				Main.log.prettyPrint("; ");
				count++;
				if (count == paramList.size()-1) {
					pd.prettyPrint();
					break;
				}
			}
		}

	}
	
}
class ConstDeclPart extends PascalDecl{
	ConstDeclList cdl = null;
	ConstDeclPart(String id, int lNum){
		super(id, lNum);
	}
	@Override
	public void check(Block block, Library lib){
		for (ConstDecl cd  : cdl.constList ) {
			cd.check(block,lib);
		}
	}
	@Override
	public void genCode(CodeFile cf){
	}

	public static ConstDeclPart parse(Scanner s ){

		if(s.curToken.kind == TokenKind.constToken){

			enterParser("const decl part");
			ConstDeclPart cdp = new ConstDeclPart("const decl part " , s.curLine);
			s.skip(TokenKind.constToken);
			cdp.cdl = ConstDeclList.parse(s);

			leaveParser("const decl part");
			return cdp ;
		}

		return null;
	}
	@Override
	public String identify() {
		return null;
	}

	@Override
	void prettyPrint() {
		Main.log.prettyPrint("const 	");
		cdl.prettyPrint();
	}
}

class ConstDecl extends ConstDeclPart{
	Constant  cstant = null;
	String name = "";
	int line ;
	ConstDecl(String id, int lNum){
		super(id, lNum);
	}
	@Override
	public void check(Block block, Library lib){
		cstant.check(block,lib);
	}
	@Override
	public void genCode(CodeFile cf){
		cstant.genCode(cf);
	}

	public static ConstDecl parse(Scanner s ){
		enterParser("const decl");
		ConstDecl cd = new ConstDecl(s.curToken.id , s.curToken.lineNum);
		cd.name =  s.curToken.id ;
		cd.line = s.curToken.lineNum;
		s.skip(TokenKind.nameToken);
		s.skip(TokenKind.equalToken);
		cd.cstant = Constant.parse(s);

		leaveParser("const decl");
		return cd;

	}
	@Override
	public String identify() {
		return "<const decl> in line " + line;
	}

	@Override
	void prettyPrint() {
		Main.log.prettyPrint(name);
		Main.log.prettyPrint(" = ");
		cstant.prettyPrint();
		Main.log.prettyPrint("; ");
	}

}

class ConstDeclList extends PascalDecl{
	ConstDecl constDecl = null;
	LinkedList<ConstDecl> constList = null;

	ConstDeclList(String id, int lNum){
		super(id, lNum);
	}
	@Override
	public void check(Block block, Library lib){
	}
	@Override
	public void genCode(CodeFile cf){
		
	}

	public static ConstDeclList parse(Scanner s ){
		ConstDeclList cdl = new ConstDeclList("const decl list", s.curLine);
		cdl.constList = new LinkedList<ConstDecl> ();
		cdl.constDecl= ConstDecl.parse(s);
		cdl.constList.add(cdl.constDecl);

		while (s.curToken.kind == TokenKind.semicolonToken 
			&& s.nextToken.kind == TokenKind.nameToken 
			&& s.nNextToken.kind ==TokenKind.equalToken) {
				
			s.skip(TokenKind.semicolonToken);
			cdl.constDecl= ConstDecl.parse(s);
			cdl.constList.add(cdl.constDecl);

		}
		s.skip(TokenKind.semicolonToken);


		return cdl;

	}
	@Override
	public String identify() {
		return null;
	}

	@Override
	void prettyPrint() {
		if (constList.size()>0) {
			for (ConstDecl cd  : constList) {
				cd.prettyPrint();
			}
		}
		
	}
}

class TypeDecl extends PascalDecl{
	Type t = null;
	String name = "";
	int line, ebpOffset,offset;
	Block typeBlock = null;
	TypeDecl(String id, int lNum){
		super(id,lNum);
	}
	@Override
	public String identify() {
		return "<type decl> in line " + line;
	}
	@Override
	public void check(Block block, Library lib){
		typeBlock = block;
		t.check(block,lib);
	}
	@Override
	public void genCode(CodeFile cf){
		//cf.genInstr("","movl",ebpOffset * typeBlock.lvl+ "(%ebp),%edx","");
		//cf.genInstr("","movl", offset+ "(%edx),%eax", name+"");
		t.genCode(cf);
	}

	public static TypeDecl parse (Scanner s){
		enterParser("type decl");
	
		TypeDecl td =  new TypeDecl(s.curToken.id, s.curLine);
		td.name = s.curToken.id;
		td.line = s.curToken.lineNum;
		s.skip(TokenKind.nameToken);
		s.skip(TokenKind.equalToken);
		td.t = Type.parse(s);

		leaveParser("type decl");
		return td;
		
	}

	@Override
	void prettyPrint(){
		Main.log.prettyPrint(name);
		Main.log.prettyPrint(" = ");
		t.prettyPrint();
		Main.log.prettyPrint("; ");
	}
}


class TypeDeclPart extends PascalDecl{
	TypeDeclList tdl = null;

	TypeDeclPart(String id, int lNum){
		super(id, lNum);
	}

	@Override
	public String identify() {
		return null;
	}
	@Override
	public void check(Block block, Library lib){
		for (TypeDecl td  : tdl.typeList ) {
			td.check(block,lib);
		}
	}
	@Override
	public void genCode(CodeFile cf){
		tdl.genCode(cf);
	}

	public static TypeDeclPart parse(Scanner s){
		if(s.curToken.kind == TokenKind.typeToken){
			TypeDeclPart tdp = new TypeDeclPart("type", s.curLine);

			enterParser("type decl part");
			s.skip(TokenKind.typeToken);

			tdp.tdl = TypeDeclList.parse(s);
			leaveParser("type decl part");
			return tdp;
		}
		return null;
	}
	@Override
	void prettyPrint() {
		Main.log.prettyPrint("type 	");
		tdl.prettyPrint();
	}

}

class TypeDeclList extends PascalDecl{
	LinkedList<TypeDecl> typeList = null;
	TypeDecl tDecl = null;
	TypeDeclList(String id, int lNum){
		super(id, lNum);
	}
	@Override
	public String identify() {
		return null;
	}
	@Override
	public void check(Block block, Library lib){
	}
	@Override
	public void genCode(CodeFile cf){
		/*int tmp_off = -32 ;
		for (TypeDecl td  : typeList ) {
			tmp_off += -4;
			td.offset = tmp_off;
			td.ebpOffset = -4;
		}*/
	}

	public static TypeDeclList parse(Scanner s){
		TypeDeclList tdl = new TypeDeclList("Type decl list " , s.curLine);
		tdl.typeList = new LinkedList<TypeDecl>();
		tdl.tDecl= TypeDecl.parse(s);
		tdl.typeList.add(tdl.tDecl);

		while(s.curToken.kind == TokenKind.semicolonToken 
			&& s.nextToken.kind == TokenKind.nameToken 
			&& s.nNextToken.kind == TokenKind.equalToken){

			s.skip(TokenKind.semicolonToken);

			tdl.tDecl= TypeDecl.parse(s);
			tdl.typeList.add(tdl.tDecl);
		}
		s.skip(TokenKind.semicolonToken);
		return tdl;
	}
	@Override
	void prettyPrint() {
		if (typeList.size()>0) {
			for (TypeDecl td :typeList ) {
				td.prettyPrint();
			}
		}
	}

}



class FuncDecl extends PascalDecl{
	ParamDeclList pdl = null;
	String name = "";
	Type t = null;
	Block  funBlock;
	int line ;
	FuncDecl(String id, int lNum){
		super(id,lNum);
	}
	@Override
	public String identify() {
		return "<fun decl> on line " + line;
	}
	@Override
	public void check(Block b, Library lib){
		funBlock.progName = name;
		funBlock.label = "func$";
		funBlock.lvl = (b.lvl +1);

		pdl.check(funBlock,lib);
		t.check(b,lib);
		funBlock.check(b,lib);
	}
	@Override
	public void genCode(CodeFile cf){
		funBlock.label += cf.getLabel(name);
		funBlock.label.toLowerCase();
		pdl.genCode(cf);
		funBlock.genCode(cf);
	}

	public static FuncDecl parse(Scanner s){
		enterParser("func decl");
		s.skip(TokenKind.functionToken);
		FuncDecl fd = new FuncDecl(s.curToken.id, s.curLine);
		fd.line = s.curLine;
		fd.name = s.curToken.id;
		s.skip(TokenKind.nameToken);
		fd.pdl = ParamDeclList.parse(s);
		s.skip(TokenKind.colonToken);
		fd.t = Type.parse(s);
		s.skip(TokenKind.semicolonToken);
		fd.funBlock= Block.parse(s);
		s.skip(TokenKind.semicolonToken);


		leaveParser("func decl");
		return fd;
	}

	@Override
	void prettyPrint() {
		
		Main.log.prettyPrint("function ");
		Main.log.prettyPrint(name + " ");
		Main.log.prettyPrint("( ");
		pdl.prettyPrint();
		Main.log.prettyPrint(" )");
		Main.log.prettyPrint(" : ");
		t.prettyPrint();
		Main.log.prettyPrint(" ; ");
		Main.log.prettyPrintLn();
		funBlock.prettyPrint();
		Main.log.prettyPrint(" ; ");
		Main.log.prettyPrintLn();
	

	}

}



class FuncAndProcList extends PascalDecl{
	LinkedList<PascalDecl> list= null ;
	ProcDecl proc = null;
	FuncDecl func = null;
	
	FuncAndProcList(String id, int lNum){
		super(id,lNum);
	}
	
	@Override
	public void check(Block block, Library lib){
		if(list != null){
			for ( PascalDecl pd : list) {
				pd.check(block,lib);
			}
		}
	}

	@Override
	public String identify() {
		return null;
	}
	@Override
	public void genCode(CodeFile cf){
		if(list != null){
			for ( PascalDecl pd : list) {
				pd.genCode(cf);
			}
		}
	}


	public static FuncAndProcList parse(Scanner s){

		FuncAndProcList fapl = new FuncAndProcList("func and proc list" , s.curLine);
		fapl.list = new LinkedList<PascalDecl> ();
		while ((s.curToken.kind == TokenKind.functionToken && s.nextToken.kind == TokenKind.nameToken)
			|| (s.curToken.kind == TokenKind.procedureToken && s.nextToken.kind == TokenKind.nameToken) ) {
			
			if (s.curToken.kind == TokenKind.functionToken && s.nextToken.kind == TokenKind.nameToken ) {
					fapl.func = FuncDecl.parse(s);
					fapl.list.add(fapl.func);
			}else if (s.curToken.kind == TokenKind.procedureToken && s.nextToken.kind == TokenKind.nameToken ) {
					fapl.proc = ProcDecl.parse(s);
					fapl.list.add(fapl.proc);
			
			}else break;

		}

		return fapl;
	}
	@Override
	void prettyPrint() {
		for (int i = 0; i< list.size() ;i++ ) {
			if (list.get(i) instanceof FuncDecl) {
				FuncDecl f = (FuncDecl)list.get(i) ;
				f.prettyPrint();
			}else if(list.get(i) instanceof ProcDecl){
				ProcDecl p = (ProcDecl)list.get(i) ;
				p.prettyPrint();
			}
			
		}
	}

}

class ProcDecl extends PascalDecl{
	Block procBlock = null;
	ParamDeclList paramDl = null;
	boolean paraTrue = false;
	String name = "";
	int lineN ;

	ProcDecl(String id, int lNum){
		super(id, lNum);
	}
	@Override
	public void check(Block b, Library lib){
		procBlock.progName = name;
		procBlock.label = "proc$";

		procBlock.lvl = (b.lvl +1);
		if (paraTrue) {
			paramDl.check(procBlock,lib);
		}
		procBlock.check(b,lib);
	}
	@Override
	public void genCode(CodeFile cf){
		procBlock.label += cf.getLabel(name);
		procBlock.label.toLowerCase();
		if (paraTrue) {
			paramDl.genCode(cf);
			paraTrue=false;

		}
		procBlock.genCode(cf);	
		 
	}

	@Override
	public String identify() {
		return "<proc decl> in line " + lineN;
	}

	public static ProcDecl parse(Scanner s){
		enterParser("proc decl");

		s.skip(TokenKind.procedureToken);
		ProcDecl pd = new ProcDecl(s.curToken.id, s.curLine);
		pd.lineN = s.curLine;

		pd.name = s.curToken.id;
		s.skip(TokenKind.nameToken);

		if (s.curToken.kind == TokenKind.leftParToken) {
			pd.paramDl = ParamDeclList.parse(s);
			pd.paraTrue = true;
		}

		s.skip(TokenKind.semicolonToken);
		pd.procBlock = Block.parse(s);
		s.skip(TokenKind.semicolonToken);
		
		leaveParser("proc decl");
		return pd;

	}
	
	@Override
	void prettyPrint() {
		Main.log.prettyPrint("procedure ");
		Main.log.prettyPrint(name + " ");
		if (paramDl != null) {
			Main.log.prettyPrint("( ");
			paramDl.prettyPrint();
			Main.log.prettyPrint(" )");
		}
		Main.log.prettyPrint(" ; ");
		Main.log.prettyPrintLn();
		procBlock.prettyPrint();
		Main.log.prettyPrint(" ; ");
		Main.log.prettyPrintLn();
	
	}
}








