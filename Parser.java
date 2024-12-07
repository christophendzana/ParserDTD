/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.html.parser;


import hsegment.JObject.Swing.Text.CommentHandler;
import hsegment.JObject.Swing.Text.EntityHandler;
import hsegment.JObject.Swing.Text.ErrorHandler;
import hsegment.JObject.Swing.Text.ErrorType;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;
import hsegment.JObject.Swing.Text.PrologHandler;
import hsegment.JObject.Swing.Text.TagHandler;
import hsegment.JObject.Swing.Text.TextHandler;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.io.StringReader;
import javax.swing.text.ChangedCharSetException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.parser.AttributeList;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.DTDConstants;
import javax.swing.text.html.parser.Element;
import javax.swing.text.html.parser.TagElement;

/**
 *<p> A simple DTD and SCHEMA driven XML parser. this parser reads 
 * a XML file from an InputStream and call various methods.
 * <p>
 * <em>it is a validated parser</em> means that during parsing 
 * it check if file is well formated and if respect DTD or schema 
 * structure if not it call error or warning methods.
 * <p> 
 * <em>it is a event parser </em> means that during parsing it call 
 * some methods to handling different event like tag start and end, 
 * comment tag and so one. all that callback method are Interface 
 * that can be implemented.
 * <p> 
 * During parser parse doctype declaration, some conflict's error will appear.
 * <ul><li> the first one is if dtd is provided to parser's constructor and his name 
 * doesn't matches with name declared to doctype</li>
 *  <li> the second one is if dtd is provided to parser's constructor and a new dtd 
 * path is given into doctype declaration</li></ul>
 * 
 * 
 * @author Ndzana Christophe
 */
public class Parser extends javax.swing.text.html.parser.Parser implements DTDConstants{
    
    protected char[] text;
    protected StringBuffer textBuffer;
    protected char[] str;
    
    private Element recent;
    private SimpleAttributeSet attributes;
    protected HDTD dtd;
    private int currentPosition = 0;
    protected Reader in;
    protected int ch = -1;
    //track index off the next character into buffer
    private int strPos = -1;
    private int ln;
    private EntityHandler entHandler;
    private ErrorHandler errHandler;
    private TagHandler tagHandler;
    private TextHandler texHandler;
    private CommentHandler comHandler;
    private PrologHandler proHandler;
    public Parser(DTD dtd){
        
        super(dtd);
        str = new char[128];
    }
    
    public void setEntityHandler(EntityHandler entHandler){
        this.entHandler = entHandler;
    }
    
    public void setErrorHandler(ErrorHandler errHandler){
        this.errHandler = errHandler;
    }
    
    public void setTagHandler(TagHandler tagHandler){
        this.tagHandler = tagHandler;
    }
    
    public void setTextHandler(TextHandler textHandler){
        this.texHandler = textHandler;
    }

    public void setComHandler(CommentHandler comHandler) {
        this.comHandler = comHandler;
    }

    public CommentHandler getComHandler() {
        return comHandler;
    }

    public void setProHandler(PrologHandler proHandler) {
        this.proHandler = proHandler;
    }

    public PrologHandler getProHandler() {
        return proHandler;
    }
    
    public EntityHandler getEntHandler() {
        return entHandler;
    }

    public ErrorHandler getErrHandler() {
        return errHandler;
    }

    public TagHandler getTagHandler() {
        return tagHandler;
    }

    public TextHandler getTexHandler() {
        return texHandler;
    }
    
    /**
     * Return this parser DTD. if null parser was given the default one was provided by 
     * the parser.
     * @return provider or generated parser.
     */
    public DTD getDTD(){
        return this.dtd;
    }
    
    
    protected synchronized void handleText(char[] text) {
        if(texHandler == null)
            return;
        
        this.texHandler.handleText(text);
    }
    
    protected void handleTitle(char[] text) {
        // default behavior is to call handleText. Subclasses
        // can override if necessary.
        handleText(text);
    }
    
    
    protected void handleComment(char[] text) {
        try {
            comHandler.handleComment(text);
        } catch (HJAXException e) {
        }
    }
    
    
    protected void handleEmptyTag(TagElement tag) throws ChangedCharSetException {
        try {
            tagHandler.handleEmptyTag(tag);
        } catch (HJAXException e) {
            
        }
        
    }
    
    protected void handleStartTag(TagElement tag) {
        
        try {
            tagHandler.handleStartTag(tag);
        } catch (HJAXException e) {
        }
        
    }
    
    protected void handleEndTag(TagElement tag) {
        try {
            tagHandler.handleEndTag(tag);
        } catch (HJAXException e) {
            
        }
    }
    
    protected void handleError(String src, String msg, String debug, ErrorType type) throws HJAXException{
        if(errHandler == null)
            throw new HJAXException("Class ErrorHandler must be implemented");
        
        errHandler.errorHandler(src, msg, debug, type);
    }
    public synchronized void parse(Reader in){
        this.in = in;
        
        try {
            while ((ch = in.read()) != -1){
            switch(ch){
                case '<' : 
                    parseTag();
            }
        }
        } catch (Exception e) {
        }
    }
    
    /**
     * Initialise DTD using by this parser. if DTD is provided by this 
     * parser user that DTD is return but if DTD is not provided or is null 
     * the default one is provided
     * @param dtd DTD used by the parser
     * @return DTD used by this parser.
     */
    private DTD initDTD(DTD dtd){
        if(dtd == null)
            return new DefaultDTD("Generated", -1);
        else 
            return dtd;
    }
    /**
     * Return a string with <code>length</code> number of caracter. the first fetched 
     * character is the one where pointer is,
     * @param length number of caracter to return
     * @return text with <code>length</code> number of carater or less if <code>length</code> 
     * is greater than parsed text capacity
     */
    private String readString(int length){
        if(length < 0){
            throw new IllegalArgumentException("Bad length parameter");
        }
        String text = "";
        //we should not have to fetch more than we have text if length is greater than 
        //text capacity.
        int minLength = Math.min(length, text.length() - currentPosition);
        int c;
        try {
            for(int i = 0; i < minLength; i++){
                c = in.read();
                text.concat(""+(char)c);
            }
        } catch (Exception e) {
            
        }
        
        
        return text;
    }
    
    /**
     * Copy and return characters.start copy to <code>startIndex</code> index and 
     * copy <code>length</code> number of character.Note that IllegalArgument exception 
     * is thrown if non parsed character should be copy.
     * 
     * @param startIndex started copying index
     * @param length number of character to be read and copy
     * @return and array which contained copied caracters
     * @throws IllegalArgumentException if attemps to copy non parsed characters
     */
    private char[] getChars(int startIndex, int length){
        if(startIndex >= getCurrentPos() || (startIndex + length) > getCurrentPos()){
            throw new IllegalArgumentException("cannot read non parsed characters");
        }
        char[] chars = new char[length];
        System.arraycopy(text, startIndex, chars, 0, length);
        return chars;
    }
    
    
    private void parseTag(){
        TagElement tag;
        Element element = null;
        AttributeList attList = null;
        boolean isClosableTag = false;
        boolean isInstructionTag = false;
        try {
            
            while((ch = in.read()) != - 1){
                
                switch(ch){
                    case '!': 
                        ch = in.read();
                        int spaceCount = skipSpace();
                        if(spaceCount != 0)
                            error("Error.XML", "Misplaced space caracter");
                        
                        char[] buff = new char[2];
                        
                        in.read(buff);
                        String strBuf = new String(buff);
                        if(strBuf.equals("--")){
                            parseComment();
                            break;
                        } else {
                            buff = new char[5];
                            in.read(buff);
                            strBuf = strBuf.concat(new String(buff));
                            
                            
                        } 
                        
                        if(strBuf.equals("DOCTYPE"))
                            parseDoctype();
                        else 
                            error("Error.XML", "Bad tag syntaxe");
                        break;
                    case '?' : 
                        isInstructionTag = true;
                    case ' ': 
                        if(!getString(0).isEmpty() && element == null){
                            element = dtd.getElement(getString(0));
                            
                            if(element == null && dtd instanceof DefaultDTD)
                                dtd.defineElement(getString(0), HDTDConstants.ANY, false, true, null, null, null, null);
                            else if(element == null && !(dtd instanceof DefaultDTD))
                                error("Error.XML", "element : "+getString(0)+" is not define to Validator");
                            resetBuffer();
                        }
                        break;
                    case '=': 
                        if(!getString(0).isEmpty() && element != null){
                            attList = element.getAttribute(getString(0));
                            if(attList == null && (dtd instanceof DefaultDTD)){
                                defineAttribute(getString(0), element.atts);
                            } else if(attList == null && !(dtd instanceof DefaultDTD)){
                                error("Error.XML", "Attribut : "+getString(0)+"is not define to Validator");
                            }
                            
                            resetBuffer();
                        } else {
                            error("Error.XML", "bad Tag synstaxe");
                        }
                        break;
                    
                    case '"' : 
                        if(!getString(0).isEmpty() && attList != null){
                            attList.value = getString(0);
                        } else if(attList == null){
                            error("Error.XML", "value without attribute");
                        }
                    case '>' :
                        if(!getString(0).isEmpty() && element == null){
                            
                        }
                        if(element == null){
                            error("Error.XML", "Missed element name");
                        } else if (attList != null && attList.value == null){
                            error("Error.XML","attribute : "+attList.name+" is not initialise");
                        }
                        
                        tag = new TagElement(element);
                        handleStartTag(tag);
                        return;
                    case '/' : 
                        isClosableTag = true;
                        break;
                    default :
                        addString(ch);
                }
            }
        } catch (Exception e) {
        }
        
    }
    
    private void parseDoctype(){
        String dtdName = null;
        int locationType = -1;
        String dtdFilePath = null;
        try {
            while((ch = in.read()) != - 1){
                switch(ch){
                    case ' ' :
                        if(!getString(0).isEmpty() && dtdName == null){
                            dtdName = getString(0);
                            resetBuffer();
                        } else if(!getString(0).isEmpty() && locationType == -1){
                            switch(getString(0)){
                                case "SYSTEM" : 
                                    locationType = HDTDConstants.SYSTEM;
                                    break;
                                case "PUBLIC" : 
                                    locationType = HDTDConstants.PUBLIC;
                                    break;
                                default :
                                    error("Error.XML", "Incorrect DOCTYPE Location");
                                    resetBuffer();
                                    break;
                            }
                        }
                        
                        if(dtd != null && !dtd.getName().equalsIgnoreCase(dtdName)){
                            error("Error.XML", "DTD's Name conflict");
                        }
                        break;
                    case '"' :
                        if(!getString(0).isEmpty() && dtdName != null && locationType != -1)
                            dtdFilePath = getString(0);
                        resetBuffer();
                        if(dtd != null)
                            error("Error.XML", "DTD conflict");
                    case '[' : 
                        if(dtdName != null && locationType == -1 && dtdFilePath == null){
                            DTDParser dtdParser = new DTDParser(dtd);
                            while((ch = in.read()) != -1){
                                if(ch == ']')
                                    break;
                                addString(ch);
                            }
                            dtdParser.parse(new StringReader(new String(getString(0))));
                            resetBuffer();
                        } else {
                            error("Error.XML", "Bad DOCTYPE declaration");
                        }
                        break;
                    case '>' :
                        if(dtdName != null && locationType != -1 && dtdFilePath != null)
                            return;
                        else
                            error("Error.XML", "Bad DOCTYPE declaration");
                        return;
                    default : 
                        addString(ch);
                }
                
                
            
            }
        } catch (Exception e) {
        }
        
    }
    
    private Element defineElement(String elementName){
        
    }
    private void defineAttribute(String name , AttributeList attList){
        if(attList == null){
            attList = new AttributeList(name);
        } else {
            defineAttribute(name, attList.next);
        }
    }
    
    private String parseTagName(){
        int c;
        String tagName = null;
        
        try {
            while(true){
                c = in.read();
                
                if((c == ' ' && tagName != null) || 
                    (c == '/' && tagName != null)|| 
                            c == '>' || 
                                   c == -1)
                    break;
                else if((c == ' ' && tagName == null) || (c == '/' && tagName == null))
                    continue;
                
                
                if(tagName == null)
                    tagName = new String();
                
                tagName.concat(""+(char)c);
            }
        } catch (Exception e) {
        }
        
        
        return tagName;
        
    }
    /**
     * Add character into parser buffer
     * @param c character to add
     */
    protected void addString(int c){
        if(++strPos > str.length){
            char[] newStr = new char[str.length + 50];
            System.arraycopy(str, 0, newStr, 0, str.length);
            str = newStr;
        }
        str[strPos] = (char)c;
        
    }
    /**
     * Add charaters into buffer
     * @param c 
     */
    protected void addString(int[] c){
        for(int i = 0; i < c.length; i++)
            addString(c[i]);
    }
    
    /**
     * return accumulated caracter. index should start to 0
     * 
     * 
     * @param index index where fetching character chould start
     * @return string with accumulated character starting to <code>
     * index</code>
     * @exception IllegualArgumentException
     */
    protected String getString(int index){
        char[] newChar = new char[(strPos + 1) - index];
        System.arraycopy(str, index, newChar, 0, newChar.length);
        return new String(newChar);
    }
    /**
     * reset buffer which method {@link #addString(int) } encapsul
     */
    protected void resetBuffer(){
        str = new char[50];
        strPos = 0;
    }
    
    /**
     * Skip extra space into parsing document and return the number of 
     * extra space skipped. this method parse file while space it encounter 
     * when non space character is parsed it is not read a return command is 
     * called.
     * @return number of extra space skipped
     */
    protected int skipSpace(){
        int space = -1;
        try {
         while(true){
             space++;
             in.mark(0);
             switch(ch = in.read()){
                 case ' ':
                     break;
                 case '\n':
                     ln++;
                     break;
                 case '\r':
                     break;
                 case '\t':
                     break;
                 default :
                     in.reset();
                     return space;
             }
         }
        } catch (Exception e) {
        }
        
        
        return space;
    }
    
    protected void parseComment(){
        char[] patern = new char[2];
        try {
              while(true){
                  if((ch = in.read()) == '-'){
                      int count = in.read(patern);
                      //if reader read less than 2 character that means
                      //Reader reach En Of file so error should be thrown
                      if(count < 2)
                          error("End Of File");
                      String s = new String(patern);
                      if(s.equals("->")){
                          handleComment(text);
                          break;
                      } else if(s.equals("-!")){
                          if((ch = in.read()) == '>'){
                              handleComment(text);
                              break;
                          }
                      }
                      
                      addString('-');
                      for(int i = 0; i< patern.length; i++){
                          addString(patern[i]);
                      }
                      addString(ch == '-' ? (char)0 : (char)ch);
                      
                  }
                  addString((char)ch);
              } 
            } catch (Exception e) {
            }
        
        
    }
    
    protected void error(String errorMessage){
        
    }
    
    protected void error(String errorMessage1, String errorMessage2){
        
    }
    
    protected void error(String errorLocation, String errorDescriptionn, String errorCorrection){
        
    }
    
    
    private char[] buff = new char[1];
    private int pos;
    private int len;
    protected int readCh() throws IOException{
         if (pos >= len) {

            // This loop allows us to ignore interrupts if the flag
            // says so
            for (;;) {
                try {
                    len = in.read(buff);
                    break;
                } catch (InterruptedIOException ex) {
                    throw ex;
                }
            }

            if (len <= 0) {
                return -1;      // eof
            }
            pos = 0;
        }
        ++currentPosition;

        return buff[pos++];
        
    }
    
    
    /**
     * return index where reader's cursor is on. Note that that current index is not yet 
     * read so only character on index 0 up to {@code getCurrentPos - 1} have already been read.
     * @return cursor's index
     */
    protected int getCurrentPos(){
        return currentPosition;
    }
    
}
