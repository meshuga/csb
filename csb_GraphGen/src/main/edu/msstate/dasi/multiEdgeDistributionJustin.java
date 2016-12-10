package edu.msstate.dasi;

/*
 * The package to generate several distributions of the fields in the connection log file generated by Bro
 * @author: Arindam Khaled
 * @version: 0.1
 * @since 2016-11-14
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import static java.lang.Math.pow;

///dasi/projects/idaho-bailiff/G6/2016-2017/Temp/2011-10-11/connSortedTime.log /dasi/projects/idaho-bailiff/G6/2016-2017/Temp/2011-10-11/dist.txt
// /dasi/projects/idaho-bailiff/G6/2016-2017/Temp/2011-10-11/connSortedTime.log /dasi/projects/idaho-bailiff/G6/2016-2017/Temp/2011-10-11/testFieldDist.txt
public class multiEdgeDistributionJustin
{
    public multiEdgeDistributionJustin() {

    }

    public void init(String args[]) throws IOException {
        multiEdgeDistributionJustin torun = new multiEdgeDistributionJustin();
        String inFile = args[0];
//        String statFile = args[1] + "_stat.txt";
//        String countFile = args[1] + "_count.txt";;
        torun.propertyDistributionConditional(inFile);
        //torun.outputValuesOfField_Range(inFile, outFile, 17, 80, 89);
    }

    public static void main(String args[]) throws IOException
    {

        multiEdgeDistributionJustin torun = new multiEdgeDistributionJustin();
        String inFile = args[0];
        String statFile = args[1] + "_stat.txt";
        String countFile = args[1] + "_count.txt";;
        torun.propertyDistributionConditional(inFile);
        //torun.outputValuesOfField_Range(inFile, outFile, 17, 80, 89);

    }
    //http://stackoverflow.com/questions/5667371/validate-ipv4-address-in-java
    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    public static boolean validate(final String ip)
    {
        return PATTERN.matcher(ip).matches();
    }

    public static boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public static boolean isDouble(String str)
    {
        //http://stackoverflow.com/questions/3133770/how-to-find-out-if-the-value-contained-in-a-string-is-double-or-not
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
        //below was arindam's approach
        //I added the above code to incorporate scientific notation
//        return str.matches("-?\\d+(\\.\\d+)+");  //match a number with optional '-' and decimal.
    }

    private static final String computeRange(String value, int interval)
    {
        long valueInt = (isDouble(value)) ? (long) Double.valueOf(value).doubleValue() : Long.parseLong(value);
        //first obtain the left interval
        int t = 10;
        while(interval / t > 0)
            t *= 10;
        t /= 10;
        long left = (valueInt / t) * t;

        //determine the range
        long right = left + interval - 1;
        String range = left + "-" + right;

        return range;
    }

    private static final void readPreviousDistribution(final String inFile) throws FileNotFoundException
    {
        FileReader fr = new FileReader(inFile);
        BufferedReader br = new BufferedReader(fr);
    }

    private static final void writeStatsToFileDist(BufferedWriter bw, String propertyName, HashMap<String,
            HashMap<String, Integer> > distribution, HashMap<String, Integer>
                                                           origByteDist, long total) throws IOException
    {
//		bw.write("Sorted " + propertyName + " (probability) Distribution below:" + "\n");

        for(String key : origByteDist.keySet())
        {
//                    bw.write(key + ": " + (double)origByteDist.get(key) / (double)total  + "\n");


            long keyTotal = origByteDist.get(key);

            if(distribution.containsKey(key))
            {
                //bw.write("Sorted " + key + " (probability) Distribution below:" + "\n");
                HashMap<String, Integer> inDistribution = distribution.get(key);
                writeStatsToFile(bw, key, inDistribution, keyTotal);
            }
        }
//		bw.write("\n");

    }



    private static final void writeStatsToFile(BufferedWriter bw, String propertyName,
                                               HashMap<String, Integer> distribution, long total)
            throws IOException
    {

        distribution = (HashMap<String, Integer>) sortByValueDescending(distribution);

//		bwc.write("Sorted " + propertyName + " (count) Distribution below:" +"\n");
//
//		for(String key : distribution.keySet())
//		{
//			bwc.write(key + "\t " + distribution.get(key) + "\n");
//		}

//		bw.write("Sorted " + propertyName + " (probability) Distribution below:" + "\n");


        double percentSum = 0.0;
        for(String key : distribution.keySet())
        {
            bw.write(propertyName + "*" + key + "\t " + ((double)distribution.get(key) / (double)total)  + "\n");
        }

//		bw.write("\n\n");
//		bwc.write("\n\n");

    }

    public static final void addToByteDistribution(HashMap<String, HashMap<String, Integer> > fieldDistribution,
                                                   String byteRange, String fieldValue)
    {

        int newCount = 1;
        if(fieldDistribution.containsKey(byteRange))
        {
            HashMap<String, Integer> insideMap = fieldDistribution.get(byteRange);
            if(insideMap.containsKey(fieldValue))
                newCount += insideMap.get(fieldValue);
            insideMap.put(fieldValue, newCount);
        }
        else
        {
            HashMap<String, Integer> insideMap = new HashMap();
            insideMap.put(fieldValue, newCount);
            fieldDistribution.put(byteRange, insideMap);
        }
    }

    void outputValuesOfField_Range(final String inFile, final String outFile, int fieldNo, int begin, int end) throws IOException
    {
        String line = "";
        FileReader fr = new FileReader(inFile);
        BufferedReader br = new BufferedReader(fr);
        FileWriter fw = new FileWriter(outFile);
        BufferedWriter bw = new BufferedWriter(fw);
        HashMap<Integer, Integer> distribution = new HashMap();

        for(int i = begin; i <= end; i++)
            distribution.put(i, 0);
        int count = 0;
        int total = 0;
        while((line = br.readLine())!= null)
        {
            String[] split = line.split("\t");

            if(split.length < 6 || !validate(split[2]) || !validate(split[4]) || !isNumeric(split[9]))
            {
                continue;
            }

            String field = split[fieldNo];
            //if(!isNumeric(field))
            //	continue;
            int fieldInt = (int)Double.parseDouble(field);

            if(fieldInt >= begin && fieldInt <= end)
            {
                count = distribution.get(fieldInt);
                distribution.put(fieldInt, count + 1);
                total++;
            }
            //if(fieldInt >= begin && fieldInt <= end)
            //	fw.write(field + "\n");
        }

        System.out.println("total " + total);
        for(int key : distribution.keySet())
        {
            bw.write(key + "\t" +distribution.get(key)   + "\n");
        }

        bw.close();
        fw.close();

    }


	/*
	 * This method takes a connection log file (sorted with respect to time of connections)
	 * and then generates the conditional distributions of all the fields given (origin)byte
	 * count.
	 * @param inFile The name of the input file
	 * @param statFile The name of the file containing distribution results
	 */

    void propertyDistributionConditional(final String inFile) throws IOException
    {
        String line = "";
        FileReader fr = new FileReader(inFile);
        BufferedReader br = new BufferedReader(fr);

        //the properties are bytecount, connection type, connection state, duration, orig_pkt_cnt, resp_pkt_cnt
        HashMap<String, Integer> origByteCount = new HashMap();
        //HashMap<String, HashMap<String, Integer> > allDistributionsCounts = new HashMap();

        HashMap<String, HashMap<String, Integer> > respByteCount = new HashMap();
        HashMap<String, HashMap<String, Integer> > connType = new HashMap();
        HashMap<String, HashMap<String, Integer> > connState = new HashMap();
        HashMap<String, HashMap<String, Integer> > durationDist = new HashMap();
        HashMap<String, HashMap<String, Integer> > orig_pkt_cnt = new HashMap();
        HashMap<String, HashMap<String, Integer> > resp_pkt_cnt = new HashMap();
        HashMap<String, HashMap<String, Integer> > origIPByteCount = new HashMap();
        HashMap<String, HashMap<String, Integer> > respIPByteCount = new HashMap();

        HashMap <String, Integer> nodes = new HashMap();
        HashMap <Integer, ArrayList<Integer> > graph = new HashMap();
        HashMap <String, Integer> edgeCount = new HashMap();
        HashSet<String> edges = new HashSet<String> ();
        int nodeCount = -1;
        int badRecords = 0;
        int totalRecords = 0;
        long tcp_udp_count = 0;
        int interval = 10;	//the interval/range of bytes and others
        String range = "";

        while((line = br.readLine())!= null)
        {
            String[] split = line.split("\t");
            totalRecords++;
            if(split.length < 6 || !validate(split[2]) || !validate(split[4]) || !isNumeric(split[9]))
            {
                badRecords++;
                continue;
            }

            String from = split[2] + ":" + split[3];
            String to = split[4] + ":"+ split[5];
            String conn = split[6];
            if(!(conn.equals("tcp") ||  conn.equals("udp")))
                continue;
            tcp_udp_count++;

            String origBytes = split[9];
            String respBytes = split[10];
            String conState = split[11];
            String origPktCnt = split[16];
            String origIPBytes = split[17];
            String respPktCnt = split[18];
            String respIPBytes = split[19];
            String duration = split[8];

            double t = Double.parseDouble(split[0]);

            //System.out.println(from);

            int fNode = 0, tNode = 0;

            if(nodes.containsKey(from))
            {
                fNode = ((Integer)nodes.get(from)).intValue();
            }
            else
            {
                nodeCount++;
                fNode = nodeCount;
                nodes.put(from, nodeCount);
            }

            if(nodes.containsKey(to))
            {
                tNode = ((Integer)nodes.get(to)).intValue();
            }
            else
            {
                nodeCount++;
                tNode = nodeCount;
                nodes.put(to, nodeCount);
            }

            String edge = String.valueOf(from) + "," + String.valueOf(to);

            int newCount = 1;

            if(edgeCount.containsKey(edge))
                newCount += edgeCount.get(edge);

            edgeCount.put(edge, newCount);

			/*
			 * The rest of the stats
			 */
            //connection type


            newCount = 1;
            String byteRange = computeRange(origBytes, interval);
            if(origByteCount.containsKey(byteRange))
                newCount += origByteCount.get(byteRange);
            origByteCount.put(byteRange, newCount);

            addToByteDistribution(connType, byteRange, conn);

            range = computeRange(respBytes, interval);
            addToByteDistribution(respByteCount, byteRange, range);

            addToByteDistribution(connState, byteRange, conState);

            range = computeRange(origPktCnt, interval);
            addToByteDistribution(orig_pkt_cnt, byteRange, range);

            range = computeRange(respPktCnt, interval);
            addToByteDistribution(resp_pkt_cnt, byteRange, range);

            range = computeRange(origIPBytes, interval);
            addToByteDistribution(origIPByteCount, byteRange, range);

            range = computeRange(respIPBytes, interval);
            addToByteDistribution(respIPByteCount, byteRange, range);

            range = computeRange(duration, interval);
            addToByteDistribution(durationDist, byteRange, range);
        }

        System.out.println("Finishes reading");
        HashMap <Integer, Integer> edgeDistribution = new HashMap();
        HashMap <String, Integer> edgeDistributionStr = new HashMap();
        System.out.println("Total records: " + totalRecords);
        System.out.println("Number of bad records: " + badRecords);
        System.out.println("Total tcp/udp connections: " + tcp_udp_count);
        System.out.println("Edge (count) Distribution below:");
        int totalMultiedges = 0;

        for(String key : edgeCount.keySet())
        {
            int count = edgeCount.get(key);
            //total += count;

            int newCount = 1;
            if(edgeDistribution.containsKey(count))
                newCount += edgeDistribution.get(count);
            totalMultiedges++;

            edgeDistribution.put(count, newCount);
            edgeDistributionStr.put(Integer.toString(count), newCount);
        }

        System.out.println("Total edges: " + totalMultiedges);

        //write the output to file
//        bw.write("Total records: " + totalRecords + "\n");
//        bw.write("Number of bad records: " + badRecords + "\n");
//        bw.write("Total tcp/udp connections: " + tcp_udp_count + "\n");
//        bw.write("Edge (count) Distribution below:" + "\n");
        SortedSet<Integer> keys = new TreeSet<Integer>(edgeDistribution.keySet());

        FileWriter fw1 = new FileWriter("Edge_distributions");
        BufferedWriter bw1 = new BufferedWriter(fw1);

        FileWriter fw2 = new FileWriter("Original_byte_count");
        BufferedWriter bw2 = new BufferedWriter(fw2);

        FileWriter fw3 = new FileWriter("Resp_byte_count");
        BufferedWriter bw3 = new BufferedWriter(fw3);

        FileWriter fw4 = new FileWriter("Connection_type");
        BufferedWriter bw4 = new BufferedWriter(fw4);

        FileWriter fw5 = new FileWriter("Connection_state");
        BufferedWriter bw5 = new BufferedWriter(fw5);

        FileWriter fw6 = new FileWriter("Original_packet_count");
        BufferedWriter bw6 = new BufferedWriter(fw6);

        FileWriter fw7 = new FileWriter("Resp_packet_count");
        BufferedWriter bw7 = new BufferedWriter(fw7);

        FileWriter fw8 = new FileWriter("Original_IP_byte_count");
        BufferedWriter bw8 = new BufferedWriter(fw8);

        FileWriter fw9 = new FileWriter("Resp_IP_byte_count");
        BufferedWriter bw9 = new BufferedWriter(fw9);

        FileWriter fw10 = new FileWriter("Duration_of_connection");
        BufferedWriter bw10 = new BufferedWriter(fw10);

        writeStatsToFile(bw1, "Edge distributions", edgeDistributionStr, totalMultiedges);
        writeStatsToFile(bw2, "Original byte count", origByteCount, tcp_udp_count);

        origByteCount = (HashMap<String, Integer>) sortByValueDescending(origByteCount);


        writeStatsToFileDist(bw3, "Resp byte count", respByteCount, origByteCount, tcp_udp_count);
        writeStatsToFileDist(bw4, "Connection type", connType, origByteCount, tcp_udp_count);
        writeStatsToFileDist(bw5, "Connection state", connState, origByteCount, tcp_udp_count);
        writeStatsToFileDist(bw6, "Original packet count", orig_pkt_cnt, origByteCount, tcp_udp_count);
        writeStatsToFileDist(bw7, "Resp packet count", resp_pkt_cnt, origByteCount, tcp_udp_count);
        writeStatsToFileDist(bw8, "Original IP byte count", origIPByteCount, origByteCount, tcp_udp_count);
        writeStatsToFileDist(bw9, "Resp IP byte count", respIPByteCount, origByteCount, tcp_udp_count);
        writeStatsToFileDist(bw10,  "Duration of connections", durationDist, origByteCount, tcp_udp_count);

//        bw.close();
//        fw.close();
        bw1.close();
        fw1.close();
        bw2.close();
        fw2.close();
        bw3.close();
        fw3.close();
        bw4.close();
        fw4.close();
        bw5.close();
        fw5.close();
        bw6.close();
        fw6.close();
        bw7.close();
        fw7.close();
        bw8.close();
        fw8.close();
        bw9.close();
        fw9.close();
        bw10.close();
        fw10.close();
    }



    /*
 * To determine the average rate each edge is added
 */
    void multiEdgeDistributionWithTime(String inFile, String statFile) throws IOException
    {
        String line = "";
        FileReader fr = new FileReader(inFile);
        BufferedReader br = new BufferedReader(fr);
        FileWriter fw = new FileWriter(statFile);
        BufferedWriter bw = new BufferedWriter(fw);

        HashMap <String, Integer> nodes = new HashMap();
        HashMap <Integer, ArrayList<Integer> > graph = new HashMap();
        HashMap <String, Integer> edgeCount = new HashMap();
        HashSet<String> edges = new HashSet<String> ();
        int nodeCount = -1;
        int badRecords = 0;
        int totalRecords = 0;
        int tcp_udp_count = 0;

        while((line = br.readLine())!= null)
        {
            String[] split = line.split("\t");
            totalRecords++;
            if(split.length < 6 || !validate(split[2]) || !validate(split[4]))
            {
                badRecords++;
                continue;
            }

            String from = split[2] + ":" + split[3];
            String to = split[4] + ":"+ split[5];
            String conn = split[6];
            if(!(conn.equals("tcp") ||  conn.equals("udp")))
                continue;
            tcp_udp_count++;

            double t = Double.parseDouble(split[0]);


            //System.out.println(from);

            int fNode = 0, tNode = 0;

            if(nodes.containsKey(from))
            {
                fNode = ((Integer)nodes.get(from)).intValue();
            }
            else
            {
                nodeCount++;
                fNode = nodeCount;
                nodes.put(from, nodeCount);
            }

            if(nodes.containsKey(to))
            {
                tNode = ((Integer)nodes.get(to)).intValue();
            }
            else
            {
                nodeCount++;
                tNode = nodeCount;
                nodes.put(to, nodeCount);
            }

            String edge = String.valueOf(from) + "," + String.valueOf(to);

            int newCount = 1;

            if(edgeCount.containsKey(edge))
                newCount += edgeCount.get(edge);

            edgeCount.put(edge, newCount);
        }

        System.out.println("Finishes reading");
        HashMap <Integer, Integer> edgeDistribution = new HashMap();
        System.out.println("Total records: " + totalRecords);
        System.out.println("Number of bad records: " + badRecords);
        System.out.println("Total tcp/udp connections: " + tcp_udp_count);
        System.out.println("Edge (count) Distribution below:");

        for(String key : edgeCount.keySet())
        {
            int count = edgeCount.get(key);

            int newCount = 1;
            if(edgeDistribution.containsKey(count))
                newCount += edgeDistribution.get(count);
            edgeDistribution.put(count, newCount);
        }

        //write the output to file
        bw.write("Total records: " + totalRecords + "\n");
        bw.write("Number of bad records: " + badRecords + "\n");
        bw.write("Total tcp/udp connections: " + tcp_udp_count + "\n");
        bw.write("Edge (count) Distribution below:" + "\n");
        SortedSet<Integer> keys = new TreeSet<Integer>(edgeDistribution.keySet());

        for(int key : keys)
        {
            bw.write(key + ": " + edgeDistribution.get(key) + "\n");
        }

        bw.write("Edge (probability) Distribution below:" + "\n");

        for(int key : keys)
        {
            bw.write(key + ": " + (double)edgeDistribution.get(key) / (double)tcp_udp_count  + "\n");
        }


        edgeDistribution = (HashMap<Integer, Integer>) sortByValue(edgeDistribution);
        bw.write("Edge distributions based on sorted multi-edge count: " + totalRecords + "\n");
        bw.write("Edge (count) Distribution below:" +"\n");
        Set<Integer> keys2 = edgeDistribution.keySet();

        for(int key : keys2)
        {
            bw.write(key + ": " + edgeDistribution.get(key) + "\n");
        }

        bw.write("Edge (probability) Distribution below:" + "\n");

        for(int key : keys2)
        {
            bw.write(key + ": " + (double)edgeDistribution.get(key) / (double)tcp_udp_count  + "\n");
        }




        bw.close();
        fw.close();
    }

    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return (o1.getValue()).compareTo( o2.getValue() );
            }
        } );

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }

        return result;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValueDescending( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return (o2.getValue()).compareTo( o1.getValue() );
            }
        } );

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }

        return result;
    }




}