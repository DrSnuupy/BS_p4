import java.util.*;

// import javax.swing.text.html.HTMLDocument.BlockElement;

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

    public static void printList(ArrayList<ArrayList<Integer>> list) {
        for (int k = 0; k < list.size(); k++) {
            System.out.println("Hal Prozessor: " +k+", mit den Ports: ");
            for(int l = 0; l < list.size(); l++){
                System.out.println(list.get(l)+", ");
            } 
        }
        System.out.println("'-' Werte sind out-Ports und '+' Werte in-Ports");
    }

    //decider true -> search for outPorts | false -> search for inPorts | of nothing matches return null
    public static Integer findPort(ArrayList<Quartet<Integer,Integer,Integer,Integer>> list, int hal, boolean decider) {
        for(int i = 0; i < list.size(); i++) {
            if (decider == true) {
                if (list.get(i).getValue0() == hal) {
                    return list.get(i).getValue1();
                } 
            }else {
                if(list.get(i).getValue2() == hal) {
                    return list.get(i).getValue3();
                }
            }
        }
        return -1;
    }

    //bekommt config file übergeben
    public static void main(String[] args) { // Argumente kann nur ein Path zur Config-datei sein
        // int maxsize = 16;
        BufferedReader br;
        ArrayList<String> lines = new ArrayList<String>();
        ArrayList<String> programs = new ArrayList<String>();
        ArrayList<HAL> processes = new ArrayList<HAL>();
        ArrayList<buffer> buf = new ArrayList<buffer>();
        ArrayList<Quartet<Integer,Integer,Integer,Integer>> links = new ArrayList<Quartet<Integer,Integer,Integer,Integer>>();
        
        // for (int n = 0; n < maxsize; n++) {
        //     programs.add("null");
        // }

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

        //loop through lines and extract values
        boolean halprg = true; String programm= ""; int halnr;
        for(int i = 0; i < lines.size(); i++) {
            if (lines.get(i) == "***") { 
                halprg = false;
                continue;
            } else if(!halprg) { // links between HAL processors
                //before ">"
                int outHal = Integer.parseInt(lines.get(i).substring(0,lines.get(i).indexOf(":")));
                int outPort = Integer.parseInt(lines.get(i).substring(lines.get(i).indexOf(":")+1,lines.get(i).indexOf(">")-1));        

                //after ">"
                int inHal = Integer.parseInt(lines.get(i).substring(lines.get(i).indexOf(">")+2, lines.get(i).lastIndexOf(":")));
                int inPort = Integer.parseInt(lines.get(i).substring(lines.get(i).lastIndexOf(":")+1));
                
                //add to List
                links.add(Quartet.with(outHal, outPort, inHal, inPort));

            } else { // HAL programm paths
                //get substrings of HAL program paths
                halnr = Integer.parseInt(lines.get(i).substring(0,1));
                programm = lines.get(i).substring(lines.get(i).indexOf(" ")+1, lines.get(i).length());
                programs.add(programm);
                // programs.set(halnr, programm);
            }
        }

        // create HAL objects with right links
        for (int i = 0; i < programs.size(); i++) {
            HAL h = new HAL(programs.get(i), findPort(links, i+1, false), findPort(links, i+1, true));
            processes.add(h);
            h.printObj();
        }

        System.out.print("\nWith buffers:\n");
        // create buffer objects
        for (int i = 0; i < links.size(); i++) {
            buffer b = new buffer();
            buf.add(b);
            //assign buffer to HAL
            processes.get(links.get(i).getValue0()-1).setOutBuffer(b);
            processes.get(links.get(i).getValue2()-1).setInBuffer(b);

            processes.get(i).printObj();
        }
        processes.get(links.size()).printObj();

        // start and run all processes
        for (HAL h : processes) {
            h.start();
        }
    }  
}