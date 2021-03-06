/** 
 * Author: Devdeep Ray
 * Project: HTTPServer (Networks)
 * Description: These are HTTP responses that are supported. Takes in received HTTP file as
 * HTTPObject, and  gives out HTTPObject
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;


public class HTTPResponses 
{
	public static HTTPObject directoryRedirectResponse(HTTPObject hto, String filePath)
	{
		String redirUri = HTTP11Helpers.getRedirectUri(hto.header.param); // Get redir URI
		HTTPObject resp = new HTTPObject(); // Start preparing header
		resp.header.version = StringConstants.HTTP11;
		resp.header.action = StringConstants.permanentlyMovedCode;
		resp.header.param = StringConstants.permanentlyMovedMessage;
		if(ServerSettings.isKeepAlive()) // For keep alive
		{
			resp.header.attributes.put(StringConstants.connection, StringConstants.keepAlive);
		}
		else
		{
			resp.header.attributes.put(StringConstants.connection,  StringConstants.closeConnection);
		}
		resp.header.attributes.put(StringConstants.location, redirUri); // Put various attributes
		resp.header.attributes.put(StringConstants.contentType, StringConstants.textHtml);
		resp.body = FileCache.getPermPage(ServerSettings.getRedirFile()); // Put body for redirection
		resp.header.attributes.put("Content-Length", ""+resp.body.length);
		return resp;
	}

	public static HTTPObject CGIResponse(HTTPObject hto, String filePath)
	{
		
		try
		{
			
			int qmindex = hto.header.param.indexOf('?');
			String args = "";
			if(qmindex >= 0 && hto.header.action.equals(StringConstants.getRequestAction))
			{
				args = hto.header.param.substring(qmindex + 1);
			}
			ProcessBuilder processBuilder = new ProcessBuilder(filePath);
			Map<String, String> env = processBuilder.environment();

			
			env.put("DOCUMENT_ROOT", "" + ServerSettings.getWebRootPath()); // Set env variables
			env.put("REQUEST_METHOD", "" + hto.header.action);
			if(hto.body != null)
				env.put("CONTENT_LENGTH", "" + hto.body.length); // Set env variables
			env.put("CONTENT_TYPE", "" + hto.header.attributes.get(StringConstants.contentType));
			env.put("GATEWAY_INTERFACE", "" + StringConstants.cgiVersion); // Set env variables
			env.put("HTTP_ACCEPT", "" + hto.header.attributes.get(StringConstants.acceptDataType));
			env.put("HTTP_REFERER", "" + hto.header.attributes.get(StringConstants.referer));
			env.put("QUERY_STRING", "" + args);
			
			Process process = processBuilder.start(); // Start process

			// Give process IP and get OP
			BufferedOutputStream bos = new BufferedOutputStream(process.getOutputStream());
			BufferedInputStream bis = new BufferedInputStream(process.getInputStream());
			if (hto.body != null && hto.header.action.equals(StringConstants.postRequestAction)) // Give input
			{ 
				bos.write(IOUtils.B2b(hto.body));
				bos.write("\r\n".getBytes());
				bos.flush();
			}
			
			String header = new String();
			String tmpline = new String();
			while (true) // Get output header
			{
				tmpline = IOUtils.readLineFromStreamReader(bis);
				if(tmpline == null || tmpline.equals(""))
				{
					break;
				}	
				header += tmpline + "\n";
			}
			CGIHeader hdrobj = new CGIHeader(header);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int x;
			while ((x = bis.read()) != -1) // Get output payload
			{	
				baos.write(x); 
			}
			hdrobj.version = StringConstants.HTTP11; // set up response header
			hdrobj.action = StringConstants.okResponseCode;
			hdrobj.param = StringConstants.okResponseMessage;
			// Otherwise read the body
			byte [] bodytmp = baos.toByteArray();
			hdrobj.attributes.put(StringConstants.contentLength, ""+bodytmp.length);
			if(ServerSettings.isKeepAlive())
			{
				hdrobj.attributes.put(StringConstants.connection, StringConstants.keepAlive);
			}
			else
			{
				hdrobj.attributes.put(StringConstants.connection,  StringConstants.closeConnection);
			}
			return (new HTTPObject(hdrobj, IOUtils.b2B(bodytmp)));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return internalErrorResponse(hto);
		}			
	}

	public static HTTPObject argsGetResponse(HTTPObject hto, String filePath) 
	{
		return CGIResponse(hto, filePath);			
	}

	public static HTTPObject headResponse(HTTPObject hto, String filePath)
	{
		String ftype = HTTP11Helpers.getFileExtType(filePath); 
		HTTPObject resp = new HTTPObject();
		resp.header.version = StringConstants.HTTP11; // Set version, action and param
		resp.header.action = StringConstants.okResponseCode;
		resp.header.param = StringConstants.okResponseMessage;
		if(ServerSettings.isKeepAlive()) // Keep alive parameters
			resp.header.attributes.put(StringConstants.connection, StringConstants.keepAlive);
		else
			resp.header.attributes.put(StringConstants.connection,  StringConstants.closeConnection);
		resp.header.attributes.put(StringConstants.location, hto.header.attributes.get(StringConstants.host.toLowerCase()) + hto.header.param);
		resp.header.attributes.put(StringConstants.contentType, ftype);
		String enc;
		if((enc = hto.header.attributes.get(StringConstants.acceptEncoding)) != null)
		{
			HashSet<String> encset = new HashSet<String>(Arrays.asList(enc.split("\\s*,\\s*")));
			if(encset.contains(ServerSettings.supportedEncoding))
			{
				resp.header.attributes.put(StringConstants.contentEncoding, ServerSettings.supportedEncoding);
			}
		}
		resp.header.attributes.put(StringConstants.contentLength, ""+(resp.body.length));
		return resp;
	}

	public static HTTPObject standardGetResponse(HTTPObject hto, String filePath) 
	{
		String ftype = HTTP11Helpers.getFileExtType(filePath); 
		HTTPObject resp = new HTTPObject();
		resp.header.version = StringConstants.HTTP11; // Set version, action and param
		resp.header.action = StringConstants.okResponseCode;
		resp.header.param = StringConstants.okResponseMessage;
		if(ServerSettings.isKeepAlive()) // Keep alive parameters
			resp.header.attributes.put(StringConstants.connection, StringConstants.keepAlive);
		else
			resp.header.attributes.put(StringConstants.connection,  StringConstants.closeConnection);
		resp.header.attributes.put(StringConstants.location, hto.header.attributes.get(StringConstants.host.toLowerCase()) + hto.header.param);
		resp.header.attributes.put(StringConstants.contentType, ftype);
		String enc;
		boolean encodingFlag = false;
		if((enc = hto.header.attributes.get(StringConstants.acceptEncoding.toLowerCase())) != null)
		{
			HashSet<String> encset = new HashSet<String>(Arrays.asList(enc.split("\\s*,\\s*")));
			if(encset.contains(ServerSettings.supportedEncoding))
			{
				resp.header.attributes.put(StringConstants.contentEncoding, ServerSettings.supportedEncoding);
				encodingFlag = true;
			}
		}
		try 
		{
			if(encodingFlag)
			{
				resp.body = IOUtils.encode(FileCache.getPage(filePath), ServerSettings.supportedEncoding);
			}
		}
		catch (IOException e) 
		{
			return internalErrorResponse(hto);
		}
		resp.header.attributes.put(StringConstants.contentLength, ""+(resp.body.length));
		return resp;
	}

	public static HTTPObject badRequestResponse(HTTPObject hto) 
	{
		HTTPObject resp = new HTTPObject();
		resp.header.version = StringConstants.HTTP11;
		resp.header.action = StringConstants.badRequestResponseCode;
		resp.header.param = StringConstants.badRequestResponseMessage;
		resp.header.attributes.put(StringConstants.connection, StringConstants.closeConnection);
		resp.header.attributes.put(StringConstants.contentType, StringConstants.textHtml);
		resp.body = FileCache.getPermPage(ServerSettings.getBadRequestFile());
		resp.header.attributes.put(StringConstants.contentLength, ""+ resp.body.length);
		return resp;
	}

	public static HTTPObject internalErrorResponse(HTTPObject hto)
	{
		
		HTTPObject resp = new HTTPObject();
		resp.header.version = StringConstants.HTTP11;
		resp.header.action = StringConstants.internalErrorCode;
		resp.header.param = StringConstants.internalErrorMessage;
		resp.header.attributes.put(StringConstants.connection, StringConstants.closeConnection);
		resp.header.attributes.put(StringConstants.contentType, StringConstants.textHtml);
		resp.body = FileCache.getPermPage(ServerSettings.getInternalErrorFile());
		resp.header.attributes.put(StringConstants.contentLength, ""+ resp.body.length);
		return resp;
	}
	
	public static HTTPObject notFoundHTTPResponse(HTTPObject hto)
	{
		
		HTTPObject resp = new HTTPObject();
		resp.header.version = StringConstants.HTTP11;
		resp.header.action = StringConstants.notFoundCode;
		resp.header.param = StringConstants.notFoundMessage;
		if(ServerSettings.isKeepAlive())
			resp.header.attributes.put(StringConstants.connection, StringConstants.keepAlive);
		else
			resp.header.attributes.put(StringConstants.connection,  StringConstants.closeConnection);
		resp.header.attributes.put(StringConstants.contentType, StringConstants.textHtml);
		resp.body = FileCache.getPermPage(ServerSettings.getNotFoundFile());
		resp.header.attributes.put(StringConstants.contentLength, ""+ resp.body.length);
		return resp;
	}

}
