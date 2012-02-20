import java.io.BufferedWriter;
import java.io.FileWriter;
import scala.collection.immutable.HashMap


class OutputScreenPrinter(sep:String=",", lineSep:String="\n") extends Output {
	
	

	def writeOut(stringToWrite:String):Boolean=
	{
		print(stringToWrite+lineSep);
		return true;
	}

	def writeOut(stringsToWrite:List[String]):Boolean=
	{
		val numElements=stringsToWrite.length;
		print(stringsToWrite(0))

		for(i <- (1 to numElements-1))
		{
			print(sep)
			print(stringsToWrite(i))
		}
		print(lineSep);
		return true;

	}

	def writeOut(toWrite:Array[Int]):Boolean=
	{
		val numElements=toWrite.length;
		print(toWrite(0))
		
		for(i <- (1 to numElements-1))
		{
			print(sep)
			print(toWrite(i).toString)
		}
		print(lineSep);
		return true;
		

	}

	def writeOut(toWrite:Map[String, Int]):Boolean=
	{
		val entries=toWrite.iterator
		
		entries.next match{
			case (a, b) => print(b.toString)
		}
		
		while(entries.hasNext)
		{
			print(sep)
			entries.next match
			{
				case (a, b) => print(b.toString)
			}
		}

		print(lineSep);
		return true;

	}
}