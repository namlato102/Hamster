package de.hsrm.cs.wwwvs.hamster.rpc.client;

import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import de.hsrm.cs.wwwvs.hamster.rpc.HamsterRPCException;
import de.hsrm.cs.wwwvs.hamster.rpc.HamsterRPCException_DatabaseCorrupt;
import de.hsrm.cs.wwwvs.hamster.rpc.HamsterRPCException_Extists;
import de.hsrm.cs.wwwvs.hamster.rpc.HamsterRPCException_NameTooLong;
import de.hsrm.cs.wwwvs.hamster.rpc.HamsterRPCException_NotFound;
import de.hsrm.cs.wwwvs.hamster.rpc.HamsterRPCException_StorageError;
import de.hsrm.cs.wwwvs.hamster.rpc.Hmstr;

public class HamsterRPCConnection implements Hmstr {
	
	private Socket serverSocket;
	private int requestCounter = 0;
	
	public final int headerSize = 8;
	public final int staticStringSize = 32;
	public final byte protocolVersion = (byte) 0xb6;
	public final static byte protocolFlagResponse = (byte) 0x01;
	public final static byte protocolFlagRequest = (byte) 0x00;
	public final static byte protocolFlagError = (byte) 0x02;
	
	public final short HAMSTER_RPC_FUNCID_NEW          = (short) 0x0001;  /**< id for the @see uint32_t hmstr_new() RPC call **/
	public final short HAMSTER_RPC_FUNCID_LOOKUP       = (short) 0x0002; /**< id for the @see int32_t hmstr_lookup() RPC call **/
	public final short HAMSTER_RPC_FUNCID_DIRECTORY    = (short) 0x0003;  /**< id for the @see int32_t hmstr_directory() RPC call **/
	public final short HAMSTER_RPC_FUNCID_HOWSDOING    = (short) 0x0004;  /**< id for the @see int32_t hmstr_howsdoing() RPC call **/
	public final short HAMSTER_RPC_FUNCID_READENTRY    = (short) 0x0005;  /**< id for the @see int32_t hmstr_readentry() RPC call **/
	public final short HAMSTER_RPC_FUNCID_GIVETREATS   = (short) 0x0006;  /**< id for the @see int32_t hmstr_givetreats() RPC call **/
	public final short HAMSTER_RPC_FUNCID_COLLECT      = (short) 0x0007;  /**< id for the @see int32_t hmstr_collect() RPC call **/
	
	public final int HMSTR_ERR_NAMETOOLONG		= -1;
	public final int HMSTR_ERR_EXISTS		 	= -2;
	public final int HMSTR_ERR_NOTFOUND			= -3;
	public final int HMSTR_ERR_STORE			= -100;
	public final int HMSTR_ERR_CORRUPT			= -101;
	
	private InputStream in ;
	private OutputStream out;
	
	private boolean testing = false;
	
	private boolean testNoPayloadAfterMessage = false;

	public HamsterRPCConnection(String hostname, int port) throws UnknownHostException, IOException {

		System.out.println("Connecting to server on port " + port);
		
		this.serverSocket = new Socket(hostname, port);
		
		in = this.serverSocket.getInputStream();
		
		out = this.serverSocket.getOutputStream();
	}
	
	public HamsterRPCConnection(String hostname, int port, boolean testing) throws UnknownHostException, IOException {
		
		// lets create a socket


		System.out.println("Connecting to server on port " + port);
		
		this.serverSocket = new Socket(hostname, port);
		
		in = this.serverSocket.getInputStream();
		
		out = this.serverSocket.getOutputStream();
		
		this.testing = testing;
	}
	
	public void setTestNoPayloadAfterMessage(boolean s) {
		this.testNoPayloadAfterMessage = s;
	}
	
	public void close() throws IOException {
		
		in.close();
		out.close();
		this.serverSocket.close();
		
		in = null;
		out = null;
		serverSocket = null;
	}
	
	
	private void encodeMsg(ByteBuffer buf, byte flags, int msgId, ByteBuffer payload, int rpccallid) {
		
		// header
		buf.put(protocolVersion);
		buf.put(flags);
		buf.putShort((short) msgId);
		buf.putShort((short) payload.array().length);
		buf.putShort((short) rpccallid);
		
		// payload
		buf.put(payload.array());
		
	}
	
	private void checkString(String str)throws HamsterRPCException_NameTooLong {
		if (str.getBytes(Charset.forName(StandardCharsets.US_ASCII.name())).length > staticStringSize-1) {
			throw new HamsterRPCException_NameTooLong();
		}
	}
	
	private byte[] getStaticAscii(String str) {
		
		if (str == null) {
			byte[] ret = new byte[staticStringSize];
			Arrays.fill(ret, (byte) 0);
			return ret;
		}
		
		return Arrays.copyOf(str.getBytes(Charset.forName(StandardCharsets.US_ASCII.name())), this.staticStringSize);
		
	}
	
	private void sendMsg(ByteBuffer msg) throws IOException {
		
		System.out.println("Sending 0x" + bytesToHex(msg) + " (" + msg.array().length + " bytes)");
		
		this.out.write(msg.array());
		this.out.flush();
	}

	private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(ByteBuffer bytes) {
		char[] hexChars = new char[bytes.capacity() * 2];
		for (int j = 0; j < bytes.capacity(); j++) {
			int v = bytes.get(j) & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars);
	}
	
	private ByteBuffer receiveHeader() throws IOException {
		
		ByteBuffer buf  = ByteBuffer.allocate(headerSize);
		
		int receivedByte = 0;
		
		InputStream input = this.serverSocket.getInputStream();
		
		do {
			receivedByte += input.read(buf.array(), receivedByte, buf.array().length - receivedByte);
			
		} while (receivedByte < buf.array().length);
		
		return buf;
	}


	/**
	 * Debug / Test method. Returns one byte (0 ... 256) if readable 
	 * from the input stream
	 * @return Value of one byte or -1 if no data is available 
	 * @throws IOException
	 */
	public int receiveOneByte() throws IOException {
		InputStream input = this.serverSocket.getInputStream();
		if (input.available() > 0) {
			return input.read();
		} else {
			return -1;
		}
		
	}
	

	private void checkResponse(int msgId, short rpcCallId, RPCMessageHeader header, ByteBuffer rpayload) throws HamsterRPCException {
		
		// check if right msg id		
		if (this.testing == false && msgId != header.msgID) {
			System.out.println("Got message with wrong message id. Got " + header.msgID + " expected " + msgId + " ");
			throw new HamsterRPCException("wrong message ID");
		}
		
		// check if right rpc call id		
		if (this.testing == false &&  rpcCallId != header.rpcCallId) {
			System.out.println("Got message response for other rpc call. Got " + header.rpcCallId + " expected " + rpcCallId + " ");
			throw new HamsterRPCException("wrong RPC call ID");
		}
		
		// check if error msg
		if (header.isError) {
			System.out.println("got error msg" );
			decodeErrorMsg(header, rpayload);
		}
		
	}


	private void decodeErrorMsg(RPCMessageHeader header, ByteBuffer rpayload) throws HamsterRPCException_NameTooLong, HamsterRPCException_Extists, HamsterRPCException_NotFound, HamsterRPCException_StorageError {
		
		// error returns one int32 with error code		
		int errorcode = rpayload.getInt();
		
		
		// debug / test
		if (this.testNoPayloadAfterMessage == true) {
			assertSame(0, rpayload.remaining());
		}
		
		switch (errorcode) {
		case (HMSTR_ERR_NAMETOOLONG) :
			throw new HamsterRPCException_NameTooLong();
		case (HMSTR_ERR_EXISTS):
			throw new HamsterRPCException_Extists();
		case (HMSTR_ERR_NOTFOUND):
			throw new HamsterRPCException_NotFound();
		case (HMSTR_ERR_STORE):
			throw new HamsterRPCException_StorageError();
		case (HMSTR_ERR_CORRUPT):
			throw new HamsterRPCException_StorageError();
		}
		
	}


	private ByteBuffer receivePayload(int payloadSize) throws IOException {

		ByteBuffer buf = ByteBuffer.allocate(payloadSize);
		int receivedByte = 0;
		
		InputStream input = this.serverSocket.getInputStream();
		
		while (receivedByte < payloadSize) {
			receivedByte += input.read(buf.array(), receivedByte, payloadSize - receivedByte);
		}
		System.out.println("Received: 0x" + bytesToHex(buf) + " (" + payloadSize + " bytes)");
		return buf;
	}

	@Override
	public int new_(String owner_name, String hamster_name, Integer treats) throws IOException, HamsterRPCException {
		
		checkString(hamster_name);
		checkString(owner_name);
		
		int psize = staticStringSize + staticStringSize + 2;
		
		ByteBuffer p = ByteBuffer.allocate(psize);
		
		
		
		p.put(getStaticAscii(owner_name));
		
		p.put(getStaticAscii(hamster_name));
		
		
		p.putShort((short) treats.intValue());
		
		ByteBuffer buff = ByteBuffer.allocate(psize + headerSize);
		int msgId = this.requestCounter++;
		
		encodeMsg(buff, protocolFlagRequest, msgId, p, HAMSTER_RPC_FUNCID_NEW);
				
		this.sendMsg(buff);
		
		ByteBuffer rheader = receiveHeader();
		RPCMessageHeader header = new RPCMessageHeader(rheader);
		
		ByteBuffer rpayload = receivePayload(header.payloadSize);
		
		checkResponse(msgId, HAMSTER_RPC_FUNCID_NEW, header, rpayload);
		
		long ret = rpayload.getInt();
		
		return (int) ret;		
	}
	

	@Override
	public int directory(Hmstr.HamsterHandle fdptr, String owner_name, String hamster_name)
			throws IOException, HamsterRPCException {
		
		if (owner_name != null) checkString(owner_name);
		if (hamster_name != null) checkString(hamster_name);
		
		int psize = staticStringSize + staticStringSize + 4;
		
		ByteBuffer p = ByteBuffer.allocate(psize);
		
		
		p.putInt(fdptr.fdptr);
		p.put(getStaticAscii(owner_name));
		p.put(getStaticAscii(hamster_name));
		
		
		ByteBuffer buff = ByteBuffer.allocate(psize + headerSize);
		int msgId = this.requestCounter++;
		
		encodeMsg(buff, protocolFlagRequest, msgId, p, HAMSTER_RPC_FUNCID_DIRECTORY);
				
		this.sendMsg(buff);
		
		ByteBuffer rheader = receiveHeader();
		
		RPCMessageHeader header = new RPCMessageHeader(rheader);
		
		ByteBuffer rpayload = receivePayload(header.payloadSize);
		
		checkResponse(msgId, HAMSTER_RPC_FUNCID_DIRECTORY, header, rpayload);
		
		int ret = rpayload.getInt();
		
		
		fdptr.fdptr = rpayload.getInt();
		
		System.out.println("next UID as out parameter " + fdptr.fdptr + " and as return parameter " + ret + " ");
	
		return ret;		
	}

	@Override
	public int howsdoing(Integer ID, State st)
			throws IOException, HamsterRPCException {

		int psize = 4;
		ByteBuffer p = ByteBuffer.allocate(psize);
		p.putInt(ID.intValue());
		
		
		ByteBuffer buff = ByteBuffer.allocate(psize + headerSize);
		int msgId = this.requestCounter++;
		
		encodeMsg(buff, protocolFlagRequest, msgId, p, HAMSTER_RPC_FUNCID_HOWSDOING);
				
		this.sendMsg(buff);
		
		ByteBuffer rheader = receiveHeader();
		RPCMessageHeader header = new RPCMessageHeader(rheader);
		ByteBuffer rpayload = receivePayload(header.payloadSize);
		checkResponse(msgId, HAMSTER_RPC_FUNCID_HOWSDOING, header, rpayload);
		
		
		// unpack response
		int retCode = rpayload.getInt();
		st.treatsLeft = rpayload.getShort();
		st.rounds = rpayload.getInt();
		st.cost = rpayload.getShort();
		
		return retCode;		
	}

	@Override
	public int readentry(Integer ID, HamsterString owner, HamsterString name, HamsterInteger price)
			throws HamsterRPCException_NotFound, HamsterRPCException_StorageError, HamsterRPCException_DatabaseCorrupt, IOException, HamsterRPCException {
		
		int psize = 4;
		
		ByteBuffer p = ByteBuffer.allocate(psize);
		
		
		p.putInt(ID.intValue());
		
		
		ByteBuffer buff = ByteBuffer.allocate(psize + headerSize);
		int msgId = this.requestCounter++;
		
		encodeMsg(buff, protocolFlagRequest, msgId, p, HAMSTER_RPC_FUNCID_READENTRY);
		this.sendMsg(buff);
		
		ByteBuffer rheader = receiveHeader();
		
		RPCMessageHeader header = new RPCMessageHeader(rheader);
		
		ByteBuffer rpayload = receivePayload(header.payloadSize);
		
		checkResponse(msgId, HAMSTER_RPC_FUNCID_READENTRY, header, rpayload);
		
		// unpack response
		int retCode = rpayload.getInt();
		
		byte[] o_str = new byte[staticStringSize];
		byte[] h_str = new byte[staticStringSize];
		
		rpayload.get(o_str);
		rpayload.get(h_str);
		
		owner.str = new String(o_str, StandardCharsets.US_ASCII).trim();
		name.str = new String(h_str, StandardCharsets.US_ASCII).trim();
		
		price.i = rpayload.getShort();
		
		return retCode;
		
	}

	@Override
	public int givetreats(Integer ID, Integer treats)
			throws HamsterRPCException_NotFound, HamsterRPCException_StorageError, HamsterRPCException_DatabaseCorrupt, IOException, HamsterRPCException {

		int psize = 6;
		
		ByteBuffer p = ByteBuffer.allocate(psize);
		
		
		p.putInt(ID.intValue());
		p.putShort((short) treats.intValue());
		
		
		ByteBuffer buff = ByteBuffer.allocate(psize + headerSize);
		int msgId = this.requestCounter++;
		
		encodeMsg(buff, protocolFlagRequest, msgId, p, HAMSTER_RPC_FUNCID_GIVETREATS);
		
		this.sendMsg(buff);		
		ByteBuffer rheader = receiveHeader();		
		RPCMessageHeader header = new RPCMessageHeader(rheader);		
		ByteBuffer rpayload = receivePayload(header.payloadSize);
		
		checkResponse(msgId, HAMSTER_RPC_FUNCID_GIVETREATS, header, rpayload);
		
		// unpack response
		int retCode = rpayload.getInt();
		return retCode;		
	}

	@Override
	public int lookup(String owner_name, String hamster_name) throws HamsterRPCException_NameTooLong,
			HamsterRPCException_NotFound, HamsterRPCException_StorageError, HamsterRPCException_DatabaseCorrupt, IOException, HamsterRPCException {
		
		int psize = staticStringSize + staticStringSize;
		
		ByteBuffer p = ByteBuffer.allocate(psize);
		
		p.put(getStaticAscii(owner_name));
		p.put(getStaticAscii(hamster_name));
		
		
		ByteBuffer buff = ByteBuffer.allocate(psize + headerSize);
		int msgId = this.requestCounter++;
		
		encodeMsg(buff, protocolFlagRequest, msgId, p, HAMSTER_RPC_FUNCID_LOOKUP);
		
		this.sendMsg(buff);
		
		ByteBuffer rheader = receiveHeader();
		
		RPCMessageHeader header = new RPCMessageHeader(rheader);		
		ByteBuffer rpayload = receivePayload(header.payloadSize);		
		checkResponse(msgId, HAMSTER_RPC_FUNCID_LOOKUP, header, rpayload);		
		
		// unpack response		
		int retCode = rpayload.getInt();		
		return retCode;
	}

	@Override
	public int collect(String owner_name)
			throws HamsterRPCException_NotFound, HamsterRPCException_StorageError, HamsterRPCException_DatabaseCorrupt , IOException, HamsterRPCException {
		
		int psize = staticStringSize;
		
		ByteBuffer p = ByteBuffer.allocate(psize);
		
		p.put(getStaticAscii(owner_name));
		
		
		ByteBuffer buff = ByteBuffer.allocate(psize + headerSize);
		int msgId = this.requestCounter++;
		
		encodeMsg(buff, protocolFlagRequest, msgId, p, HAMSTER_RPC_FUNCID_COLLECT);
		
		this.sendMsg(buff);
		
		ByteBuffer rheader = receiveHeader();		
		RPCMessageHeader header = new RPCMessageHeader(rheader);		
		ByteBuffer rpayload = receivePayload(header.payloadSize);		
		checkResponse(msgId, HAMSTER_RPC_FUNCID_COLLECT, header, rpayload);
				
		// unpack response		
		int retCode = rpayload.getInt();		
		return retCode;
	}

}
