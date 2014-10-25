import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class IOUtils {
	
	public static String readLineFromStreamReader(InputStream is) throws IOException{
		
		String tmpstr = new String();
		boolean flag = false;
		while(true){

			int tmp = is.read();
			if(tmp == -1) break;
			if(tmp == (int)'\n'){
				
				if(tmpstr.length()>0 && tmpstr.charAt(tmpstr.length()-1)=='\r')
					tmpstr = tmpstr.substring(0, tmpstr.length()-1);
				break;
			}
			
			tmpstr += (char)tmp;
			flag = true;
		}
		
		if(flag) return tmpstr;
		else return null;
		
	}

	public static Byte[] b2B(byte[] bodytmp) {
		// TODO Auto-generated method stub
		Byte[] tmp = new Byte[bodytmp.length];
		int i = 0;
		for(byte x : bodytmp){
			tmp[i++] = x;
		}
			
		return tmp;
	}

	public static byte[] B2b(Byte[] body) {
		// TODO Auto-generated method stub
		byte[] tmp = new byte[body.length];
		int i = 0;
		for(byte x : body){
			tmp[i++] = x;
		}
			
		return tmp;
	}

	public static char[] B2c(Byte[] body) {
		// TODO Auto-generated method stub
		char[] tmp = new char[body.length];
		int i = 0;
		for(Byte x : body){
			tmp[i++] = (char)(byte)x;
		}
		return tmp;
	}

	public static Byte[] c2B(char[] bodytmp) {
		Byte[] tmp = new Byte[bodytmp.length];
		int i = 0;
		for(char x : bodytmp){
			tmp[i++] = (byte)x;
		}
			
		return tmp;
		
	}
	
	public static Byte[] compressGzip(Byte[] data) throws IOException{
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream gs = new GZIPOutputStream(baos);
		gs.write(B2b(data));
		gs.finish();
		gs.flush();
		return b2B(baos.toByteArray());
	}

	public static Byte[] compressDeflate(Byte[] data) throws IOException {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DeflaterOutputStream dos = new DeflaterOutputStream(baos);
		dos.write(B2b(data));
		dos.finish();
		dos.flush();
		return b2B(baos.toByteArray());
	}
	
}
