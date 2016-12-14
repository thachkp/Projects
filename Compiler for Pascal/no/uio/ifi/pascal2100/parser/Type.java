package no.uio.ifi.pascal2100.parser;
import java.util.LinkedList;

import no.uio.ifi.pascal2100.scanner.*;
import no.uio.ifi.pascal2100.main.*;

public class Type extends PascalDecl{
	Type(String id, int lNum) {
		super(id, lNum);
	}

	@Override
	public void check(Block b, Library lib){

	}
	@Override
	public String identify() {
		return null;
	}
	@Override
	public void genCode(CodeFile cf){
	}
	@Override
	void prettyPrint() {
	}
	
	public static Type parse(Scanner s){
		enterParser("type");

		Type t = new Type(s.curToken.id, s.curLine);
		
		if (s.curToken.kind == TokenKind.arrayToken){
			t = ArrayType.parse(s);
		}else if(s.curToken.kind == TokenKind.leftParToken){
			t= EnumType.parse(s);
		}else if (s.curToken.kind == TokenKind.nameToken){
			t = TypeName.parse(s);
		}else if((s.curToken.kind == TokenKind.nameToken 
			|| s.curToken.kind == TokenKind.intValToken ) && s.nextToken.kind == TokenKind.rangeToken) {
			t = RangeType.parse(s);
		}
		
		leaveParser("type");
		return t;
	}

}


class TypeName extends Type{
	TypeName(String id, int lNum) {
		super(id, lNum);
	}
	@Override
	public String identify() {
		return "<type decl>";
	}
	@Override
	public void check(Block b, Library lib){
		if (name.equalsIgnoreCase("integer") || name.equalsIgnoreCase("Boolean")||name.equalsIgnoreCase("char") ) {
			PascalDecl d = lib.findType(name, this, this);
		}else {
			PascalDecl d = b.findDecl(name, this);

		}

	}
	public  static TypeName parse(Scanner s){
		enterParser("name");
		TypeName tn = new TypeName(s.curToken.id, s.curLine);
		tn.name = s.curToken.id;
		s.skip(TokenKind.nameToken);
		leaveParser("name");
		return tn;
	}
	@Override
	void prettyPrint() {
		Main.log.prettyPrint(name);
	}

}

class RangeType extends Type{
	Constant ct1 = null, ct2 = null;
	
	RangeType(String id, int lNum) {
		super(id, lNum);
	}

	@Override
	public void check(Block b, Library lib) {
		ct1.check(b,lib);
		ct2.check(b,lib);
	}
	public static RangeType parse(Scanner s){
		enterParser("range type");
		RangeType rt = new RangeType("range type", s.curLine);
		rt.ct1 = Constant.parse(s);
		s.skip(TokenKind.rangeToken);
		rt.ct2 = Constant.parse(s);
		leaveParser("range type");
		return rt;
		
	}
	@Override
	void prettyPrint() {
		ct1.prettyPrint();
		Main.log.prettyPrint("..");
		ct2.prettyPrint();
	}
	
}


class EnumType extends Type{
	EnumLiteralList enlList;
	EnumType(String id, int lNum) {
		super(id, lNum);
	}
	public static EnumType parse(Scanner s){
		EnumType ent = new EnumType(s.temp, s.curLine);
			
		enterParser("enum type");
		if(s.curToken.kind == TokenKind.leftParToken){
			s.skip(TokenKind.leftParToken);
			ent.enlList = EnumLiteralList.parse(s);
			s.skip(TokenKind.rightParToken);
		}
		leaveParser("enum type");

		return ent;
	}

	@Override
	void prettyPrint() {
		Main.log.prettyPrint("(");
		enlList.prettyPrint();
		Main.log.prettyPrint(")");
	}
	
}
class EnumLiteral extends EnumType{
	String name ;
	EnumLiteralList elL;
	
	EnumLiteral(String id, int lNum) {
		super(id, lNum);
	}
	
	public static EnumLiteral parse(Scanner s){
		enterParser("enum literal");
		EnumLiteral el = new EnumLiteral(s.curToken.id, s.curLine);
		el.elL = new EnumLiteralList("enumLiterallList", s.curLine);
		el.name = s.curToken.id;
		s.skip(TokenKind.nameToken);			
		return el;
	}
	
	@Override
	void prettyPrint() {
		Main.log.prettyPrint(name);
	}
	
}



class EnumLiteralList extends PascalDecl{
	EnumLiteral enumLiteral = null;
	LinkedList<EnumLiteral> enumList = null;
	EnumLiteralList(String id, int lNum) {
		super(id, lNum);
	}
	@Override
	public void check(Block b, Library lib ){

	}
	@Override
	public void genCode(CodeFile cf){
	}
	public static EnumLiteralList parse(Scanner s){
		EnumLiteralList elList = new EnumLiteralList(s.temp, s.curLine);
		elList.enumList = new LinkedList<EnumLiteral>();

		if(s.curToken.kind != TokenKind.rightParToken){
			elList.enumLiteral= EnumLiteral.parse(s);
			elList.enumList.add(elList.enumLiteral);

			while(s.curToken.kind == TokenKind.commaToken){
				s.skip(TokenKind.commaToken);
				
				elList.enumLiteral= EnumLiteral.parse(s);
				elList.enumList.add(elList.enumLiteral);
			}
		}
		
		return elList;
	}
	
	
	@Override
	public String identify() {
		return null;
	}

	@Override
	void prettyPrint() {
		for (EnumType et : enumList) {
			et.prettyPrint();
			Main.log.prettyPrint(",");
		}
	}
	
}


class ArrayType extends Type{
	ArrayType(String id, int lNum) {
		super(id, lNum);
	}

	RangeType rt;
	Type type ;
	Block ablock =null;

	@Override
	public void genCode(CodeFile cf){
		int rangeInt = 0;
		if (rt.ct1 instanceof NumericLiteral){
			NumericLiteral nl = (NumericLiteral)rt.ct1;
			cf.genInstr("","subl","$"+ nl.integer+",%eax","");// fikse på low som er den misnt i range token
		}else if(rt.ct1 instanceof Name ){
			Name tmp = (Name)rt.ct1;
			NumericLiteral nl = getInteger(tmp.name, ablock);
			getRangeType();
			if (nl!=null) {
				cf.genInstr("","subl","$"+ nl.integer+",%eax","");// fikse på low som er den misnt i range token
			}
		}
	}
	public NumericLiteral getInteger(String s, Block b){

		PascalDecl d = ablock.getFromBlock(s,b);
		if (d == null) return null;

		if (d instanceof NumericLiteral) {
			NumericLiteral nl = (NumericLiteral)d;
			return nl;
		}else if (d instanceof ConstDecl) {
			ConstDecl cd = (ConstDecl)d;
			if (cd.cstant instanceof Name){
				Name tmp = (Name)cd.cstant;
				getInteger(tmp.name,b);
			}else if (cd.cstant instanceof NumericLiteral){
				NumericLiteral nl = (NumericLiteral)cd.cstant;
				return nl;
			}
		}
		return null;
	}
	public int getRangeType(){
		int constant1 = 0, constant2 = 0;
		if (rt.ct1 instanceof Name) {
			Name tmp = (Name)rt.ct1;
			NumericLiteral nl1 =  getInteger(tmp.name,ablock);
			if (nl1 != null) constant1 = nl1.integer;
		}else{
			NumericLiteral nl1 =(NumericLiteral) rt.ct1;
			constant1 = nl1.integer;
		} 
		if (rt.ct2 instanceof Name) {
			Name tmp = (Name)rt.ct2;
			NumericLiteral nl2 =  getInteger(tmp.name,ablock);
			if (nl2 != null) constant2 = nl2.integer;
		}else {
			NumericLiteral nl2 =(NumericLiteral) rt.ct2;
			constant1 = nl2.integer;
		}
		return constant2 - constant1;
	}

	@Override
	public String identify() {
		return "arrayType decl";
	}

	@Override
	public void check(Block b, Library lib) {
		ablock = b;
		rt.check(b,lib);
		type.check(b,lib);
	}
	@Override
	void prettyPrint() {
		Main.log.prettyPrint("array");
		Main.log.prettyPrint("[");
		rt.prettyPrint();
		Main.log.prettyPrint("] ");
		Main.log.prettyPrint(" of ");
		type.prettyPrint();

	}

	public static ArrayType parse(Scanner s){
		
		enterParser ("array type");
		ArrayType a = new ArrayType(s.temp, s.curLine);
		s.skip(TokenKind.arrayToken);

		if (s.curToken.kind == TokenKind.leftBracketToken) {
			s.skip(TokenKind.leftBracketToken);
			a.rt = RangeType.parse(s);
			s.skip(TokenKind.rightBracketToken);
			s.skip(TokenKind.ofToken);
			a.type = Type.parse(s);
		}
		leaveParser("array type");
		return a;
	}

}




class Constant extends Factor{
	String name = "";
	Block cstBlock = null;
	Constant(String id,int n) {
		super(id,n);
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
	public static Constant parse(Scanner s){
		enterParser("constant");
		Constant c = null;

		if(s.curToken.kind == TokenKind.intValToken){
			c = NumericLiteral.parse(s);
		}else if(s.curToken.kind == TokenKind.nameToken){
			c = Name.parse(s);
		}else if(s.curToken.kind == TokenKind.stringValToken){
			c = StringLiteral.parse(s);
		}else{
			Main.error("No constant" + s.curToken.kind + " " + s.curLine);
		}
		
		leaveParser("constant");
		
		return c;
			
	}
	
	@Override
	void prettyPrint() {
	}
	
}

class NumericLiteral extends Constant{
	int integer;
	NumericLiteral(String id,int n) {
		super(id,n);
	}
	@Override
	public void genCode(CodeFile cf){
		cf.genInstr("","movl","$"+integer+ ",%eax"," " + integer);
	}
	@Override
	public String identify() {
		return null;
	}
	public String toString(){
		return integer + "";
	}
	public static NumericLiteral parse(Scanner s){
		enterParser("NumericLiteral");
		NumericLiteral nl = new NumericLiteral("",s.curLine);
		nl.integer = s.curToken.intVal;
		s.skip(TokenKind.intValToken);
		leaveParser("NumericLiteral");
		
		return nl;
	}
	@Override
	void prettyPrint() {
		Main.log.prettyPrint(integer +"");
	}
	
	
}
class StringLiteral extends Constant{
	String text;
	char c ;
	boolean textTrue = false;
	StringLiteral(String id,int n) {
		super(id,n);
	}
	@Override
	public void check(Block b , Library lib){
	}
	@Override
	public void genCode(CodeFile cf){
		if (textTrue) {
			String startLoop = 	cf.getLocalLabel();
			cf.genString(startLoop,text,"");
			cf.genInstr("","leal",startLoop+",%eax", "Adr: " + text);
		}else{
			int ascii = (int) c;
			cf.genInstr("","movl","$"+ascii + ",%eax","move " + c+ " to %eax");
		}
	}
	@Override
	public String identify() {
		return null;
	}
	public static StringLiteral parse(Scanner s) {
		
		StringLiteral sl = new StringLiteral("",s.curLine);


		sl.text = s.curToken.strVal;

		s.skip(TokenKind.stringValToken);
		if (sl.text.length() > 1) {
			enterParser("StringLiteral");
			leaveParser("StringLiteral");
			sl.textTrue = true;
			return sl;
		} else if(sl.text.length() == 1){
			enterParser("charLiteral");
			sl.c = sl.text.charAt(0);
			leaveParser("charLiteral");
			return sl;
		}

		return null;
	}
	@Override
	void prettyPrint() {
		if (textTrue) {
			Main.log.prettyPrint("'"+text+"'");
		}else{
			Main.log.prettyPrint("'"+c+"'");
		}
		
	}
	
}
class Name extends Constant{
	String name = "";
	Block nameBlock = null;
	Name(String id,int n ){
		super(id,n);
	}
	@Override
	public void genCode(CodeFile cf){
		PascalDecl d = nameBlock.getFromBlock(name,nameBlock);
		if (d instanceof ConstDecl) {
			ConstDecl cd = (ConstDecl)d;
			cd.genCode(cf);
		}
	}
	@Override
	public void check(Block b, Library lib){
		nameBlock = b;
		b.findDecl(name,this);
	}
	public static Name parse(Scanner s){
		Name n = new Name("",s.curLine);
		n.name = s.curToken.id;
		s.skip(TokenKind.nameToken);
		return n;

	}
	@Override
	void prettyPrint() {
		Main.log.prettyPrint(name);
	}

}











