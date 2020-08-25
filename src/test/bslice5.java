package test;

import com.ibm.wala.classLoader.*;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
//import com.ibm.wala.examples.analysis.js.JSCallGraphBuilderUtil;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.AnalysisOptions.ReflectionOptions;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.modref.ModRef;
import com.ibm.wala.ipa.slicer.*;
import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import com.ibm.wala.ipa.slicer.thin.ThinSlicer;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Descriptor;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.Heap;
import com.ibm.wala.util.collections.Iterator2Iterable;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.strings.Atom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class bslice5{
	//for files in the package
	
	//useful when we are slicing a "non-packaged" (e.g. java.util.ArrayList) file
	public String fileName="";
	public String filePath="C:/Users/qzstu/Documents/rutgers/cs112/gitrepos/done/acs-aem-commons-asset-share-commons-4.8.0/acs-aem-commons-oakpal-checks-4.8.0.jar";
			//"C:/Users/qzstu/Documents/rutgers/cs112/gitrepos/shattered-pixel-dungeon/desktop/build/libs/desktop-0.8.2.jar";//"src/main/java/test/"
	
	//Output file name
	public String OFileName="test.txt";
	
	// not useful when slicing full class
	private static String sliceType="return";//IndexOutOfBoundsException"; //e.g. return, IndexOutOfBoundsException
	//may need to change exception name to full name, for exceptions from other packages. 
	private static String methodName="newInstance";//"set";//e.g. set
    private static String methodIO="(Ljavax/json/JsonObject;)Lnet/adamcin/oakpal/api/ProgressCheck;"; 
    	//"(I,Ljava/lang/Object)V";//e.g. (I,I)I for set, (int,int) return int
    private static Slicer.ControlDependenceOptions cOption=ControlDependenceOptions.NO_EXCEPTIONAL_EDGES;
	//if we only want to slice one method
	private static boolean one=false;
    
    //for source code
    public String SCFile="C:/Users/qzstu/Documents/rutgers/cs112/gitrepos/done/acs-aem-commons-asset-share-commons-4.8.0/oakpal-checks/com/adobe/acs/commons/oakpal/checks/RecommendEnsureAuthorizable.java";
    		//"C:/Users/qzstu/Documents/rutgers/cs112/gitrepos/shattered-pixel-dungeon/core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/actors/mobs/Statue.java";
    //compute(Ljava/lang/Object;Ljava/util/function/BiFunction;)Ljava/lang/Object;
    //clone()Ljava/lang/Object;
	private static boolean source=true;
    //className used for source code
    public String SCFclassName="Lcom/adobe/acs/commons/oakpal/checks/RecommendEnsureAuthorizable";
    //com/shatteredpixel/shatteredpixeldungeon/actors/mobs/Statue";     
	
    /*className for classes that appear in ClassHierarchy.
     * null if we are slicing a .class file  
     */
    public String className=SCFclassName;//"Lcom/shatteredpixel/shatteredpixeldungeon/actors/mobs/Statue";//Ljava/util/ArrayList

	private static String[] sliceTypes= {"return","Exception"};/*
			"java.lang.IndexOutOfBoundsException","java.util.ConcurrentModificationException","java.lang.IllegalStateException","java.util.NoSuchElementException","java.lang.NullPointerException","java.lang.IllegalArgumentException","java.lang.CloneNotSupportedException",
			"java.io.InvalidObjectException","java.lang.InternalError"};
	 */

    
    public void print(CallGraph cg) {
    	int count = 1000;
        try {
            FileWriter myWriter = new FileWriter("src/main/java/test/ALcg.txt");
            for (Iterator<? extends CGNode> it = cg.iterator(); it.hasNext(); ) {

            	CGNode n = it.next(); //***
//			System.out.println("method:  " + n.getMethod().getName() + "    class:  "
//					+ n.getMethod().getDeclaringClass().getName().toString());
            
            //write to file version

                myWriter.write(n.toString()+"\n");
            //System.err.println(n);
            }
        
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
        	System.out.println("An error occurred.");
        	e.printStackTrace();
        }
            
    }
    public Iterable<Entrypoint> makeEntrypoints(Iterable<Entrypoint> entrypoints, String methodName) {
        final HashSet<Entrypoint> result = HashSetFactory.make();

        for (Entrypoint E : entrypoints) {
            if (E.getMethod().getName().toString().equals(methodName)) {
                System.out.println("Entrypoint: " + E);
                result.add(E);
            }
        }
        return result::iterator;
    }
    public Iterable<Entrypoint> loadEntrypoints(ClassHierarchy cha, String className){
    	final HashSet<Entrypoint> result = HashSetFactory.make();
    	for (IClass c:cha) {
    	  	if (c!=null) {
    	   		if (c.getName().toString().equals(className)) {
    	   			//System.out.println(c.getName());
    	   			for (IMethod m:c.getAllMethods()) {
    	   				//System.out.println("our good method:"+m.getSignature()+"\n"+m.getClass());
    	   				if (m.getSignature().startsWith(className.replace('/', '.').substring(1))) {
    	   						result.add(new DefaultEntrypoint(m, cha));
    	   				}
    	   				//myWriter.write(m.getSignature().toString()+"\n");
    	   				//System.out.println(m.toString());
    	   			}
    	   			break;
    	   		}
    	   	}
    	}
    	return result::iterator;
    }

	public void doSlicingMultipleMethods(String appJar) throws WalaException, IOException, IllegalArgumentException, CancelException, InvalidClassFileException {
	      // create an analysis scope representing the appJar as a J2SE application
		File exFile = new FileProvider().getFile("Java60RegressionExclusions.txt");
        AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(appJar, exFile);
	    //AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(appJar,new File(CallGraphTestUtil.REGRESSION_EXCLUSIONS));
	    ClassHierarchy cha = ClassHierarchyFactory.make(scope);
	    /*
	    try {
            FileWriter myWriter = new FileWriter("src/main/java/test/cha.txt");
           
    	    for (IClass c:cha) {
    	    	if (c!=null) {
    	    		if (c.getName().toString().equals("Ljava/util/ArrayList")) {
    	    			System.out.println(c.getName());
    	    			for (IMethod m:c.getAllMethods()) {
    	    				//myWriter.write(m.getSignature().toString()+"\n");    	
    	    			}
    	    		}
    	    	}
    	    }
            
    		
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
          } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
          }*/
	    

	    
	    /*
	    String methodName="read_product";
	    String methodIO="(I)I";
	    */
	    
	    //Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(scope, cha, "Ljava/util/ArrayList");
	    //Iterable<Entrypoint> entrypoints = new AllApplicationEntrypoints(scope, cha);
	    Iterable<Entrypoint> entrypoints;
        Queue<String> methodNames=new LinkedList();
        Queue<String> methodIOs=new LinkedList();
	    if (className==null) {
	    	entrypoints = new AllApplicationEntrypoints(scope, cha);
	    	/*
	    	methodNames.add(methodName);
	    	methodIOs.add(methodIO);
	    	*/
	    	
	    }else {
	    	entrypoints = loadEntrypoints(cha, className);
	    }
        
	    

	    
//        try {
            
//        	FileWriter myWriter = new FileWriter("src/main/java/test/ALentrypts.txt");
//            myWriter.write(scope.toString()+"\n\n--------cha-------\n");
//            myWriter.write(cha.toString()+"\n\n-----entrypts-------\n");
            //myWriter.write(entrypoints.toString()+"\n-----entrypts------\n");
            
            //perhaps add a conditional here to restrict multiple methods if needed
            if (one) {
            	methodNames.add(methodName);
            	methodIOs.add(methodIO);
            }else {
	            for(Entrypoint enn:entrypoints) {/*
	            	myWriter.write(enn.toString()+"\n");
	            	myWriter.write(//enn.getMethod().getSignature().toString()+"\n\n");
	            			enn.getMethod().getDescriptor()+" "+enn.getMethod().getName()+"\n");*/
	            	methodNames.add(enn.getMethod().getName().toString());
	            	methodIOs.add(enn.getMethod().getDescriptor().toString());
	    		}		
            }
//            myWriter.close();
//            System.out.println("Successfully wrote to the file.");
//          } catch (IOException e) {
//            System.out.println("An error occurred.");
//            e.printStackTrace();
//          }
        //try {//actual slicings
            FileWriter myWriter = new FileWriter("src/test/"+OFileName);
            

            while (!methodNames.isEmpty()) {
            	methodName=methodNames.poll();
            	methodIO=methodIOs.poll();
            	
            	//seem not present
            	if (methodName.contains("$")) {continue;}
            	
            	//problematic???
            	/*if (methodName.equals("storeInBundle")&&methodIO.equals("(Lcom/watabou/utils/Bundle;)V")) {
            		continue;
            	}*/
            	
            	myWriter.write("\n"+methodName+methodIO+"\n");
    	    	//System.out.println("entrypoints1");
                Iterable<Entrypoint> entrypoints1 = makeEntrypoints(entrypoints, methodName);
        	    AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
    	    	//System.out.println("options1");
        	    AnalysisOptions options1 = new AnalysisOptions(scope, entrypoints1);
        	    //System.out.println(options1.getEntrypoints()+"\n\n\n");
    	    	//System.out.println("cgb");

    	    	options1.setReflectionOptions(ReflectionOptions.NONE);
    	    	
    	    	
        	    CallGraphBuilder<InstanceKey> cgb = Util.makeZeroCFABuilder(Language.JAVA, options1, new AnalysisCacheImpl(),
                        cha, scope);
        	    
        	    //System.out.println(cgb.toString());
        	    CallGraph cg=null;
        	    try {
        	    	//System.out.println("cg");
        	    	cg= cgb.makeCallGraph(options1, null);//**
        	    }catch(IllegalStateException e) {
        	    	System.err.println(methodName+" gives a IllegalStateException, could not create entrypoint callsites\n");
        	    	//e.printStackTrace();
        	    	continue;
        	    }
    	    	//System.out.println("pa");
                final PointerAnalysis<InstanceKey> pa = cgb.getPointerAnalysis();
                //System.out.println("here");
                if (!source) {
                myWriter.write(findMethod(methodName,methodIO,cg).getIR().toString()+"\n");
                }
                for (String st : sliceTypes) {
//                	try {
                		if (className==null) {
                			st=sliceType;
                		}
                		if (!st.equals("return")) {
                			cOption=ControlDependenceOptions.FULL;
                		}else {
                			cOption=ControlDependenceOptions.NO_EXCEPTIONAL_EDGES;
                		}
               	Collection<Statement> statement=findCallTo(findMethod(methodName,methodIO,cg),st);
               	//System.err.println(st);
              	System.out.println("statement: "+statement);
                	
        	    Collection<Statement> slice;
        	    //sdg
        	    SDG<?> sdg = new SDG<>(cg, pa, DataDependenceOptions.NO_HEAP,
                        cOption);//.NO_EXCEPTIONAL_EDGES);
    	    	slice=Slicer.computeBackwardSlice(sdg, statement);
    	    	if (!slice.isEmpty()) {
    		    	myWriter.write("-------------\n");
    	    		myWriter.write(st+"\n");
    	    		if (source) {
    	    			//System.out.println(slice);
    	    			convert(slice, SCFile,1 , myWriter);
    	    		}else {
    	    			dumbdumpSlice(myWriter,slice);
    	    		}
    	    	}
    	        if (className==null) {
    	        	break;
    	        }
/*                	}catch (Exception e) {
                		//myWriter.write(e+"\n");
                	}*/
    	        }
                myWriter.write("===============================\n");
        	}
            myWriter.close();
        /*}
        	catch (IOException e) {
	          	System.out.println("An error occurred.");
	          	e.printStackTrace();
	          }*/
	}

	    public CGNode findMethod(String nomen, String descriptor, CallGraph cg) {
	      Descriptor d = Descriptor.findOrCreateUTF8(descriptor);//"([Ljava/lang/String;)V");
	      Atom name = Atom.findOrCreateUnicodeAtom(nomen);
	      for (Iterator<? extends CGNode> it = cg.getSuccNodes(cg.getFakeRootNode()); it.hasNext();) {
	        CGNode n = it.next();
	        if (n.getMethod().getName().equals(name) && n.getMethod().getDescriptor().equals(d)) {
	          return n;
	        }
	      }
	      //Assertions.UNREACHABLE("failed to find method");
	      return null;
	    }

	    public Collection<Statement> findCallTo(CGNode n, String methodName) {
	      IR ir = n.getIR();
	      
	      /*
	      System.out.println(n);
	      System.out.println("----IR-----");
	      System.out.println(ir);
	      //System.out.println("---------");
	      //System.out.println(ir.getInstructions());
	      /*  iterate over instructions, does not seem full;
	      System.out.println("---------");
	      for (SSAInstruction s:ir.getInstructions()) {
	    	  System.out.println(s);
	      }
	      */
	      ////System.out.println("--------------");
	      Collection<Statement> ss = new ArrayList<Statement>();//new Collection<Statement>();
	      //System.err.println(ss.isEmpty());
	      if (ir==null) return ss;
	      for (Iterator<SSAInstruction> it = ir.iterateAllInstructions(); it.hasNext();) {
	        SSAInstruction s = it.next();
	        /*
	        System.out.println("----------------\n"+": "+s);
	        System.out.println(s.getDef()+" "+s.getClass());
	        System.out.println(s.getClass().descriptorString());
	        System.out.println(s.getClass().getName()+" "+s.getClass().getAnnotations());
	        //*/
	        
	        if (methodName.equals("return")&& s.getClass().descriptorString().equals("Lcom/ibm/wala/ssa/SSAReturnInstruction;")) {
	        	//System.err.println("here");
	  	      	//System.out.println(ir.getBasicBlockForInstruction(s).getLastInstructionIndex());
	        	ss.add(new com.ibm.wala.ipa.slicer.NormalStatement(n,ir.getBasicBlockForInstruction(s).getLastInstructionIndex()));
	        	//System.err.println(ss);
	        	//return new com.ibm.wala.ipa.slicer.NormalStatement(n,ir.getBasicBlockForInstruction(s).getLastInstructionIndex());
	        }
	        else if (s instanceof com.ibm.wala.ssa.SSAAbstractInvokeInstruction) {
	        	//expected method invocation, e.g. exception
	        
	          com.ibm.wala.ssa.SSAAbstractInvokeInstruction call = (com.ibm.wala.ssa.SSAAbstractInvokeInstruction) s;
	          
	          /*
	          System.out.println("Here: "+ call.getCallSite());
	          
	          //System.out.println(call.getDeclaredResultType());
	          //System.out.println(call.getExceptionTypes());
	          System.out.println(call.getDeclaredTarget());
	          System.out.println(call.getDeclaredTarget().getReturnType());
	          System.out.println(call.getDeclaredTarget().getName());
	          System.out.println(call.getDeclaredTarget().getSignature());
	          */
	          
	          if (methodName.contentEquals("println(")) {
	        	  if (call.getCallSite().getDeclaredTarget().getSignature().toString().startsWith("java.io.PrintStream")) {
	  	            ss.add( new com.ibm.wala.ipa.slicer.NormalStatement(n,ir.getBasicBlockForInstruction(s).getLastInstructionIndex()));
	  	            continue;
	        	  }
	          }
	          //may use full

	          String exceptionName=methodName;
	          //String exceptionName="java.lang."+methodName+".<init>(";//Ljava/lang/String;)V";
	          //java.lang.IndexOutOfBoundsException.<init>(Ljava/lang/String;)V
	          //java.util.ConcurrentModificationException
          
	          //System.out.println(call.getInvocationCode());

/*
	          com.ibm.wala.util.intset.IntSet indices = ir.getCallInstructionIndices(call.getCallSite());
	          //System.out.println(indices.intIterator().next());
	          //com.ibm.wala.util.debug.Assertions.productionAssertion(indices.size() == 1, "expected 1 but got " + indices.size());
	          if (call.getCallSite().getDeclaredTarget().getSignature().toString().startsWith(exceptionName)) {
	            //com.ibm.wala.util.intset.IntSet indices = ir.getCallInstructionIndices(call.getCallSite());
	            com.ibm.wala.util.debug.Assertions.productionAssertion(indices.size() == 1, "expected 1 but got " + indices.size());
		        System.out.println(ir.getBasicBlockForInstruction(s).getFirstInstructionIndex());
	            System.out.println(ir.getBasicBlockForInstruction(s).getLastInstructionIndex()+1);
	            //return new com.ibm.wala.ipa.slicer.NormalStatement(n,ir.getBasicBlockForInstruction(s).getFirstInstructionIndex()-1);
	            //ss.add( new com.ibm.wala.ipa.slicer.NormalStatement(n,ir.getBasicBlockForInstruction(s).getFirstInstructionIndex()-2));
	            ss.add( new com.ibm.wala.ipa.slicer.NormalStatement(n,ir.getBasicBlockForInstruction(s).getFirstInstructionIndex()-1));
	            ss.add( new com.ibm.wala.ipa.slicer.NormalStatement(n,ir.getBasicBlockForInstruction(s).getLastInstructionIndex()));
	            ss.add( new com.ibm.wala.ipa.slicer.NormalStatement(n,ir.getBasicBlockForInstruction(s).getLastInstructionIndex()+1));
	            
	            //return new com.ibm.wala.ipa.slicer.NormalStatement(n,ir.getBasicBlockForInstruction(s).getLastInstructionIndex()+1); //indices.intIterator().next());
	          }*/
	        }
	        else if (methodName.equals("Exception")){
		          if (s.toString().startsWith("throw")) {
			            //ss.add( new com.ibm.wala.ipa.slicer.NormalStatement(n,ir.getBasicBlockForInstruction(s).getLastInstructionIndex()-1));
			            ss.add( new com.ibm.wala.ipa.slicer.NormalStatement(n,ir.getBasicBlockForInstruction(s).getLastInstructionIndex()));
		          }
	        }
	      }
	      //Assertions.UNREACHABLE("failed to find call to " + methodName + " in " + n);
	      //System.err.println(ss.toString());
	      //System.out.println(ss.isEmpty());
	      return ss;
	      //return null;
	    }
	    public Statement findCallToOne(CGNode n, String methodName) {
		      IR ir = n.getIR();
		      System.out.println(n);
		      System.out.println("----IR-----");
		      System.out.println(ir);
		      System.out.println("--------------");
		      for (Iterator<SSAInstruction> it = ir.iterateAllInstructions(); it.hasNext();) {
		        SSAInstruction s = it.next();

		    	if (methodName.contentEquals("params")) {//forward Slice...?
		        	return new com.ibm.wala.ipa.slicer.NormalStatement(n,ir.getBasicBlockForInstruction(s).getLastInstructionIndex());
		    	}
		        System.out.println("----------------\n"+": "+s);
		        System.out.println(s.getDef()+" "+s.getClass());
		        System.out.println(s.getClass().descriptorString());
		        System.out.println(s.getClass().getName()+" "+s.getClass().getAnnotations());
		        
		        if (methodName.equals("return")&& s.getClass().descriptorString().equals("Lcom/ibm/wala/ssa/SSAReturnInstruction;")) {
		        	return new com.ibm.wala.ipa.slicer.NormalStatement(n,ir.getBasicBlockForInstruction(s).getLastInstructionIndex());
		        }
		        else if (s instanceof com.ibm.wala.ssa.SSAAbstractInvokeInstruction) {
		        	//expected method invocation, e.g. exception
		          com.ibm.wala.ssa.SSAAbstractInvokeInstruction call = (com.ibm.wala.ssa.SSAAbstractInvokeInstruction) s;
		          System.out.println("Here: "+ call.getCallSite());
		          System.out.println(call.getDeclaredTarget());
		          System.out.println(call.getDeclaredTarget().getReturnType());
		          System.out.println(call.getDeclaredTarget().getName());
		          System.out.println(call.getDeclaredTarget().getSignature());
		          String exceptionName="java.lang."+methodName+".<init>(Ljava/lang/String;)V";
		          if (call.getCallSite().getDeclaredTarget().getSignature().toString().equals(exceptionName)) {
			        System.out.println(ir.getBasicBlockForInstruction(s).getFirstInstructionIndex());
		            System.out.println(ir.getBasicBlockForInstruction(s).getLastInstructionIndex()+1);
		            return new com.ibm.wala.ipa.slicer.NormalStatement(n,ir.getBasicBlockForInstruction(s).getLastInstructionIndex()+1); //indices.intIterator().next());
		          }
		        }
		      }
		      Assertions.UNREACHABLE("failed to find call to " + methodName + " in " + n);
		      return null;
		    }
	    public int convert(Collection<Statement> slice,String SCFile, int currLine ,FileWriter myWriter) throws IOException, InvalidClassFileException {
	    	Queue<Integer> lines = new LinkedList<Integer>();
	    	
	    	//write new function names
	    	Queue<String> fmNames=new LinkedList<String>();
	    	
	    	//used to determine if a name is within one call of the function
	    	Queue<String> oneNames=new LinkedList<String>();
	    	//try same class all
	    	
	    	String goodName=null;
	    	//ArrayList<Integer> lines = new ArrayList<Integer>();
	    	boolean first=true;
	    	for (Statement s : slice) {
	    		if (!s.getNode().getMethod().getDeclaringClass().getName().toString().equals(SCFclassName)) {
	    			//System.err.println(s.getNode().getMethod().getDeclaringClass().getName().toString());
	    			//System.out.println(s.getNode().getMethod().getDeclaringClass().getName().toString());
	    			continue;
	    		}
	    		
	    		if (goodName==null) {
	    			goodName=s.getNode().getMethod().getSignature().toString();
	    			//goodName is name of sliced method
	    			//System.err.println(goodName);
	    		}
	    		oneNames.add(goodName);
				

	    		//perhaps can use mNames to filter those out of 2 levels of separation
	    		//may need another one to find which function it is. 
	  	    	  if (s.getKind().toString().equals("PARAM_CALLEE")
	  	    			  ||s.getKind().toString().equals("METHOD_ENTRY")) {
	  	    		  //within bounds of new method, this method passed. 
	  	    		  lines.add(-1);  
	  	    		  first=false;
	  	    		  //break;
	  	    	  }
	  	    	  if (!oneNames.contains(s.getNode().getMethod().getSignature()) ||
	  	    			  (s.getNode().getMethod().getSignature().equals(goodName)
	  	    					  &&lines.contains(-1))) {
	  	    		  //second condition when we added (-1) and this is goodName
	  	    		  continue;
	  	    	  }
	  	    	  int instructionIndex;
	  	    	  if (!s.getKind().toString().equals("NORMAL")&&
	  	  	    	 !s.getKind().toString().equals("CATCH")&&!s.getKind().toString().equals("NORMAL_RET_CALLER")){
	  	  	    		  continue;
	  	  	    	  }
	  	    	  IR ir;IBytecodeMethod method;
	  	    	  if (s.getKind().toString().contentEquals("PHI")) {
	  	    		  instructionIndex=((PhiStatement) s).getPhi().iIndex();
	  	    		  //s.getNode().getIR().getBasicBlockForInstruction()
	  	    	  }else if (s.getKind().toString().equals("CATCH")) {
	  	    		instructionIndex=s.getNode().getIR().getBasicBlockForCatch(((GetCaughtExceptionStatement)s).getInstruction()).getFirstInstructionIndex();
	  	    		//System.err.println(s);
	  	    		  //instructionIndex=((GetCaughtExceptionStatement)s)..getInstruction().iIndex();
	  	    	  }else if (s.getKind().toString().equals("NORMAL_RET_CALLER")) {
	  	    		  instructionIndex=((NormalReturnCaller)s).getInstructionIndex();
	  	    	  }else {//NORMAL
		    		  instructionIndex = ((NormalStatement) s).getInstructionIndex();
	  	    	  }
	  	    	  /*
	    		if (!s.getKind().toString().equals("NORMAL")) {
	    			continue;
	    		}*/ 
	    		  int bcIndex;
	    		  

//	    		    try {
	    		    	int src_line_number;
		    			if (s.getKind().toString().equals("NORMAL")||s.getKind().toString().equals("NORMAL_RET_CALLER")) {
		    				IMethod m =s.getNode().getMethod();
		    				if (m instanceof com.ibm.wala.ipa.summaries.SummarizedMethod) {
		    					continue;
		    				}
		    				bcIndex = ((ShrikeBTMethod) s.getNode().getMethod()).getBytecodeIndex(instructionIndex);
		    	    		if (!lines.contains(-1)) {
		    	    			//not yet another fxn
		    	    			SSAInstruction sin=s.getNode().getIR().getInstructions()[instructionIndex];
		    	    			if (sin instanceof com.ibm.wala.ssa.SSAAbstractInvokeInstruction) {
		    	    		          com.ibm.wala.ssa.SSAAbstractInvokeInstruction call = (com.ibm.wala.ssa.SSAAbstractInvokeInstruction) sin;
		    	    		          oneNames.add(call.getCallSite().getDeclaredTarget().getSignature());
		    	    				
		    	    			}
		    	    		}
		    				src_line_number =s.getNode().getMethod().getLineNumber(bcIndex);
		    			}else {//not NORMAL
			  	    		  ir =s.getNode().getIR();
			  	    		  method = (IBytecodeMethod)ir.getMethod();
			  	    		  //myWriter.write(instructionIndex+"!!\n");
			  	    	    bcIndex = method.getBytecodeIndex(instructionIndex);
		    				src_line_number= method.getLineNumber(bcIndex);
		    				/*
		    				if (s.getKind().toString().equals("CATCH")) {
		    					myWriter.write("CATCH"+src_line_number+"!!!\n");
		    				}*/
		    			}
	    		        if (!lines.contains(src_line_number)) {
	    		        	lines.add(src_line_number);
	    		        	//System.out.println(s+":"+src_line_number);
	    		        	if (!first) {
	    		        		first=true;
	    		        		fmNames.add(s.getNode().getMethod().getSignature().toString());
	    		        	}
	    		        }
	    		        //System.err.println ( "Source line number = " + src_line_number );
/*	    		      } catch (Exception e) {
	    		        System.err.println("Bytecode index no good");
	    		      }*/
	    		 }
//perhaps use array and sort
	    	//lines.sort(null);
	    	//System.out.println(oneNames.toString());
//	    	try {
	    		/*
    			BufferedReader br = new BufferedReader(new InputStreamReader(fs));
    			boolean nova=false;
		    	int nextLine;
		    	//for (int i =0;i<lines.size();i++) {
		    	 * 
		    	 */
		    	while (!lines.isEmpty()) {
		    		
		    		//do that each loop to support function calls within
		    		writeSourceCode(SCFile,myWriter,lines,fmNames);
		    		
		    		/*
		    		nextLine=lines.poll();
		    		//nextLine=lines.get(i);
		    		
		    		if (nova && nextLine==-1) {
		    			br = new BufferedReader(new InputStreamReader(fs));
		    			myWriter.write("-----\n");
		    			currLine=1;
		    			nova=false;
		    			continue;
		    		}
		    		else if (nextLine==-1) {continue;}
		    		
		    		//System.err.println(nextLine);
		    		while (currLine<nextLine) {
		    			br.readLine();
		    			  //System.out.println(currLine+":"+br.readLine());
		    			  currLine++;
		    		}
		    		myWriter.write(nextLine+": "+br.readLine()+"\n");nova=true;
		    		currLine++;
		    	}
		    	
		    	br.close();
		    	*/
		    	}
//		    	myWriter.write("-------------\n\n");
		    	
/*			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
	    	
	    	return currLine;
	    }
	    public void writeSourceCode(String SCFile, FileWriter myWriter, Queue<Integer> lines, Queue<String>fmNames) 
	    		throws IOException {
			FileInputStream fs = new FileInputStream(SCFile);	
	    	BufferedReader br = new BufferedReader(new InputStreamReader(fs));
				boolean nova=false;
		    	int nextLine=-1;
		    	int currLine=1;
		    	
		    	ArrayList<Integer> linea=new ArrayList<>();
		    	
		    	//for (int i =0;i<lines.size();i++) {
		    	while (!lines.isEmpty()) {
		    		nextLine=lines.poll();
		    		//nextLine=lines.get(i);
		    		
		    		if (nova && nextLine==-1&&!fmNames.isEmpty()) {
		    			//leave here, may add another bump for the printing after sorting, if needed
		    			//cannot do this before first line printed: nova
		    			writeSCforOne(br, myWriter, linea);
		    			myWriter.write("-----\n"+fmNames.poll()+"\n");
		    			br.close();
		    			return;
		    			//finished this fxn
		    		}
		    		else if (nextLine==-1) {continue;}
		    		
		    		//System.err.println(nextLine);
		    		while (currLine<nextLine) {
		    			///br.readLine();
		    			  //System.out.println(currLine+":"+br.readLine());
		    			  currLine++;
		    		}
		    		/*
		    		if (nextLine<oldLine) {
		    			//the previous line is ahead of this line
		    			continue;
		    		}*/
		    		linea.add(nextLine);
		    		//myWriter.write(nextLine+": "+br.readLine()+"\n");
		    		nova=true;
		    		currLine++;
		    	}
		    	
		    	writeSCforOne(br, myWriter, linea);
		    	br.close();fs.close();
		    	
		    	myWriter.write("-------------\n\n");
	    }
	    public void writeSCforOne(BufferedReader br,FileWriter myWriter, ArrayList<Integer> lines) throws IOException {
	    	//sort for the lines within one function
	    	lines.sort(null);
	    	int nextLine;
	    	int currLine=1;

	    	for (int i =0;i<lines.size();i++) {
	    		nextLine=lines.get(i);
	    		while (currLine<nextLine) {
	    			br.readLine();
	    			currLine++;
	    		}	  
	    		myWriter.write(nextLine+": "+br.readLine()+"\n");
	    		currLine++;
	    	}

	    	
	    }
	    public void dumbdumpSlice(FileWriter myWriter, Collection<Statement> slice) throws IOException {
	    	for (Statement s : slice) {
  	    	  if (s.getKind().toString().equals("PARAM_CALLEE")||s.getKind().toString().equals("METHOD_ENTRY")) {
  	    		  //break;
  	    	  }
  	    	  //System.out.println(s.getKind()+" "+s.getNode());
  	    	  //System.out.println((s.getKind()));
  	    	  if (!s.getKind().toString().equals("NORMAL")&&!s.getKind().toString().equals("PHI")&&
  	    	      !s.getKind().toString().equals("CATCH")&&!s.getKind().toString().equals("NORMAL_RET_CALLER")){
  	    		  //continue;
  	    	  }
  	    	  myWriter.write(/*s.getKind().toString()+" "+*/ s.toString()+"\n");
  		      //System.err.println(s);
  		  	}
	    	myWriter.write("-------------\n\n");
	    }

	  public static void main(String[] args) throws WalaException, IllegalArgumentException, IOException, CancelException, InvalidClassFileException {
		  //doSlicing(filePath+fileName);
		  bslice5 slicerr=new bslice5();
		  slicerr.doSlicingMultipleMethods(slicerr.filePath+slicerr.fileName);
	  }
	
}