import java.util.*;
import java.io.*;

/**
 * @author Finn Lidbetter
 * The TestAdditiveBasis class works with automata and the software package
 * "Walnut," created by Hamoon Mousavi, to determine whether an automaton
 * accepts a language with polynomial or exponential growth, and whether the
 * GCD of the values corresponding to the words accepted is one or not.
 * The algorithm for testing exponential vs. polynomial growth is given in
 * Gawrychowski, Krieger, Ramperad, Shallit 2010---Finding the Growth Rate
 * of a Regular Language in Polynomial Time, whereas the algorithm for testing
 * unit vs. non-unit GCD is described in Bell, Hare, Shallit 2018---When is an 
 * Automatic Set an Additive Basis?
 * This file also includes implementations of heuristic based implementations
 * for determining exponential vs. polynomial growth and unit vs. non-unit GCD
 * to further validate the correctness of the implementation of the algorithms.
 */

public class TestAdditiveBasis {
  static int VERBOSITY = 0;
  static boolean keepLogs = false;
  static boolean calculateOrder = false;
  static boolean quiet = false;
  static boolean calculateNonAsymptotic = false;
  static int maxOrder = Integer.MAX_VALUE;
  static int HEURISTIC_POLYNOMIAL_MAX_WORD_LEN = 62;
  static int HEURISTIC_GCD_MAX_WORD_LEN = 10;
  static String absolutePathToWalnut = System.getProperty("user.home")+"/Drive/Documents/Waterloo/walnut-for-linz";
  static String proverClassPath = absolutePathToWalnut+"/bin";
  static String[] startWalnutCommand = new String[]{"java","-Xms64g","-cp", proverClassPath, "Main.prover"};

  public static void main(String[] args) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    StringBuilder sb = new StringBuilder();
    
    if (args.length>0) {
      for (int i=0; i<args.length; i++) {
        String option = args[i];
        if (option.equals("-v")) {
          VERBOSITY++;
        } else if (option.equals("-l")) {
          keepLogs = true;
        } else if (option.equals("-o") || option.equals("-O")) {
          calculateOrder = true;
          if (option.equals("-O")) {
            calculateNonAsymptotic = true;
          }
          if (i<args.length-1) {
            try {
              maxOrder = Integer.parseInt(args[i+1]);
              i++;
            } catch(NumberFormatException e) {
              // Parsing failed. Ignore
            }
          }
        } else if (option.equals("-q")) {
          quiet = true;
        }
      }
    }
    
    int polyGrowthGCD1 = 0;
    int polyGrowthBadGCD = 0;
    int expGrowthGCD1 = 0;
    int expGrowthBadGCD = 0;
    int numAdditiveBasis = 0;
    TreeMap<Integer,Integer> asymptoticBasisOrderToCount = new TreeMap<>();
    TreeMap<Integer,Integer> basisOrderToCount = new TreeMap<>();
    String line = br.readLine();
    while (line!=null) {
      String[] autInfo = line.split(" ");
      if (autInfo[1].charAt(0)!='0') {
        line = br.readLine();
        continue;
      }
      Automaton aut = new Automaton(Integer.parseInt(autInfo[0]), autInfo[1], autInfo[2]);

      int autGCD = aut.computeGCD();
      int heuristicGCD = aut.heuristicGCD(HEURISTIC_GCD_MAX_WORD_LEN);
      
      if (autGCD!=heuristicGCD) {
        System.err.println("For "+line+", Walnut approach says GCD is : "+autGCD);
        System.err.println("Warning! Heuristic approach says GCD is "+heuristicGCD);
      }
      
      boolean polyGrowth = aut.isPolynomial();
      if (polyGrowth!=aut.heuristicIsPolynomial(HEURISTIC_POLYNOMIAL_MAX_WORD_LEN)) {
        if (polyGrowth) {
          System.err.println(line+" has polynomial growth");
        } else {
          System.err.println(line+" has exponential growth");
        }
        System.err.println("Warning! Heuristic approach says otherwise");
      }
      if ((autGCD==1 && !polyGrowth)) {
        expGrowthGCD1++;
        if (!quiet) {
          System.out.println(line);
        }
        if (aut.isAccepted("1")) {
          numAdditiveBasis++;
        }

        if (calculateOrder) {
          int asymptoticAdditiveBasisOrder = aut.getAdditiveBasisOrder(true);
          if (asymptoticBasisOrderToCount.containsKey(asymptoticAdditiveBasisOrder)) {
            asymptoticBasisOrderToCount.put(asymptoticAdditiveBasisOrder, asymptoticBasisOrderToCount.get(asymptoticAdditiveBasisOrder)+1);
          } else {
            asymptoticBasisOrderToCount.put(asymptoticAdditiveBasisOrder,1);
          }
          String orderString = (asymptoticAdditiveBasisOrder<=maxOrder) ? ""+asymptoticAdditiveBasisOrder : "greater than "+maxOrder;
          if (!quiet) {
            if (aut.isAccepted("1")) {
              System.out.print(" forms an additive basis and");
            } 
            System.out.println(" has asymptotic additive basis order "+orderString);
          }
          if (calculateNonAsymptotic && aut.isAccepted("1")) {
            int additiveBasisOrder = aut.getAdditiveBasisOrder(false);
            if (basisOrderToCount.containsKey(additiveBasisOrder)) {
              basisOrderToCount.put(additiveBasisOrder, basisOrderToCount.get(additiveBasisOrder)+1);
            } else {
              basisOrderToCount.put(additiveBasisOrder, 1);
            }
            orderString = (additiveBasisOrder<=maxOrder) ? ""+additiveBasisOrder : "greater than "+maxOrder;
            if (!quiet) {
              System.out.println(" has additive basis order "+orderString);
            }
          }
        }
      } else if (autGCD==1 && polyGrowth) {
        polyGrowthGCD1++;
      } else if (autGCD!=1 && !polyGrowth) {
        expGrowthBadGCD++;
      } else {
        polyGrowthBadGCD++;
      }
      line = br.readLine();
    }
    System.out.println("Polynomial growth and GCD!=1: "+polyGrowthBadGCD);
    System.out.println("Polynomial growth and GCD==1: "+polyGrowthGCD1);
    System.out.println("Exponential growth and GCD!=1: "+expGrowthBadGCD);
    System.out.println("Exponential growth and GCD==1: "+expGrowthGCD1);
    System.out.println("Form additive basis: "+numAdditiveBasis);
    for (int asymptoticBasisOrder:asymptoticBasisOrderToCount.keySet()) {
      String orderString = (asymptoticBasisOrder<=maxOrder) ? (""+asymptoticBasisOrder) : ("greater than "+maxOrder);
      System.out.println(asymptoticBasisOrderToCount.get(asymptoticBasisOrder)+" automata with asymptotic additive basis order "+orderString);
    }
    for (int basisOrder:basisOrderToCount.keySet()) {
      String orderString = (basisOrder<=maxOrder) ? (""+basisOrder) : ("greater than "+maxOrder);
      System.out.println(basisOrderToCount.get(basisOrder)+" automata with additive basis order "+orderString);
    }
    
  }

  static String executeCommand(String[] command) {
    StringBuffer output = new StringBuffer();
    Process p;
    try {
      p = Runtime.getRuntime().exec(command);
      p.waitFor();
      BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line = "";
      while ((line = reader.readLine())!= null) {
        output.append(line + "\n");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return output.toString();
  }
  
  
}
class Automaton {
  String canonicalString;
  int initialState = 0;
  int nStates;
  int alphabetSize;
  int[][] transition; 
  boolean[] accept;

  public Automaton(int nStates, String transitionString, String acceptString) {
    this.nStates = nStates;
    canonicalString = nStates+"_"+transitionString+"_"+acceptString;
    alphabetSize = transitionString.length()/nStates;
    transition = new int[nStates][alphabetSize];
    accept = new boolean[nStates];
    for (int i=0; i<transitionString.length(); i++) {
      int state = i/alphabetSize;
      int symbol = i%alphabetSize;
      int nextState = (transitionString.charAt(i))-'0';
      transition[state][symbol] = nextState;
    }
    for (int i=0; i<acceptString.length(); i++) {
      int acceptState = acceptString.charAt(i)-'0';
      accept[acceptState] = true;
    }
  }
  public Automaton(int nStates, String transitionString, String acceptString, int initialState) {
    this(nStates, transitionString, acceptString);
    this.initialState = initialState;
  }
  public boolean[][] getAdj() {
    boolean[][] adj = new boolean[nStates][nStates];
    for (int i=0; i<nStates; i++) {
      for (int j=0; j<alphabetSize; j++) {
        adj[i][transition[i][j]] = true;
      }
    }
    return adj;
  }

  public int heuristicGCD(int certainty) {
    int max = 1<<certainty;
    int currGCD = 0;
    for (int i=1; i<max; i++) {
      boolean goodWord = isAccepted(Integer.toBinaryString(i));
      if (goodWord && currGCD==0) {
        currGCD = i;
      } else if (goodWord) {
        currGCD = gcd(i,currGCD);
      }
    }
    return currGCD;
  }

  public boolean isAccepted(String word) {
    int currState = initialState;
    for (int i=0; i<word.length(); i++) {
      currState = transition[currState][word.charAt(i)-'0'];
    }
    return accept[currState];
  }

  private int gcd(int a, int b) {
    return (b>0) ? gcd(b, a%b) : a;
  }

  
  public int getAdditiveBasisOrder(boolean asymptotic) {
    File walnutAutFile = createWalnutAutomatonFile();
    Process p;
    try {
      p = Runtime.getRuntime().exec(TestAdditiveBasis.startWalnutCommand);
      BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
      BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
      boolean basisOrderFound = false;
      int nSummands = 1;
      while (!basisOrderFound) {
        String fileName = "ord"+nSummands+"_"+canonicalString;
        String walnutCommand = "";
        if (asymptotic) {
          walnutCommand = getWalnutAsymptoticAdditiveBasisOrderCommandString(nSummands,fileName);
        } else {
          walnutCommand = getWalnutAdditiveBasisOrderCommandString(nSummands,fileName);
        }
        writer.write(walnutCommand);
        writer.flush();
        File resultFile = new File(TestAdditiveBasis.absolutePathToWalnut+"/Result/"+fileName+".txt");
        while (!resultFile.exists()) {
          Thread.sleep(5);
        }
        BufferedReader resultReader = new BufferedReader(new FileReader(resultFile));
        String result = "";
        while ((result=resultReader.readLine())==null) {
          Thread.sleep(5);
        }
        if (result.equals("true")) {
          resultReader.close();
          cleanup(p, reader, errorReader, writer);
          deleteWalnutResultFiles(fileName);
          while (!walnutAutFile.delete());
          return nSummands;
        }
        verbosityMessage(nSummands+" summands is not enough for "+canonicalString, 1);
        resultReader.close();
        deleteWalnutResultFiles(fileName);
        if (nSummands>=TestAdditiveBasis.maxOrder) {
          cleanup(p,reader,errorReader,writer);
          return Integer.MAX_VALUE;
        }
        nSummands++;
      }
      cleanup(p, reader, errorReader, writer);
    } catch (Exception e) {
      e.printStackTrace();
    }
    while (!walnutAutFile.delete());
    return 0;
  }

  private String getWalnutAdditiveBasisOrderCommandString(int nSummands, String fileName) {
    String fixed = "eval "+fileName+" \"A n ";
    StringBuilder variableListBuilder = new StringBuilder();
    StringBuilder variableMembershipBuilder = new StringBuilder();
    StringBuilder sumBuilder = new StringBuilder();
    for (int i=0; i<nSummands; i++) {
      variableListBuilder.append("x"+i);
      variableMembershipBuilder.append("((LL[x"+i+"]=@1)|(x"+i+"=0))");
      sumBuilder.append("x"+i);
      if (i!=nSummands-1) {
        variableListBuilder.append(",");
        variableMembershipBuilder.append("&");
        sumBuilder.append("+");
      }
    }
    String command = fixed+"(E "+variableListBuilder.toString()+" "+variableMembershipBuilder.toString()+"&(n="+sumBuilder.toString()+"))\":\n";
    return command;
  }
  private String getWalnutAsymptoticAdditiveBasisOrderCommandString(int nSummands, String fileName) {
    String fixed = "eval "+fileName+" \"E m (A n (n>=m)=>";
    StringBuilder variableListBuilder = new StringBuilder();
    StringBuilder variableMembershipBuilder = new StringBuilder();
    StringBuilder sumBuilder = new StringBuilder();
    for (int i=0; i<nSummands; i++) {
      variableListBuilder.append("x"+i);
      variableMembershipBuilder.append("((LL[x"+i+"]=@1)|(x"+i+"=0))");
      sumBuilder.append("x"+i);
      if (i!=nSummands-1) {
        variableListBuilder.append(",");
        variableMembershipBuilder.append("&");
        sumBuilder.append("+");
      }
    }
    String command = fixed+"(E "+variableListBuilder.toString()+" "+variableMembershipBuilder.toString()+"&(n="+sumBuilder.toString()+")))\":\n";
    return command;
  }

  public int computeGCD() {
    File autFile = createWalnutAutomatonFile();
    ArrayList<Integer> candidateGCDs = getCandidateGCDs();
    if (candidateGCDs==null)
      return 0;
    int gcd = executeGCDCommands(candidateGCDs);
    autFile.delete();
    return gcd;
  }
  
  private int executeGCDCommands(ArrayList<Integer> candidates) {
    Process p;
    try {
      p = Runtime.getRuntime().exec(TestAdditiveBasis.startWalnutCommand);
      BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
      BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
      for (int candidate:candidates) {
        String fileIdentifier = canonicalString;
        String fileName = "gcd"+candidate+"_"+fileIdentifier;
        String walnutCommand = "eval "+fileName+" \"A n (LL[n]=@1)=>(E t (n="+candidate+"*t))\":\n";
        writer.write(walnutCommand);
        writer.flush();
        File resultFile = new File(TestAdditiveBasis.absolutePathToWalnut+"/Result/"+fileName+".txt");
        while (!resultFile.exists()) {
          Thread.sleep(5);
        }
        BufferedReader resultReader = new BufferedReader(new FileReader(resultFile));
        String result = "";
        while ((result=resultReader.readLine())==null) {
          Thread.sleep(5);
        }
        if (result.equals("true")) {
          resultReader.close();
          cleanup(p, reader, errorReader, writer);
          deleteWalnutResultFiles(fileName);
          return candidate;
        }
        resultReader.close();
        deleteWalnutResultFiles(fileName);
      }
      cleanup(p, reader, errorReader, writer);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 0;
  }
  private void deleteWalnutResultFiles(String fileName) {
    String walnutResultPath = TestAdditiveBasis.absolutePathToWalnut+"/Result/";
    File resultFile = new File(walnutResultPath+fileName+".txt");
    File logFile = new File(walnutResultPath+fileName+"_log.txt");
    File gvFile = new File(walnutResultPath+fileName+".gv");
    while (!resultFile.delete());
    if (!TestAdditiveBasis.keepLogs) {
      while (!logFile.delete());
    }
    while (!gvFile.delete());
  }
  

  private void cleanup(Process p, BufferedReader pOutputReader, BufferedReader pErrorReader, BufferedWriter pInputWriter) {
    try {
      pInputWriter.write("exit:");
      pInputWriter.flush();
      if (pErrorReader.ready()) {
        String error = pErrorReader.readLine();
        System.out.println("");
        while (error!=null) {
          System.out.println(error);
          error = pErrorReader.readLine();
        }
      }
      if (pOutputReader.ready()) {
        String output = pOutputReader.readLine();
        while (output!=null) {
          if (pOutputReader.ready())
            output = pOutputReader.readLine();
          else
            output = null;
        }
      }
      pOutputReader.close();
      pErrorReader.close();
      pInputWriter.close();
    } catch(IOException e) {
      System.err.println("Failed closing process i/o readers/writer");
    }
    try {
      p.waitFor();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }


  public File createWalnutAutomatonFile() {
    String walnutString = toWalnutString();
    String filePath = TestAdditiveBasis.absolutePathToWalnut+"/Word Automata Library/LL.txt";
    File f = new File(filePath); 
    Writer writer = null;
    try {
      writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "utf-8"));
      writer.write(walnutString);
    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      try {
        writer.close();
      } catch (Exception ex) {
        /*ignore*/
      }
    }
    return f;
  }

  public ArrayList<Integer> getCandidateGCDs() {
    String smallestNonZeroAcceptedWord = getSmallestNonZeroAccepted();
    if (smallestNonZeroAcceptedWord==null || smallestNonZeroAcceptedWord.length()==0) {
      return null;
    }
    int smallestNonZeroAcceptedValue = Integer.parseInt(smallestNonZeroAcceptedWord,alphabetSize);
    int hi = (int)Math.sqrt(smallestNonZeroAcceptedValue);
    ArrayList<Integer> candidateGCDs = new ArrayList<>();
    for (int i=1; i<=hi; i++) {
      if (smallestNonZeroAcceptedValue%i==0) {
        int div1 = i;
        int div2 = smallestNonZeroAcceptedValue/i;
        candidateGCDs.add(div1);
        if (div2!=div1)
          candidateGCDs.add(div2);
      }
    }
    Collections.sort(candidateGCDs, Collections.reverseOrder());
    return candidateGCDs;
  }

  public String getSmallestNonZeroAccepted() {
    LinkedList<Integer> q = new LinkedList<Integer>();
    boolean[] vis = new boolean[nStates];
    int[] prev = new int[nStates];
    int[] prevSymbol = new int[nStates];
    for (int symbol=1; symbol<alphabetSize; symbol++) {
      int startState = transition[initialState][symbol];
      if (!vis[startState]) {
        q.offer(startState);
        vis[startState] = true;
        prev[startState] = initialState;
        prevSymbol[startState] = symbol;
      }
    }
    boolean pathFound = false;
    int endState = -1;
    while (q.size()>0) {
      int curr = q.poll();
      if (accept[curr]) {
        pathFound = true;
        endState = curr;
        break;
      }
      for (int symbol=0; symbol<alphabetSize; symbol++) {
        int nextState = transition[curr][symbol];
        if (!vis[nextState]) {
          vis[nextState] = true;
          prev[nextState] = curr;
          prevSymbol[nextState] = symbol;
          q.offer(nextState);
        }
      }
    }
    if (!pathFound) {
      return null;
    }
    int curr = endState;
    boolean nonZeroAppended = false;
    StringBuilder sb = new StringBuilder();
    sb.append(prevSymbol[curr]);
    if (prevSymbol[curr]!=0)
      nonZeroAppended = true;
    curr = prev[curr];
    while (curr!=initialState || !nonZeroAppended) {
      sb.append(prevSymbol[curr]);
      if (!nonZeroAppended && prevSymbol[curr]!=0) {
        nonZeroAppended = true;
      }
      curr = prev[curr];
    }
    sb.reverse();
    return sb.toString();
  }

  public boolean isPolynomial() {
    boolean[] commutative = new boolean[nStates];
    HashSet<Integer> completedComponents = new HashSet<Integer>();
    int[] component = getComponents();
    Integer[] aSetOfState = new Integer[nStates];
    
    for (int i=0; i<nStates; i++) {
      verbosityMessage("State: "+i, 3);
      if (completedComponents.contains(component[i]) || !isCoReachable(i))
        continue;
      String cyclingWord = findCyclingWord(i, component);
      if (cyclingWord==null) 
        continue;
      verbosityMessage("Cycling word: "+cyclingWord, 3);
      String primitiveRoot = getPrimitiveRoot(cyclingWord);
      verbosityMessage("Primitive root: "+primitiveRoot, 3);
      int primitiveRootLen = primitiveRoot.length();
      ArrayList[] aStates = new ArrayList[primitiveRootLen];
      for (int j=0; j<primitiveRootLen; j++) {
        aStates[j] = new ArrayList<Integer>();
      }
      int residue = 0;
      boolean commutativeAssignment = assignStatesToASets(i, primitiveRoot, residue, component, aSetOfState, aStates);
      verbosityMessage("aSetOfState: "+Arrays.toString(aSetOfState), 3);
      if (!commutativeAssignment) {
        return false;
      }
      commutativeAssignment = verifyAssignment(primitiveRoot, aStates, component);
      if (!commutativeAssignment)
        return false;
      completedComponents.add(component[i]);
    }
    return true;
  }

  public boolean heuristicIsPolynomial(int powerLimit) {
    long[][] transitionMatrix = new long[nStates][nStates];
    for (int i=0; i<nStates; i++) {
      for (int symbol=0; symbol<alphabetSize; symbol++) {
        transitionMatrix[i][transition[i][symbol]]++;
      }
    }
    int maxExpPow = 0;
    while ((1<<maxExpPow)<powerLimit) {
      maxExpPow++;
    }
    long[][][] exponentialPowers = new long[maxExpPow][nStates][nStates];
    exponentialPowers[0] = transitionMatrix;
    for (int i=1; i<maxExpPow; i++) {
      exponentialPowers[i] = matrixMult(exponentialPowers[i-1], exponentialPowers[i-1]);
    }
    int baseWordLength = powerLimit-nStates;
    long maxAccepted = 0;
    for (int i=0; i<nStates; i++) {
      int wordLength = baseWordLength+i;
      long numWords = countWordsOfLength(wordLength, exponentialPowers);
      if (numWords>maxAccepted) {
        maxAccepted = numWords;
      }
    }
    long exponentialThreshold = (1L<<((powerLimit-nStates)/nStates));
    verbosityMessage("maxAccepted: "+maxAccepted, 2);
    verbosityMessage("Threshold: "+exponentialThreshold, 2);
    return maxAccepted<exponentialThreshold;
  }
 
  private long countWordsOfLength(int wordLength, long[][][] exponentialPowers) {
    long[][] prod = new long[nStates][nStates];
    for (int i=0; i<nStates; i++) {
      prod[i][i] = 1;
    }
    int powIndex = 0;
    while ((1<<powIndex)<wordLength) {
      if (((1<<powIndex)&wordLength)!=0) {
        prod = matrixMult(prod, exponentialPowers[powIndex]);
      }
      powIndex++;
    }
    long sum = 0;
    for (int i=0; i<nStates; i++) {
      if (accept[i])
        sum += prod[0][i];
    }
    return sum;
  }
  

  private long[][] matrixMult(long[][] a, long[][] b) {
    int m = a.length;
    int n = b[0].length;
    long[][] result = new long[m][n];
    for (int i=0; i<m; i++) {
      for (int j=0; j<n; j++) {
        long sum = 0;
        for (int k=0; k<a[0].length; k++) {
          sum += a[i][k] * b[k][j];
        }
        result[i][j] = sum;
      }
    }
    return result;
  }


  private boolean isCoReachable(int state) {
    boolean[] vis = new boolean[nStates];
    vis[state] = true;
    return coReachableDfs(state, vis);
  }

  private boolean coReachableDfs(int curr, boolean[] vis) {
    if (accept[curr])
      return true;
    boolean result = false;
    for (int symbol=0; symbol<alphabetSize; symbol++) {
      int nextState = transition[curr][symbol];
      if (!vis[nextState]) {
        vis[nextState] = true;
        result = result || coReachableDfs(nextState, vis);
      }
    }
    return result;
  }

  private boolean verifyAssignment(String primitiveRoot, ArrayList[] aStates, int[] component) {
    for (int i=0; i<aStates.length; i++) {
      for (Object objIntState:aStates[i]) {
        int state = (Integer) objIntState;
        for (int symbol=0; symbol<alphabetSize; symbol++) {
          int nextState = transition[state][symbol];
          if (component[state]==component[nextState] && symbol!=primitiveRoot.charAt(i)-'0') {
            return false;
          }
        }
      }
    }
    return true;
  }

  private boolean assignStatesToASets(int state, String primitiveRoot, int residue, int[] component, Integer[] aSetOfState, ArrayList[] aStates) {
    if (aSetOfState[state]==null) {
      aSetOfState[state] = residue;
      aStates[residue].add(state);
      int nextSymbol = primitiveRoot.charAt(residue%primitiveRoot.length())-'0';
      int nextState = transition[state][nextSymbol];
      verbosityMessage("nextSymbol: "+nextSymbol,3);
      verbosityMessage("nextState: "+nextState,3);
      if (component[nextState]==component[state]) {
        return assignStatesToASets(nextState, primitiveRoot, (residue+1)%primitiveRoot.length(), component, aSetOfState, aStates);
      }
      return true;
    } else {
      return aSetOfState[state]==residue;
    }
  }

  public int[] getComponents() {
    boolean[][] adj = getAdj();
    Tarjan scc = new Tarjan(adj);
    return scc.id;
  }

  public String findCyclingWord(int state, int[] component) {
    boolean[] vis = new boolean[nStates];
    int[] prev = new int[nStates];
    int[] prevSymbol = new int[nStates];
    boolean cycleFound = false;
    int targetState = state;
    for (int i=0; i<alphabetSize; i++) {
      int next = transition[state][i];
      prev[next] = state;
      prevSymbol[next] = i;
      vis[next] = true;
      cycleFound = dfs(targetState, next, component, vis, prev, prevSymbol);
      if (cycleFound) {
        return buildCyclingWord(state, prev, prevSymbol);
      }
    }
    return null;
  }

  private String buildCyclingWord(int state, int[] prev, int[] prevSymbol) {
    StringBuilder sb = new StringBuilder();
    sb.append(prevSymbol[state]);
    int curr = prev[state];
    while (curr!=state) {
      sb.append(prevSymbol[curr]);
      curr = prev[curr];
    }
    sb.reverse();
    return sb.toString();
  }

  private boolean dfs(int targetState, int curr, int[] component, boolean[] vis, int[] prev, int[] prevSymbol) {
    if (curr==targetState)
      return true;
    boolean result = false;
    for (int i=0; i<alphabetSize; i++) {
      int next = transition[curr][i];
      if (component[next]==component[targetState] && !vis[next]) {
        prev[next] = curr;
        prevSymbol[next] = i;
        vis[next] = true;
        result = result || dfs(targetState, next, component, vis, prev, prevSymbol);
      }
    }
    return result;
  }

  public String getPrimitiveRoot(String word) {
    String txt = word.substring(1,word.length()) + word;
    int firstMatchIndex = kmpFirstMatchIndex(txt, word);
    return word.substring(0, firstMatchIndex+1);
  }

  private int kmpFirstMatchIndex(String txt, String pat) {
    int m = pat.length();
    int n = txt.length();
    int i = 0;
    int j = 0;
    int[] arr = kmpHelper(pat, m);
    int minMatch = txt.length()+1;
    while (i < n) {
      if (pat.charAt(j) == txt.charAt(i)) { 
        j++; 
        i++; 
      }
      if (j == m) {
        if (i-j<minMatch) {
          minMatch = i-j;
        }
        j = arr[j-1];
      } else if (i < n && pat.charAt(j) != txt.charAt(i)) {
        if (j != 0) 
          j = arr[j-1];
        else i = i+1; 
      } 
    }
    return minMatch; 
  }
  private int[] kmpHelper(String pat, int m) {
    int [] arr = new int[m];
    for (int i = 1, len = 0; i < m;) {
      if (pat.charAt(i) == pat.charAt(len)) {
        arr[i++] = ++len;
      } else {
        if (len > 0) 
          len = arr[len-1];
        else i++;
      }
    }
    return arr; 
  }
  static void verbosityMessage(String msg, int level) {
    if (TestAdditiveBasis.VERBOSITY>=level) {
      System.out.println(msg);
    }
  }
  public String toWalnutString() {
    StringBuilder sb = new StringBuilder();
    sb.append("msd_2\n");
    for (int state=0; state<nStates; state++) {
      sb.append(state+" "+(accept[state] ? "1\n" : "0\n"));
      for (int symbol=0; symbol<alphabetSize; symbol++) {
        sb.append(symbol+" -> "+transition[state][symbol]+"\n");
      }
    }
    return sb.toString();
  }
}
class Tarjan {
  // Class for computing the Strongly Connected Components of a graph
  boolean[][] adj;
  int n = 0;
  int pre = 0;
  int count = 0;
  boolean[] marked;
  int[] id, low;
  Stack<Integer> stack = new Stack<>();
  public Tarjan(boolean[][] adj) {
    n = adj.length; 
    this.adj = adj;
    marked = new boolean[n]; 
    id = new int[n]; 
    low = new int[n];
    for (int u = 0; u < n; u++) 
      if (!marked[u]) 
        dfs(u); 
  }
  public void dfs(int u) {
    marked[u] = true; 
    low[u] = pre++; 
    int min = low[u]; 
    stack.push(u);
    for (int v = 0; v < n; v++) {
      if (adj[u][v]) {
        if (!marked[v]) 
          dfs(v);
        if (low[v] < min) 
          min = low[v]; 
      } 
    }
    if (min < low[u]) { 
      low[u] = min; 
      return; 
    }
    int v;
    do { 
      v = stack.pop(); 
      id[v] = count; 
      low[v] = n; 
    } while (v != u);
    count++; 
  } 
}
