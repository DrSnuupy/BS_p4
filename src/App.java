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
            list.add(i, Pair.with(0, 0));
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
            String preS=""; String postS=""; 
            int firstHalNext=0; int scndHalNext =0; int inPortNext=0; int outPortNext=0;
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

                //prefill Links-list with 0-vals
                if(links.size() < scndHal) {
                    preFillList(links, scndHal);
                }

                if (!lines.get(i-1).equals("***")) {
                    //Teilen in VOR und NACH ">"
                    preS = lines.get(i-1).substring(0, lines.get(i-1).indexOf(">")-1);
                    postS = lines.get(i-1).substring(lines.get(i).indexOf(">")+2);
                    // get Hal numbers
                    firstHalNext = Integer.parseInt(preS.substring(0,1));
                    outPortNext = Integer.parseInt(preS.substring(2));
                    // get Port numbers
                    scndHalNext = Integer.parseInt(postS.substring(0,1));
                    inPortNext = Integer.parseInt(postS.substring(2));
                }
                //ArrayList of Pairs with index as haln
                if(lines.get(i-1).equals("***")) {
                    links.set(firstHal, Pair.with(null, outPort));
                } else if (i == lines.size()-1) {
                    links.set(scndHal, Pair.with(inPort, null));
                    links.set(firstHal, Pair.with(inPortNext, outPort));
                } else {
                    links.set(scndHalNext, Pair.with(inPortNext, outPort));
                }
                // DEBUG
                // System.out.println("firstHal: "+firstHal+", outPort: "+outPort+", scndHal: "+scndHal+", inPort: "+inPort);
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

        /*boolean help = true; 
        ArrayList<String> programs = new ArrayList<String>();
        for(int h = 0; h < lines.size()+1; h++) {
            if (lines.get(h).equals("***")) {
                help = false;
                continue;
            } else if (help) {
                halnr = Integer.parseInt(lines.get(h).substring(0,1));
                programm = lines.get(h).substring(lines.get(h).indexOf(" "), lines.get(h).length());
                programs.add(programm);
            } else if (!help){
                //Teilen in VOR und NACH ">"
                String pre = lines.get(h).substring(0, lines.get(h).indexOf(">")-1);
                String post = lines.get(h).substring(lines.get(h).indexOf(">")+2);
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

                //ArrayList of Pairs with index as haln
                links.get(firstHal).setAt1(outPort);
                links.get(scndHal).setAt0(inPort);

                //DEBUG
                System.out.println("firstHal: "+firstHal+", outPort: "+outPort+", scndHal: "+scndHal+", inPort: "+inPort);
            }
        }*/
        printList(links);
    }    
}
