import java.util.*;
import org.javatuples.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HAL extends Thread {

    private ArrayList<buffer> bufIn = new ArrayList<buffer>();
    private ArrayList<buffer> bufOut = new ArrayList<buffer>();
    private String PATH;
    private ArrayList<Integer> bpIn = new ArrayList<Integer>();
    private ArrayList<Integer> bpOut = new ArrayList<Integer>();
    // private boolean debug = false;
    private BufferedReader br;
    private ArrayList<Triplet<Float, String, Float>> line = new ArrayList<Triplet<Float, String, Float>>();
    // int pc = 0;
    private float regs[] = { 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
    private float accu = 0;

    //Constructors
    // Constructor for just one link to other HALs
    public HAL(String p, int bufferPortIn, int bufferPortOut){
        PATH = p;
        if (bufferPortIn == -1) {
            bpIn.add(null);
        } else {
            bpIn.add(bufferPortIn);
        }
        if (bufferPortOut == -1) {
            bpOut.add(null);
        } else {
            bpOut.add(bufferPortOut);
        }
    }

    public HAL(String p, buffer bIn, buffer bOut, int bufferPortIn, int bufferPortOut){
        PATH = p;
        if (bufferPortIn == -1) {
            bpIn.add(null);
        } else {
            bpIn.add(bufferPortIn);
        }
        if (bufferPortOut == -1) {
            bpOut.add(null);
        } else {
            bpOut.add(bufferPortOut);
        }
        bufIn.add(bIn);
        bufOut.add(bOut);
    }

    public void setInBuffer(buffer inBuffer) {
        bufIn.add(inBuffer);
    }

    public void setOutBuffer(buffer outBuffer) {
        bufOut.add(outBuffer);
    }

    public void printObj() {
        System.out.println("Path: "+PATH+", buffer port in: "+bpIn+", buffer port out: "+bpOut+", buffer in: "+bufIn+", buffer out: "+bufOut);
    }

    public buffer getInBuffer() {
        return bufIn.get(bufIn.size());
    }

    public int getbufferPortCount(ArrayList<Integer> list) {
        int count= 0;
        for(Integer i : list) {
            if (i != null) {
                count = count+1;
            } else {continue;}
        }
        return count;
    }



    //get programm code
    public ArrayList<Triplet<Float, String, Float>> LoopThroughFile(File f, BufferedReader reader,
                                                                           ArrayList<Triplet<Float, String, Float>> l) {
        String st = "";
        Triplet<Float, String, Float> singleLine = Triplet.with(null, null, null); // new
        // Triplet<Float,String,Float>(null,
        // null, null);
        try {
            while ((st = reader.readLine()) != null) {
                // Get LineNumber
                Float n = Float.parseFloat(st.substring(0, st.indexOf(" ")));
                singleLine = singleLine.setAt0(Float.valueOf(n));
                // Get Instruction Name
                String i = st.substring(st.indexOf(" ") + 1, st.lastIndexOf(" "));
                singleLine = singleLine.setAt1(i);
                // Get Operand
                if (!i.equals("START") && !i.equals("STOP")) {
                    // Add vals to Triplet
                    Float o = Float.parseFloat(st.substring(st.lastIndexOf(" ") + 1)); // rafft er nicht weil in "1 START"
                    // keine 3. value existiert
                    singleLine = singleLine.setAt2(o);
                } else {
                    singleLine = singleLine.setAt2((float)0.0);
                }
                l.add(singleLine);
            }
        } catch (IOException ioe) {
            System.out.println("File not found.");
            System.out.printf("File: %s not found", st);
            System.exit(0);
        }
        return l;
    }

    public void printArrayList(ArrayList<Triplet<Float, String, Float>> l) {
        for (Triplet<Float, String, Float> t : l) {
            System.out.println(t.getValue0() + " " + t.getValue1() + " " + t.getValue2());
        }
    }

    public void printRegustus(float[] r) {
        for (float i : r) {
            System.out.println(i);
        }
    }

    public static int findIndex(ArrayList<Integer> list, Float val) {
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i) == null) {
                return 0;
            } else {
                if ((float) list.get(i) == val) {
                    return i;
                }
            }
        }
        return 0;
    }

    public float IN(int i, ArrayList<Triplet<Float, String, Float>> l, float acc) {
        Scanner userInput = new Scanner(System.in);
        acc = Float.MAX_VALUE;
        while (acc == Float.MAX_VALUE) {
            try {
                System.out.println("Please enter value in I/O " + l.get(i).getValue2() + ": ");
                acc = userInput.nextFloat();
            } catch (InputMismatchException t) {
                System.err.println("Input does not match the float regular expression\n" +
                        "Format is x.xx or x,xx ..depends");
                userInput.next();
            } catch(NoSuchElementException x) {
                System.err.println(x);
                userInput.next();
            }
        }
        // userInput.close();
        return acc;
    }

    public void run() {
        // Loop args for programm-path & check if file si readable
        if (Files.isReadable(Paths.get(PATH))) {
            try {
                File programm = new File(PATH);
                br = new BufferedReader(new FileReader(programm));
                LoopThroughFile(programm, br, line);
                // printArrayList(line);
            } catch (Exception x) {
                System.err.print(x+"\n");
                System.err.print(PATH+"\n");
                // System.out.println("Please insert a programm you want to run! (PATH)");
                System.exit(0);
            }
        }

        // for(Triplet<Float, String, Float> t: line) {
        for (int i = 0; i < line.size(); i++) {
            switch (line.get(i).getValue1()) {
                case "START":
                    regs[0] = 0;
                    break;

                case "STOP":
                    regs[0] = 0;
                    System.exit(1);
                    break;

                case "OUT":
                    if (bpOut.get(0) == null) {
                        // System.exit(1);
                        // System.out.println("null");
                        if (line.get(i).getValue2() == 1) {
                            System.out.println(PATH);
                            System.out.println("Aktueller Akkumulatorinhalt: [" + accu + "].\n");
                            regs[0] = +regs[0];
                        }
                    } else {
                        int iBpOut = findIndex(bpOut, line.get(i).getValue2());
                        if (line.get(i).getValue2() == 1) {
                            System.out.println(PATH);
                            System.out.println("Aktueller Akkumulatorinhalt: [" + accu + "].\n");
                            regs[0] = +regs[0];
                        } else if (line.get(i).getValue2() == (float) bpOut.get(iBpOut)) {
                            bufOut.get(iBpOut).put((int) accu);
                            regs[0] = +regs[0];
                        } else if (bpOut.size() == 0) {
                            System.err.println("No output port assigned!");
                            regs[0] = +regs[0];
                        } else {
                            regs[0] = +regs[0];
                        }
                    }
                    break;

                case "IN":
                    if (getbufferPortCount(bpIn) > bufIn.size()) {
                        System.err.print("to many buffer ports for buffers");
                        System.exit(1);
                    } else if(getbufferPortCount(bpIn) < bufIn.size()) {
                        System.err.print("to many buffers for buffer ports");
                        System.exit(1);
                    }    

                    int iBpIn = findIndex(bpIn, line.get(i).getValue2());
                    if (line.get(i).getValue2() == 1) {
                        // accu = IN(debug, i, line, regs, accu);
                        
                        accu = IN(i, line, accu);
                    } else if (line.get(i).getValue2() == (float) bpIn.get(iBpIn)) {
                        accu = bufIn.get(iBpIn).get();
                        regs[0] = +regs[0];
                    } else if(bpIn.get(i) == null) {
                        System.err.println("No output port assigned!");
                        regs[0] = +regs[0];

                    } else {
                        regs[0] = +regs[0];
                    }
                    break;

                case "LOAD":
                    if (line.get(i).getValue2() == 0) {
                        System.out.println("Provide Register Adress.");
                        System.exit(1);
                    } else {
                        accu = line.get(i).getValue2();
                        regs[0] = +regs[0];
                    }
                    break;

                case "LOADNUM":
                    if (line.get(i).getValue2() == 0) {
                        System.out.println("Provide Register Adress.");
                        System.exit(1);
                    } else {
                        accu = line.get(i).getValue2();
                        regs[0] = +regs[0];
                    }
                    break;

                case "STORE":
                    if (line.get(i).getValue2() == 0) {
                        System.out.println("Provide Register Adress.");
                        System.exit(1);
                    } else {
                        accu = regs[(int) (line.get(i).getValue2() + 0)];
                        regs[0] = +regs[0];
                    }
                    break;

                case "JUMPNEG":
                    if (line.get(i).getValue2() == 0) {
                        System.out.println("Provide Register Adress.");
                        System.exit(1);
                    } else {
                        if (accu < 0) {
                            i = (int) (line.get(i).getValue2() + 0);
                        }
                    }
                    break;

                case "JUMPPOS":
                    if (line.get(i).getValue2() == 0) {
                        System.out.println("Provide Register Adress.");
                        System.exit(1);
                    } else {
                        if (accu > 0) {
                            i = (int) (line.get(i).getValue2() + 0);
                        }
                    }
                    break;

                case "JUMPNULL":
                    if (line.get(i).getValue2() == 0) {
                        System.out.println("Provide Register Adress.");
                        System.exit(1);
                    } else {
                        if (accu == 0) {
                            i = (int) (line.get(i).getValue2() + 0);
                        }
                    }
                    break;

                case "JUMP":
                    if (line.get(i).getValue2() == 0) {
                        System.out.println("Porgramm Counter Adress.");
                        System.exit(1);
                    } else {
                        i = (int) (line.get(i).getValue2()+0);
                        i = i-2;
                    }
                    break;

                case "ADD":
                    accu = accu + regs[(int) (line.get(i).getValue2()+0)];
                    break;

                case "ADDNUM":
                    accu = accu + line.get(i).getValue2();
                    break;

                case "SUB":
                    accu = accu - regs[(int) (line.get(i).getValue2()+0)];
                    break;

                case "MUL":
                    accu = accu * regs[(int) (line.get(i).getValue2()+0)];
                    break;

                case "DIV":
                    accu = accu / regs[(int)(line.get(i).getValue2()+0)];
                    break;

                case "SUBNUM":
                    accu = accu - line.get(i).getValue2();
                    break;

                case "MULNUM":
                    accu = accu * line.get(i).getValue2();
                    break;

                case "DIVNUM":
                    accu = accu * line.get(i).getValue2();
                    break;

                case "LOADIND":
                    accu = regs[(int)(line.get(i).getValue2()+0)];
                    break;

                case "STOREIND":

                    regs[(int)(line.get(i).getValue2()+0)] = accu;

                    break;
                default:
                    System.out.println("wtf u eingebing man?");
                    System.exit(1);
            }
        }
    }
}