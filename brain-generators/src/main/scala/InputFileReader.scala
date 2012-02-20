import java.io.BufferedReader;
import java.io.InputStreamReader
import java.io.FileInputStream


class InputFileReader(fileName:String, sep:String=",") {
	
	val reader:BufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));

	def readLine():String=
	{
		val line = reader.readLine();
		line match{
			case a:String => return a;
			case _ => return "";
		}
		
	}

	def readItems():Array[String]=
	{
		val line = reader.readLine();
		line match{
			case a:String => return line.split(sep)			
			case _ => return new Array[String](0)
		}

	}

	
	def close():Unit=
	{	
		reader.close()
		
	}
}