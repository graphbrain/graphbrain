import java.io.BufferedWriter;
import java.io.FileWriter;
import scala.collection.immutable.HashMap


class OutputFileWriter(fileName:String, sep:String=",", lineSep:String="\n") extends Output {
	
	val writer:BufferedWriter = new BufferedWriter(new FileWriter(fileName))

	def writeOut(stringToWrite:String):Boolean=
	{
		try{
			writer.write(stringToWrite+lineSep);
			return true;	
		}
		catch{case e => return false}
		
	}

	def writeOut(stringsToWrite:List[String]):Boolean=
	{
		val numElements=stringsToWrite.length;
		writer.write(stringsToWrite(0))
		try{
			for(i <- (1 to numElements-1))
			{
				writer.write(sep)
				writer.write(stringsToWrite(i))
			}
			writer.write(lineSep);
			return true;
		}
		catch{
			case e => return false
		}

	}

	def writeOut(toWrite:Array[Int]):Boolean=
	{
		val numElements=toWrite.length;
		writer.write(toWrite(0))
		try{
			for(i <- (1 to numElements-1))
			{
				writer.write(sep)
				writer.write(toWrite(i).toString)
			}
			writer.write(lineSep);
			return true;
		}
		catch{
			case e => return false
		}

	}

	def writeOut(toWrite:Map[String, Int]):Boolean=
	{
		val entries=toWrite.iterator
		
		entries.next match{
			case (a, b) => writer.write(b.toString)
		}
		
		try{

			while(entries.hasNext)
			{
				writer.write(sep)
				entries.next match
				{
					case (a, b) => writer.write(b.toString)
				}
			}

			writer.write(lineSep);
			return true;
		}
		catch{
			case e => return false
		}

	}


	def close():Unit=
	{	
		writer.close()
		
	}
}