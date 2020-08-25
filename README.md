# bslice

## rough instructions

### to slice all relevant classes within a Jar

To slice all methods within all relevant classes in a Jar file, you just need to copy the following code into the main function of **bslicemany.java**
```
id="<UNIQUE_IDENTIFIER_OF_RELEVANT_CLASSES>";
filePath="<PATH_TO_JAR>";
SCFilePath="<PATH_TO_SRC_FILES>";
classNames=repoClasses();
sliceMany(classNames);
```
For the path to source files at SCFilePath, you just need the path which can be followed by e.g. /java/util/ArrayList from the class name Ljava/util/ArrayList. Currently, the code just recognizes files ending with .java; but it be be changed easily if needed. 

- Classes which cannot be sliced will be recorded in an id_errClasses.txt file, they mostly will be interfaces or abstract. 

- There are certain methods in certain classes which cannot have a callsite built for their entrypoints, and will only show the method name in the slices. 


### to slice methods within a single class

To slice methods within a .class file, you need to modify several fields within **bslice5.java**. 

- `filePath` requires a path to the .class or .jar file which contains your method(s)
- `OFileName` is the file name with which you want the file to be outputted. 
- `methodName` is the method name you want to slice, and `methodIO` is the method input/output in the form of (inputs)output, e.g Z-boolean, I-integer, V-void, Ljava/util/ArrayList-Arraylist. This can be ignored if you want to slice multiple/all methods in the class, and turn the boolean `one` to `true`.
- `SCFile` is the source file in which the output can be in the form of code. Else, this can be ignored, and turn the boolean `source` to `false`.
- `SCFclassName` is just the class name, e.g. Ljava/util/ArrayList
