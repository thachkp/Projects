package no.uio.ifi.pascal2100.parser;

import java.util.HashMap;
import java.util.ArrayList;

import no.uio.ifi.pascal2100.scanner.Scanner;
import no.uio.ifi.pascal2100.scanner.TokenKind;
import no.uio.ifi.pascal2100.main.*;

public class Library extends Block{
	TypeInteger ti = null;
	TypeChar  tc= null;
	ConstEol ce= null;
	ProcWrite pw = null;
	TypeBoolean tbl = null;
	Negation neg = null;
	HashMap<String,PascalDecl> libList = new HashMap<String,PascalDecl>();
	public Library(int n) {
		super(n);
	}

	public PascalDecl findType(String id , PascalSyntax where, PascalDecl pd ){
		String temp = id.toLowerCase();

		if (temp.equals("write")) {
			pw = new ProcWrite("", 0);
			Main.log.noteBinding(id,where, pw);
			libList.put(id,pw);
		}else if(temp.equals("eol")){
			ce = new ConstEol("eol",0);
			Main.log.noteBinding(id,where, ce);
			libList.put(id,ce);
		}else if (temp.equals("integer")){
			ti = new TypeInteger("",0);
			Main.log.noteBinding(id,where, ti);
			libList.put(id,ti);
		}else if (temp.equals("boolean")) {
			tbl = new TypeBoolean("boolean",0);
			Main.log.noteBinding(id,where, tbl);
			libList.put(id,tbl);
		}else if (temp.equals("false") ){
			tbl = new TypeBoolean("false",0);
			Main.log.noteBinding(id,where, tbl);
			libList.put(id,tbl);
		}else if (temp.equals("true") ){
			tbl = new TypeBoolean("true",0);
			Main.log.noteBinding(id,where, tbl);
			libList.put(id,tbl);
		}else if(temp.equals("not")){
			neg = new Negation("not",0);
			Main.log.noteBinding(id,where, neg);
			libList.put(id,neg);
		}else if (temp.equals("char")){
			tc = new TypeChar("char",0);
			Main.log.noteBinding(id,where, tc);
			libList.put(id,tc);
		}else{
			Main.error("id : " + id +" is not in the library");
		}
		return null;

	}
	PascalDecl getFromLib(String s){
		String id = s.toLowerCase();
		PascalDecl d = null;
		if (libList.containsKey(id)) {
			d = libList.get(id);
			return d;
		}
		return d;
	}
	@Override
	public void genCode(CodeFile cf ){
		cf.genInstr("", ".extern", "write_char","");
		cf.genInstr("", ".extern", "write_int","");
		cf.genInstr("", ".extern", "write_string","");
	}
	@Override
	public String identify() {
		return null;
	}
	

} 

class ProcWrite extends PascalDecl{

	ProcWrite(String id,int n ){
		super(id,n);
	}
		@Override
	public String identify() {
		return "<proc decl> in the Library";
	}
	@Override
	public void prettyPrint(){

	}
	@Override
	public void check(Block b, Library lib){

	}
	@Override
	public void genCode(CodeFile cf ){

	}
}

class TypeInteger extends PascalDecl{

	TypeInteger(String id,int n ){
		super(id,n);
	}
		@Override
	public String identify() {
		return "<type decl> in the Library";
	}
	@Override
	public void prettyPrint(){

	}
	@Override
	public void check(Block b, Library lib){

	}
	@Override
	public void genCode(CodeFile cf ){

	}
}

class TypeChar extends PascalDecl{

	TypeChar(String id,int n ){
		super(id,n);
	}
		@Override
	public String identify() {
		return "<type char> in the Library";
	}
	@Override
	public void prettyPrint(){

	}
	@Override
	public void check(Block b, Library lib){

	}
	@Override
	public void genCode(CodeFile cf ){

	}
}

class ConstEol extends PascalDecl{

	ConstEol(String id,int n ){
		super(id,n);
	}
		@Override
	public String identify() {
		return "<const decl> in the Library";
	}
	@Override
	public void prettyPrint(){

	}
	@Override
	public void check(Block b, Library lib){

	}
	@Override
	public void genCode(CodeFile cf ){

	}
} 



class TypeBoolean extends PascalDecl{
	TypeBoolean(String id,int n ){
		super(id,n);
	}
		@Override
	public String identify() {
		return "<type boolean> in the Library";
	}
	@Override
	public void genCode(CodeFile cf ){

	}
	@Override
	public void prettyPrint(){

	}
	@Override
	public void check(Block b, Library lib){

	}
} 