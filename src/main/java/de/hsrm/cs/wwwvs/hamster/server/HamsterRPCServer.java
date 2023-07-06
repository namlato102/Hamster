package de.hsrm.cs.wwwvs.hamster.server;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

import de.hsrm.cs.wwwvs.hamster.HamsterRPCException;
import de.hsrm.cs.wwwvs.hamster.HamsterRPCException_DatabaseCorrupt;
import de.hsrm.cs.wwwvs.hamster.HamsterRPCException_Extists;
import de.hsrm.cs.wwwvs.hamster.HamsterRPCException_NameTooLong;
import de.hsrm.cs.wwwvs.hamster.HamsterRPCException_NotFound;
import de.hsrm.cs.wwwvs.hamster.HamsterRPCException_StorageError;
import de.hsrm.cs.wwwvs.hamster.Hmstr;
import de.hsrm.cs.wwwvs.hamster.lib.*;

import static org.junit.Assert.assertSame;


public class HamsterRPCServer implements Hmstr {

	private ServerSocket server;
	private Socket socket;
	public final int maxClients = 10;
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

	private final HamsterLib lib = new HamsterLib();
	private HamsterIterator it;
	private int handle;

	private InputStream in;
	private OutputStream out;
	
	private boolean testing = false;
	
	private boolean testNoPayloadAfterMessage = false;

	public HamsterRPCServer(String hostname, int port) throws UnknownHostException, IOException {
		System.out.println("Server started on port " + port);
		System.out.println("Waiting for a client ...");

		this.server = new ServerSocket(port, maxClients, InetAddress.getByName(hostname));

		socket = server.accept();

		while (true) {
			try
			{
				if (socket.isClosed()) {
					socket = server.accept();
				}

				System.out.println("Client accepted");

//				it = lib.iterator(); !!! if at all, init outside of the loop
//				handle = new HamsterHandle(); !!! if at all, init outside of the loop. also it is an int now

				in = this.socket.getInputStream();

				ByteBuffer receivedHeader = this.receiveHeader();

				out = this.socket.getOutputStream();

				processClientRequests(receivedHeader);
			}
			catch(IOException e) {
				System.err.println("Server exception: " + e.getMessage());
				System.out.println("Waiting for a client ...");
				in.close();
				socket.close();
			} catch (HamsterRPCException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public HamsterRPCServer(String hostname, int port, boolean testing) throws UnknownHostException, IOException {
		System.out.println("Server started on port " + port);
		System.out.println("Waiting for a client ...");

		this.server = new ServerSocket(port, maxClients, InetAddress.getByName(hostname));
		this.testing = testing;

		socket = server.accept();

		while (true) {
			try
			{
				if (socket.isClosed()) {
					socket = server.accept();
				}

				System.out.println("Client accepted");

				in = this.socket.getInputStream();

				ByteBuffer receivedHeader = this.receiveHeader();

				out = this.socket.getOutputStream();

				processClientRequests(receivedHeader);
			}
			catch(IOException e) {
				System.err.println("Server exception: " + e.getMessage());
				System.out.println("Waiting for a client ...");
				in.close();
				socket.close();
			} catch (HamsterRPCException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void setTestNoPayloadAfterMessage(boolean s) {
		this.testNoPayloadAfterMessage = s;
	}
	
	public void close() throws IOException {

		in.close();
		out.close();
		this.server.close();

		in = null;
		out = null;
		server = null;
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
		
		InputStream input = this.socket.getInputStream();
		
		do {
			receivedByte += input.read(buf.array(), receivedByte, buf.array().length - receivedByte);
			
		} while (receivedByte < buf.array().length);
		System.out.println(
				"Received header: 0x"
						+ bytesToHex(buf)
						+ " (" + buf.array().length + " bytes)"
		);
		return buf;
	}


	/**
	 * Debug / Test method. Returns one byte (0 ... 256) if readable 
	 * from the input stream
	 * @return Value of one byte or -1 if no data is available 
	 * @throws IOException
	 */
	public int receiveOneByte() throws IOException {
		InputStream input = this.socket.getInputStream();
		if (input.available() > 0) {
			return input.read();
		} else {
			return -1;
		}
		
	}
	

	private void checkRequest(int msgId, short rpcCallId, RPCMessageHeader header, ByteBuffer rpayload) throws HamsterRPCException {
		
		// check if right msg id		
		if (this.testing == false && msgId != header.msgID) {
			System.out.println("Got message with wrong message id. Got " + header.msgID + " expected " + msgId + " ");
			throw new HamsterRPCException("wrong message ID");
		}
		
		// check if right rpc call id		
		if (this.testing == false &&  rpcCallId != header.rpcCallId) {
			System.out.println("Got message request for other rpc call. Got " + header.rpcCallId + " expected " + rpcCallId + " ");
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
		
		InputStream input = this.socket.getInputStream();
		
		while (receivedByte < payloadSize) {
			receivedByte += input.read(buf.array(), receivedByte, payloadSize - receivedByte);
		}
		System.out.println("Received payload: 0x" + bytesToHex(buf) + " (" + payloadSize + " bytes)");
		return buf;
	}

	/**
	 * Server handler. Includes functions that process client requests
 	 * @param receivedHeader
	 * @return
	 * @throws IOException
	 * @throws HamsterRPCException
	 */
	public void processClientRequests(ByteBuffer receivedHeader) throws IOException, HamsterRPCException {

		byte version = receivedHeader.get(0);
		byte flags = receivedHeader.get(1);
		short msg_id = receivedHeader.getShort(2);
		short payload_length = receivedHeader.getShort(4);
		short rpc_call_id = receivedHeader.getShort(6);

		RPCMessageHeader header = new RPCMessageHeader(receivedHeader);
		ByteBuffer receivedPayload = this.receivePayload(payload_length);
		checkRequest(msg_id, rpc_call_id, header, receivedPayload);

		switch (rpc_call_id) {
			case HAMSTER_RPC_FUNCID_NEW -> { // new_ Funktion
				System.out.println("new");
				byte[] owner_name_buf = new byte[staticStringSize];
				byte[] hamster_name_buf = new byte[staticStringSize];

				receivedPayload.get(owner_name_buf);
				receivedPayload.get(hamster_name_buf);
				short treats = receivedPayload.getShort();
				String owner_name = new String(owner_name_buf, StandardCharsets.US_ASCII).trim();
				String hamster_name = new String(hamster_name_buf, StandardCharsets.US_ASCII).trim();
				try {
					int ret = lib.new_(owner_name, hamster_name, treats); // Return value (from function)
					ByteBuffer res = ByteBuffer.allocate(4 + headerSize); // Response to client
					res.put(0, version); // Byte 0 from header: version of payload/header
					res.put(1, protocolFlagResponse); // Byte 1: flag (response/request/error)
					res.putShort(2, msg_id); // Byte 2: message ID
					res.putShort(4, (short) 4); // Byte 4: length of int 4 bytes
					res.putShort(6, rpc_call_id); // Byte 6: RPC Call ID
					res.putInt(8, ret); // Byte 8: Return value (from new_ function)
					sendMsg(res);

				} catch (HamsterNameTooLongException e) {
					ByteBuffer res = ByteBuffer.allocate(4 + headerSize);
					res.put(0, version);
					res.put(1, (byte) (protocolFlagResponse | protocolFlagError));
					res.putShort(2, msg_id);
					res.putShort(4, (short) 4);
					res.putShort(6, rpc_call_id);
					res.putInt(8, HMSTR_ERR_NAMETOOLONG);
					sendMsg(res);
				} catch (HamsterStorageException | HamsterAlreadyExistsException | HamsterDatabaseCorruptException e) {
					throw new RuntimeException(e);
				}
			}
			case HAMSTER_RPC_FUNCID_COLLECT -> {
				System.out.println("Bill");
				byte[] owner_name_buf = new byte[staticStringSize];

				receivedPayload.get(owner_name_buf);

				String owner_name = new String(owner_name_buf, StandardCharsets.US_ASCII).trim();

				try {
					int ret = lib.collect(owner_name); // Return value (from function)
					ByteBuffer res = ByteBuffer.allocate(4 + headerSize); // Response to client
					res.put(0, version); // Byte 0 from header: version of payload/header
					res.put(1, protocolFlagResponse); // Byte 1: flag (response/request/error)
					res.putShort(2, msg_id); // Byte 2: message ID
					res.putShort(4, (short) 4); // Byte 4: length of int 4 bytes
					res.putShort(6, rpc_call_id); // Byte 6: RPC Call ID
					res.putInt(8, ret); // Byte 8: Return value (from collect function)
					sendMsg(res);

				} catch (HamsterNameTooLongException e) {
					ByteBuffer res = ByteBuffer.allocate(4 + headerSize);
					res.put(0, version);
					res.put(1, (byte) (protocolFlagResponse | protocolFlagError));
					res.putShort(2, msg_id);
					res.putShort(4, (short) 4);
					res.putShort(6, rpc_call_id);
					res.putInt(8, HMSTR_ERR_NAMETOOLONG);
					sendMsg(res);
				} catch (HamsterStorageException | HamsterNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
			case HAMSTER_RPC_FUNCID_DIRECTORY -> {
				System.out.println("Dir");
				int fdptr = receivedPayload.getInt();
				if (handle != fdptr || it == null) {
					it = lib.iterator();
					handle++;
				}
				byte[] owner_name_buf = new byte[staticStringSize];
				byte[] hamster_name_buf = new byte[staticStringSize];

				receivedPayload.get(owner_name_buf);
				receivedPayload.get(hamster_name_buf);
				String owner_name = new String(owner_name_buf, StandardCharsets.US_ASCII).trim();
				String hamster_name = new String(hamster_name_buf, StandardCharsets.US_ASCII).trim();

				try {
					int ret = lib.directory(it, (owner_name.length() == 0 ? null : owner_name), (hamster_name.length() == 0 ? null : hamster_name)); // Return value (from function)
					ByteBuffer res = ByteBuffer.allocate(4 + 4 + headerSize); // Response to client
					res.put(0, version); // Byte 0 from header: version of payload/header
					res.put(1, protocolFlagResponse); // Byte 1: flag (response/request/error)
					res.putShort(2, msg_id); // Byte 2: message ID
					res.putShort(4, (short) 8); // Byte 4: length of int 8 bytes
					res.putShort(6, rpc_call_id); // Byte 6: RPC Call ID
					res.putInt(8, ret); // Byte 8: Return value (from directory function)
					res.putInt(12, handle); // Byte 12: Iterator Handle
					sendMsg(res);
				} catch (HamsterNameTooLongException e) {
					ByteBuffer res = ByteBuffer.allocate(4 + headerSize);
					res.put(0, version);
					res.put(1, (byte) (protocolFlagResponse | protocolFlagError));
					res.putShort(2, msg_id);
					res.putShort(4, (short) 4);
					res.putShort(6, rpc_call_id);
					res.putInt(8, HMSTR_ERR_NAMETOOLONG);
					sendMsg(res);
				} catch (HamsterNotFoundException | HamsterEndOfDirectoryException e) {
					ByteBuffer res = ByteBuffer.allocate(4 + headerSize);
					res.put(0, version);
					res.put(1, (byte) (protocolFlagResponse | protocolFlagError));
					res.putShort(2, msg_id);
					res.putShort(4, (short) 4);
					res.putShort(6, rpc_call_id);
					res.putInt(8, HMSTR_ERR_NOTFOUND);
					sendMsg(res);
				}
			}
			case HAMSTER_RPC_FUNCID_LOOKUP -> {
				System.out.println("Lookup");
				byte[] owner_name_buf = new byte[staticStringSize];
				byte[] hamster_name_buf = new byte[staticStringSize];

				receivedPayload.get(owner_name_buf);
				receivedPayload.get(hamster_name_buf);
				String owner_name = new String(owner_name_buf, StandardCharsets.US_ASCII).trim();
				String hamster_name = new String(hamster_name_buf, StandardCharsets.US_ASCII).trim();
				try {
					int ret = lib.lookup(owner_name, hamster_name); // Return value (from function)
					ByteBuffer res = ByteBuffer.allocate(4 + headerSize); // Response to client
					res.put(0, version); // Byte 0 from header: version of payload/header
					res.put(1, protocolFlagResponse); // Byte 1: flag (response/request/error)
					res.putShort(2, msg_id); // Byte 2: message ID
					res.putShort(4, (short) 4); // Byte 4: length of int 4 bytes
					res.putShort(6, rpc_call_id); // Byte 6: RPC Call ID
					res.putInt(8, ret); // Byte 8: Return value (from lookup function)
					sendMsg(res);

				} catch (HamsterNameTooLongException e) {
					ByteBuffer res = ByteBuffer.allocate(4 + headerSize);
					res.put(0, version);
					res.put(1, (byte) (protocolFlagResponse | protocolFlagError));
					res.putShort(2, msg_id);
					res.putShort(4, (short) 4);
					res.putShort(6, rpc_call_id);
					res.putInt(8, HMSTR_ERR_NAMETOOLONG);
					sendMsg(res);
				} catch (HamsterNotFoundException e) {
					ByteBuffer res = ByteBuffer.allocate(4 + headerSize);
					res.put(0, version);
					res.put(1, (byte) (protocolFlagResponse | protocolFlagError));
					res.putShort(2, msg_id);
					res.putShort(4, (short) 4);
					res.putShort(6, rpc_call_id);
					res.putInt(8, HMSTR_ERR_NOTFOUND);
					sendMsg(res);
				}
			}
			case HAMSTER_RPC_FUNCID_READENTRY -> {
				System.out.println("Readentry");
				int hamster_ID = receivedPayload.getInt();
				HamsterLib.OutString outOwnerName = lib.new OutString();
				HamsterLib.OutString outHamsterName = lib.new OutString();
				HamsterLib.OutShort outPrice = lib.new OutShort();

				if (payload_length > 4) {
					byte[] owner_name_buf = new byte[staticStringSize];
					byte[] hamster_name_buf = new byte[staticStringSize];

					receivedPayload.get(owner_name_buf);
					receivedPayload.get(hamster_name_buf);
					String owner_name = new String(owner_name_buf, StandardCharsets.US_ASCII).trim();
					String hamster_name = new String(hamster_name_buf, StandardCharsets.US_ASCII).trim();

					short hamster_price = receivedPayload.getShort();
					outOwnerName.setValue(owner_name);
					outHamsterName.setValue(hamster_name);
					outPrice.setValue(hamster_price);
				}
				try {
					int ret = lib.readentry(hamster_ID, outOwnerName, outHamsterName, outPrice); // Return value (from function)
					ByteBuffer res = ByteBuffer.allocate(4 + staticStringSize + staticStringSize + 2 + headerSize); // Response to client
					res.put(0, version); // Byte 0 from header: version of payload/header
					res.put(1, protocolFlagResponse); // Byte 1: flag (response/request/error)
					res.putShort(2, msg_id); // Byte 2: message ID
					res.putShort(4, (short) (4 + staticStringSize + staticStringSize + 2)); // Byte 4: length of buffer
					res.putShort(6, rpc_call_id); // Byte 6: RPC Call ID
					res.putInt(8, ret); // Byte 8: Return value (from readEntry function)
					res.put(12, getStaticAscii(outOwnerName.getValue())); // Byte 12: Ownername
					res.put(12+staticStringSize, getStaticAscii(outHamsterName.getValue())); // Byte 12+32: Hamstername
					res.putShort(12+staticStringSize+staticStringSize, outPrice.getValue());
					sendMsg(res);

				} catch (HamsterNotFoundException e) {
					ByteBuffer res = ByteBuffer.allocate(4 + headerSize);
					res.put(0, version);
					res.put(1, (byte) (protocolFlagResponse | protocolFlagError));
					res.putShort(2, msg_id);
					res.putShort(4, (short) 4);
					res.putShort(6, rpc_call_id);
					res.putInt(8, HMSTR_ERR_NOTFOUND);
					sendMsg(res);
				}
			}
			case HAMSTER_RPC_FUNCID_GIVETREATS -> {
				System.out.println("Feed");
				int ret = 0;
				int hamster_ID = 0;
				short treats = 0;
				String owner_name = null;
				String hamster_name = null;
				if (payload_length > 6) {
					byte[] owner_name_buf = new byte[staticStringSize];
					byte[] hamster_name_buf = new byte[staticStringSize];

					receivedPayload.get(owner_name_buf);
					receivedPayload.get(hamster_name_buf);
					owner_name = new String(owner_name_buf, StandardCharsets.US_ASCII).trim();
					hamster_name = new String(hamster_name_buf, StandardCharsets.US_ASCII).trim();
				} else if (payload_length == 6) {
					hamster_ID = receivedPayload.getInt();
					treats = receivedPayload.getShort();
				}
				try {
					if (payload_length > 6) {
						ret = lib.lookup(owner_name, hamster_name); // Return value (from lookup function)
					}
					else if (payload_length == 6) {
						ret = lib.givetreats(hamster_ID, treats);
					}
					ByteBuffer res = ByteBuffer.allocate(4 + headerSize); // Response to client
					res.put(0, version); // Byte 0 from header: version of payload/header
					res.put(1, protocolFlagResponse); // Byte 1: flag (response/request/error)
					res.putShort(2, msg_id); // Byte 2: message ID
					res.putShort(4, (short) 4); // Byte 4: length of int 4 bytes
					res.putShort(6, rpc_call_id); // Byte 6: RPC Call ID
					res.putInt(8, ret); // Byte 8: Return value (from givetreats function)
					sendMsg(res);

				} catch (HamsterNameTooLongException e) {
					ByteBuffer res = ByteBuffer.allocate(4 + headerSize);
					res.put(0, version);
					res.put(1, (byte) (protocolFlagResponse | protocolFlagError));
					res.putShort(2, msg_id);
					res.putShort(4, (short) 4);
					res.putShort(6, rpc_call_id);
					res.putInt(8, HMSTR_ERR_NAMETOOLONG);
					sendMsg(res);
				} catch (HamsterStorageException | HamsterNotFoundException e) {
					throw new RuntimeException(e);
				}

			}
			case HAMSTER_RPC_FUNCID_HOWSDOING -> {
				System.out.println("Howsdoing");
				int hamster_ID = 0;
				int ret = 0;
				String owner_name = null;
				String hamster_name = null;

				if (payload_length > 4) {
					byte[] owner_name_buf = new byte[staticStringSize];
					byte[] hamster_name_buf = new byte[staticStringSize];

					receivedPayload.get(owner_name_buf);
					receivedPayload.get(hamster_name_buf);
					owner_name = new String(owner_name_buf, StandardCharsets.US_ASCII).trim();
					hamster_name = new String(hamster_name_buf, StandardCharsets.US_ASCII).trim();
				} else if (payload_length == 4) {
					hamster_ID = receivedPayload.getInt();
				}
				try {
					if (payload_length > 4) {
						ret = lib.lookup(owner_name, hamster_name); // Return value (from lookup function)
						ByteBuffer res = ByteBuffer.allocate(4 + headerSize); // Response to client
						res.put(0, version); // Byte 0 from header: version of payload/header
						res.put(1, protocolFlagResponse); // Byte 1: flag (response/request/error)
						res.putShort(2, msg_id); // Byte 2: message ID
						res.putShort(4, (short) (4 + 2 + 4 + 2)); // Byte 4: length of output 4+2+4+2= 12 bytes
						res.putShort(6, rpc_call_id); // Byte 6: RPC Call ID
						res.putInt(8, ret); // Byte 8: Return value (from howsdoing function)

						sendMsg(res);
					}
					else if (payload_length == 4) {
						HamsterState hamster_state = new HamsterState();
						ret = lib.howsdoing(hamster_ID, hamster_state);
						ByteBuffer res = ByteBuffer.allocate(12 + headerSize); // Response to client
						res.put(0, version); // Byte 0 from header: version of payload/header
						res.put(1, protocolFlagResponse); // Byte 1: flag (response/request/error)
						res.putShort(2, msg_id); // Byte 2: message ID
						res.putShort(4, (short) 12); // Byte 4: length of output 4+2+4+2= 12 bytes
						res.putShort(6, rpc_call_id); // Byte 6: RPC Call ID
						res.putInt(8, ret); // Byte 8: Return value (from howsdoing function)
						res.putShort(12, (short) hamster_state.getTreatsLeft()); // Byte 12: Treats left value
						res.putInt(14, hamster_state.getRounds()); // Byte 14: Rounds run value
						res.putShort(18, (short) hamster_state.getCost()); // Byte 18: Cost value

						sendMsg(res);
					}

				} catch (HamsterNameTooLongException e) {
					ByteBuffer res = ByteBuffer.allocate(4 + headerSize);
					res.put(0, version);
					res.put(1, (byte) (protocolFlagResponse | protocolFlagError));
					res.putShort(2, msg_id);
					res.putShort(4, (short) 4);
					res.putShort(6, rpc_call_id);
					res.putInt(8, HMSTR_ERR_NAMETOOLONG);
					sendMsg(res);
				} catch (HamsterStorageException | HamsterNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
			default -> {
				System.out.printf("RPC_CALL_ID %d is not implemented%n", rpc_call_id);
			}
		}
	}


	@Override
	public int new_(String owner_name, String hamster_name, Integer treats)
			throws IOException, HamsterRPCException {
		return 0;
	}


	@Override
	public int directory(Hmstr.HamsterHandle fdptr, String owner_name, String hamster_name)
			throws IOException, HamsterRPCException {
		return 0;
	}

	@Override
	public int howsdoing(Integer ID, State st)
			throws IOException, HamsterRPCException {
		return 0;
	}

	@Override
	public int readentry(Integer ID, HamsterString owner, HamsterString name, HamsterInteger price)
			throws HamsterRPCException_NotFound, HamsterRPCException_StorageError, HamsterRPCException_DatabaseCorrupt, IOException, HamsterRPCException {
		return 0;
	}

	@Override
	public int lookup(String owner_name, String hamster_name)
			throws HamsterRPCException_NameTooLong, HamsterRPCException_NotFound, HamsterRPCException_StorageError, HamsterRPCException_DatabaseCorrupt, IOException, HamsterRPCException {
		return 0;
	}

	@Override
	public int collect(String owner_name)
			throws HamsterRPCException_NotFound, HamsterRPCException_StorageError, HamsterRPCException_DatabaseCorrupt , IOException, HamsterRPCException {
		return 0;
	}

	@Override
	public int givetreats(Integer ID, Integer treats)
			throws HamsterRPCException_NotFound, HamsterRPCException_StorageError, HamsterRPCException_DatabaseCorrupt, IOException, HamsterRPCException
	{
		return 0;
	}
}
