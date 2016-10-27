<<<<<<< HEAD
package packets;

public enum ErrorType {
	NOT_DEFINED((byte)0),
	FILE_NOT_FOUND((byte)1),
	ACCESS_VIOLATION((byte)2),
	ALLOCATION_EXCEEDED((byte)3),
	ILLEGAL_TFTP_OPERATION((byte)4),
	UNKNOWN_TRANSFER_ID((byte)5),
	FILE_ALREADY_EXISTS((byte)6),
	NO_SUCH_USER((byte)7);
	
	private final byte code;
	
	private ErrorType(byte code){
		this.code = code;
	}

	/**
	 * returns the ErrorType with the code value code,
	 * of null if there is no match. 
	 */
	public static ErrorType fromCode(byte code){
		for(ErrorType e : ErrorType.values()){
			if(e.code == code){
				return e;
			}
		}
		return null;
	}
	
	public byte getOpcode() {
		return code;
	}
}
=======
package packets;

public enum ErrorType {
	NOT_DEFINED((byte)0),
	FILE_NOT_FOUND((byte)1),
	ACCESS_VIOLATION((byte)2),
	ALLOCATION_EXCEEDED((byte)3),
	ILLEGAL_TFTP_OPERATION((byte)4),
	UNKNOWN_TRANSFER_ID((byte)5),
	FILE_ALREADY_EXISTS((byte)6),
	NO_SUCH_USER((byte)7);
	
	private final byte code;
	
	private ErrorType(byte code){
		this.code = code;
	}

	/**
	 * returns the ErrorType with the code value code,
	 * of null if there is no match. 
	 */
	public static ErrorType fromCode(byte code){
		for(ErrorType e : ErrorType.values()){
			if(e.code == code){
				return e;
			}
		}
		return null;
	}
	
	public byte getOpcode() {
		return code;
	}
}
>>>>>>> 9e1e11a572111a2572274ab61298e6048e5c7c7c
