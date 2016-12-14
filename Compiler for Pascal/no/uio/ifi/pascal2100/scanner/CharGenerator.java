package no.uio.ifi.pascal2100.scanner;

import java.io.*;
import no.uio.ifi.pascal2100.main.Main;

public class CharGenerator {
	public  char curC, nextC;
	public  int cLineNum, nLineNum;
	public  int lastTwoChar;
	private  LineNumberReader sourceFile = null;
	private  String sourceLine;
	private  int sourcePos;

	public CharGenerator(String fileName) {
		try {
			sourceFile = new LineNumberReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			Main.error("Cannot read " + fileName + "!");
		}
		sourcePos = 0;
		sourceLine = "";
		curC = nextC = ' ';
		cLineNum = nLineNum = 0;
		lastTwoChar = 2;
		//readNext();
		//readNext();
	}

	public  void finish() {
		if (sourceFile != null) {
			try {
				sourceFile.close();
			} catch (IOException e) {
				Main.error("Could not close source file!");
			}
		}
	}

	public  boolean isMoreToRead() {
		if (sourceLine == null) {
			return false;
		}
		// checks if there is more to read in the sourceLine
		if (sourcePos != sourceLine.length())
			return true;
		// jump to nextLine of the fil if SourceLine er tom 
		do {
			jumpToNext();
			if (sourceLine == null) {
				return false;
			}
			sourceLine = sourceLine.trim();
		} while (sourceLine.isEmpty());
		return true;
	}

	/*public  boolean isMoreToRead() {
		if (sourceFile != null) {
			return true;
		}
		return false;
	}*/


	public  int curLineNum() {
		return (sourceFile == null ? 0 : sourceFile.getLineNumber());
	}

	public  void readNext() {
		// reads the next character in the codeline and update our variables
		curC = nextC;
		cLineNum = nLineNum;
		if (!isMoreToRead()) {
			if (lastTwoChar > 0)
				lastTwoChar--;

			nextC = 0;
			return;
		}
		//jumpToNext();
		//Main.log.noteSourceLine(sourceFile.getLineNumber(), sourceLine);
		// curC is being set to nextC and nextC is being updated here..
		nextC = sourceLine.charAt(sourcePos++);
		nLineNum = curLineNum();

	}
	/*public  void readNext() {

		// reads the next character in the codeline and update our variables
		curC = nextC;
		cLineNum = nLineNum;

		// jump to nextLine of the fil if SourceLine er tom 
		if(!isMoreToRead()){
			return;
		}
		int i = 0;
		while(sourcePos >= sourceLine.length()){
			try{
				sourceLine = sourceFile.readLine();
				if (sourceLine == null) {
					sourceFile.close();
					sourceLine = "";
				}else{
					//sourceLine = sourceLine.replace('\t' , ' ');
					if (sourceLine.length() > i) {
						i = sourceLine.length();
					}
					Main.log.noteSourceLine(sourceFile.getLineNumber(), sourceLine);

				}
				sourceLine += " ";
			}catch(Exception e){
				e.printStackTrace();
			}
			sourcePos = 0;
		}

		//jumpToNext();
		//Main.log.noteSourceLine(sourceFile.getLineNumber(), sourceLine);
		// curC is being set to nextC and nextC is being updated here..
		nextC = sourceLine.charAt(sourcePos++);
		nLineNum = curLineNum();

	}*/

	/**
	 * sjekker om man er p samme linje
	 * 
	 * @return false
	 */
	public  boolean checkLineBetween() {
		if (cLineNum != nLineNum)
			return true;
		return false;
	}

	/**
	 * sjekker om man har lest de 2 siste charene.
	 * 
	 * @return true hvis man har lest
	 */
	public  boolean lastTwoChar() {
		return lastTwoChar <= 0;
	}

	/**
	 * lese neste linje og notere det inn p logg fil.
	 */
	public  void jumpToNext() {
		// get new line from code and log it..
		try {
			sourceLine = sourceFile.readLine();
			sourcePos = 0;

		Main.log.noteSourceLine(sourceFile.getLineNumber(), sourceLine);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}


