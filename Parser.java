/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.html.parser;


import hsegment.JObject.Swing.Text.CommentHandler;
import hsegment.JObject.Swing.Text.EntityHandler;
import hsegment.JObject.Swing.Text.ErrorHandler;
import hsegment.JObject.Swing.Text.ErrorType;
import hsegment.JObject.Swing.Text.InstructionTagHandler;
import hsegment.JObject.Swing.Text.ParserException.HJAXException;
import hsegment.JObject.Swing.Text.PrologHandler;
import hsegment.JObject.Swing.Text.TagHandler;
import hsegment.JObject.Swing.Text.TextHandler;
import hsegment.JObject.Swing.Text.ValidatorHandler;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Vector;
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
    protected char[] stream;
    protected char[] str;
    
    private Element recent;
    private SimpleAttributeSet attributes;
    private int currentPosition = 0;
    protected Reader in;
    protected int ch = -1;
    private TagStack tagStack;
    //track index off the next character into buffer
    private int strPos = -1;
    
    private int textPos = -1;
    private int ln;
    //
    private int step = -1;
    private EntityHandler entHandler;
    private ErrorHandler errHandler;
    private TagHandler tagHandler;
    private TextHandler texHandler;
    private CommentHandler comHandler;
    private HandlePrologue proHandler;
    private ValidatorHandler doctHandler;
    private InstructionTagHandler instHandler;
    public Parser(){
        
        super(new DefaultDTD("parser"));
        str = new char[128];
        text = new char[10];
        stream = new char[1024];
        tagStack = new TagStack();
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
    
    public void setValidatorHandler(ValidatorHandler doctHandler){
        this.doctHandler = doctHandler;
    }

    public CommentHandler getComHandler() {
        return comHandler;
    }

    public void setProHandler(HandlePrologue proHandler) {
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
    public ValidatorHandler getValidatorHandler(){
        return doctHandler;
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
        if(new String(text).trim().isEmpty())
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
        tagStack.stack(tag);
        try {
            tagHandler.handleStartTag(tag);
        } catch (HJAXException e) {
        }
        
    }
    
    protected void handleInstructionTag(TagElement tag){
        Element element = tag.getElement();
        step++;
        if(element.getName().equalsIgnoreCase("xml") && step != 0){
            error("XML.Error", "Misplaced XML instruction");
        } else if(!element.getName().equalsIgnoreCase("xml") && (step != 0 || step != 3)){
            error("XML.Error", "Instruction tag is Misplaced");
        }
        try {
            instHandler.handleIntruction(tag);
        } catch (Exception e) {
        }
    }
    
    @Override
    protected void handleEndTag(TagElement tag) {
        
        try {
            tagHandler.handleEndTag(tag);
        } catch (HJAXException e) {
            
        }
        
        if(tagStack.pullOut(tag)){
            return;
        } else {
            error("XML.error","Misplaced element "+tag.getElement().getName());
        }
    }
    
    /**
     * Called when an error occured into code. <code>type</code> can have two value either it 
     * is egal to Fatal error in this case parser stop parse document or it is egal to Warning 
     * in this case parser continue to parse document after error declaration but event if 
     * parser declare that error is a warning it's possible to stop parser immidiatly by 
     * throwing HJAXException
     * 
     * 
     * @param src error source
     * @param msg message error
     * @param debug how to debug error
     * @param type error type
     * @throws HJAXException if parser should stop parse
     */
    protected void handleError(String src, String msg, String debug, ErrorType type) throws HJAXException{
        try {
            
            errHandler.errorHandler(src, msg, debug, type);
            
        } catch (NullPointerException e) {
            
        } catch(HJAXException e){
            type = ErrorType.FatalError;
            throw new HJAXException(e.getMessage());
        }finally{
            try {
                if(type == ErrorType.FatalError){
                in.close(); 
                stream = null;
                currentPosition = 0;
            }
            } catch (Exception e) {
            }
        }
    }
    
    
    public synchronized void parse(Reader in){
        this.in = in;
        
        try {
            while ((ch = readCh()) != -1){
            switch(ch){
                case '<' :
                    mark();
                    
                    parseTag();
                    
                    char[] buff = new char[getCurrentPos() - marker];
                    resetStreamCursor(); read(buff);
                    char[] newText = new char[strPos + buff.length + 1];
                    System.arraycopy(str, 0, newText, 0, strPos + 1);
                    System.arraycopy(buff, 0, newText, strPos + 1, buff.length);
                    str = newText; handleText(str); 
                    break;
                default : 
                    //if(tagStack.count() > 0){
                        addString(ch);
                    //} else {
                        //addText(ch);
                    //}
                    
            }
            }
        } catch (Exception e) {
            
        }
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
                c = readCh();
                text.concat(""+(char)c);
            }
        } catch (Exception e) {
            
        }
        
        
        return text;
    }
    
    /**
     * Copy and return characters. start copy to <code>startIndex</code> index and 
     * copy <code>length</code> number of character. Note that IllegalArgument exception 
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
    
    
    private void parseTag() throws Exception{
        TagElement tag;
        Element element = null;
        AttributeList attList = null;
        boolean isClosableTag = false;
        int isInstructionTag = 0;
        StringBuffer buffer = new StringBuffer();
        try {
            
            while((ch = readCh()) != - 1){
                
                switch(ch){
                    case '!': //this is the comment or Doctype charater starter
                        
                        int spaceCount = getCurrentPos();
                        skipSpace(); char[] buff = new char[2];
                        readCh();
                        if((getCurrentPos() - spaceCount) > 2){
                            error("Error.XML", "Misplaced space character", "wipe space","w");
                            
                            read(buff, getCurrentPos() - 2);
                        } else {
                            read(buff, spaceCount);
                        }
                        
                        String strBuf = new String(buff);
                        if(strBuf.equals("--")){
                            parseComment();
                            break;
                        } else {
                            buff = new char[5];
                            for(int i = 0; i< 5; i++)
                                buff[i] = (char)readCh();
                            strBuf = strBuf.concat(new String(buff));
                        } 
                        
                        if(strBuf.equalsIgnoreCase("DOCTYPE")){
                            parseDoctype();
                        } else 
                            error("Error.XML", "Bad tag syntaxe");
                        
                        break;
                    case '?' :
                        if((getString(0).isEmpty() && element == null) || (element != null && getString(0).isEmpty()))
                            isInstructionTag += 1;
                        else 
                            error("XML.Error", "Bad instruction tag syntax");
                        break;
                    case ' ':
                        if(!buffer.isEmpty() && element == null){
                            element = getElement(buffer.toString());
                            buffer = new StringBuffer();
                        } else if(element != null && !buffer.isEmpty() 
                                    && attList != null && attList.value == null){
                            growAttributesValues(attList, buffer.toString());
                            buffer = new StringBuffer();
                        }
                        
                        break;
                    case '=': 
                        if(!getString(0).isEmpty() && element != null){
                            attList = getAttribute(getString(0), element.atts);
                            resetBuffer();
                        } else {
                            error("Error.XML", "bad Tag synstaxe");
                        }
                        break;
                    
                    case '"' : 
                        if(!getString(0).isEmpty() && attList != null){
                            attList.value = getString(0);
                            resetBuffer(); attList = null;
                        } else if(attList == null){
                            error("Error.XML", "value without attribute");
                        }
                    case '>' :
                        if(!buffer.isEmpty() && element == null){
                            element = getElement(buffer.toString());
                            buffer = new StringBuffer();
                        }
                        if(element == null){
                            error("Error.XML", "Missed element name");
                        } else if(!buffer.isEmpty()){
                            error("Error.XML", "Attribute of tag = "+element, "Initialise attribute", "w");
                        }
                        
                        tag = makeTag(element);
                        marker = getCurrentPos();
                        handleText(str); resetBuffer();
                        if(isInstructionTag == 2){
                            handleInstructionTag(tag);
                            return;
                        }else if(isInstructionTag == 1){
                            error("XML.Error", "Bad Instruction Tag");
                            handleInstructionTag(tag);
                            return;
                        }
                        
                        if(!isClosableTag)
                            handleStartTag(tag);
                        else
                            handleEndTag(tag);
                        return;
                    case '<' :
                        char[] text = new char[getCurrentPos() - (marker + 1)];
                        resetStreamCursor(); read(text);
                        addString(text); mark();
                        parseTag();
                        break;
                    case '/' : 
                        isClosableTag = true;
                        break;
                    case '\r' : //carrier return case
                        break;
                    case '\n'://line jump case
                        break;
                    case '\t' ://tabulation case
                        break;
                    case -1:
                        break;
                    default :
                        buffer.append((char)ch);
                        //addString(ch);
                }
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
        
    }
    
    private void parsePrologue(){
        
    }
    
    private void parseInstructionTag(){
        
    }
    
    private void growAttributesValues(AttributeList attList, String value){
        Vector values = new Vector();
        
        for(Object o : attList.values){
            values.add(o);
        }
        
        values.add(value);
        attList.values = values;
    }
    private void parseDoctype(){
        String dtdName = null;
        String locationType = null;
        String dtdFilePath = null;
        StringBuffer buffer = new StringBuffer();
        step++;
        
        try {
            while((ch = readCh()) != - 1){
                switch(ch){
                    case ' ' :
                        if(!buffer.isEmpty() && dtdName == null){
                            dtdName = buffer.toString();
                            buffer.delete(0, buffer.length());
                        } else if(!buffer.isEmpty() && locationType == null){
                            locationType = buffer.toString();
                            buffer.delete(0, buffer.length());
                        }
                        break;
                    case '"' :
                        
                        if(!buffer.isEmpty() && dtdName != null && locationType != null)
                            dtdFilePath = buffer.toString();
                        buffer.delete(0, buffer.length());
                        break;
                    case '[' : 
                        DTDParser dtdParser = null;
                        if(dtdName != null && locationType == null && dtdFilePath == null){
                            dtdParser = new DTDParser(dtd);
                            dtd = new HDTD(dtdName);
                            while((ch = readCh()) != -1){
                                if(ch == ']')
                                    break; 
                                
                                buffer.append((char)ch);
                            }
                            
                            if(ch == -1)
                                error("End Of File");
                            
                        } else {
                            error("Error.XML", "Bad DOCTYPE declaration");
                        }
                        dtdParser.parse(new StringReader(buffer.toString()));
                        buffer.delete(0, buffer.length());
                        break;
                    case '>' :
                        this.doctHandler.handleValidator(dtdName, locationType, dtdFilePath);
                        marker = getCurrentPos();
                        return;
                    default : 
                        buffer.append((char)ch);
                }
            }
        } catch (Exception e) {
        }
        
    }
    
    private Element getElement(String elementName){
        if(elementName == null || elementName.trim().isEmpty())
            throw new IllegalArgumentException("null Element Name");
        return dtd.getElement(elementName);
    }
    private AttributeList getAttribute(String name , AttributeList attList){
        if(attList == null){
            attList = new AttributeList(name);
        } else {
            getAttribute(name, attList.next);
        }
        
        return attList;
    }
    
    private String parseTagName(){
        int c;
        String tagName = null;
        
        try {
            while(true){
                c = readCh();
                
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
        if(++strPos >= str.length){
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
    
    protected void addString(char[] c){
        for(int i = 0; i <c.length; i++){
            addString(c[i]);
        }
    }
    
    protected void addText(int c){
        if(++textPos > text.length){
            char[] newText = new char[text.length + 10];
            System.arraycopy(text, 0, newText, 0, text.length);
            text = newText;
        }
        
        text[textPos] = (char)c;
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
     * Reset buffer which is used while tag is parsing
     */
    protected void resetBuffer(){
        str = new char[50];
        strPos = 0;
    }
    
    protected void resetTextBuffer(){
        text = new char[10];
        textPos = -1;
    }
    
    /**
     * <p>Skip extra space into parsing document and return the number of 
     * extra space skipped. this method parse stream while space character is encountered. 
     * when non space character is parsed it is not read and a return command is 
     * called.
     * @return number of extra space skipped
     */
    protected void skipSpace(){
       
        try {
         while(true){
             switch(ch = readCh()){
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
                     return ;
             }
         }
        } catch (Exception e) {
        }
    }
    
    
    /**
     * Method called when pattern {@literal '<--'} is encountred
     */
    protected void parseComment(){
        char[] pattern = new char[2];
        StringBuffer buffer = new StringBuffer();
        try {
              while(true){
                  if((ch = readCh()) == '-'){
                      ch = readCh();
                      switch(ch){
                          case '-':
                              ch = readCh();
                              if(ch == '>'){
                                  marker = getCurrentPos();
                                  handleComment(buffer.toString().toCharArray());
                                  return;
                              }else{
                                  error("Error.XML", "Bad Comment syntax", "add '>' after '--' character", "w");
                              }
                              buffer = null;
                              return;
                          case '>' : 
                              marker = getCurrentPos();
                              error("Error.XML", "Bad comment end syntax", "add '' before '>'", "w");
                              handleComment(buffer.toString().toCharArray());
                              return;
                          case -1:
                              error("End of File");
                              break;
                              
                      }
                  } else {
                      buffer.append((char)ch);
                  }
                  
              } 
            } catch (Exception e) {
            }
        
        
    }
    
    @Override
    protected void error(String src){
        error(src, null);
    }
    
    @Override
    protected void error(String src, String errorMessage){
        error(src, errorMessage);
    }
    
    @Override
    protected void error(String src, String errorMessage, String debug){
        error(src, errorMessage, debug, "F");
    }
    
    @Override
    protected void error(String scr, String errorMessage, String debug, String errorType) throws HJAXException{
        
        if(errorType == null)
            throw new NullPointerException("errorType cannot be null");
        ErrorType type = !errorType.equalsIgnoreCase("w")? ErrorType.FatalError : ErrorType.Warning;
        
        try {
            
            handleError(scr, errorMessage, debug, type);
        } catch (HJAXException e) {
            throw new HJAXException(e.getMessage());
        }
        if(type == ErrorType.FatalError)
            throw new HJAXException(errorMessage);
    }
    
    
    private char[] buff = new char[1];
    private int pos;
    private int len;
    private int marker = -1;
    
    /**
     * Read and return character or -1 if end of a stream is reached
     * @return
     * @throws IOException 
     */
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
         growStreamIfNecessary();
        ++currentPosition;

        return stream[currentPosition - 1] = buff[pos++];
        
    }
    
    private void growStreamIfNecessary(){
        if(currentPosition > stream.length){
            char[] newStream = new char[stream.length + 1024];
            System.arraycopy(stream, 0, newStream, 0, stream.length);
            stream = newStream;
        }
    }
    
    /**
     * Mark specific position index into stream to when you call <code>reset</code> method, 
     * cursor should be egal to that specific position and the stream reading should start 
     * at that specific marked index.
     * @see #resetStream()  
     */
    protected void mark(){
        marker = getCurrentPos() - 1;
    }
    
    /**
     * Reset stream cursor to Marked Index.
     */
    protected void resetStreamCursor(){
        currentPosition = marker;
    }
    
    /**
     * Read caracter into buffer <code>buff</code> and return the number of 
     * character which have been read.
     * 
     * 
     * @param buff buffer into which characters must be read;
     * @return number of character which have been read
     * @throws IOException
     */
    protected int read(char[] buff) throws IOException{
        
        if(buff.length == 0)
            return -1;
        
        
        int readCount = getCurrentPos();
        for(int i = 0; i< buff.length; i++){
            try {
                buff[i] = (char)stream[currentPosition++];
            } catch (Exception e) {
                
            }
         
        }
        
        return getCurrentPos() - readCount;
    }
    
    protected void read(char[] buff, int offSet){
        
        if(buff.length == 0)
            return;
        for(int i = 0; i < buff.length; i++){
            buff[i] = (char)stream[Math.min(offSet + i, getCurrentPos())];
        }
    }
    
    
    /**
     * return index where reader's cursor is on. Note that, that current index is not yet 
     * read so only character on index 0 up to {@code getCurrentPos - 1} have already been read.
     * @return cursor's index
     */
    protected int getCurrentPos(){
        return currentPosition;
    }
    
}
