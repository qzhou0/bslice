 package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Queue;
import java.util.LinkedList;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;



public class bslicemany{
	private static boolean givenclasses=false;
	private static String SCFilePath="C:/Program Files/Java/jdk-13/lib/src/java.base/";
	private static String filePath="src/main/java/test/Demo.class";//"src/main/java/test/"
	private static String id="";
	
	public static Queue<String> loadLibraryClassNames() throws ClassHierarchyException, IOException{
		 //load all classnames in cha, for all classes there
			File exFile = new FileProvider().getFile("Java60RegressionExclusions.txt");
	        AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope("src/main/java/test/ArrayList.class", exFile);
		    ClassHierarchy cha = ClassHierarchyFactory.make(scope);
		    Queue<String> classNames=new LinkedList();
    	    for (IClass c:cha) {
    	    	if (c!=null) {
    	    		String className=c.getName().toString();
    	    		if (className.contains("$")||className.startsWith("Lorg")||className.startsWith("Ljavax")) {
    	    			continue;
    	    		}

    	    		try {
    	    			FileInputStream fs = new FileInputStream(SCFilePath+className.substring(1)+".java");	
    	    		}catch(Exception e) {
    	    			System.err.println("No "+ className+" source file.");
    	    			continue;
    	    		}
    	    		if (!classNames.contains(className)) {
    	    			classNames.add(className);
    	    		}
    	    	}
    	    }
    	    //System.out.print(classNames+"\n"+classNames.contains("Ljava/util/ArrayList"));
		    return classNames;
	 }
	public static Queue<String> repoClasses() throws IOException, ClassHierarchyException{

		
		File exFile = new FileProvider().getFile("Java60RegressionExclusions.txt");
        AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(filePath, exFile);
	    ClassHierarchy cha = ClassHierarchyFactory.make(scope);
	    Queue<String> classNames=new LinkedList();
	    for (IClass c:cha) {
	    	if (c!=null) {
	    		String className=c.getName().toString();
	    		//System.out.println(className);

	    		if (className.contains("$")||!className.contains(id)) {
	    			continue;
	    		}
	    		    		
	    		System.out.println(className);
	    		try {
	    			FileInputStream fs = new FileInputStream(SCFilePath+className.substring(1)+".java");
	    			fs.close();
	    		}catch(Exception e) {
	    			System.err.println("No "+ className+" source file.");
	    			continue;
	    		}
	    		if (!classNames.contains(className)) {
	    			classNames.add(className);
	    		}
	    	}
	    }
	    //System.out.print(classNames+"\n"+classNames.contains("Ljava/util/ArrayList"));
	    return classNames;
	}
	public static Queue<String> apacheCommons() throws IOException, ClassHierarchyException{
		String Path="C:/Users/qzstu/eclipse/java-2019-12/eclipse/dropins/commons-collections4-4.4-src/src/main/java/";
		File exFile = new FileProvider().getFile("Java60RegressionExclusions.txt");
        AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope("C:/Users/qzstu/eclipse/java-2019-12/eclipse/dropins/commons-collections4-4.4.jar", exFile);
	    ClassHierarchy cha = ClassHierarchyFactory.make(scope);
	    Queue<String> classNames=new LinkedList();
	    for (IClass c:cha) {
	    	if (c!=null) {
	    		String className=c.getName().toString();
	    		if (className.contains("$")||!className.contains("apache/commons")) {
	    			continue;
	    		}
	    		System.out.println(className);
	    		try {
	    			FileInputStream fs = new FileInputStream(Path+className.substring(1)+".java");	
	    		}catch(Exception e) {
	    			System.err.println("No "+ className+" source file.");
	    			continue;
	    		}
	    		if (!classNames.contains(className)) {
	    			classNames.add(className);
	    		}
	    	}
	    }
	    //System.out.print(classNames+"\n"+classNames.contains("Ljava/util/ArrayList"));
	    return classNames;
	}
	public static void sliceMany(Queue<String> classNames) throws ClassHierarchyException, IOException {

		 bslice5 slicerr=new bslice5();
		 try {
	         System.out.println(id);  
			 FileWriter errWriter = new FileWriter("src/test/libs/"+id+"_errClasses.txt");
	            
		            for (String cn:classNames) {
			            try {
			            	slicerr.className=cn;
			            	slicerr.SCFclassName=cn;
			            	slicerr.SCFile=SCFilePath+cn.substring(1)+".java";
			            	slicerr.OFileName="libs/"+id+"/"+cn.replace('/', '.')+".txt";
			            	slicerr.doSlicingMultipleMethods(filePath);
			            }
			            catch(Exception e) {//may need to let doSlicingMultipleMethods throw 
			            	errWriter.write(cn+"\n");
			            	errWriter.write("\t"+e.getClass().descriptorString()+"\n--------\n");
			            }
		            }
		            errWriter.close();
		 }
		 catch(Exception e) {
			 System.err.println("failure in error writer");
		 }
	}
	 public static void main(String[] args) throws ClassHierarchyException, IOException {
		  Queue<String> classNames;
	      if (!givenclasses) {
					 
	    	  //classNames=loadLibraryClassNames();
		  }else {//given class names, TO-DO.
					    classNames=new LinkedList();
		  }
		 //apacheCommons();
		 //loadLibraryClassNames();
		 
	     //jdk
		 SCFilePath="C:/Program Files/Java/jdk-13/lib/src/java.base/";
		 //sliceMany(classNames,"jdk");
		 
		 //apache commons
		 /*
		 classNames=apacheCommons();
		 SCFilePath="C:/Users/qzstu/eclipse/java-2019-12/eclipse/dropins/commons-collections4-4.4-src/src/main/java/";
		 filePath="C:/Users/qzstu/eclipse/java-2019-12/eclipse/dropins/commons-collections4-4.4.jar";
		 sliceMany(classNames,"ac");*/
		 /*
		 //repos shatteredpixel
			filePath="C:/Users/qzstu/Documents/rutgers/cs112/gitrepos/shattered-pixel-dungeon/desktop/build/libs/desktop-0.8.2.jar";
		    id="shatteredpixel";//ripme";//"shatteredpixel";			
			SCFilePath="C:/Users/qzstu/Documents/rutgers/cs112/gitrepos/shattered-pixel-dungeon/core/src/main/java/";//"C:/Users/qzstu/Documents/rutgers/cs112/gitrepos/ripme-1.7.93/src/main/java/";
		 classNames=repoClasses();
		 sliceMany(classNames);
	     
		 //ripme
	     filePath="C:/Users/qzstu/Documents/rutgers/cs112/gitrepos/ripme-1.7.93/src/main/java/ripme.jar";
	     id="ripme";
	     SCFilePath="C:/Users/qzstu/Documents/rutgers/cs112/gitrepos/ripme-1.7.93/src/main/java/";
	     classNames=repoClasses();
	     sliceMany(classNames);*/
		 
		 //acra
		 /*
		 String[] spids= {"druid","fastjson"};
		 //engine","spring","bpmn-model","bpmn-layout","image-generator","json-converter","process-validation"};
		 
				 //"core","toast","advanced-scheduler","core-ktx","http","javacore","mail","notification","limiter"};
		 for (String spid:spids) {
			 //System.out.println(spid);
		     id="alibaba";
			 filePath="C:/Users/qzstu/Documents/rutgers/cs112/gitrepos/alibaba/"+spid+".jar";
			 SCFilePath="C:\\Users\\qzstu\\Documents\\rutgers\\cs112\\gitrepos\\alibaba/"+spid+"/";
			 
			 classNames=repoClasses();
			 id=id+"/"+spid;
			 sliceMany(classNames);
		 }*/
		 String[] spid2= {"core","espresso","gradle-plugin","junit","noop"};
		 for (String spid:spid2) {
			 System.out.println(spid);
		     id="okreplay";
			 filePath="C:\\Users\\qzstu\\Documents\\rutgers\\cs112\\gitrepos\\okreplay/"+spid+"-1.6.0.jar";
			 SCFilePath="C:\\Users\\qzstu\\Documents\\rutgers\\cs112\\gitrepos\\okreplay/okreplay-"+spid+"/src/main/java/";
			 
			 classNames=repoClasses();
			 id=id+"/"+spid;
			 sliceMany(classNames);
		 }
		 /*
		 id="unifiedpush";
		 filePath="C:\\Users\\qzstu\\Documents\\rutgers\\cs112\\gitrepos\\unified push/unifiedpush-service-2.5.0.jar";
		 SCFilePath="C:\\Users\\qzstu\\Documents\\rutgers\\cs112\\gitrepos\\unified push/";
		 classNames=repoClasses();
		 sliceMany(classNames);
		 
		 //mapper, not finished
		 id="mapper";
		 filePath="C:\\Users\\qzstu\\Documents\\rutgers\\cs112\\gitrepos\\Mapper/mapper-4.1.5.jar";
		 SCFilePath="C:\\Users\\qzstu\\Documents\\rutgers\\cs112\\gitrepos\\Mapper/";
		 classNames=repoClasses();
		 sliceMany(classNames);*/
		 
	 }
 }