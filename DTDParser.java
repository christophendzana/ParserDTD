/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hsegment.JObject.Swing.Text.html.parser;

import java.io.Reader;
import java.util.Collection;
import java.util.Vector;
import javax.swing.text.html.parser.AttributeList;
import javax.swing.text.html.parser.ContentModel;
import javax.swing.text.html.parser.DTD;
import javax.swing.text.html.parser.Element;
import javax.swing.text.html.parser.Entity;

/**
 *this class parse a DTD file and make DTD ready to validate XML file associated 
 * with. this class is subclass of {@link Parser} and the given to constructor 
 * is filled by the DTDParser.
 * @author Ndzana christophe
 */
public class DTDParser extends Parser{
    
    
    public DTDParser(DTD dtd) {
    }
    
    
    public synchronized void parse(Reader in){
        
        try {
            
        int tag = 0;
        Entity entity = null;//if parameter entity englobe an attribut it should be store
        this.in = in;
        StringBuffer buffer = new StringBuffer();
            while((ch = readCh()) != -1){
                switch(ch){
                    case '<':
                        ch = readCh();
                        //skipSpace(); we have to rewrite this one
                        
                        if(ch != '!')
                            error("Error.DTD", "Expected: ! read: "+(char)ch);
                        /**
                        if(tag != 0 || tag != '%')//tag == % if only if string <![%Entity; have been parsed successfully
                            error("Syntax.Error", "Missplaced open tag character");
                        tag = ch;
                        //skipSpace();**/
                        break;
//in this case we have three possibility which are 
 //<!-- : Comment possibility here we should parse comment
 //<!TYPE : TYPE should be egual to : ELEMENT, ATTRIBUTE, ENTITY or ANNOTATION
 //<![%entity;[ : ATTRIBUTE exclusion or ignore possibility; Here if entity is parameter type it can take 
      //two value which are INCLUDE or IGNORE if value is ignore value Attribute should be excluded from DTD 
      // and if value's entity is INCLUDE attribute should be included from DTD
                    /**case '!':
                        if(tag != '<'){
                            error("Error.DTD", "! missed");
                        }
                        tag = 0;
                        int spaceCount = 0;//skipSpace();
                        
                        if(spaceCount != 0)
                            error("Error.DTD", "Extra space");
                        
                        switch(ch = readCh()){
                            case '[' : //in this case we have this type of syntaxe <![
                                skipSpace();
                                if((ch = in.read()) == '%'){ //type of syntaxe <![%
                                    for(;;){
                                        ch = in.read();
                                        if(ch == -1){
                                            error("End Of FIle");
                                            break;
                                        } else if(ch == ';'){
                                            entity = dtd.getEntity(getString(0));
                                            if(entity == null){
                                                error("Error.DTD", "Parameter entity "+getString(0)+" doesn't exist");
                                            }
                                            tag = '%';
                                            break;
                                        } else {
                                            addString(ch);
                                        }
                                    
                                    }
                                    if(!entity.isParameter())
                                        error("Error.DTD", "Entity : "+entity.getName()+" should be egual to INCLUDE or IGNORE");
                                } else {
                                    error("Error.DTD", "Misplaced character : "+(char)ch);
                                }
                                
                                break;
                            case '-' :
                                ch = in.read();
                                skipSpace();
                                if(ch != '-')
                                    error("Error.DTD", "Misplaced character");
                                
                                parseComment();
                                break;
                            default :
                                buffer.append((char)ch);
                                break;
                        }**/
                    case '[' ://this character is read if only Entity had been already verified
                        if(tag != '%')
                            error("Error.DTD", "Missplaced character [");
                        tag = ch;
                        skipSpace();
                        resetBuffer();
                        break;
                    case ' ' :
                        if(!buffer.isEmpty())
                            switch(buffer.toString().toUpperCase()){
                                case "ELEMENT" :
                                    buffer.delete(0, buffer.length());
                                    parseElement();
                                    break;
                                case "ATTLIST" : 
                                    resetBuffer();
                                    boolean b = true;
                                    String param = null;
                                    if(entity != null){
                                        param = new String(entity.data);
                                        b = param.equalsIgnoreCase("INCLUDE");
                                    }
                                    parseAttributeList(b);
                                    break;
                                case "Entity" :
                                    if(entity != null)
                                        error("Error.DTD", "Syntax error");
                                    resetBuffer();
                                    parseEntity();
                                    break;
                                case "NOTATION" :
                                    if(entity != null)
                                        error("Error.DTD", "Syntax error");
                                    parseNotation();
                                    break;
                                default :
                                    error("Error.DTD", "");
                            }
                        break;
                    case ']' : 
                        if(tag != '[' || tag != ']')
                            error("Error.DTD", "missplaced character : "+(char)ch);
                        skipSpace();
                        if((ch = in.read()) != ']')
                            error("Error.DTD", "Misplaced character : "+(char)ch);
                        tag = ch;
                        break;
                    case '>' :
                        if(tag != ']' || tag != '<')
                            error("Error.DTD", "Misplaced character : "+(char)ch);
                        break;
                        
                    default : 
                        buffer.append((char)ch);
                }
                
            }
        } catch (Exception e) {
            error("file is reading");
        }
    }
    
    /**
     * verify if character <code>ch</code> at index <code>index</code> 
     * into XML element name is valid character. example according to XML 
     * rules, name of element can't start with a number like <em>6tagName</em>
     *  is an invalid element's name but <em>tag6name</em> is a valid one that 
     * why character index is important to make validation
     * @param ch element's name character
     * @param index index of character
     * @return true if <code>ch</code> at index <code>index</code> is valid 
     * according to XML rule or return false otherwise
     */
    protected boolean isAvalidElementNameChar(char ch, int index){
        boolean isValidated = false;
        switch(ch){
            case '.' :
            case '-' : 
            case ',' :
                
                isValidated = false;
                break;
            default: 
                if(!Character.isAlphabetic(ch) && index == 0)
                    isValidated = false;
                
            
        }
        
        return isValidated;
    }
    
    /**
     * we have to verify that element's name respect conventions like 
     * an element should start with alphabet, element should not contain 
     * accentued caracter and so one
     * @param element element's name
     * @return element name if it well spelled and return null otherwise
     */
    private String validateElementName(String element){
        
        if(element.equals("ELEMENT"))
            error("Error.Element", "Element is a XML reserved name");
        
        
        for(int i = 0; i < element.length(); i++){
            if(!isAvalidElementNameChar(element.charAt(i), i)){
                error("Error.Element", element+" is not a valid element's name");
                return null;
            }
                
        }
        
        return element;
    }
    
    /**
     * Parse whole Element's name and return it when delimiter is 
     * encounter
     * @return 
     */
    private String parseValueName(){
        String valueName = new String();
        try {
            
            while(true){
                ch = in.read();
                switch(ch){
                    case -1 : 
                        error("En Of File");
                        break;
                    case '(' :    //parser reach here into DTD document  <!ELEMENT elementName (
                    case ',' :    //parser reach here into DTD document  <!ELEMENT elementName (...., childElementName, 
                    case ')':     //parser reach here into DTD document  <!ELEMENT elementName (...., ..., lastElementChild)
                    case ' ' :    //parser reach here into DTD document  <!ELEMENT elementName (..., childElementName 
                        skipSpace();
                        return valueName;
                    default : 
                        valueName = valueName.concat(""+(char)ch);
                        break;
                }
                
            }
                
        } catch (Exception e) {
        }
 
        
        return valueName;
    }
    
    /**
     * 
     */
    protected void parseElement(){
        
        ContentModel cm = null;
        ContentModel bufferContent = cm;
        Element rootElement = null;
        int spacialChar = 0;
        int spaceCount;
        //Identifier can be one of that value (, (, < or >
        int identifier = 0;
        StringBuffer buffer = new StringBuffer();
        try {
            
            spaceCount = 0;//skipSpace();
            
            //if(spaceCount != 0)
               // error("extra Space");
            
            while((ch = readCh()) != -1){
                
                switch(ch){
                    case '('://when parser reach this caracter two case are possible
                        
                        
                        //first case is: parser has already read those caracters '<!ELEMENT rootElement (' so string buffer 
                        //has already accumuluted rootElement's name bufferContent is still null and identifier is still egual to 0
                        if(rootElement == null && !buffer.isEmpty()){
                            rootElement = dtd.defineElement(buffer.toString(), -1, false, false, null, null, null, null);
                            bufferContent = cm = new ContentModel(-1, new HContentModel(rootElement), null);
                            rootElement.content = cm;
                            identifier = '(';
                            
                            //second case is: parser has already read those caracters '<!Element rootElement (content1 | (' so
                            //rootElement has been already created and one or some content model have been already created too
                        } else if(rootElement != null && bufferContent == cm && getString(0).isEmpty()){
                            for(bufferContent = cm; bufferContent == null; bufferContent = bufferContent.next);
                            bufferContent = new ContentModel();
                        }else{
                            error("Error.DTD", "( character is misplaced");
                        }
                        resetBuffer(); identifier = '(';  
                        break;
                    case ')':
                        if(bufferContent != cm && identifier == '(' && !getString(0).isEmpty()){
                            bufferContent = cm;
                        } else if(identifier == ')' && bufferContent == cm && getString(0).isEmpty()){
                            bufferContent = null; 
                        } else {
                            error("Error.DTD", ") character is misplaced");
                        }
                        resetBuffer(); identifier = ')';
                        break;
                    case ' ':
                        if(!buffer.isEmpty() && rootElement == null){
                            rootElement = dtd.defineElement(getString(0), -1, false, false, null, null, null, null);
                        }
                        spaceCount = 0;//skipSpace(); 
                        //if(spaceCount >= 2)
                            //error("Error.DTD", "Extra space");
                        //spacialChar = ch;
                        break;
                    case '>':
                        if(rootElement != null && cm != null  && identifier == ')' && getString(0).isEmpty()){
                            bufferContent = null; 
                        } else if(rootElement != null && identifier == 0 && cm == null && 
                                (getString(0).equalsIgnoreCase("PCDATA") || getString(0).equalsIgnoreCase("EMPTY"))){
                            resetBuffer();
                        } else {
                            error("Error.DTD", "> character is misplaced");
                        }
                        bufferContent = null; identifier = '>';
                        return;
                    case '#' :
                        if((identifier == 0 && (bufferContent = cm) == null && rootElement == null &&
                                !getString(0).toString().isEmpty())) {
                            rootElement = dtd.defineElement(getString(0), HDTDConstants.ANY, false, false, null, null, null, null);
                            
                        } else if(identifier == '(' && bufferContent != null && rootElement != null){
                            rootElement.type = HDTDConstants.ANY;
                        }else {
                            error("Error.DTD", "# character misplaced");
                        }
                        resetBuffer();
                        
                        for(int i = 0; i <7; i++){
                                addString(ch = in.read());
                            }
                        if(!getString(0).equalsIgnoreCase("PCDATA")){
                            error("Error.DTD", "character # should be followed by PCDATA");
                        }
                        break;
                        //cardinality caracter should be apply either to the text (rootElement and PCDATA except) or to content model
                    case '*' :
                    case '+' :
                    case '?' :
                        //if cardinality is applied to text rootElement should not be null that means rootElement have been 
                        //already read
                        if(rootElement != null && identifier == '(' && !getString(0).isEmpty() && 
                                !getString(0).toString().equalsIgnoreCase("PCDATA")){
                           dtd.defineElement(getString(0), ch, false, false, bufferContent, null, null, null);
                           spacialChar = ch;
                           break;
                           
                           //if cardinality is apply to content model 
                        } else if(identifier ==')' && getString(0).isEmpty() && 
                                (bufferContent == cm || (bufferContent == null && cm != null))){
                            for(bufferContent = cm; bufferContent == null; bufferContent = bufferContent.next){
                                if(bufferContent.next == null)
                                    break;
                            }
                            bufferContent.type &= ch;
                            
                            bufferContent = cm;
                        } else {
                            error("Eroor.DTD", (char)ch+" character is misplaced");
                        }
                        identifier = -1;
                        break;
                    case '|' :   
                    case ',' :
                        if(!getString(0).isEmpty() && identifier == '(' 
                                && bufferContent != null && (bufferContent.type == ch || bufferContent.type == 0)){
                            dtd.defineElement(getString(0), 0, false, false, null, null, null, null);
                        } else{
                            error("Error.DTD", Character.toString(ch)+"character is misplaced");
                        }
                        bufferContent.type  = ch;
                        resetBuffer();
                        identifier = -1;
                        break;
                        
                    case -1 : 
                        if(identifier != '>')
                            
                            error("Error.DTD", "End of file");
                        identifier = -1;
                        break;
                    default :
                        /**if(spacialChar == ' '){
                            if(rootElement == null && !getString(0).isEmpty()){
                                dtd.defineElement(getString(0), HDTDConstants.EMPTY, false, true, null, null, null, null);
                                spacialChar = -1; resetBuffer();
                            }
                        }**/
                        buffer.append((char)ch);
                        identifier = -1;
                        break;
                }
            } 
        
        } catch (Exception e) {}
    
    }
    
    private ContentModel get(ContentModel content, ContentModel token){
        for(ContentModel cm = content; cm == token; cm = content.next){
            if(cm == token)
                return cm;
        }
        return null;
    }
    
    
    private void parseAttributeList(boolean include){
        //buffer which will store non specific String like #, ), ( and so else
        StringBuffer attElement = new StringBuffer();
        //element on witch attribute belong
        Element element = null;
        //list of attributes
        AttributeList attList = null;
        
        try {
            while((ch = in.read()) != -1){
            switch(ch){
                
                case '#' :
                    if(attList == null){
                        error("Error.DTD", "bad declaration attribute");
                    }
                    resetBuffer();
                    break;
                    
                case '"' :
                    if(element != null && attList != null && attList.values != null 
                            && !attList.values.isEmpty() && !getString(0).isEmpty()){
                        attList.value = getString(0);
                    } else {
                        error("Error.DTD", "Default value is misplaced");
                    }
                    resetBuffer();
                    break;
                case ' ' :
                    if(element == null && !getString(0).isEmpty()){
                         element = dtd.getElement(getString(0).toString());
                         
                         if(element == null)
                             error("Error.DTD", "Element "+attElement+" does not exist");
                         attList = element.atts;
                         resetBuffer();
                    } else if(element != null && attList == null && !getString(0).isEmpty()){
                        attList = element.atts;
                        for(;;){
                            if(attList != null)
                                attList = attList.next;
                            else 
                                break;
                            
                        }
                        attList = new AttributeList(getString(0).toString());
                        attList.modifier = HDTDConstants.IMPLIED;
                        resetBuffer();
                    } else if(element != null && attList != null && !getString(0).isEmpty()){
                        
                                
                        switch(getString(0).toUpperCase()){
                            case "CDATA":
                                attList.type = HDTDConstants.CDATA;
                                break;
                            case "ID" :
                                attList.type = HDTDConstants.ID;
                                break;
                            case "IDREF" :
                                attList.type = HDTDConstants.IDREF;
                                break;
                            case "IDREFS" :
                                attList.type = HDTDConstants.IDREFS;
                                break;
                            case "NMTOKEN" : 
                                attList.type = HDTDConstants.NMTOKEN;
                                break;
                            case "NMTOKENS" :
                                attList.type = HDTDConstants.NMTOKENS;
                                break;
                            case "REQUIRED" :
                                attList.modifier = HDTDConstants.REQUIRED;
                                break;
                            case "FIXED" :
                                attList.modifier = HDTDConstants.FIXED;
                                break;
                            default :
                                error("Error.DTD", "( character is missed");
                                Vector values = new Vector();
                                fillAttributeValue(values);
                                if(!values.isEmpty())
                                    attList.values = values;
                                attList = null;
                        }
                    }
                    resetBuffer();
                    break;
                case '(' :
                    Vector values = new Vector();
                    fillAttributeValue(values);
                    if(!values.isEmpty())
                        attList.values = values;
                    attList = null;
                    break;
                case '>' :
                    
                    if(element == null || attList == null)
                        error("Error.DTD", "inexpoitable attribute");
                    //this character can be read without read space character like this IMPLIED> so in this case first thing 
                    //to do is to verify if string buffer is not empty in this case try to determine modifier
                    if(!getString(0).isEmpty()){
                        switch(getString(0).toUpperCase()){
                            case "REQUIRED" :
                                attList.modifier = HDTDConstants.REQUIRED;
                                break;
                            case "FIXED" :
                                attList.modifier = HDTDConstants.FIXED;
                            case "IMPLIED" : 
                                attList.modifier = HDTDConstants.IMPLIED;
                                break;
                            default :
                                error("Error.DTD", "Unexpected modifier");
                        }
                    }
                    
                    if(attList.modifier == HDTDConstants.FIXED && attList.value == null){
                        error("Error.DTD", "FIXED attribut should have default attribute");
                    }
                    if(!include) attList = null;//if include is false, this attribute must not be addeed into element
                    break;
                default :
                    addString((char)ch);
            }
            }
        } catch (Exception e) {
        }
        
    }
    
    private void fillAttributeValue(Collection values){
        int specialChar = 0;
        try {
            while((ch = in.read()) != -1){
                switch(ch){
                    case '|' :
                        String buf = getString(0);
                        if(!buf.isEmpty()){
                            values.add(buf);
                            resetBuffer();
                        }else {
                            error("Error.DTD", "Values should be specified after | character");
                        }
                        specialChar = -1;
                        break;
                    case ' ' : 
                        specialChar = ' ';
                        error("Error.DTD", "Extra space");
                        break;
                    case ')' : 
                        if(!getString(0).isEmpty()){
                            values.add(getString(0));
                        }
                        resetBuffer();
                        specialChar = -1;
                        return;
                    case '>' : 
                        String buff = getString(0);
                        if(!buff.isEmpty()){
                            error("Error.DTD", "Default attribut value is missed");
                            
                        } else {
                            error("Error.DTD", "value must follow | character");
                        }
                        values.add(buff);
                        specialChar = -1;
                        return;
                    case '"' : 
                        resetBuffer();
                        specialChar = -1;
                        return;
                    default :
                        //here we test case user write something like this ( value1 value2| that means nothing
                        //because two value must be separated by pipe character
                        if(specialChar == ' ' && !getString(0).isEmpty())
                            error("Error.DTD", "Bad attibute character");
                        addString((char)ch);
                }
            }
        } catch (Exception e) {
        }
    }
    
    private void parseEntity(){
        
        try {
            Entity entity = null;
            int paramEntity = -1;
            while((ch = in.read()) != -1){
            switch(ch){
                case ' ':
                    if(entity == null && !getString(0).isEmpty()){
                        entity = dtd.defEntity(getString(0), paramEntity, -1);
                    } else if(entity != null && entity.type == - 1  && !getString(0).isEmpty()){
                        
                        switch(getString(0).toUpperCase()){
                            case "SYSTEM" : 
                                entity.type = HDTDConstants.SYSTEM;
                                break;
                            case "PUBLIC" : 
                                entity.type = HDTDConstants.PUBLIC;
                                break;
                            default : 
                                error("Error.DTD", "Entity type not recognized");
                        }
                    }
                    resetBuffer();
                    break;
                case '%' : 
                    paramEntity = HDTDConstants.PARAMETER;
                    break;
                case '"' :
                    if(entity != null && entity.type != -1 && !getString(0).isEmpty()){
                        entity.data = getString(0).toCharArray();
                        
                    } 
                    resetBuffer();
                    break;
                case '\'' :
                    if(entity != null && entity.type == HDTDConstants.PARAMETER && getString(0).isEmpty()){
                        switch(getString(0).toUpperCase()){
                            case "INCLUDE" :
                            case "IGNORE" :
                                entity.data = getString(0).toCharArray();
                                resetBuffer();
                                break;
                            default : 
                                error("Error.DTD", "Type parameter entity must be INCLUDE or IGNORE");
                        }
                    }
                default :
                    addString((char)ch);
            }
            }
        } catch (Exception e) {
        }
        
    }
    
    private void parseNotation(){
        
        try {
            while((ch = in.read()) != -1){
                
            
            }
        } catch (Exception e) {
        }
        
    }
    
    
}
