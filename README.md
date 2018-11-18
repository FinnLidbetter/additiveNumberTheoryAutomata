# additiveNumberTheoryAutomata
Containing files and code relating to Thomas Finn Lidbetter's University of Waterloo master's thesis.

---

The `list2.txt`, `list3.txt`, etc., files contain representations of all minimal automata with 2, 3, etc., states respectively. Note that the file containing all minimal automata with 5 states was too large to be uploaded directly to github without compression. 

The `additiveBasisOrders-listN.txt` files list the automata that correspond to sets forming (asymptotic) additive bases, together with their additive basis order and asymptotic additive basis order.

The `additiveBasisStats-listN.txt` files list the automata that correspond to sets forming (asymptotic) additive bases, and list the number of automata that have `GCD=1` vs. `GCD!=1` and the number that are sparse vs. non-sparse.

The `TestAdditiveBasis.java` file contains the code used to produce the `additiveBasisOrders-listN.txt` and `additiveBasisStats-listN.txt` files. This program has a number of command line options. Most useful among these are the `-o [maxAsymptoticOrder]` and `-O [maxOrder]`. The `-o` option results in the program calculating the asymptotic additive basis order of those sets, corresponding to automata, that from asymptotic additive bases. The optional argument is the maximum order to test. The `-O` option results in the program calculating both the asymptotic additive basis order and the additive basis order. Again, this option can be followed by a number to specify the greatest order to test before giving up. 

Note that in order to get the `TestAdditiveBasis.java` program working correctly, it is necessary to make the following modifications to the `TestAdditiveBasis.java` file and the `src/Main.UtilityMethods.java` file in the `Walnut` package.

In the `src/Main.UtilityMethods.java` file in the `setPaths()` method replace the following lines:
```
if (path.substring(path.length()-3).equals("bin")) {
  dir = "../";
}
```
with
```
String ABSOLUTE_PATH_TO_WALNUT = "insert the absolute path to the Walnut directory";
dir =  ABSOLUTE_PATH_TO_WALNUT;
```
Similarly, in the `TestAdditiveBasis.java` file replace line 29 
```
static String absolutePathToWalnut = System.getProperty("user.home")+"/Drive/Documents/Waterloo/walnut-for-linz"
```
with
```
static String absolutePathToWalnut = insert the absolute path to the Walnut directory;
```

