
package tftp;

/**
 * FileType enum used for the file mode type
 * @author Team 17
 */
public enum FileType {
	OCTET,NETASCII;
	
	public static FileType fromString(String str){
		if(OCTET.name().equalsIgnoreCase(str)){
			return OCTET;
		}else if(NETASCII.name().equalsIgnoreCase(str)){
			return NETASCII;
		}else{
			throw new IllegalArgumentException(str+" is not a valid file type");
		}
	}
}

