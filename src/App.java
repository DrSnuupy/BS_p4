import java.util.*;
import org.javatuples.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App {
    //return ArrayList of all lines in config-file
    public static ArrayList<String> getFileContent(BufferedReader reader, ArrayList<String> lines) {
        String st = "";
        try{
            while((st = reader.readLine()) != null) {
                if (st.equals("HAL-Prozessoren:") || st.equals("HAL-Prozessoren: ")) {
                    continue;
                } else if(st.equals("HAL-Verbindungen:") || st.equals("HAL-Verbindungen: ")) {
                    lines.add("***");
                } else {
                    lines.add(st);
                }
            }
        }catch(IOException ioe) {
            System.out.println("File not found.");
            System.out.printf("File: %s not found", st);
            System.exit(0);
        }
        return lines;
    }
    public static void printList(ArrayList<Pair<Integer, Integer>> list) {
        for (int k = 0; k < list.size(); k++) {
            System.out.println("HAL-Prozessor: "+ k +" inPort: "+ list.get(k).getValue0() +", outPort: "+list.get(k).getValue1());
        }
    }
    public static void preFillList(ArrayList<Pair<Integer, Integer>> list, int amount) {
        for (int i = 0; i < amount+1; i++) {
            list.add(i, Pair.with(null, null));
        }
    }

    //bekommt config file übergeben
    public static void main(String[] args) { // Argumente kann nur ein Path zur Config-datei sein
        ArrayList<String> lines = new ArrayList<String>();
        ArrayList<buffer> puffer = new ArrayList<buffer>();
        ArrayList<HAL> processes = new ArrayList<HAL>();
        ArrayList<Pair<Integer,Integer>> links = new ArrayList<Pair<Integer, Integer>>();
        BufferedReader br;
        for(String e : args) {
            if (Files.isReadable(Paths.get(e)) == true) {
                try {
                    File programm = new File(e);
                    br = new BufferedReader(new FileReader(programm));
                    lines = getFileContent(br, lines);
                } catch (Exception x) {
                    System.out.print(x+"\n");
                    System.out.println("Please insert a programm you want to run! (PATH)");
                    System.exit(0);
                }
            } else {
                System.err.println("Please insert a valid file path!");
                System.exit(1);
            }
        }
        //loop through lines
        boolean halprg = true; String programm= ""; int halnr; int bufferPort = 0;
        for(int i = lines.size()-1; i >= 0; i--) {
            if (lines.get(i).equals("***")) {
                halprg = false;
                continue;
            } else if (halprg){
                //Teilen in VOR und NACH ">"
                String pre = lines.get(i).substring(0, lines.get(i).indexOf(">")-1);
                String post = lines.get(i).substring(lines.get(i).indexOf(">")+2);
                // get Hal numbers
                int firstHal = Integer.parseInt(pre.substring(0,1));
                int outPort= Integer.parseInt(pre.substring(2));
                // get Port numbers
                int scndHal = Integer.parseInt(post.substring(0,1));
                int inPort = Integer.parseInt(post.substring(2));

                //prefill Links-list with null-vals
                if(links.size() < scndHal) {
                    preFillList(links, scndHal);
                }

                //ArrayList of Hashmaps with <inPort,OutPort> and index as halnr
                links.get(firstHal).setAt1(outPort);
                links.get(scndHal).setAt0(inPort);


                System.out.println("firstHal: "+firstHal+", outPort: "+outPort+", scndHal: "+scndHal+", inPort: "+inPort);
                buffer newb = new buffer();
                puffer.add(newb);
            } else if(!halprg) {
                //Hal-Nr for identification and program as a path to the HAL program
                halnr = Integer.parseInt(lines.get(i).substring(0,1));
                programm = lines.get(i).substring(lines.get(i).indexOf(" "), lines.get(i).length());
                //Identify and rightly assign ports (in- and out-ward)


                //HAL newHAL = new HAL(programm, );
                //processes.add(newHAL);
            }
        }
        printList(links);
    }    
}
