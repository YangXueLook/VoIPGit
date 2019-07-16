package controlexperiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;

public class TrigramExtractor
{
	
	
	public static ArrayList<String> filePathList = new ArrayList<String>();

	//read content of file. There is only 1 line for each file.
	private static String readFileByLines(String fileName)
	{
		System.out.println("loading files");
		File file = new File(fileName);
		BufferedReader reader = null;
		String result = "";
		try
		{
			String tempString = null;

			reader = new BufferedReader(new FileReader(file));

			int line = 1;

			while ((tempString = reader.readLine()) != null)
			{

				result = tempString;

				line++;
			}
			reader.close();

		}
		catch (IOException e)
		{

			return null;
		}

		return result;
	}
	//get all raw data file path
	private static ArrayList<String> getAllFilePaths(String filePath)
	{
		File f = null;
		f = new File(filePath);
		File[] files = f.listFiles();
		List<File> list = new ArrayList<File>();
		for (File file : files)
		{
			if (file.isDirectory())
			{

				getAllFilePaths(file.getAbsolutePath());
			}
			else
			{
				list.add(file);
			}
		}
		for (File file : files)
		{
			// System.out.println(file.getAbsolutePath());

			String path = file.getAbsolutePath();

			if (path.contains(".dat"))
			{
				if (path.contains("multi040"))
					filePathList.add(path);

			}
		}
		return filePathList;
	}
	//given a string of padded packets, get frequency of each 3-gram combination.
	//since there are 8 possible packets in all, the total number of combination is 8*8*8 = 512
	//from aaa to hhh
	private static int[] extractTrigramFre(String s)
	{
		int[] fre = new int[512];
		for (int i = 0; i < s.length() - 2; i++)
		{
			char c0 = s.charAt(i);
			char c1 = s.charAt(i + 1);
			char c2 = s.charAt(i + 2);
			int index = (c0 - 'a') * 64 + (c1 - 'a') * 8 + (c2 - 'a') * 1;
			fre[index]++;
		}
		return fre;
	}

	//write everything to arff file.
	private static void writeToFile(String homePath, String fileName,
			HashMap<String, ArrayList<int[]>> map)
	{

		File file = new File(homePath + "/" + fileName);
		try
		{
			if (!file.exists())
			{
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());

			BufferedWriter bw = new BufferedWriter(fw);

			String[] trigram512 = getAllTrigrams();

			bw.write("@relation 'controlExperiment'" + "\n");
			
			for(String s : trigram512)
			{
				bw.write("@attribute FRE" + s + " numeric" + "\n");
			}
			
			StringBuffer allSpeakers = new StringBuffer();
			for (String speaker : map.keySet())
			{
				allSpeakers.append("\'");
				allSpeakers.append(speaker);
				allSpeakers.append("\',");
			}
			allSpeakers.deleteCharAt(allSpeakers.length() - 1);
			bw.write("@attribute Speaker {" + allSpeakers + "}" + "\n");
			bw.write("@data" + "\n");

			Iterator it = map.keySet().iterator();
			while (it.hasNext())
			{
				String speaker = (String) it.next();
				ArrayList<int[]> list = map.get(speaker);
				for (int i = 0; i < list.size(); i++)
				{
					int[] fre = list.get(i);

					
					for(int f : fre)
						bw.write(f + ",");
					bw.write(speaker);
					bw.write("\n");
				}

			}

			bw.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	//get all combinations of 3-gram
	private static String[] getAllTrigrams()
	{
		String[] result = new String[512];

		int index = 0;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 8; i++)
		{
			sb.append((char)('a' + i));
			for (int j = 0; j < 8; j++)
			{
				sb.append((char)('a' + j));
				for (int k = 0; k < 8; k++)
				{
					sb.append((char)('a' + k));
					result[index] = sb.toString();
					index++;
					sb.deleteCharAt(sb.length() - 1);
				}
				sb.deleteCharAt(sb.length() - 1);
			}
			sb.deleteCharAt(sb.length() - 1);
		}

		return result;
	}

	public static void main(String[] args)
	{
//		for(String s :getAllTrigrams())
//		{
//			System.out.println(s);
//		}
//		
		
		
		System.out.println("start");
		

		String trainingFilePath = "G:\\VoIPControl\\brad-trimmeddata";
		ArrayList<String> pathList = getAllFilePaths(trainingFilePath);

		HashMap<String, ArrayList<int[]>> speakerToTrigramMap = new HashMap<String, ArrayList<int[]>>();

		for (String path : pathList)
		{
			// System.out.println(path);
			String[] array = path.split("\\\\");
			// System.out.println(array.length);
			String speaker = array[array.length - 2];
			System.out.println(speaker);
			// speakerSet.add(speaker);

			//
			String content = readFileByLines(path);

			// System.out.println(content);
			int[] trigramFre = extractTrigramFre(content);

			if (speakerToTrigramMap.containsKey(speaker))
			{
				speakerToTrigramMap.get(speaker).add(trigramFre);
			}
			else
			{
				speakerToTrigramMap.put(speaker, new ArrayList<int[]>());
				speakerToTrigramMap.get(speaker).add(trigramFre);
			}

		}

		writeToFile("G:\\VoIPControl", "Trigram.arff",
				speakerToTrigramMap);

		System.out.println("write done");

	}

	/**
	 * @param content
	 * @return
	 */
	

}
