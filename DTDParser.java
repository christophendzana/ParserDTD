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
import javax.swing.text.html.parser.Element;

/**
 *this class parse a DTD file and make DTD ready to validate XML file associated 
 * with. this class is subclass of {@link Parser} and the given to constructor 
 * is filled by the DTDParser.
 * @author Ndzana christophe
 */
public class DTDParser extends Parser{
    
    
    public DTDParser(HDTD dtd) {
        super(dtd);
    }
    
    
    public void parse(Reader in){
        
        try {
            
            
        this.ch = in.read();
        String name = null;
        String attributeName = null;
        Element element = null;
        int tag = 0;
        
            while(this.in.ready()){
                switch(ch){
                    case '<':
                        if(tag == '<')
                            error("Syntax.Error", "Tag should be closed");
                        tag = ch;
                        ch = in.read();
                        skipSpace();
                        break;
                    case '!':
                        if(tag != '<'){
                            error("Syntax.Instruction", "< missed");
                        }
                        int spaceCount = skipSpace();
                        
                        if(spaceCount != 0)
                            warning("extra space");
                        // we can have <!-- characters which correspond to comment 
                        char[] chs = new char[2];
                        //we read next character to see if they maching with 
                        //comment character
                        int len = in.read(chs); 
                        
                        //we assume that if reader can't read two characters it means we 
                        //reached end of file, so error should be thrown
                        if(len == 2){
                            name = new String(chs); 
                        } else {
                            error("End of File");
                        } 
                        //then verify if its correspond to comment caracters
                        if(name.equals("--")){//pattern <!-- have been parsed
                            parseComment();
                            ch = in.read();
                            break;
                        }
                        //now let's determine what we are parsing for (ELEMENT, ATTRIBUTE or ENTITY)
                        while((ch = in.read()) != ' '){
                            if(name.length() > 10)
                                /**name should contain ELEMENT, ATTRIBUTE or ENTITY which correspond 
                                 * to 9 characters at the max so over this length we assume that it is
                                 * an error for avoiding unnacessary loop
                                 */ 
                                 error("Syntax.Error", "inexpected loop");
                            
                                //occur when parser reach the last character of this file
                            if(ch == -1){
                                error("End Of File");
                            }
                            name = name.concat(""+(char)ch);
                        }
                        
                       if(name.equals("ELEMENT")){
                           parseElement();
                       } else if(name.equals("ATTLIST")){
                           parseAttributeList();
                       } else if(name.equals("ENTITY")){
                           parseEntity();
                       } else {
                           error("DTD parse Error");
                       }
                    default : ;     
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
        StringBuffer elementName = new StringBuffer(30);
        int spacialChar = 0;
        int spaceCount;
        //Identifier can be one of that value (, (, < or >
        int identifier = 0;
        
        try {
            
            spaceCount = skipSpace();
            
            if(spaceCount != 0)
                warning("extra Space");
            
            while((ch = in.read()) != -1){
                
                switch(ch){
                    case '(':
                        
                        if(rootElement == null && identifier == 0 && bufferContent == null &&  
                                elementName != null && !elementName.isEmpty()){
                            dtd.addElement(elementName.toString());
                            rootElement = dtd.getElement(elementName.toString());
                            bufferContent = cm = new ContentModel(rootElement);
                            identifier = '(';
                        } else if(rootElement != null && bufferContent == cm){
                            for(bufferContent = cm; bufferContent == null; bufferContent = bufferContent.next);
                            bufferContent = new ContentModel();
                        }else{
                            error("Error.DTD", "( character is misplaced");
                        }
                        elementName = null; identifier = '(';  
                        break;
                    case ')':
                        if(bufferContent != cm && identifier == '(' && elementName != null && !elementName.isEmpty()){
                            bufferContent = cm;
                        } else if(identifier == ')' && bufferContent == cm && elementName != null && !elementName.isEmpty()){
                            bufferContent = null; 
                        } else {
                            error("Error.DTD", ") character is misplaced");
                        }
                        elementName = null; identifier = ')';
                        break;
                    case ' ':
                        
                        spaceCount = skipSpace(); 
                        if(spaceCount >= 2)
                            error("Error.DTD", "Extra space");
                        break;
                    case '>':
                        if(bufferContent == cm && identifier == ')' && elementName == null){
                            bufferContent = null; 
                        } else if(bufferContent == cm && identifier == 0 && 
                                elementName != null && elementName.toString().equalsIgnoreCase("PCDATA")){
                            elementName = null;
                        } else {
                            error("Error.DTD", "> character is misplaced");
                        }
                        bufferContent = null; identifier = '>';
                        return;
                    case '#' :
                        if(identifier == 0 && (bufferContent = cm) == null && rootElement == null &&
                                !elementName.toString().isEmpty()) {
                            dtd.addElement(elementName.toString());
                            rootElement = dtd.getElement(elementName.toString());
                            
                            bufferContent = cm = new ContentModel(rootElement);
                            
                        } else if(identifier == '(' && bufferContent != null){
                            
                        }else {
                            error("Error.DTD", "# character misplaced");
                        }
                        elementName = new StringBuffer();
                        
                        for(int i = 0; i <7; i++){
                                elementName = elementName.append((char)in.read());
                            }
                        if(!elementName.toString().equalsIgnoreCase("PCDATA")){
                            error("Error.DTD", "character # should be followed by PCDATA");
                        }
                        dtd.addElement(elementName.toString()); 
                        
                        break;
                    case '*' :
                    case '+' :
                    case '?' :
                        if(identifier == '(' && elementName != null && 
                                  !elementName.isEmpty() && !elementName.toString().equalsIgnoreCase("PCDATA")){
                           dtd.addElement(elementName.toString());
                           Element element = dtd.getElement(elementName.toString());
                           element.type = ch; spacialChar = ch;
                           break;
                        } else if(identifier ==')' && elementName == null && bufferContent == cm){
                            for(bufferContent = cm; bufferContent == null; bufferContent = bufferContent.next){
                                if(bufferContent.next == null)
                                    break;
                            }
                            
                            
                            if(bufferContent.type == '|'){
                                switch(ch){
                                    case '*':
                                        bufferContent.type = HDTDConstants.PIPE_STAR_CARDINALITY;
                                        break;
                                    case '+': 
                                        bufferContent.type = HDTDConstants.PIPE_PLUS_CARDINALITY;
                                        break;
                                    case '?': 
                                        bufferContent.type = HDTDConstants.PIPE_ONCE_CARDINALITY;
                                }
                            } else if(bufferContent.type == ','){
                                
                                switch(ch){
                                    case '*':
                                        bufferContent.type = HDTDConstants.SEQ_STAR_CARDINALITY;
                                        break;
                                    case '+': 
                                        bufferContent.type = HDTDConstants.SEQ_PLUS_CARDINALITY;
                                        break;
                                    case '?': 
                                        bufferContent.type = HDTDConstants.SEQ_ONCE_CARDINALITY;
                                }
                            }
                            
                            bufferContent = cm;
                        } else {
                            error("Eroor.DTD", (char)ch+" character is misplaced");
                        }
                        
                        break;
                    case '|' :
                        if(elementName != null && !elementName.toString().isEmpty() && identifier == '(' 
                                && bufferContent != null && bufferContent != cm){
                            dtd.addElement(elementName.toString());
                        } else{
                            error("Error.DTD", "| character is misplaced");
                        }
                        bufferContent.type  = '|';
                        elementName = new StringBuffer();
                        break;
                        
                    case ',' :
                        if(elementName != null && !elementName.toString().isEmpty() && identifier == '(' 
                                && bufferContent != null && bufferContent != cm){
                            dtd.addElement(elementName.toString());
                        } else{
                            error("Error.DTD", "| character is misplaced");
                        }
                        bufferContent.type  = ',';
                        elementName = new StringBuffer();
                        break;
                        
                    case -1 : 
                        if(identifier != '>')
                            
                            error("Error.DTD", "End of file");
                        break;
                    default :
                        if(identifier == ')' && elementName == null){
                            error("Error.DTD", "misplaced element");
                        } else 
                        if(spacialChar != 0 && elementName.length() != 0)
                            error("Error.DTD", "Misplaced element", 
                                          "Element must not directly follow "+(char)spacialChar);
                        else if (elementName == null){
                            elementName = new StringBuffer();
                        }
                        
                        if(isAvalidElementNameChar((char)ch, elementName.length())){
                            elementName.append((char)ch);
                        } else {
                            warning("Invalid element Name");
                            elementName.append((char)ch);
                        }
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
    
    
    private void parseAttributeList(){
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
                    break;
                case ' ' :
                    if(element == null && !attElement.isEmpty()){
                         element = dtd.getElement(attElement.toString());
                         
                         if(element == null)
                             error("Error.DTD", "Element "+attElement+" does not exist");
                         attList = element.atts;
                         attElement = new StringBuffer();
                    } else if(element != null && attList == null && !attElement.isEmpty()){
                        attList = element.atts;
                        for(;;){
                            if(attList != null)
                                attList = attList.next;
                            else 
                                break;
                            
                        }
                        attList = new AttributeList(attElement.toString());
                        attElement = new StringBuffer();
                    } else if(element != null && attList != null && !attElement.isEmpty()){
                        
                                
                        switch(attElement.toString().toLowerCase()){
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
                            default :
                                error("Error.DTD", "( character is missed");
                                Vector values = new Vector();
                                values.add(attElement);
                                fillAttributeValue(values);
                                if(!values.isEmpty())
                                    attList.values = values;
                                attList = null;
                        }
                    }
                    break;
                case '(' :
                    Vector values = new Vector();
                    fillAttributeValue(values);
                    if(!values.isEmpty())
                        attList.values = values;
                    attList = null;
                    break;
                default :
                    attElement.append((char)ch);
            }
            }
        } catch (Exception e) {
        }
        
    }
    
    private void fillAttributeValue(Collection values){
        StringBuffer attValue = new StringBuffer();
        int specialChar = 0;
        try {
            while((ch = in.read()) != -1){
                switch(ch){
                    case '|' :
                        if(!values.isEmpty() && !attValue.isEmpty()){
                            values.add(attValue);
                            attValue.delete(0, attValue.length());
                        }else {
                            error("Error.DTD", "Values should be specified after | character");
                        }
                        break;
                    case ' ' : 
                        specialChar = ' ';
                        error("Error.DTD", "Extra space");
                        break;
                    case ')' : 
                        attValue = null;
                        return;
                    case '>' : 
                        if(!attValue.isEmpty()){
                            error("Error.DTD", "Default attribut value is missed");
                            
                        } else {
                            error("Error.DTD", "value must follow | character");
                        }
                        values.add(attValue);
                        return;
                    case '"' : 
                        attValue = null;
                        return;
                    default :
                        if(specialChar == ' ' && !attValue.isEmpty())
                            error("Error.DTD", "Bad attibute character");
                        attValue.append((char)ch);
                }
            }
        } catch (Exception e) {
        }
    }
    
    private void parseEntity(){
        
    }
    
    
}
